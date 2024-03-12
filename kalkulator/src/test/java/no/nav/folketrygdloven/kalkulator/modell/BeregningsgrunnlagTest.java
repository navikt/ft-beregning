package no.nav.folketrygdloven.kalkulator.modell;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;

public class BeregningsgrunnlagTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    private BeregningsgrunnlagDto.Builder builder;
    private BeregningsgrunnlagDto beregningsgrunnlag;

    @BeforeEach
    public void setup() {
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
    public void skal_bygge_instans_med_påkrevde_felter() {
        BeregningsgrunnlagDto beregningsgrunnlag = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag.getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        BeregningsgrunnlagDto.Builder builder = BeregningsgrunnlagDto.builder();
        try {
            builder.build();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("skjæringstidspunkt");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        BeregningsgrunnlagDto beregningsgrunnlag = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag).isNotEqualTo(null);
        assertThat(beregningsgrunnlag).isNotEqualTo("blabla");
        assertThat(beregningsgrunnlag).isEqualTo(beregningsgrunnlag);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        BeregningsgrunnlagDto beregningsgrunnlag2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag).isEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag2).isEqualTo(beregningsgrunnlag);

        builder.medSkjæringstidspunkt(LocalDate.now().plusDays(1));
        beregningsgrunnlag2 = builder.build();
        assertThat(beregningsgrunnlag).isNotEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag2).isNotEqualTo(beregningsgrunnlag);
    }

    @Test
    public void skal_bruke_skjaeringstidspunkt_i_equalsOgHashCode() {
        BeregningsgrunnlagDto beregningsgrunnlag2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag).isEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag.hashCode()).isEqualTo(beregningsgrunnlag2.hashCode());

        builder.medSkjæringstidspunkt(LocalDate.now().plusDays(1));
        beregningsgrunnlag2 = builder.build();

        assertThat(beregningsgrunnlag).isNotEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag.hashCode()).isNotEqualTo(beregningsgrunnlag2.hashCode());
    }
}
