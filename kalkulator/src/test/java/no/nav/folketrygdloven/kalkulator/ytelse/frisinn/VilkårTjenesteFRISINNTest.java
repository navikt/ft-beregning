package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;

class VilkårTjenesteFRISINNTest {

    public static final LocalDate NOW = LocalDate.now();
    public static final LocalDate SØKNAD_FOM = NOW.plusMonths(1);

    @Test
    void skal_gi_avslag_om_søkt_fl_uten_flandel_og_søkt_avkortet_næring() {
        // Arrange
        BeregningsgrunnlagDto bg = lagBgMedAvkortetNæring();
        KoblingReferanseMock behandlingReferanse = new KoblingReferanseMock(LocalDate.now());
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagIayMedNæringOgFrilans();
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(
                behandlingReferanse,
                iayGrunnlag, null,
                List.of(),
                new FrisinnGrunnlag(List.of(),
                        Collections.singletonList(
                                new FrisinnPeriode(
                                        Intervall.fraOgMedTilOgMed(TIDENES_BEGYNNELSE, TIDENES_ENDE), true, true)), FrisinnBehandlingType.NY_SØKNADSPERIODE));

        // Act
        var beregningVilkårResultat = new VilkårTjenesteFRISINN().lagVilkårResultatFullføre(input, bg);

        // Assert
        boolean finnesAvslåttVilkår = beregningVilkårResultat.stream().anyMatch(vr -> !vr.getErVilkårOppfylt());
        assertThat(finnesAvslåttVilkår).isTrue();
    }

    private BeregningsgrunnlagDto lagBgMedAvkortetNæring() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(NOW)
                .medGrunnbeløp(Beløp.fra(10))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SØKNAD_FOM, SØKNAD_FOM.plusMonths(1))
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAndelsnr(1L)
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medBeregnetPrÅr(Beløp.fra(100_000))
                .medRedusertPrÅr(Beløp.ZERO)
                .medRedusertBrukersAndelPrÅr(Beløp.ZERO)
                .medRedusertRefusjonPrÅr(Beløp.ZERO)
                .medAvkortetPrÅr(Beløp.ZERO)
                .build(periode);
        return bg;
    }

    private InntektArbeidYtelseGrunnlagDto lagIayMedNæringOgFrilans() {
        Intervall søknadsperiode = Intervall.fraOgMedTilOgMed(SØKNAD_FOM, SØKNAD_FOM.plusMonths(1));
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny()
                        .leggTilEgneNæring(OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                                .medPeriode(søknadsperiode)
                                .medBruttoInntekt(Beløp.fra(100_000)))
                        .leggTilFrilansOpplysninger(
                                new OppgittFrilansDto(false,
                                        List.of(new OppgittFrilansInntektDto(søknadsperiode, Beløp.fra(10))))))
                .build();
    }
}
