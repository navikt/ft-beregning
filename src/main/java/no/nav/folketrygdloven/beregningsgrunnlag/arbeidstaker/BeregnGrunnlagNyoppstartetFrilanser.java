package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnGrunnlagNyoppstartetFrilanser.ID)
class BeregnGrunnlagNyoppstartetFrilanser extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FB_BR 14.10";
    static final String BESKRIVELSE = "Beregn grunnlag nyoppstartet frilanser = (avklart inntekt * 12)";

    BeregnGrunnlagNyoppstartetFrilanser() {
        super(ID, BESKRIVELSE);
    }

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		throw new IllegalStateException("Utviklerquiz: Hvorfor slår denne til?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag, ServiceArgument arg) {
		var arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold) arg.getVerdi();
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        resultater.put("beregnetPrÅr", arbeidsforhold.getBeregnetPrÅr());
        return beregnet(resultater);
    }
}
