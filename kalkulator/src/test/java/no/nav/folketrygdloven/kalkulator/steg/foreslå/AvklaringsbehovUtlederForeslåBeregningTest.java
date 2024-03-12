package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovUtlederForeslåBeregningTest {

    private KoblingReferanse referanse = new KoblingReferanseMock();

    @Test
    public void skalIkkeFåAvklaringsbehovVed100PDekningsgrad() {
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(lagInput(referanse), Collections.emptyList());
        // Assert
        assertThat(avklaringsbehov).isEmpty();
    }

    @Test
    public void skalIkkeFåAvklaringsbehovVed80PDekningsgrad() {
        // Arrange
        var input = lagInput();
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.emptyList());
        // Assert
        assertThat(avklaringsbehov).isEmpty();
    }

    @Test
    public void skalFåAvklaringsbehov5042() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat(BeregningUtfallÅrsak.VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT);
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(lagInput(referanse), Collections.singletonList(regelResultat));
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN);
    }

    @Test
    public void skalFåAvklaringsbehov5049() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat(BeregningUtfallÅrsak.FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET);
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(lagInput(referanse), Collections.singletonList(regelResultat));
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactly(AvklaringsbehovDefinisjon.FASTSETT_BG_SN_NY_I_ARB_LIVT);
    }

    @Test
    public void skalFåAvklaringsbehov5038() {
        // Arrange
        RegelResultat regelResultat = lagRegelResultat(BeregningUtfallÅrsak.FASTSETT_AVVIK_OVER_25_PROSENT);

        var input = lagInput();

        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.singletonList(regelResultat));
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).containsExactlyInAnyOrder(
            AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL
        );
    }

    private BeregningsgrunnlagInput lagInput() {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_80, false);
        return new BeregningsgrunnlagInput(referanse, null, null, List.of(), foreldrepengerGrunnlag);
    }

    @Test
    public void skal_ikke_få_avklaringsbehov5087_når_barnet_ikke_har_dødd() {
        // Arrange
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(lagInput(referanse), Collections.emptyList());
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_avklaringsbehov5087_når_ingen_barn_et_født() {
        // Arrange
        var input = lagInput();
        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.emptyList());
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_avklaringsbehov5087_når_ikke_alle_barnene_døde_innen_seks_uker() {
        // Arrange
        var input = lagInput();

        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.emptyList());
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    @Test
    public void skal_ikke_få_avklaringsbehov5087_når_ett_barn_døde_og_ett_barn_levde() {
        // Arrange
        var input = lagInput();

        // Act
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, Collections.emptyList());
        // Assert
        var apDefs = avklaringsbehov.stream().map(BeregningAvklaringsbehovResultat::getBeregningAvklaringsbehovDefinisjon).collect(Collectors.toList());
        assertThat(apDefs).isEmpty();
    }

    private BeregningsgrunnlagInput lagInput(KoblingReferanse referanse) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        return new BeregningsgrunnlagInput(referanse, null, null, List.of(), foreldrepengerGrunnlag);
    }

    private RegelResultat lagRegelResultat(BeregningUtfallÅrsak utfallÅrsak) {
        RegelMerknad regelMerknad = new RegelMerknad(utfallÅrsak);
        return RegelResultat.medRegelMerknad(new RegelResultat(ResultatBeregningType.IKKE_BEREGNET, "1.2.3", "regelInput", "regelSporing"), regelMerknad);
    }

}
