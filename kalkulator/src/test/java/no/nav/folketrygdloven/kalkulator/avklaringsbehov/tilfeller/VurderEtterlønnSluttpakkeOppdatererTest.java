package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderEtterlønnSluttpakkeDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class VurderEtterlønnSluttpakkeOppdatererTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private static final Beløp GRUNNBELØP = Beløp.fra(85000);
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagInput input;

    @BeforeEach
    public void setup() {
        beregningsgrunnlag = lagBeregningsgrunnlag();
    }

    @Test
    public void skalTesteAtOppdatererSetterInntekt0DersomBrukerIkkeHarEtterlønnSluttpakke() {
        // Arrange
        VurderEtterlønnSluttpakkeDto vurderDto = new VurderEtterlønnSluttpakkeDto(false);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_ETTERLØNN_SLUTTPAKKE));
        dto.setVurderEtterlønnSluttpakke(vurderDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderEtterlønnSluttpakkeOppdaterer.oppdater(dto, oppdatere);

        // Assert
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        Assertions.assertThat(bgPerioder).hasSize(1);
        assertThat(bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        Assertions.assertThat(andel.getBeregnetPrÅr().compareTo(Beløp.ZERO) == 0).isTrue();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE);
    }

    @Test
    public void skalTesteAtOppdatererIkkeSetterInntektDersomBrukerHarEtterlønnSluttpakke() {
        // Arrange
        VurderEtterlønnSluttpakkeDto vurderDto = new VurderEtterlønnSluttpakkeDto(true);
        FaktaBeregningLagreDto dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_ETTERLØNN_SLUTTPAKKE));
        dto.setVurderEtterlønnSluttpakke(vurderDto);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderEtterlønnSluttpakkeOppdaterer.oppdater(dto, oppdatere);

        // Assert
        BeregningsgrunnlagDto nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = nyttBg.getBeregningsgrunnlagPerioder();
        Assertions.assertThat(bgPerioder).hasSize(1);
        assertThat(bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndelDto andel = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(andel.getBeregnetPrÅr()).isNull();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        BeregningsgrunnlagPeriodeDto periode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            SKJÆRINGSTIDSPUNKT, null);
        buildBgPrStatusOgAndel(periode);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlag(koblingReferanse, beregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT);
        return beregningsgrunnlag;
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE)
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

}
