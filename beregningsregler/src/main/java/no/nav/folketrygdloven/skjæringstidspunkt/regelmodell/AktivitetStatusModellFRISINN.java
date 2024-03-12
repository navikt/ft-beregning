package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class AktivitetStatusModellFRISINN extends AktivitetStatusModell {

    private Inntektsgrunnlag inntektsgrunnlag;
    private List<Periode> beregningsperioder;
    private List<FrisinnPeriode> frisinnPerioder;

    public AktivitetStatusModellFRISINN() {
        super();
    }

    public AktivitetStatusModellFRISINN(Inntektsgrunnlag inntektsgrunnlag,
                                        AktivitetStatusModell aktivitetStatusModell,
                                        List<FrisinnPeriode> frisinnPerioder) {
        super(aktivitetStatusModell);
        this.inntektsgrunnlag = inntektsgrunnlag;
        this.frisinnPerioder = frisinnPerioder;
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

    public boolean getSøkerYtelseFrilans() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerYtelseFrilans);
    }

    public boolean getSøkerYtelseNæring() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerYtelseNæring);
    }

    public List<FrisinnPeriode> getFrisinnPerioder() {
        return frisinnPerioder;
    }

    public void setFrisinnPerioder(List<FrisinnPeriode> frisinnPerioder) {
        this.frisinnPerioder = frisinnPerioder;
    }
}
