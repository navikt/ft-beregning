package no.nav.folketrygdloven.kalkulator.adapter.util;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;

public class KopierBeregningsgrunnlagUtilTest {

    private static final Periode periode = Periode.of(LocalDate.now().minusMonths(3), LocalDate.now().minusMonths(1));

    @Test
    public void skalKopiereBeregninggrunnlagPeriode() {
        // Arrange
        BeregningsgrunnlagPeriode orginal = lagPeriode();
        BeregningsgrunnlagPeriode kopi = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(periode.getTom().plusDays(1), null))
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)
            .build();
        Beregningsgrunnlag.builder().medBeregningsgrunnlagPeriode(orginal).medYtelsesdagerIEtÅr(KonfigTjeneste.getYtelsesdagerIÅr());
        Beregningsgrunnlag.builder().medBeregningsgrunnlagPeriode(kopi).medYtelsesdagerIEtÅr(KonfigTjeneste.getYtelsesdagerIÅr());

        // Act
        KopierBeregningsgrunnlagUtil.kopierBeregningsgrunnlagPeriode(orginal, kopi);
        //Assert
        assertBeregningsgrunnlagPerioderErLike(kopi);
    }


    private void assertBeregningsgrunnlagPerioderErLike(BeregningsgrunnlagPeriode kopi) {
        assertThat(kopi.getBeregningsgrunnlagPrStatus()).hasSize(2);
        assertThat(kopi.getBeregningsgrunnlagPeriode()).isEqualTo(Periode.of(periode.getTom().plusDays(1), null));
        assertThat(kopi.getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        assertATFLAndel(kopi.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL));
        assertSNAndel(kopi.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN));
    }

    private void assertSNAndel(BeregningsgrunnlagPrStatus kopi) {
        assertThat(kopi.getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
        assertThat(kopi.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(kopi.getNyIArbeidslivet()).isFalse();
        assertThat(kopi.getAndelNr()).isEqualTo(2L);
        assertThat(kopi.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(kopi.getArbeidsforhold()).isEmpty();
    }

    private void assertATFLAndel(BeregningsgrunnlagPrStatus kopi) {
        assertThat(kopi.getArbeidsforhold()).hasSize(1);
        assertThat(kopi.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        BeregningsgrunnlagPrArbeidsforhold af = kopi.getArbeidsforhold().get(0);

        assertThat(af.getArbeidsforhold()).isEqualTo(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345"));
        assertThat(af.getAndelNr()).isEqualTo(1L);
        assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(af.getNaturalytelseBortfaltPrÅr().get()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(af.getNaturalytelseTilkommetPrÅr().get()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(af.getMaksimalRefusjonPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(af.getAvkortetRefusjonPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(af.getRedusertRefusjonPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(4));
        assertThat(af.getAvkortetBrukersAndelPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(af.getRedusertBrukersAndelPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(6));
        assertThat(af.getFordeltRefusjonPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(7));
        assertThat(af.getGjeldendeRefusjonPrÅr()).hasValueSatisfying(refusjonskrav ->
            assertThat(refusjonskrav).isEqualByComparingTo(BigDecimal.valueOf(7))
        );
        assertThat(af.getTidsbegrensetArbeidsforhold()).isTrue();
        assertThat(af.getFastsattAvSaksbehandler()).isTrue();
    }

    private BeregningsgrunnlagPeriode lagPeriode() {
        BeregningsgrunnlagPeriode bgPeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(periode)
            .build();

        lagATFLStatus(bgPeriode);
        lagSNStatus(bgPeriode);

        return bgPeriode;
    }

    private void lagATFLStatus(BeregningsgrunnlagPeriode bgPeriode) {
        BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345"))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAndelNr(1L)
            .medBeregnetPrÅr(BigDecimal.valueOf(1))
            .medNaturalytelseBortfaltPrÅr(BigDecimal.ZERO)
            .medNaturalytelseTilkommetPrÅr(BigDecimal.TEN)
            .medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(7))
            .medFordeltRefusjonPrÅr(BigDecimal.valueOf(7))
            .medMaksimalRefusjonPrÅr(BigDecimal.valueOf(2))
            .medAvkortetRefusjonPrÅr(BigDecimal.valueOf(3))
            .medRedusertRefusjonPrÅr(BigDecimal.valueOf(4), KonfigTjeneste.getYtelsesdagerIÅr())
            .medAvkortetBrukersAndelPrÅr(BigDecimal.valueOf(5))
            .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(6), KonfigTjeneste.getYtelsesdagerIÅr())
            .medErTidsbegrensetArbeidsforhold(true)
            .medFastsattAvSaksbehandler(true)
            .build();

        BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medBeregningsgrunnlagPeriode(bgPeriode)
            .medArbeidsforhold(bgArbeidsforhold)
            .build();
    }

    private void lagSNStatus(BeregningsgrunnlagPeriode bgPeriode) {
        BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medBeregningsgrunnlagPeriode(bgPeriode)
            .medBeregnetPrÅr(BigDecimal.valueOf(100000))
            .medErNyIArbeidslivet(false)
            .medAndelNr(2L)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build();
    }


}
