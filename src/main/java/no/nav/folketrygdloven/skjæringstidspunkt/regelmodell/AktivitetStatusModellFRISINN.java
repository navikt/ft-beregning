package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

import java.time.LocalDate;

@RuleDocumentationGrunnlag
public class AktivitetStatusModellFRISINN extends AktivitetStatusModell {

    private Inntektsgrunnlag inntektsgrunnlag;

    @Override
    public LocalDate sisteAktivitetsdato() {
        return finnSisteAktivitetsdatoFraSistePeriode();
    }

    public Inntektsgrunnlag getInntektsgrunnlag() {
        return inntektsgrunnlag;
    }

    public void setInntektsgrunnlag(Inntektsgrunnlag inntektsgrunnlag) {
        this.inntektsgrunnlag = inntektsgrunnlag;
    }
}
