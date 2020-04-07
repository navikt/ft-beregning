package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet.FRILANSINNTEKT;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet.NÆRINGSINNTEKT;

@RuleDocumentation(ErAktivSomSNEllerFLPåSKjæringstidspunktForOpptjening.ID)
class ErAktivSomSNEllerFLPåSKjæringstidspunktForOpptjening extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FRISINN 1.3";
    static final String BESKRIVELSE = "Er søker aktiv på eller etter skjæringstidspunktet for opptjening med aktivitet SN eller FL.";
    private List<Aktivitet> NØDVENDIGE_AKTIVITETER = Arrays.asList(NÆRINGSINNTEKT, FRILANSINNTEKT);

    ErAktivSomSNEllerFLPåSKjæringstidspunktForOpptjening() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        LocalDate sisteAktivitetDato = regelmodell.getAktivePerioder().stream()
            .filter(ap -> NØDVENDIGE_AKTIVITETER.contains(ap.getAktivitet()))
            .map(ap -> ap.getPeriode().getTom())
            .max(Comparator.naturalOrder())
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ikke være mulig å havne her uten en aktivitet som er frilans eller næring"));

        LocalDate skjæringstidspunkt = !sisteAktivitetDato.isBefore(regelmodell.getSkjæringstidspunktForOpptjening())
            ? regelmodell.getSkjæringstidspunktForOpptjening()
            : null;

        regelmodell.setSkjæringstidspunktForBeregning(skjæringstidspunkt);
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktForBeregning", skjæringstidspunkt);
        return beregnet(resultater);
    }
}
