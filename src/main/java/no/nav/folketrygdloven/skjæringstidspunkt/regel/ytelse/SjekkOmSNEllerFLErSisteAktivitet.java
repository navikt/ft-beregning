package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet.FRILANSINNTEKT;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet.NÆRINGSINNTEKT;

@RuleDocumentation(SjekkOmSNEllerFLErSisteAktivitet.ID)
class SjekkOmSNEllerFLErSisteAktivitet extends LeafSpecification<AktivitetStatusModell> {

    private List<Aktivitet> NØDVENDIGE_AKTIVITETER = Arrays.asList(NÆRINGSINNTEKT, FRILANSINNTEKT);

    static final String ID = "FRISINN 1.1";
    static final String BESKRIVELSE = "Er siste aktivitet før skjæringstidspunkt for opptjening næring eller frilans?";

    SjekkOmSNEllerFLErSisteAktivitet() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
        List<Aktivitet> sisteAktiviteter = aktivePerioder.stream()
            .filter(ap -> !ap.getPeriode().getTom().isBefore(regelmodell.sisteAktivitetsdato()))
            .map(AktivPeriode::getAktivitet)
            .collect(Collectors.toList());

        return sisteAktiviteter.stream().anyMatch(a -> NØDVENDIGE_AKTIVITETER.contains(a))
            ? ja() : nei();
    }

}
