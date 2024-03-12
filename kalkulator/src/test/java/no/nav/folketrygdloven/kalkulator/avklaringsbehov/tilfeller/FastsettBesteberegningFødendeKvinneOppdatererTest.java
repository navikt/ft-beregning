package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.DagpengeAndelLagtTilBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class FastsettBesteberegningFødendeKvinneOppdatererTest {

    private static final Long ANDELSNR_DAGPENGER = 1L;
    private static final Long ANDELSNR_ARBEIDSTAKER = 2L;
    private static final List<FaktaOmBeregningTilfelle> FAKTA_OM_BEREGNING_TILFELLER = Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE);
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = Beløp.fra(600000);

    private FastsettBesteberegningFødendeKvinneOppdaterer fastsettBesteberegningFødendeKvinneOppdaterer;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPrStatusOgAndelDto dagpengeAndel;
    private BeregningsgrunnlagPrStatusOgAndelDto arbeidstakerAndel;
    private Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet("234432423");
    private BeregningsgrunnlagInput input;


    @BeforeEach
    public void setup() {
        fastsettBesteberegningFødendeKvinneOppdaterer = new FastsettBesteberegningFødendeKvinneOppdaterer();
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
            .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        dagpengeAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(ANDELSNR_DAGPENGER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
            .build(periode1);
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(ANDELSNR_ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode1);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT), beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

    @Test
    public void skal_sette_inntekt_på_andeler() {
        // Arrange
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(FAKTA_OM_BEREGNING_TILFELLER);
        int dagpengerBeregnet = 10000;
        BesteberegningFødendeKvinneAndelDto dpDto = new BesteberegningFødendeKvinneAndelDto(ANDELSNR_DAGPENGER, dagpengerBeregnet, Inntektskategori.DAGPENGER, false);
        int arbeidstakerBeregnet = 20000;
        BesteberegningFødendeKvinneAndelDto atDto = new BesteberegningFødendeKvinneAndelDto(ANDELSNR_ARBEIDSTAKER, arbeidstakerBeregnet,
            Inntektskategori.ARBEIDSTAKER, false);
        BesteberegningFødendeKvinneDto bbDto = new BesteberegningFødendeKvinneDto(List.of(dpDto, atDto));
        dto.setBesteberegningAndeler(bbDto);

        // Act);
        BeregningsgrunnlagGrunnlagDtoBuilder builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        fastsettBesteberegningFødendeKvinneOppdaterer.oppdater(dto, Optional.empty(), builder);

        // Assert
        assertThat(dagpengeAndel.getBesteberegningPrÅr()).isEqualByComparingTo(Beløp.fra(dagpengerBeregnet*12));
        assertThat(dagpengeAndel.getFastsattAvSaksbehandler()).isTrue();
        assertThat(arbeidstakerAndel.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(arbeidstakerBeregnet*12));
        assertThat(arbeidstakerAndel.getFastsattAvSaksbehandler()).isTrue();

    }

    @Test
    public void skal_sette_inntekt_på_andeler_og_legge_til_ny_dagpengeandel() {
        // Arrange
        BeregningsgrunnlagDto bg = lagBGUtenDagpenger();
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE));
        int dagpengerBeregnet = 10000;
        var dpDto = new DagpengeAndelLagtTilBesteberegningDto(dagpengerBeregnet, Inntektskategori.DAGPENGER);
        int arbeidstakerBeregnet = 20000;
        var atDto = new BesteberegningFødendeKvinneAndelDto(ANDELSNR_ARBEIDSTAKER, arbeidstakerBeregnet,
            Inntektskategori.ARBEIDSTAKER, false);
        BesteberegningFødendeKvinneDto bbDto = new BesteberegningFødendeKvinneDto(List.of(atDto), dpDto);
        dto.setBesteberegningAndeler(bbDto);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT), bg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        fastsettBesteberegningFødendeKvinneOppdaterer.oppdater(dto, Optional.empty(), builder);

        // Assert
        BeregningsgrunnlagPrStatusOgAndelDto dpAndel = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.erLagtTilAvSaksbehandler())
            .findFirst().get();
        assertThat(dpAndel.getBesteberegningPrÅr()).isEqualByComparingTo(Beløp.fra(dagpengerBeregnet*12));
        assertThat(dpAndel.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(dagpengerBeregnet*12));
        assertThat(dpAndel.getFastsattAvSaksbehandler()).isTrue();
        BeregningsgrunnlagPrStatusOgAndelDto atAndel = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> !andel.erLagtTilAvSaksbehandler())
            .findFirst().get();
        assertThat(atAndel.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(arbeidstakerBeregnet*12));
        assertThat(atAndel.getFastsattAvSaksbehandler()).isTrue();

    }


    @Test
    public void skal_kunne_bekrefte_avklaringsbehov_på_nytt_med_dagpengeandel() {
        // Arrange
        BeregningsgrunnlagDto nyttBg = lagBGUtenDagpenger();
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE));
        int dagpengerBeregnet = 10000;
        BesteberegningFødendeKvinneAndelDto dpDto = new BesteberegningFødendeKvinneAndelDto(ANDELSNR_DAGPENGER, dagpengerBeregnet,
            Inntektskategori.DAGPENGER,
            true);
        int arbeidstakerBeregnet = 20000;
        BesteberegningFødendeKvinneAndelDto atDto = new BesteberegningFødendeKvinneAndelDto(ANDELSNR_ARBEIDSTAKER, arbeidstakerBeregnet,
            Inntektskategori.ARBEIDSTAKER, false);
        BesteberegningFødendeKvinneDto bbDto = new BesteberegningFødendeKvinneDto(List.of(dpDto, atDto));
        dto.setBesteberegningAndeler(bbDto);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT), nyttBg, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);


        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        fastsettBesteberegningFødendeKvinneOppdaterer.oppdater(dto, Optional.of(beregningsgrunnlag), builder);

        // Assert
        BeregningsgrunnlagPrStatusOgAndelDto dpAndel = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> andel.erLagtTilAvSaksbehandler())
            .findFirst().get();
        assertThat(dpAndel.getBesteberegningPrÅr()).isEqualByComparingTo(Beløp.fra(dagpengerBeregnet*12));
        assertThat(dpAndel.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(dagpengerBeregnet*12));
        assertThat(dpAndel.getFastsattAvSaksbehandler()).isTrue();
        BeregningsgrunnlagPrStatusOgAndelDto atAndel = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> !andel.erLagtTilAvSaksbehandler())
            .findFirst().get();
        assertThat(atAndel.getBeregnetPrÅr()).isEqualByComparingTo(Beløp.fra(arbeidstakerBeregnet*12));
        assertThat(atAndel.getFastsattAvSaksbehandler()).isTrue();

    }
    private BeregningsgrunnlagDto lagBGUtenDagpenger() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilFaktaOmBeregningTilfeller(FAKTA_OM_BEREGNING_TILFELLER)
            .build();
        BeregningsgrunnlagPeriodeDto periode1 = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAndelsnr(ANDELSNR_ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
            .build(periode1);
        return bg;
    }

}
