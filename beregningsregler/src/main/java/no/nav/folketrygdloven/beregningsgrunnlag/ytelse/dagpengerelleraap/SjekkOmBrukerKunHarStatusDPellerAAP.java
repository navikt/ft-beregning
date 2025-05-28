package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBrukerKunHarStatusDPellerAAP.ID)
class SjekkOmBrukerKunHarStatusDPellerAAP extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_10.3";
    static final String BESKRIVELSE = "Har bruker kun status dagpenger/AAP?";

    SjekkOmBrukerKunHarStatusDPellerAAP() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var aktivitetStatuser = grunnlag.getAktivitetStatuser().stream().map(AktivitetStatusMedHjemmel::getAktivitetStatus).toList();
        if (aktivitetStatuser.stream().noneMatch(AktivitetStatus::erAAPellerDP)) {
            throw new IllegalStateException("Utviklerfeil: Skal ikke inntreffe. Ingen aktivitetstatuser funnet med aktivitetstatus DP eller AAP.");
        }
        return aktivitetStatuser.size() == 1 ? ja() : nei();
    }
}
