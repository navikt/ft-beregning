package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.felles.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class MatchBeregningsgrunnlagTjenesteTest {


    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final Arbeidsgiver arbeidsgiverEn = Arbeidsgiver.virksomhet("973152351");


    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto bg) {
        return BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(bg);
    }

    @Test
    public void skal_matche_på_andelsnr() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nullRef();
        Long andelsnr = 1L;

        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(andelsnr)
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .medInntektskategori(Inntektskategori.SJØMANN)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiverEn))
                .build(periode);

        // Act
        BeregningsgrunnlagPrStatusOgAndelDto korrektAndel = MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriode(periode, andelsnr, arbId);

        // Assert
        assertThat(korrektAndel).isEqualTo(andel);
    }

    @Test
    public void skal_matche_på_arbid_om_andelsnr_input_er_null() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;

        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiverEn).medArbeidsforholdRef(arbId))
                .medAndelsnr(andelsnr)
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .medInntektskategori(Inntektskategori.SJØMANN)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);

        // Act
        BeregningsgrunnlagPrStatusOgAndelDto korrektAndel = MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriode(periode, null, arbId);

        // Assert
        assertThat(korrektAndel).isEqualTo(andel);
    }

    @Test
    public void skal_kaste_exception_om_andel_ikkje_eksisterer_i_grunnlag() {
        // Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        Long andelsnr = 1L;

        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiverEn).medArbeidsforholdRef(arbId))
                .medAndelsnr(andelsnr)
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .medInntektskategori(Inntektskategori.SJØMANN)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);

        // Act
        Assertions.assertThrows(KalkulatorException.class, () -> MatchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriode(periode, 2L, InternArbeidsforholdRefDto.nyRef()));
    }
}
