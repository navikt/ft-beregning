package no.nav.folketrygdloven.kalkulator.modell;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;


class BeregningsgrunnlagAktivitetStatusTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final AktivitetStatus AKTIVITET_STATUS = AktivitetStatus.ARBEIDSTAKER;
    private static final Hjemmel BARE_ARBEIDSTAKER = Hjemmel.F_14_7_8_30;
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagAktivitetStatusDto beregningsgrunnlagAktivitetStatus;

    @BeforeEach
    void setup() {
        beregningsgrunnlag = lagBeregningsgrunnlag();
        beregningsgrunnlagAktivitetStatus = lagMedPaakrevdeFelter();
    }

    @Test
    void skal_bygge_instans_med_påkrevde_felter() {
        assertThat(beregningsgrunnlagAktivitetStatus.getBeregningsgrunnlag()).isEqualTo(beregningsgrunnlag);
        assertThat(beregningsgrunnlagAktivitetStatus.getAktivitetStatus()).isEqualTo(AKTIVITET_STATUS);
    }

    @Test
    void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
	    var builder = BeregningsgrunnlagAktivitetStatusDto.builder();
        try {
            builder.build(null);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlag");
        }

        builder = BeregningsgrunnlagAktivitetStatusDto.builder();

        try {
            builder.build(beregningsgrunnlag);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("aktivitetStatus");
        }
    }

    @Test
    void skal_ha_refleksiv_equalsOgHashCode() {
	    var beregningsgrunnlagAktivitetStatus2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlagAktivitetStatus).isEqualTo(beregningsgrunnlagAktivitetStatus2)
            .hasSameHashCodeAs(beregningsgrunnlagAktivitetStatus2);
        assertThat(beregningsgrunnlagAktivitetStatus2).isEqualTo(beregningsgrunnlagAktivitetStatus)
            .hasSameHashCodeAs(beregningsgrunnlagAktivitetStatus);

        beregningsgrunnlagAktivitetStatus2 = lagBuilderMedPaakrevdeFelter().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(beregningsgrunnlag);
        assertThat(beregningsgrunnlagAktivitetStatus).isNotEqualTo(beregningsgrunnlagAktivitetStatus2)
            .doesNotHaveSameHashCodeAs(beregningsgrunnlagAktivitetStatus2);
        assertThat(beregningsgrunnlagAktivitetStatus2).isNotEqualTo(beregningsgrunnlagAktivitetStatus)
            .doesNotHaveSameHashCodeAs(beregningsgrunnlagAktivitetStatus);
    }

    @Test
    void skal_bruke_aktivitetstatus_i_equalsOgHashCode() {
	    var beregningsgrunnlagAktivitetStatus2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlagAktivitetStatus).isEqualTo(beregningsgrunnlagAktivitetStatus2)
            .hasSameHashCodeAs(beregningsgrunnlagAktivitetStatus2);

        beregningsgrunnlagAktivitetStatus2 = lagBuilderMedPaakrevdeFelter().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE).build(beregningsgrunnlag);

        assertThat(beregningsgrunnlagAktivitetStatus).isNotEqualTo(beregningsgrunnlagAktivitetStatus2)
            .doesNotHaveSameHashCodeAs(beregningsgrunnlagAktivitetStatus2);
    }

    @Test
    void skal_bruke_beregningsgrunnlag_i_equalsOgHashCode() {
	    var beregningsgrunnlagAktivitetStatus2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlagAktivitetStatus).isEqualTo(beregningsgrunnlagAktivitetStatus2)
            .hasSameHashCodeAs(beregningsgrunnlagAktivitetStatus2);

	    var beregningsgrunnlag2 = lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate.now().plusDays(1));
	    var builder = lagBuilderMedPaakrevdeFelter();
        beregningsgrunnlagAktivitetStatus2 = builder.build(beregningsgrunnlag2);

        assertThat(beregningsgrunnlagAktivitetStatus).isNotEqualTo(beregningsgrunnlagAktivitetStatus2)
            .doesNotHaveSameHashCodeAs(beregningsgrunnlagAktivitetStatus2);
    }

    private BeregningsgrunnlagAktivitetStatusDto lagMedPaakrevdeFelter() {
        return lagBuilderMedPaakrevdeFelter().build(beregningsgrunnlag);
    }

    private BeregningsgrunnlagAktivitetStatusDto.Builder lagBuilderMedPaakrevdeFelter() {
        return BeregningsgrunnlagAktivitetStatusDto.builder()
            .medHjemmel(BARE_ARBEIDSTAKER)
            .medAktivitetStatus(AKTIVITET_STATUS);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        return lagBeregningsgrunnlagMedSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(skjæringstidspunkt).build();
    }
}
