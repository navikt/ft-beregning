package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalBrukerKunAvviksvurderesSomNæringsdrivende.ID)
class SkalBrukerKunAvviksvurderesSomNæringsdrivende extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.6";
    static final String BESKRIVELSE = "Har bruker kombinasjonsstatus med SN?";

    SkalBrukerKunAvviksvurderesSomNæringsdrivende() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
	    var fomdatoIndividuellSammenligning = grunnlag.getFomDatoForIndividuellSammenligningATFL_SN();
	    var harStatusSN = grunnlag.getBeregningsgrunnlagPrStatus().stream()
			    .anyMatch(bgps -> AktivitetStatus.SN.equals(bgps.getAktivitetStatus()));
	    if (fomdatoIndividuellSammenligning.isEmpty()) {
			return harStatusSN ? ja() : nei();
		}
		return grunnlag.getSkjæringstidspunkt().isBefore(fomdatoIndividuellSammenligning.get()) && harStatusSN ? ja() : nei();
    }
}
