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
    private boolean søkerYtelseFrilans;
    private boolean søkerYtelseNæring;

    public AktivitetStatusModellFRISINN() {
        super();
    }

    public AktivitetStatusModellFRISINN(Inntektsgrunnlag inntektsgrunnlag,
                                        AktivitetStatusModell aktivitetStatusModell,
                                        boolean søkerYtelseFrilans,
                                        boolean søkerYtelseNæring) {
        super(aktivitetStatusModell);
        this.inntektsgrunnlag = inntektsgrunnlag;
        this.søkerYtelseFrilans = søkerYtelseFrilans;
        this.søkerYtelseNæring = søkerYtelseNæring;
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
        return søkerYtelseFrilans;
    }

    public boolean getSøkerYtelseNæring() {
        return søkerYtelseNæring;
    }
}
