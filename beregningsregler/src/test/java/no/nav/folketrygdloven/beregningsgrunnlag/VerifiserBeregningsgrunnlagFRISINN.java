package no.nav.folketrygdloven.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

public class VerifiserBeregningsgrunnlagFRISINN {

    public static void verifiserBeregningsgrunnlagBruttoPrPeriodeType(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagHjemmel hjemmel, AktivitetStatus aktivitetStatus, double beløp) {
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(aktivitetStatus, hjemmel, grunnlag);
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isCloseTo(beløp, within(0.01));
        if (aktivitetStatus.erSelvstendigNæringsdrivende()) {
            assertThat(bgpsa.getGjennomsnittligPGI()).isNull();
            assertThat(bgpsa.getPgiListe()).isEmpty();
        }
    }

    public static void verifiserBeregningsperiode(AktivitetStatus aktivitetStatus, BeregningsgrunnlagHjemmel hjemmel, BeregningsgrunnlagPeriode grunnlag, Periode periode) {
        BeregningsgrunnlagPrStatus bgpsa = verifiserGrunnlag(aktivitetStatus, hjemmel, grunnlag);
        assertThat(bgpsa.getBeregningsperiode()).isNotNull();
        assertThat(bgpsa.getBeregningsperiode()).isEqualTo(periode);
    }

    public static void verifiserBeregningsperiode(BeregningsgrunnlagPrArbeidsforhold af, Periode periode) {
        assertThat(af.getBeregningsperiode()).isNotNull();
        assertThat(af.getBeregningsperiode()).isEqualTo(periode);
    }

    private static BeregningsgrunnlagPrStatus verifiserGrunnlag(AktivitetStatus aktivitetStatus, BeregningsgrunnlagHjemmel hjemmel, BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgpsa = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
        assertThat(bgpsa).isNotNull();
        if (hjemmel != null) {
            assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(bgpsa.getAktivitetStatus()).getHjemmel()).isEqualTo(hjemmel);
        }
        return bgpsa;
    }

    public static void verifiserBeregningsgrunnlagBeregnet(BeregningsgrunnlagPeriode grunnlag, double beregnet, double avkortet) {
        verifiserBeregningsgrunnlagBeregnet(grunnlag, beregnet, avkortet, avkortet);
    }

    public static void verifiserBeregningsgrunnlagBeregnet(BeregningsgrunnlagPeriode grunnlag, double beregnet) {
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isCloseTo(beregnet, within(0.01));
        assertThat(grunnlag.getAvkortetPrÅr()).isNull();
        assertThat(grunnlag.getRedusertPrÅr()).isNull();
    }

    public static void verifiserBeregningsgrunnlagBeregnet(BeregningsgrunnlagPeriode grunnlag, double beregnet, double avkortet, double redusert) {
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isCloseTo(beregnet, within(0.01));
        assertThat(grunnlag.getAvkortetPrÅr().doubleValue()).isCloseTo(avkortet, within(0.01));
        assertThat(grunnlag.getRedusertPrÅr().doubleValue()).isCloseTo(redusert, within(0.01));
    }

}
