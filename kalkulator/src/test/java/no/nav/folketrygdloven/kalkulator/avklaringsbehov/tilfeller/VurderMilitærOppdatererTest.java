package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderMilitærDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class VurderMilitærOppdatererTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private static final Beløp GRUNNBELØP = Beløp.fra(85000);

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);

    @Test
    public void skal_legge_til_militærandel_om_vurdert_til_true_og_andel_ikke_finnes() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));
        VurderMilitærDto militærDto = new VurderMilitærDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE));
        dto.setVurderMilitaer(militærDto);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderMilitærOppdaterer.oppdater(dto, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        Optional<BeregningsgrunnlagAktivitetStatusDto> militærStatus = nyttBg.getAktivitetStatuser().stream().filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).findFirst();
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> militærAndel = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).findAny();
        Assertions.assertThat(militærAndel).isPresent();
        Assertions.assertThat(militærStatus).isPresent();
        assertThat(militærStatus.get().getAktivitetStatus()).isEqualTo(AktivitetStatus.MILITÆR_ELLER_SIVIL);
        assertThat(militærStatus.get().getHjemmel()).isEqualTo(Hjemmel.F_14_7);
        assertThat(militærAndel.get().getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.UDEFINERT);
    }

    @Test
    public void skal_ikke_legge_til_militærandel_om_vurdert_til_true_og_andel_finnes_fra_før() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.MILITÆR_ELLER_SIVIL));
        VurderMilitærDto militærDto = new VurderMilitærDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE));
        dto.setVurderMilitaer(militærDto);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderMilitærOppdaterer.oppdater(dto, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagAktivitetStatusDto> militærStatus = nyttBg.getAktivitetStatuser().stream().filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> militærAndeler = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        Assertions.assertThat(militærAndeler).hasSize(1);
        Assertions.assertThat(militærStatus).hasSize(1);
    }

    @Test
    public void skal_ikke_gjøre_noe_dersom_militær_er_false_men_det_ikke_ligger_militær_på_grunnlaget() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));
        VurderMilitærDto militærDto = new VurderMilitærDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE));
        dto.setVurderMilitaer(militærDto);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderMilitærOppdaterer.oppdater(dto, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagAktivitetStatusDto> militærStatus = nyttBg.getAktivitetStatuser().stream().filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> militærAndeler = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        Assertions.assertThat(militærAndeler).hasSize(0);
        Assertions.assertThat(militærStatus).hasSize(0);
        assertThat(nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
    }

    @Test
    public void skal_fjerne_andel_dersom_militær_er_false_og_det_ligger_militær_på_grunnlaget() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag(Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.MILITÆR_ELLER_SIVIL));
        VurderMilitærDto militærDto = new VurderMilitærDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE));
        dto.setVurderMilitaer(militærDto);
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderMilitærOppdaterer.oppdater(dto, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagAktivitetStatusDto> militærStatus = nyttBg.getAktivitetStatuser().stream().filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> militærAndeler = nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(a.getAktivitetStatus())).collect(Collectors.toList());
        Assertions.assertThat(militærAndeler).hasSize(0);
        Assertions.assertThat(militærStatus).hasSize(0);
        assertThat(nyttBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(List<AktivitetStatus> statuser) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();

        BeregningsgrunnlagPeriodeDto periode = buildBeregningsgrunnlagPeriode(bg,
            SKJÆRINGSTIDSPUNKT, null);

        statuser.forEach(status -> {
            BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(status)
                .medHjemmel(Hjemmel.F_14_7).build(bg);
            buildBgPrStatusOgAndel(periode, status);
        });

        return bg;
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(aktivitetStatus)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID);
        if (aktivitetStatus.erArbeidstaker()) {
            builder.medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.fra(AktørId.dummy())));
        }
        builder
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

}
