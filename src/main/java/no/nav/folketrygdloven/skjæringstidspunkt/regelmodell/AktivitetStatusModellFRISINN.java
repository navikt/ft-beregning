package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

import java.time.LocalDate;
import java.util.List;

@RuleDocumentationGrunnlag
public class AktivitetStatusModellFRISINN extends AktivitetStatusModell {

    private Inntektsgrunnlag inntektsgrunnlag;
    private List<Periode> beregningsperioder;

    public AktivitetStatusModellFRISINN() {
        super();
    }

    public AktivitetStatusModellFRISINN(Inntektsgrunnlag inntektsgrunnlag, AktivitetStatusModell aktivitetStatusModell) {
        super(aktivitetStatusModell);
        this.inntektsgrunnlag = inntektsgrunnlag;
    }


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

    public List<Periode> getBeregningsperioder() {
        return beregningsperioder;
    }

    public void setBeregningsperioder(List<Periode> beregningsperioder) {
        this.beregningsperioder = beregningsperioder;
    }
}
