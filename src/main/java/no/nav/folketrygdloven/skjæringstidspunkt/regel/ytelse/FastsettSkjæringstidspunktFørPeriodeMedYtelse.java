package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.FinnPerioderUtenYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringstidspunktFørPeriodeMedYtelse.ID)
class FastsettSkjæringstidspunktFørPeriodeMedYtelse extends LeafSpecification<AktivitetStatusModellFRISINN> {

    static final String ID = "FP_BR 21.5";
    static final String BESKRIVELSE = "Skjæringstidspunkt for beregning settes til første dag etter siste aktivitetsdag";
    static final String VENT = "8000";

    FastsettSkjæringstidspunktFørPeriodeMedYtelse() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModellFRISINN regelmodell) {
        Map<String, Object> resultater = new HashMap<>();
        List<Periode> perioder = finnBeregningsperioder(regelmodell, resultater);
        if (perioder.isEmpty()) {
            List<Aktivitet> tilstøtendeYtelse = Arrays.asList(Aktivitet.SYKEPENGER_MOTTAKER, Aktivitet.FORELDREPENGER_MOTTAKER,
                Aktivitet.PLEIEPENGER_MOTTAKER, Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Aktivitet.OPPLÆRINGSPENGER, Aktivitet.FRISINN_MOTTAKER,
                Aktivitet.OMSORGSPENGER);
            List<AktivPeriode> aktiverPeriodeStp = hentAktivePerioderPåSkjæringtidspunkt(regelmodell.getSkjæringstidspunktForOpptjening(), regelmodell.getAktivePerioder());
            boolean harAAPEllerDPVedSTP = aktiverPeriodeStp.stream()
                .anyMatch(ap -> ap.getAktivitet().equals(Aktivitet.DAGPENGEMOTTAKER) || ap.getAktivitet().equals(Aktivitet.AAP_MOTTAKER));
            boolean harAndreYtelserVedSTP = aktiverPeriodeStp.stream()
                .anyMatch(ap -> tilstøtendeYtelse.contains(ap.getAktivitet()));
            if (!harAAPEllerDPVedSTP || harAndreYtelserVedSTP) {
                return nei(new RuleReasonRefImpl(VENT, "Ingen perioder uten ytelse 3 siste år"));
            }
        }
        resultater.put("beregningsperioder", "Perioder: " + perioder.stream().map(Periode::toString).reduce("", (p1, p2) -> p1 + ", " + p2));
        regelmodell.setBeregningsperioder(perioder);
        LocalDate sisteDatoIBeregningsperioden = perioder.stream().map(Periode::getTom)
            .max(Comparator.naturalOrder())
            .orElse(regelmodell.getSkjæringstidspunktForOpptjening().minusDays(1));
        regelmodell.setSkjæringstidspunktForBeregning(sisteDatoIBeregningsperioden.plusDays(1));
        resultater.put("skjæringstidspunktForBeregning", regelmodell.getSkjæringstidspunktForBeregning());
        return beregnet(resultater);
    }

    private List<Periode> finnBeregningsperioder(AktivitetStatusModellFRISINN regelmodell, Map<String, Object> resultater) {
        Inntektsgrunnlag inntektsgrunnlag = regelmodell.getInntektsgrunnlag();
        return FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, regelmodell.getSkjæringstidspunktForOpptjening(), resultater);
    }

    private List<AktivPeriode> hentAktivePerioderPåSkjæringtidspunkt(LocalDate dato, List<AktivPeriode> aktivePerioder) {
        return aktivePerioder.stream()
            .filter(ap -> ap.getPeriode().getFom().isBefore(dato))
            .filter(ap -> ap.inneholder(dato)).collect(Collectors.toList());
    }


}
