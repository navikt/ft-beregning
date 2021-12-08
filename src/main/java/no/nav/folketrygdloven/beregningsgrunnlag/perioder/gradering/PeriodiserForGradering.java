package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodiseringGraderingProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(PeriodiserForGradering.ID)
public class PeriodiserForGradering extends LeafSpecification<PeriodiseringGraderingProsesstruktur> {

    static final String ID = FastsettPerioderGraderingRegel.ID + ".2";
    static final String BESKRIVELSE = "Periodiserer beregningsgrunnlaget ved gitte datoer og oppretter nye andeler.";

    public PeriodiserForGradering() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(PeriodiseringGraderingProsesstruktur prosesstruktur) {
        List<SplittetPeriode> splittetPerioder = periodiserBeregningsgrunnlag(prosesstruktur.getInput(), prosesstruktur.getIdentifisertePeriodeÅrsaker());
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
	        LocalDate periodeTom = utledPeriodeTom(entries, listIterator);
	        Set<PeriodeSplittData> periodeSplittData = entry.getValue();

	        List<SplittetAndel> nyeAndeler = input.getAndelGraderinger().stream()
			        .filter(utbGrad -> utbGrad.erNyAktivitetPåDato(periodeFom))
			        .filter(andel -> harGraderingFørPeriode(andel, periodeFom))
			        .map(PeriodiserForGradering::mapSplittetAndel)
			        .collect(Collectors.toList());

            Periode periode = new Periode(periodeFom, periodeTom);
            SplittetPeriode splittetPeriode = SplittetPeriode.builder()
                .medPeriode(periode)
                .medPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData, input.getSkjæringstidspunkt(), periodeFom))
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

    private static boolean harGraderingFørPeriode(AndelGradering im, LocalDate periodeFom) {
        return im.getGraderinger().stream()
            .anyMatch(gradering -> !gradering.getPeriode().getFom().isAfter(periodeFom));
    }

    private static SplittetAndel mapSplittetAndelFLSN(AndelGradering im) {
        return SplittetAndel.builder()
            .medAktivitetstatus(im.getAktivitetStatus())
            .build();
    }

    private static SplittetAndel mapSplittetAndel(AndelGradering gradering) {
    	if (AktivitetStatusV2.FL.equals(gradering.getAktivitetStatus()) || AktivitetStatusV2.SN.equals(gradering.getAktivitetStatus())) {
    		return mapSplittetAndelFLSN(gradering);
	    }
        Periode ansettelsesPeriode = gradering.getArbeidsforhold() == null ? null : gradering.getArbeidsforhold().getAnsettelsesPeriode();
        SplittetAndel.Builder builder = SplittetAndel.builder()
            .medAktivitetstatus(gradering.getAktivitetStatus())
            .medArbeidsforhold(gradering.getArbeidsforhold());
        settAnsettelsesPeriodeHvisFinnes(ansettelsesPeriode, builder);
        return builder.build();
    }

    private static void settAnsettelsesPeriodeHvisFinnes(Periode ansettelsesPeriode, SplittetAndel.Builder builder) {
        if (ansettelsesPeriode != null) {
            builder
                .medArbeidsperiodeFom(ansettelsesPeriode.getFom())
                .medArbeidsperiodeTom(ansettelsesPeriode.getTom());
        }
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
