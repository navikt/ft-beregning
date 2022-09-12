package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBortfallAvNaturalytelse.ID)
class SjekkOmBortfallAvNaturalytelse extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.3";
    static final String BESKRIVELSE = "Har bruker bortfall av naturalytelse ved start av perioden eller i tidligere periode?";

    SjekkOmBortfallAvNaturalytelse() {
        super(ID, BESKRIVELSE);
    }

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		throw new IllegalStateException("Utviklerquiz: Hvorfor slår denne til?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag, ServiceArgument arg) {
		var arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold) arg.getVerdi();

        LocalDate fom = grunnlag.getSkjæringstidspunkt();
        LocalDate tom = grunnlag.getBeregningsgrunnlagPeriode().getFom();
        Optional<BigDecimal> naturalytelse = grunnlag.getInntektsgrunnlag().finnTotaltNaturalytelseBeløpMedOpphørsdatoIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), fom, tom);
        return naturalytelse.isPresent() ? ja() : nei();
    }
}
