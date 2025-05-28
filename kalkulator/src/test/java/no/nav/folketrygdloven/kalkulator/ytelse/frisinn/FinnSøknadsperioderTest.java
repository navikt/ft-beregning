package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FrisinnBehandlingType;

class FinnSøknadsperioderTest {

    @Test
    void finn_søknadsperioder_for_2_frisinnperiode_som_slutter_i_mars_og_april() {

        var tidligsteFom = LocalDate.of(2020, 3, 30);
        var sisteTom = LocalDate.of(2020, 4, 30);
        var mars = new FrisinnPeriode(Intervall.fraOgMedTilOgMed(tidligsteFom, LocalDate.of(2020, 3, 31)), true, false);
        var april = new FrisinnPeriode(Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), sisteTom), true, true);
        var frisinnGrunnlag = new FrisinnGrunnlag(List.of(), List.of(mars, april), FrisinnBehandlingType.NY_SØKNADSPERIODE);

        var søknadsperioder = FinnSøknadsperioder.finnSøknadsperioder(frisinnGrunnlag);

        assertThat(søknadsperioder).hasSize(1);
        assertThat(søknadsperioder.get(0).getFomDato()).isEqualTo(tidligsteFom);
        assertThat(søknadsperioder.get(0).getTomDato()).isEqualTo(sisteTom);
    }

    @Test
    void finn_søknadsperioder_for_2_frisinnperiode_som_starter_i_mars_og_slutter_i_april() {

        var tidligsteFom = LocalDate.of(2020, 3, 30);
        var sisteTom = LocalDate.of(2020, 4, 30);
        var mars = new FrisinnPeriode(Intervall.fraOgMedTilOgMed(tidligsteFom, LocalDate.of(2020, 4, 3)), true, false);
        var april = new FrisinnPeriode(Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 4), sisteTom), true, true);
        var frisinnGrunnlag = new FrisinnGrunnlag(List.of(), List.of(mars, april), FrisinnBehandlingType.NY_SØKNADSPERIODE);

        var søknadsperioder = FinnSøknadsperioder.finnSøknadsperioder(frisinnGrunnlag);

        assertThat(søknadsperioder).hasSize(1);
        assertThat(søknadsperioder.get(0).getFomDato()).isEqualTo(tidligsteFom);
        assertThat(søknadsperioder.get(0).getTomDato()).isEqualTo(sisteTom);
    }

    @Test
    void finn_søknadsperioder_for_2_frisinnperiode_april_og_mai() {


        var april = new FrisinnPeriode(Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 4), LocalDate.of(2020, 4, 30)), true, true);
        var mai = new FrisinnPeriode(Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 4), LocalDate.of(2020, 5, 31)), true, true);

        var frisinnGrunnlag = new FrisinnGrunnlag(List.of(), List.of(mai, april), FrisinnBehandlingType.NY_SØKNADSPERIODE);

        var søknadsperioder = FinnSøknadsperioder.finnSøknadsperioder(frisinnGrunnlag);

        assertThat(søknadsperioder).hasSize(2);
        assertThat(søknadsperioder.get(0).getFomDato()).isEqualTo(LocalDate.of(2020, 4, 4));
        assertThat(søknadsperioder.get(0).getTomDato()).isEqualTo(LocalDate.of(2020, 4, 30));
        assertThat(søknadsperioder.get(1).getFomDato()).isEqualTo(LocalDate.of(2020, 5, 4));
        assertThat(søknadsperioder.get(1).getTomDato()).isEqualTo(LocalDate.of(2020, 5, 31));
    }

}
