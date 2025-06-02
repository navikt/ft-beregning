package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;

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

class VurderEtterlønnSluttpakkeOppdatererTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019,1,1);
    private static final Beløp GRUNNBELØP = Beløp.fra(85000);
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagInput input;

    @BeforeEach
    void setup() {
        beregningsgrunnlag = lagBeregningsgrunnlag();
    }

    @Test
    void skalTesteAtOppdatererSetterInntekt0DersomBrukerIkkeHarEtterlønnSluttpakke() {
        // Arrange
        var vurderDto = new VurderEtterlønnSluttpakkeDto(false);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_ETTERLØNN_SLUTTPAKKE));
        dto.setVurderEtterlønnSluttpakke(vurderDto);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderEtterlønnSluttpakkeOppdaterer.oppdater(dto, oppdatere);

        // Assert
        var bgPerioder = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        Assertions.assertThat(bgPerioder).hasSize(1);
        assertThat(bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        var andel = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        Assertions.assertThat(andel.getBeregnetPrÅr().compareTo(Beløp.ZERO) == 0).isTrue();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE);
    }

    @Test
    void skalTesteAtOppdatererIkkeSetterInntektDersomBrukerHarEtterlønnSluttpakke() {
        // Arrange
        var vurderDto = new VurderEtterlønnSluttpakkeDto(true);
        var dto = new FaktaBeregningLagreDto(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_ETTERLØNN_SLUTTPAKKE));
        dto.setVurderEtterlønnSluttpakke(vurderDto);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        VurderEtterlønnSluttpakkeOppdaterer.oppdater(dto, oppdatere);

        // Assert
        var nyttBg = oppdatere.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        var bgPerioder = nyttBg.getBeregningsgrunnlagPerioder();
        Assertions.assertThat(bgPerioder).hasSize(1);
        assertThat(bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        var andel = bgPerioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(andel.getBeregnetPrÅr()).isNull();
        assertThat(andel.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        var periode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
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
