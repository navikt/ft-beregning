package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.BRUKERS_ANDEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattBrukersAndel;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBgKunYtelseDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

class FastsettBgKunYtelseOppdatererTest {


    private static final Long ANDELSNR = 1L;
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = Beløp.fra(600000);


    private AktivitetStatus brukers_andel = BRUKERS_ANDEL;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    void setup() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        var periode1 = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(ANDELSNR)
            .medAktivitetStatus(brukers_andel)
            .build(periode1);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_ved_første_utførelse_av_avklaringsbehov() {
        // Arrange
        var nyAndel = false;
        var lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        var inntektskategori = Inntektskategori.SJØMANN;
        var andel = new FastsattBrukersAndel(nyAndel, ANDELSNR, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        var kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(andel), null);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.empty(), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        var oppdatert1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(oppdatert1.getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
    }

    @Test
    void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_ved_første_utførelse_av_avklaringsbehov_ved_besteberegning() {
        // Arrange
        final var nyAndel = false;
        final var lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        var inntektskategori = Inntektskategori.SJØMANN;
        var andel = new FastsattBrukersAndel(nyAndel, ANDELSNR, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        final var skalBrukeBesteberegning = true;
        var kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(andel), skalBrukeBesteberegning);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.empty(), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        var oppdatert1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getBesteberegningPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(oppdatert1.getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
    }

    @Test
    void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_ved_første_utførelse_av_avklaringsbehov_ved_ikkje_besteberegning() {
        // Arrange
        final var nyAndel = false;
        final var lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        var inntektskategori = Inntektskategori.SJØMANN;
        var andel = new FastsattBrukersAndel(nyAndel, ANDELSNR, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        final var skalBrukeBesteberegning = false;
        var kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(andel), skalBrukeBesteberegning);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.empty(), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        var oppdatert1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getBesteberegningPrÅr()).isNull();
        assertThat(oppdatert1.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(oppdatert1.getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
    }

    @Test
    void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_fastsatt_lik_overstyrt_i_forrige_utførelse_av_aksonspunkt() {
        // Arrange
        var nyAndel = false;
        var lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        var inntektskategori = Inntektskategori.SJØMANN;
        var brukersAndel = new FastsattBrukersAndel(nyAndel, ANDELSNR, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        var kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(brukersAndel), null);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        var eksisterendeGrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        eksisterendeGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel ->
            BeregningsgrunnlagPrStatusOgAndelDto.kopier(andel).medBeregnetPrÅr(Beløp.fra(fastsatt*12))));

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.of(eksisterendeGrunnlag), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        var oppdatert1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(oppdatert1.getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
    }

    @Test
    void skal_sette_verdier_på_ny_andel() {
        // Arrange
        var nyAndel = true;
        var lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        var inntektskategori = Inntektskategori.SJØMANN;
        var brukersAndel = new FastsattBrukersAndel(nyAndel, null, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        var kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(brukersAndel), null);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.empty(), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var lagtTil1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());
        Assertions.assertThat(lagtTil1).hasSize(1);
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(lagtTil1.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
        var fastsattAvSaksbehandler1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        Assertions.assertThat(fastsattAvSaksbehandler1).hasSize(1);
    }

    @Test
    void skal_sette_verdier_på_andel_lagt_til_av_saksbehandler_ved_tilbakehopp_til_KOFAKBER() {
        // Arrange
        var nyAndel = false;
        var lagtTilAvSaksbehandler = true;
        var førsteGrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        Long andelsnr = 2133L;
        førsteGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(andelsnr)
            .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
            .medAktivitetStatus(brukers_andel)
            .build(periode));
        Integer fastsatt = 100000;
        var inntektskategori = Inntektskategori.SJØMANN;
        var brukersAndel = new FastsattBrukersAndel(nyAndel, andelsnr, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        var kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(brukersAndel), null);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.of(førsteGrunnlag), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var lagtTil1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());
        Assertions.assertThat(lagtTil1).hasSize(1);
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(lagtTil1.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);

        var fastsattAvSaksbehandler1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        Assertions.assertThat(fastsattAvSaksbehandler1).hasSize(1);
    }

    @Test
    void skal_sette_verdier_på_andel_lagt_til_av_saksbehandler_ved_tilbakehopp_til_steg_før_KOFAKBER() {
        // Arrange
        var førsteGrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        Long andelsnr = 2133L;
        var overstyrt = 5000;
        førsteGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel ->
                BeregningsgrunnlagPrStatusOgAndelDto.kopier(andel)
                    .medBeregnetPrÅr(Beløp.fra(100000*12))
            );
            BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(andelsnr)
            .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
            .medAktivitetStatus(brukers_andel)
            .medBeregnetPrÅr(Beløp.fra(overstyrt *12))
            .build(periode);
        });
        var nyAndel = false;
        var lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        var inntektskategori = Inntektskategori.SJØMANN;
        var brukersAndel = new FastsattBrukersAndel(nyAndel, andelsnr, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        var brukersAndel2 = new FastsattBrukersAndel(nyAndel, ANDELSNR, false, fastsatt, Inntektskategori.ARBEIDSTAKER);

        var kunYtelseDto = new FastsettBgKunYtelseDto(Arrays.asList(brukersAndel, brukersAndel2), null);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.of(førsteGrunnlag), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        var lagtTil1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());
        Assertions.assertThat(lagtTil1).hasSize(1);
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(lagtTil1.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
        var fastsattAvSaksbehandler1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        Assertions.assertThat(fastsattAvSaksbehandler1).hasSize(2);
    }

}
