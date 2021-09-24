package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.FastsettPeriodeRegel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeSplittProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.GraderingPrAktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(PeriodiserGradertBeregningsgrunnlag.ID)
public class PeriodiserGradertBeregningsgrunnlag extends LeafSpecification<PeriodeSplittProsesstruktur> {

    static final String ID = FastsettGraderingPeriodeRegel.ID + ".2";
    static final String BESKRIVELSE = "Periodiserer beregningsgrunnlaget ved gitte datoer og oppretter nye andeler.";

    public PeriodiserGradertBeregningsgrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodeSplittProsesstruktur prosesstruktur) {
        List<SplittetPeriode> splittetPerioder = periodiserBeregningsgrunnlag((PeriodeModellGradering) prosesstruktur.getInput(), prosesstruktur.getIdentifisertePeriodeÅrsaker());
        prosesstruktur.setSplittetPerioder(splittetPerioder);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperty("splittetPerioder", splittetPerioder);
        return resultat;
    }

    private static List<SplittetPeriode> periodiserBeregningsgrunnlag(PeriodeModellGradering input, IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker) {
        // lag alle periodene, med riktige andeler
        Map<LocalDate, Set<PeriodeSplittData>> periodeMap = identifisertePeriodeÅrsaker.getPeriodeMap();

        List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries = new ArrayList<>(periodeMap.entrySet());

        ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator = entries.listIterator();

        List<SplittetPeriode> list = new ArrayList<>();
        while (listIterator.hasNext()) {
            Map.Entry<LocalDate, Set<PeriodeSplittData>> entry = listIterator.next();
            LocalDate periodeFom = entry.getKey();
            Set<PeriodeSplittData> periodeSplittData = entry.getValue();

            List<EksisterendeAndel> førstePeriodeAndeler = input.getArbeidsforholdOgInntektsmeldinger().stream()
                .filter(im -> !im.erNyAktivitet())
                .map(im -> mapToArbeidsforhold(im, periodeFom))
                .collect(Collectors.toList());

            List<SplittetAndel> nyeAndeler = input.getGraderingerPrAktivitet().stream()
                .filter(GraderingPrAktivitet::erNyAktivitet)
                .filter(aktivitet -> harGraderingFørPeriode(aktivitet, periodeFom))
                .map(PeriodiserGradertBeregningsgrunnlag::mapSplittetAndel)
                .collect(Collectors.toList());

            LocalDate tom = utledPeriodeTom(entries, listIterator);
            Periode periode = new Periode(periodeFom, tom);
            SplittetPeriode splittetPeriode = SplittetPeriode.builder()
                .medPeriode(periode)
                .medPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData, input.getSkjæringstidspunkt(), periodeFom))
                .medFørstePeriodeAndeler(førstePeriodeAndeler)
                .medNyeAndeler(nyeAndeler)
                .build();
            list.add(splittetPeriode);
        }
        return list;
    }

    private static LocalDate utledPeriodeTom(List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries, ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator) {
        return listIterator.hasNext() ?
            entries.get(listIterator.nextIndex()).getKey().minusDays(1) :
            null;
    }

    private static boolean harGraderingFørPeriode(GraderingPrAktivitet gradering, LocalDate periodeFom) {
        return gradering.getPerioder().stream().noneMatch(p -> p.getFom().isAfter(periodeFom));
    }

    private static SplittetAndel mapSplittetAndel(GraderingPrAktivitet gradering) {
        SplittetAndel.Builder builder = SplittetAndel.builder()
            .medAktivitetstatus(gradering.getAktivitetStatus())
            .medArbeidsforhold(gradering.getArbeidsforhold());
        return builder.build();
    }

    private static void settAnsettelsesPeriodeHvisFinnes(Periode ansettelsesPeriode, SplittetAndel.Builder builder) {
        if (ansettelsesPeriode != null) {
            builder
                .medArbeidsperiodeFom(ansettelsesPeriode.getFom())
                .medArbeidsperiodeTom(ansettelsesPeriode.getTom());
        }
    }


    private static EksisterendeAndel mapToArbeidsforhold(ArbeidsforholdOgInntektsmelding im, LocalDate fom) {
        Optional<BigDecimal> refusjonskravPrÅr = im.getGyldigeRefusjonskrav().stream()
            .filter(refusjon -> refusjon.getPeriode().inneholder(fom))
            .findFirst()
            .map(refusjonskrav -> refusjonskrav.getMånedsbeløp().multiply(BigDecimal.valueOf(12)));
        Optional<BigDecimal> naturalytelseBortfaltPrÅr = im.getNaturalYtelser().stream()
            .filter(naturalYtelse -> naturalYtelse.getFom().isEqual(DateUtil.TIDENES_BEGYNNELSE))
            .filter(naturalYtelse -> naturalYtelse.getTom().isBefore(fom))
            .map(NaturalYtelse::getBeløp)
            .reduce(BigDecimal::add);
        Optional<BigDecimal> naturalytelseTilkommer = im.getNaturalYtelser().stream()
            .filter(naturalYtelse -> naturalYtelse.getTom().isEqual(DateUtil.TIDENES_ENDE))
            .filter(naturalYtelse -> naturalYtelse.getFom().isBefore(fom))
            .map(NaturalYtelse::getBeløp)
            .reduce(BigDecimal::add);
        return EksisterendeAndel.builder()
            .medAndelNr(im.getAndelsnr())
            .medRefusjonskravPrÅr(refusjonskravPrÅr.orElse(null))
            .medNaturalytelseTilkommetPrÅr(naturalytelseTilkommer.orElse(null))
            .medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr.orElse(null))
            .medArbeidsforhold(im.getArbeidsforhold())
            .medAnvendtRefusjonskravfristHjemmel(im.getRefusjonskravFrist().map(RefusjonskravFrist::getAnvendtHjemmel).orElse(null))
            .build();
    }

    private static List<PeriodeÅrsak> getPeriodeÅrsaker(Set<PeriodeSplittData> periodeSplittData, LocalDate skjæringstidspunkt, LocalDate periodeFom) {
        return periodeSplittData.stream()
            .map(PeriodeSplittData::getPeriodeÅrsak)
            .filter(periodeÅrsak -> !PeriodeÅrsak.UDEFINERT.equals(periodeÅrsak))
            .filter(periodeÅrsak -> !(Set.of(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR).contains(periodeÅrsak)
                && skjæringstidspunkt.equals(periodeFom)))
            .collect(Collectors.toList());
    }

}
