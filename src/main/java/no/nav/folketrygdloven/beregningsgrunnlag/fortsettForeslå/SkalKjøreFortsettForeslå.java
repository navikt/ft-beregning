package no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå;

import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalKjøreFortsettForeslå.ID)
public class SkalKjøreFortsettForeslå extends LeafSpecification<BeregningsgrunnlagPeriode> {

    /**
     * Dersom MS eller sn skal RegelFortsettForeslåBeregningsgrunnlag kjøres.
     */
    static final String ID = "FP_BR 2.20";
    static final String BESKRIVELSE = "Skal Fortsett Foreslå kjøres for gjeldende statuser?";

    public SkalKjøreFortsettForeslå() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
	    var harStatusSomMåBeregnesEtterForeslå = grunnlag.getBeregningsgrunnlag().getAktivitetStatuser().stream()
			    .map(AktivitetStatusMedHjemmel::getAktivitetStatus)
			    .anyMatch(s -> s.erSelvstendigNæringsdrivende() || s.erMilitær());
	    return harStatusSomMåBeregnesEtterForeslå
            ? ja()
            : nei();
    }
}
