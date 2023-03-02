package no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.IdentifiserteNaturalytelsePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.NaturalytelserPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeModellNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeSplittDataNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodiseringNaturalytelseProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(PeriodiserForNaturalytelse.ID)
public class PeriodiserForNaturalytelse extends LeafSpecification<PeriodiseringNaturalytelseProsesstruktur> {

    static final String ID = FastsettPerioderNaturalytelseRegel.ID + ".2";
    static final String BESKRIVELSE = "Periodiserer beregningsgrunnlaget ved gitte datoer og oppretter nye andeler.";

    public PeriodiserForNaturalytelse() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodiseringNaturalytelseProsesstruktur prosesstruktur) {
        List<SplittetPeriode> splittetPerioder = periodiserBeregningsgrunnlag(prosesstruktur.getInput(), prosesstruktur.getIdentifisertePeriodeÅrsaker());
        prosesstruktur.setSplittetPerioder(splittetPerioder);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperty("splittetPerioder", splittetPerioder);
        return resultat;
    }

    private static List<SplittetPeriode> periodiserBeregningsgrunnlag(PeriodeModellNaturalytelse input, IdentifiserteNaturalytelsePeriodeÅrsaker identifisertePeriodeÅrsaker) {
        // lag alle periodene, med riktige andeler
        Map<LocalDate, Set<PeriodeSplittDataNaturalytelse>> periodeMap = identifisertePeriodeÅrsaker.getPeriodeMap();

        List<Map.Entry<LocalDate, Set<PeriodeSplittDataNaturalytelse>>> entries = new ArrayList<>(periodeMap.entrySet());

        var listIterator = entries.listIterator();

        List<SplittetPeriode> list = new ArrayList<>();
        while (listIterator.hasNext()) {
            var entry = listIterator.next();
            LocalDate periodeFom = entry.getKey();
	        LocalDate periodeTom = utledPeriodeTom(entries, listIterator);
	        var periodeSplittData = entry.getValue();

            List<EksisterendeAndel> førstePeriodeAndeler = input.getNaturalytelserPrArbeidsforhold().stream()
                .filter(im -> !im.erNyAktivitet())
                .map(im -> mapToArbeidsforhold(im, periodeFom))
                .toList();


            Periode periode = new Periode(periodeFom, periodeTom);
            SplittetPeriode splittetPeriode = SplittetPeriode.builder()
                .medPeriode(periode)
                .medPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData, input.getSkjæringstidspunkt(), periodeFom))
                .medFørstePeriodeAndeler(førstePeriodeAndeler)
                .build();
            list.add(splittetPeriode);
        }
        return list;
    }

    private static LocalDate utledPeriodeTom(List<Map.Entry<LocalDate, Set<PeriodeSplittDataNaturalytelse>>> entries, ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittDataNaturalytelse>>> listIterator) {
        return listIterator.hasNext() ?
            entries.get(listIterator.nextIndex()).getKey().minusDays(1) :
            null;
    }

    private static EksisterendeAndel mapToArbeidsforhold(NaturalytelserPrArbeidsforhold naturalytelse, LocalDate fom) {
        Optional<BigDecimal> naturalytelseBortfaltPrÅr = naturalytelse.getNaturalYtelser().stream()
            .filter(naturalYtelse -> naturalYtelse.getFom().isEqual(DateUtil.TIDENES_BEGYNNELSE))
            .filter(naturalYtelse -> naturalYtelse.getTom().isBefore(fom))
            .map(NaturalYtelse::getBeløp)
            .reduce(BigDecimal::add);
        Optional<BigDecimal> naturalytelseTilkommer = naturalytelse.getNaturalYtelser().stream()
            .filter(naturalYtelse -> naturalYtelse.getTom().isEqual(DateUtil.TIDENES_ENDE))
            .filter(naturalYtelse -> naturalYtelse.getFom().isBefore(fom))
            .map(NaturalYtelse::getBeløp)
            .reduce(BigDecimal::add);
        return EksisterendeAndel.builder()
            .medAndelNr(naturalytelse.getAndelsnr())
            .medNaturalytelseTilkommetPrÅr(naturalytelseTilkommer.orElse(null))
            .medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr.orElse(null))
            .medArbeidsforhold(naturalytelse.getArbeidsforhold())
            .build();
    }

    private static List<PeriodeÅrsak> getPeriodeÅrsaker(Set<PeriodeSplittDataNaturalytelse> periodeSplittData, LocalDate skjæringstidspunkt, LocalDate periodeFom) {
        return periodeSplittData.stream()
            .map(PeriodeSplittDataNaturalytelse::getPeriodeÅrsak)
            .filter(periodeÅrsak -> !PeriodeÅrsak.UDEFINERT.equals(periodeÅrsak))
            .filter(periodeÅrsak -> !(Set.of(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR).contains(periodeÅrsak)
                && skjæringstidspunkt.equals(periodeFom)))
            .toList();
    }

}
