package no.nav.folketrygdloven.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;

public class VerifiserFastsettBeregningsgrunnlag {

    public static void verifiserBeregningsgrunnlagAvkortetPrÅr(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel, AktivitetStatus aktivitetStatus, double beløp) {
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(aktivitetStatus, hjemmel, grunnlag);
        assertThat(bgpsa.getAvkortetPrÅr().doubleValue()).isCloseTo(beløp, within(0.01));
    }

    public static void verifiserBeregningsgrunnlagAvkortetPrÅrFrilanser(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel, double beløp){
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(AktivitetStatus.ATFL, hjemmel, grunnlag);
        Optional<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdOptional = bgpsa.getFrilansArbeidsforhold();
        arbeidsforholdOptional.ifPresent(af -> assertThat(af.getAvkortetPrÅr().doubleValue()).isCloseTo(beløp, within(0.01)));
    }

    private static BeregningsgrunnlagPrStatus verifiserGrunnlag(AktivitetStatus aktivitetStatus, BeregningsgrunnlagHjemmel hjemmel, BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
        assertThat(bgpsa).isNotNull();
        if (hjemmel != null) {
            assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(bgpsa.getAktivitetStatus()).getHjemmel()).isEqualTo(hjemmel);
        }
        return bgpsa;
    }

}
