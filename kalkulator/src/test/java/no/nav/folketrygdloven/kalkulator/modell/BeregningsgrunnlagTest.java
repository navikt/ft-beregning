package no.nav.folketrygdloven.kalkulator.modell;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

class BeregningsgrunnlagTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    private BeregningsgrunnlagDto.Builder builder;
    private BeregningsgrunnlagDto beregningsgrunnlag;

    @BeforeEach
    void setup() {
        beregningsgrunnlag = lagMedPaakrevdeFelter();
        builder = lagBuilderMedPaakrevdeFelter();
    }

    private static BeregningsgrunnlagDto lagMedPaakrevdeFelter() {
        return lagBuilderMedPaakrevdeFelter().build();
    }

    private static BeregningsgrunnlagDto.Builder lagBuilderMedPaakrevdeFelter() {
        return BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    @Test
    void skal_bygge_instans_med_påkrevde_felter() {
        var beregningsgrunnlag = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag.getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT);
    }

    @Test
    void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        var builder = BeregningsgrunnlagDto.builder();
        try {
            builder.build();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("skjæringstidspunkt");
        }
    }

    @Test
    void skal_ha_refleksiv_equalsOgHashCode() {
        var beregningsgrunnlag2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag).isEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag2).isEqualTo(beregningsgrunnlag);

        builder.medSkjæringstidspunkt(LocalDate.now().plusDays(1));
        beregningsgrunnlag2 = builder.build();
        assertThat(beregningsgrunnlag).isNotEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag2).isNotEqualTo(beregningsgrunnlag);
    }

    @Test
    void skal_bruke_skjaeringstidspunkt_i_equalsOgHashCode() {
        var beregningsgrunnlag2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag).isEqualTo(beregningsgrunnlag2).hasSameHashCodeAs(beregningsgrunnlag2);

        builder.medSkjæringstidspunkt(LocalDate.now().plusDays(1));
        beregningsgrunnlag2 = builder.build();

        assertThat(beregningsgrunnlag).isNotEqualTo(beregningsgrunnlag2).doesNotHaveSameHashCodeAs(beregningsgrunnlag2);
    }
}
