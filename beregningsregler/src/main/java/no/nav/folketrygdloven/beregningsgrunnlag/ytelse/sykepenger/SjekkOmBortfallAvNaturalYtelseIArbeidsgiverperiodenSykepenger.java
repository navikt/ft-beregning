package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.sykepenger;

import java.math.BigDecimal;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBortfallAvNaturalYtelseIArbeidsgiverperiodenSykepenger.ID)
public class SjekkOmBortfallAvNaturalYtelseIArbeidsgiverperiodenSykepenger extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.8";
    static final String BESKRIVELSE = "Er det bortfall av naturalytelse i arbeidsgiverperioden (gjelder sykepenger spesifikt)?";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    public SjekkOmBortfallAvNaturalYtelseIArbeidsgiverperiodenSykepenger(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        var erBortfaltNaturalytelseIArbeidsgiverperioden = arbeidsforhold.getArbeidsgiverperioder()
            .stream()
            .anyMatch(periode -> erBortfaltNaturalytelseIPeriode(inntektsgrunnlag, periode));

        return erBortfaltNaturalytelseIArbeidsgiverperioden ? ja() : nei();
    }

    private boolean erBortfaltNaturalytelseIPeriode(Inntektsgrunnlag inntektsgrunnlag, Periode periode) {
        var naturalytelseOpt =
            inntektsgrunnlag.finnTotaltNaturalytelseBeløpMedOpphørsdatoIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), periode.getFom(), periode.getTom());
        return naturalytelseOpt.isPresent();
    }
}
