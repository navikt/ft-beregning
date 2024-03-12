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

public class FastsettBgKunYtelseOppdatererTest {


    private static final Long ANDELSNR = 1L;
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = Beløp.fra(600000);


    private AktivitetStatus brukers_andel = BRUKERS_ANDEL;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setup() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(ANDELSNR)
            .medAktivitetStatus(brukers_andel)
            .build(periode1);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_ved_første_utførelse_av_avklaringsbehov() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsattBrukersAndel andel = new FastsattBrukersAndel(nyAndel, ANDELSNR, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        FastsettBgKunYtelseDto kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(andel), null);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.empty(), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto oppdatert1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(oppdatert1.getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
    }

    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_ved_første_utførelse_av_avklaringsbehov_ved_besteberegning() {
        // Arrange
        final boolean nyAndel = false;
        final boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsattBrukersAndel andel = new FastsattBrukersAndel(nyAndel, ANDELSNR, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        final boolean skalBrukeBesteberegning = true;
        FastsettBgKunYtelseDto kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(andel), skalBrukeBesteberegning);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.empty(), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto oppdatert1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getBesteberegningPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(oppdatert1.getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
    }

    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_ved_første_utførelse_av_avklaringsbehov_ved_ikkje_besteberegning() {
        // Arrange
        final boolean nyAndel = false;
        final boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsattBrukersAndel andel = new FastsattBrukersAndel(nyAndel, ANDELSNR, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        final boolean skalBrukeBesteberegning = false;
        FastsettBgKunYtelseDto kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(andel), skalBrukeBesteberegning);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.empty(), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto oppdatert1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getBesteberegningPrÅr()).isNull();
        assertThat(oppdatert1.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(oppdatert1.getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
    }

    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_fastsatt_lik_overstyrt_i_forrige_utførelse_av_aksonspunkt() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsattBrukersAndel brukersAndel = new FastsattBrukersAndel(nyAndel, ANDELSNR, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        FastsettBgKunYtelseDto kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(brukersAndel), null);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        BeregningsgrunnlagDto eksisterendeGrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        eksisterendeGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel ->
            BeregningsgrunnlagPrStatusOgAndelDto.kopier(andel).medBeregnetPrÅr(Beløp.fra(fastsatt*12))));

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.of(eksisterendeGrunnlag), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto oppdatert1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(oppdatert1.getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(oppdatert1.getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
    }

    @Test
    public void skal_sette_verdier_på_ny_andel() {
        // Arrange
        boolean nyAndel = true;
        boolean lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsattBrukersAndel brukersAndel = new FastsattBrukersAndel(nyAndel, null, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        FastsettBgKunYtelseDto kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(brukersAndel), null);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.empty(), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        List<BeregningsgrunnlagPrStatusOgAndelDto> lagtTil1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());
        Assertions.assertThat(lagtTil1).hasSize(1);
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(lagtTil1.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
        List<BeregningsgrunnlagPrStatusOgAndelDto> fastsattAvSaksbehandler1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        Assertions.assertThat(fastsattAvSaksbehandler1).hasSize(1);
    }

    @Test
    public void skal_sette_verdier_på_andel_lagt_til_av_saksbehandler_ved_tilbakehopp_til_KOFAKBER() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        BeregningsgrunnlagDto førsteGrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        Long andelsnr = 2133L;
        førsteGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(andelsnr)
            .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
            .medAktivitetStatus(brukers_andel)
            .build(periode));
        Integer fastsatt = 100000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsattBrukersAndel brukersAndel = new FastsattBrukersAndel(nyAndel, andelsnr, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        FastsettBgKunYtelseDto kunYtelseDto = new FastsettBgKunYtelseDto(Collections.singletonList(brukersAndel), null);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.of(førsteGrunnlag), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        List<BeregningsgrunnlagPrStatusOgAndelDto> lagtTil1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());
        Assertions.assertThat(lagtTil1).hasSize(1);
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(lagtTil1.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);

        List<BeregningsgrunnlagPrStatusOgAndelDto> fastsattAvSaksbehandler1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        Assertions.assertThat(fastsattAvSaksbehandler1).hasSize(1);
    }

    @Test
    public void skal_sette_verdier_på_andel_lagt_til_av_saksbehandler_ved_tilbakehopp_til_steg_før_KOFAKBER() {
        // Arrange
        BeregningsgrunnlagDto førsteGrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        Long andelsnr = 2133L;
        int overstyrt = 5000;
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
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsattBrukersAndel brukersAndel = new FastsattBrukersAndel(nyAndel, andelsnr, lagtTilAvSaksbehandler, fastsatt,inntektskategori);
        FastsattBrukersAndel brukersAndel2 = new FastsattBrukersAndel(nyAndel, ANDELSNR, false, fastsatt, Inntektskategori.ARBEIDSTAKER);

        FastsettBgKunYtelseDto kunYtelseDto = new FastsettBgKunYtelseDto(Arrays.asList(brukersAndel, brukersAndel2), null);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE));
        dto.setKunYtelseFordeling(kunYtelseDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        FastsettBgKunYtelseOppdaterer.oppdater(dto, Optional.of(førsteGrunnlag), oppdatere);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        List<BeregningsgrunnlagPrStatusOgAndelDto> lagtTil1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(BeregningsgrunnlagPrStatusOgAndelDto::erLagtTilAvSaksbehandler).collect(Collectors.toList());
        Assertions.assertThat(lagtTil1).hasSize(1);
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(fastsatt*12));
        assertThat(lagtTil1.get(0).getGjeldendeInntektskategori()).isEqualTo(inntektskategori);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(BRUKERS_ANDEL);
        List<BeregningsgrunnlagPrStatusOgAndelDto> fastsattAvSaksbehandler1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        Assertions.assertThat(fastsattAvSaksbehandler1).hasSize(2);
    }

}
