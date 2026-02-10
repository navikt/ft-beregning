package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

@ExtendWith(MockitoExtension.class)
class FinnInntektFraYtelseTest {

    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = Beløp.fra(600000);

    private final LocalDate fom = SKJÆRINGSTIDSPUNKT;
    private final LocalDate tom = SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1);



    @Test
    void skal_finne_årsbeløpp_dagpenger_når_kilde_er_dpsak() {
        // Arrange
        KoblingReferanse ref = new KoblingReferanse();
        var skjæringstidspunkt = LocalDate.of(2020, 1, 1);
        ref = ref.medSkjæringstidspunkt(skjæringstidspunkt);

        var aapYtelse = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, skjæringstidspunkt.minusMonths(2), skjæringstidspunkt.minusMonths(1), YtelseKilde.KELVIN).build();
        var dpYtelse = lagYtelse(YtelseType.DAGPENGER, skjæringstidspunkt.minusMonths(1), skjæringstidspunkt.minusDays(1), YtelseKilde.DPSAK).medVedtaksDagsats(
            Beløp.fra(1500)).build();

        var ytelseFilter = new YtelseFilterDto(Arrays.asList(aapYtelse, dpYtelse));
        var beregningsgrunnlagPrStatusOgAndel = opprettBeregningsgrunnlagPrStatusOgAndelDto();

        // Act
        var result = FinnInntektFraYtelse.finnÅrbeløpForDagpenger(ref, beregningsgrunnlagPrStatusOgAndel, ytelseFilter);

        // Assert
        assertTrue(result.isPresent(), "Expected a present årsbeløp when vedtak/meldekort exist");
        assertThat(result.get()).isEqualTo(Beløp.fra(390000));
    }

    @Test
    void skal_finne_halvparten_av_årsbeløpp_dagpenger_når_kilde_er_arena() {
        // Arrange
        KoblingReferanse ref = new KoblingReferanse();
        var skjæringstidspunkt = LocalDate.of(2020, 1, 1);
        ref = ref.medSkjæringstidspunkt(skjæringstidspunkt);

        var aapYtelse = lagYtelse(YtelseType.ARBEIDSAVKLARINGSPENGER, skjæringstidspunkt.minusMonths(2), skjæringstidspunkt.minusMonths(1), YtelseKilde.ARENA).build();
        var dpYtelse = lagYtelse(YtelseType.DAGPENGER, skjæringstidspunkt.minusMonths(1), skjæringstidspunkt.minusDays(1), YtelseKilde.ARENA).medVedtaksDagsats(
            Beløp.fra(1500)).build();

        var ytelseFilter = new YtelseFilterDto(Arrays.asList(aapYtelse, dpYtelse));
        var beregningsgrunnlagPrStatusOgAndel = opprettBeregningsgrunnlagPrStatusOgAndelDto();

        // Act
        var result = FinnInntektFraYtelse.finnÅrbeløpForDagpenger(ref, beregningsgrunnlagPrStatusOgAndel, ytelseFilter);

        // Assert
        assertTrue(result.isPresent(), "Expected a present årsbeløp when vedtak/meldekort exist");
        assertThat(result.get().verdi().longValue()).isEqualTo(195000);
    }


    private BeregningsgrunnlagPrStatusOgAndelDto opprettBeregningsgrunnlagPrStatusOgAndelDto() {
        var bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER))
            .build();


        var andel1 = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("1234534")))
            .medAndelsnr(2L)
            .build();

        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .medBruttoPrÅr(new Beløp(BigDecimal.valueOf(500)))
            .leggTilBeregningsgrunnlagPrStatusOgAndel(andel1)
            .build(bg);

        var andel2 = BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .medBeregnetPrÅr(Beløp.fra(5000))
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("1234534")))
            .medAndelsnr(1L)
            .medBeregningsperiode(fom, tom)
            .build(periode);
        return andel2;
    }

    private YtelseDtoBuilder lagYtelse(YtelseType ytelsetype, LocalDate fom, LocalDate tom, YtelseKilde ytelseKilde) {
        var ytelseAnvistDto = YtelseAnvistDtoBuilder.ny()
            .medAnvistPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .medBeløp(Beløp.fra(50000))
            .medUtbetalingsgradProsent(Stillingsprosent.HUNDRED);
        return YtelseDtoBuilder.ny()
            .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .medYtelseType(ytelsetype)
            .medYtelseKilde(ytelseKilde)
            .leggTilYtelseAnvist(ytelseAnvistDto.build());
    }
}
