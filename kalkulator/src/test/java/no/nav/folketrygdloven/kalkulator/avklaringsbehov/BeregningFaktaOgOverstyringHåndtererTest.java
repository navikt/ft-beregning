package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;


public class BeregningFaktaOgOverstyringHåndtererTest {

    private static final LocalDate STP = LocalDate.of(2019, 1, 1);

    private BeregningFaktaOgOverstyringHåndterer beregningFaktaOgOverstyringHåndterer;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(STP);

    @BeforeEach
    public void setup() {
        this.beregningFaktaOgOverstyringHåndterer = new BeregningFaktaOgOverstyringHåndterer();
    }

    @Test
    public void skal_sette_inntekt_for_en_andel_i_en_periode() {
        // Arrange
        Long andelsnr = 1L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(andelsnr, List.of(Intervall.fraOgMedTilOgMed(STP, TIDENES_ENDE)));
        int fastsattBeløp = 10000;
        OverstyrBeregningsgrunnlagDto overstyrDto = new OverstyrBeregningsgrunnlagDto(lagFastsattAndeler(andelsnr, fastsattBeløp), null);
        BeregningsgrunnlagInput input = lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        HåndterBeregningsgrunnlagInput håndterBeregningsgrunnlagInput = new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = beregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(håndterBeregningsgrunnlagInput, overstyrDto);

        // Assert
        Optional<BeregningsgrunnlagDto> nyttBg = nyttGrunnlag.getBeregningsgrunnlagHvisFinnes();
        assertThat(nyttBg).isPresent();
        assertThat(nyttBg.get().isOverstyrt()).isTrue();
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBg.get().getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        BeregningsgrunnlagPeriodeDto p1 = perioder.get(0);
        assertThat(p1.getBeregningsgrunnlagPeriodeFom()).isEqualTo(STP);
        validerAndeler(fastsattBeløp, p1);
    }


    @Test
    public void skal_sette_inntekt_for_en_andel_i_to_perioder() {
        // Arrange
        Long andelsnr = 1L;
        LocalDate tilOgMed = STP.plusMonths(1).minusDays(1);
        List<Intervall> periodeList = List.of(Intervall.fraOgMedTilOgMed(STP, tilOgMed),
                Intervall.fraOgMedTilOgMed(tilOgMed.plusDays(1), TIDENES_ENDE));
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(andelsnr,
                periodeList);
        int fastsattBeløp1 = 10000;
        OverstyrBeregningsgrunnlagDto overstyrDto = new OverstyrBeregningsgrunnlagDto(lagFastsattAndeler(andelsnr, fastsattBeløp1), null);
        BeregningsgrunnlagInput input = lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        HåndterBeregningsgrunnlagInput håndterBeregningsgrunnlagInput = new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = beregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(håndterBeregningsgrunnlagInput, overstyrDto);

        // Assert
        Optional<BeregningsgrunnlagDto> nyttBg = nyttGrunnlag.getBeregningsgrunnlagHvisFinnes();
        assertThat(nyttBg).isPresent();
        assertThat(nyttBg.get().isOverstyrt()).isTrue();
        List<BeregningsgrunnlagPeriodeDto> perioder = nyttBg.get().getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        BeregningsgrunnlagPeriodeDto p1 = perioder.get(0);
        assertThat(p1.getBeregningsgrunnlagPeriodeFom()).isEqualTo(STP);
        validerAndeler(fastsattBeløp1, p1);
        BeregningsgrunnlagPeriodeDto p2 = perioder.get(1);
        assertThat(p2.getBeregningsgrunnlagPeriodeFom()).isEqualTo(tilOgMed.plusDays(1));
        validerAndeler(fastsattBeløp1, p2);
    }



    @Test
    public void skal_ikke_opprette_status_som_allerede_finnes_kombinert() {
        // Arrange
        Long andelsnr = 1L;
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(andelsnr, List.of(Intervall.fraOgMedTilOgMed(STP, TIDENES_ENDE)), AktivitetStatus.KOMBINERT_AT_FL);
        int fastsattBeløp = 10000;
        OverstyrBeregningsgrunnlagDto overstyrDto = new OverstyrBeregningsgrunnlagDto(lagFastsattAndeler(andelsnr, fastsattBeløp, AktivitetStatus.ARBEIDSTAKER), null);
        BeregningsgrunnlagInput input = lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        HåndterBeregningsgrunnlagInput håndterBeregningsgrunnlagInput = new HåndterBeregningsgrunnlagInput(input, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        BeregningsgrunnlagGrunnlagDto nyttGrunnlag = beregningFaktaOgOverstyringHåndterer.håndterMedOverstyring(håndterBeregningsgrunnlagInput, overstyrDto);

        // Assert
        Optional<BeregningsgrunnlagDto> nyttBg = nyttGrunnlag.getBeregningsgrunnlagHvisFinnes();
        assertThat(nyttBg).isPresent();
        assertThat(nyttBg.get().isOverstyrt()).isTrue();
        var statuser = nyttBg.get().getAktivitetStatuser().stream().map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus).collect(Collectors.toList());
        assertThat(statuser).isEqualTo(Collections.singletonList(AktivitetStatus.KOMBINERT_AT_FL));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(Long andelsnr, List<Intervall> perioder) {
        return lagBeregningsgrunnlag(andelsnr, perioder, AktivitetStatus.ARBEIDSTAKER);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(Long andelsnr, List<Intervall> perioder, AktivitetStatus aktivitetStatus) {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(aktivitetStatus))
                .build();
        perioder.forEach(p -> {
            BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(p.getFomDato(), p.getTomDato())
                    .build(beregningsgrunnlag);
            BeregningsgrunnlagPrStatusOgAndelDto.ny().medAndelsnr(andelsnr)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.fra(AktørId.dummy())))
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).build(periode);
        });
        return beregningsgrunnlag;
    }

    private void validerAndeler(int fastsattBeløp, BeregningsgrunnlagPeriodeDto p1) {
        assertThat(p1.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(p1.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getBeregnetPrÅr().intValue()).isEqualTo(fastsattBeløp * 12);
        assertThat(p1.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getFastsattAvSaksbehandler()).isTrue();
        assertThat(p1.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getGjeldendeInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
    }

    private List<FastsettBeregningsgrunnlagAndelDto> lagFastsattAndeler(Long andelsnr, int fastsattBeløp1) {
        return lagFastsattAndeler(andelsnr, fastsattBeløp1, AktivitetStatus.ARBEIDSTAKER);
    }

    private List<FastsettBeregningsgrunnlagAndelDto> lagFastsattAndeler(Long andelsnr, int fastsattBeløp1, AktivitetStatus aktivitetStatus) {
        RedigerbarAndelFaktaOmBeregningDto andelsInfo = new RedigerbarAndelFaktaOmBeregningDto(andelsnr, false, aktivitetStatus, false);
        FastsatteVerdierDto fastsatteVerdier1 = FastsatteVerdierDto.Builder.ny().medFastsattBeløpPrMnd(fastsattBeløp1).build();
        FastsettBeregningsgrunnlagAndelDto andelDto1 = new FastsettBeregningsgrunnlagAndelDto(andelsInfo, fastsatteVerdier1, aktivitetStatus.getInntektskategori(), null, null);
        return List.of(andelDto1);
    }

}
