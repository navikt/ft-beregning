package no.nav.folketrygdloven.kalkulator.modell;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;

public class BeregningsgrunnlagPeriodeTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final LocalDate PERIODE_FOM = LocalDate.now();

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelListe;
    private BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode;

    @BeforeEach
    public void setup() {
        beregningsgrunnlag = lagBeregningsgrunnlag();
        beregningsgrunnlagPrStatusOgAndelListe = new ArrayList<>();
        beregningsgrunnlagPrStatusOgAndelListe.add(lagBeregningsgrunnlagPrStatusOgAndel());
        beregningsgrunnlagPeriode = lagMedPaakrevdeFelter(beregningsgrunnlag);
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).isEqualTo(beregningsgrunnlagPrStatusOgAndelListe);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(PERIODE_FOM);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        var beregningsgrunnlagPeriodeBuilder = BeregningsgrunnlagPeriodeDto.ny();
        try {
            beregningsgrunnlagPeriodeBuilder.build(null);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlag");
        }

        beregningsgrunnlagPeriodeBuilder.medBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndelListe);

        try {
            beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlag);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlagPeriodeFom");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        assertThat(beregningsgrunnlagPeriode).isNotEqualTo(null);
        assertThat(beregningsgrunnlagPeriode).isNotEqualTo("blabla");
        assertThat(beregningsgrunnlagPeriode).isEqualTo(beregningsgrunnlagPeriode);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = lagMedPaakrevdeFelter(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode2).isEqualTo(beregningsgrunnlagPeriode);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isEqualTo(beregningsgrunnlagPeriode2.hashCode());
        assertThat(beregningsgrunnlagPeriode2.hashCode()).isEqualTo(beregningsgrunnlagPeriode.hashCode());

        BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder = lagBuilderMedPaakrevdeFelter();
        beregningsgrunnlagPeriodeBuilder.medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(1), null);
        beregningsgrunnlagPeriode2 = beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlag);
        assertThat(beregningsgrunnlagPeriode).isNotEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode2).isNotEqualTo(beregningsgrunnlagPeriode);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isNotEqualTo(beregningsgrunnlagPeriode2.hashCode());
        assertThat(beregningsgrunnlagPeriode2.hashCode()).isNotEqualTo(beregningsgrunnlagPeriode.hashCode());
    }

    @Test
    public void skal_bruke_beregningsgrunnlagPeriodeFom_i_equalsOgHashCode() {
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = lagMedPaakrevdeFelter(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isEqualTo(beregningsgrunnlagPeriode2.hashCode());

        BeregningsgrunnlagPeriodeDto.Builder beregningsgrunnlagPeriodeBuilder = lagBuilderMedPaakrevdeFelter();
        beregningsgrunnlagPeriodeBuilder.medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(1), null);
        beregningsgrunnlagPeriode2 = beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isNotEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isNotEqualTo(beregningsgrunnlagPeriode2.hashCode());
    }

    @Test
    public void skal_bruke_beregningsgrunnlag_i_equalsOgHashCode() {
        var builder = lagBuilderMedPaakrevdeFelter();
        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = builder.build(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isEqualTo(beregningsgrunnlagPeriode2.hashCode());

        var builder2 = lagBuilderMedPaakrevdeFelter();
        builder2.medBeregningsgrunnlagPeriode(LocalDate.now().plusDays(1), null);
        beregningsgrunnlagPeriode2 = builder2.build(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isNotEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isNotEqualTo(beregningsgrunnlagPeriode2.hashCode());
    }

    private BeregningsgrunnlagPeriodeDto lagMedPaakrevdeFelter(BeregningsgrunnlagDto beregningsgrunnlag) {
        return lagBuilderMedPaakrevdeFelter().build(beregningsgrunnlag);
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBuilderMedPaakrevdeFelter() {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPrStatusOgAndelListe)
            .medBeregningsgrunnlagPeriode(PERIODE_FOM, null);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        return lagBeregningsgrunnlagMedSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(skjæringstidspunkt).build();
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagBeregningsgrunnlagPrStatusOgAndel() {
        return new BeregningsgrunnlagPrStatusOgAndelDto();
    }
}
