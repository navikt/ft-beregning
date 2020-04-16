package no.nav.folketrygdloven.beregningsgrunnlag.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HeleMånederUtilTest {

    @Test
    public void skal_teste_en_mnd_varighet() {
        LocalDate fom = LocalDate.of(2020, 1, 1);
        LocalDate tom = LocalDate.of(2020, 2, 1);
        int heleMnd = HeleMånederUtil.heleMånederMellom(fom, tom);
        assertThat(heleMnd).isEqualTo(1);
    }

    @Test
    public void skal_teste_tolv_mnd_varighet() {
        LocalDate fom = LocalDate.of(2020, 1, 1);
        LocalDate tom = LocalDate.of(2021, 1, 1);
        int heleMnd = HeleMånederUtil.heleMånederMellom(fom, tom);
        assertThat(heleMnd).isEqualTo(12);
    }

    @Test
    public void skal_teste_perioder_midt_i_måned() {
        LocalDate fom = LocalDate.of(2020, 1, 15);
        LocalDate tom = LocalDate.of(2020, 2, 29);
        int heleMnd = HeleMånederUtil.heleMånederMellom(fom, tom);
        assertThat(heleMnd).isEqualTo(0);
    }

    @Test
    public void skal_teste_kryssing_av_år() {
        LocalDate fom = LocalDate.of(2020, 5, 15);
        LocalDate tom = LocalDate.of(2021, 2, 12);
        int heleMnd = HeleMånederUtil.heleMånederMellom(fom, tom);
        assertThat(heleMnd).isEqualTo(8);
    }

    @Test
    public void skal_oppgi_datoer_i_feil_rekkefølge() {
        LocalDate fom = LocalDate.of(2021, 2, 12);
        LocalDate tom = LocalDate.of(2020, 5, 15);
        int heleMnd = HeleMånederUtil.heleMånederMellom(fom, tom);
        assertThat(heleMnd).isEqualTo(8);
    }

    @Test
    public void skal_teste_like_datoer() {
        LocalDate fom = LocalDate.of(2020, 5, 15);
        LocalDate tom = LocalDate.of(2020, 5, 15);
        int heleMnd = HeleMånederUtil.heleMånederMellom(fom, tom);
        assertThat(heleMnd).isEqualTo(0);
    }



}
