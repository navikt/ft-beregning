package no.nav.folketrygdloven.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class BevegeligeHelligdagerUtilTest {

    @Test
    void skal_gi_helligdagene_for_2017() {
        // 1.nyttårsdag, palmesømdag og 1.påskedag fjernes fordi de er søndager
        var skjærTorsdag = LocalDate.of(2017, 4, 13);
        var langFredag = LocalDate.of(2017, 4, 14);
        var andrePåskedag = LocalDate.of(2017, 4, 17);
        var førsteMai = LocalDate.of(2017, 5, 1);
        var syttendeMai = LocalDate.of(2017, 5, 17);
        var kristiHimmelfart = LocalDate.of(2017, 5, 25);
        var andrePinsedag = LocalDate.of(2017, 6, 5);
        var førsteJuledag = LocalDate.of(2017, 12, 25);
        var andreJuledag = LocalDate.of(2017, 12, 26);

        var helligdager2018 = BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(
                LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 1));

        assertThat(helligdager2018).hasSize(9);

        assertThat(helligdager2018.get(0)).isEqualTo(skjærTorsdag);
        assertThat(helligdager2018.get(1)).isEqualTo(langFredag);
        assertThat(helligdager2018.get(2)).isEqualTo(andrePåskedag);
        assertThat(helligdager2018.get(3)).isEqualTo(førsteMai);
        assertThat(helligdager2018.get(4)).isEqualTo(syttendeMai);
        assertThat(helligdager2018.get(5)).isEqualTo(kristiHimmelfart);
        assertThat(helligdager2018.get(6)).isEqualTo(andrePinsedag);
        assertThat(helligdager2018.get(7)).isEqualTo(førsteJuledag);
        assertThat(helligdager2018.get(8)).isEqualTo(andreJuledag);
    }

    @Test
    void skal_gi_helligdagene_for_2018() {
        // palmesømdag og 1.påskedag fjernes fordi de er søndager

        var førsteNyttårsdag = LocalDate.of(2018, 1, 1);
        var skjærTorsdag = LocalDate.of(2018, 3, 29);
        var langFredag = LocalDate.of(2018, 3, 30);
        var andrePåskedag = LocalDate.of(2018, 4, 2);
        var førsteMai = LocalDate.of(2018, 5, 1);
        var kristiHimmelfart = LocalDate.of(2018, 5, 10);
        var syttendeMai = LocalDate.of(2018, 5, 17);
        var andrePinsedag = LocalDate.of(2018, 5, 21);
        var førsteJuledag = LocalDate.of(2018, 12, 25);
        var andreJuledag = LocalDate.of(2018, 12, 26);

        var helligdager2018 = BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(
                LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1));

        assertThat(helligdager2018).hasSize(10);

        assertThat(helligdager2018.get(0)).isEqualTo(førsteNyttårsdag);
        assertThat(helligdager2018.get(1)).isEqualTo(skjærTorsdag);
        assertThat(helligdager2018.get(2)).isEqualTo(langFredag);
        assertThat(helligdager2018.get(3)).isEqualTo(andrePåskedag);
        assertThat(helligdager2018.get(4)).isEqualTo(førsteMai);
        assertThat(helligdager2018.get(5)).isEqualTo(kristiHimmelfart);
        assertThat(helligdager2018.get(6)).isEqualTo(syttendeMai);
        assertThat(helligdager2018.get(7)).isEqualTo(andrePinsedag);
        assertThat(helligdager2018.get(8)).isEqualTo(førsteJuledag);
        assertThat(helligdager2018.get(9)).isEqualTo(andreJuledag);
    }

    @Test
    void skal_gi_helligdagene_for_2019() {
        // palmesømdag og 1.påskedag fjernes fordi de er søndager
        var førsteNyttårsdag = LocalDate.of(2019, 1, 1);
        var skjærTorsdag = LocalDate.of(2019, 4, 18);
        var langFredag = LocalDate.of(2019, 4, 19);
        var andrePåskedag = LocalDate.of(2019, 4, 22);
        var førsteMai = LocalDate.of(2019, 5, 1);
        var syttendeMai = LocalDate.of(2019, 5, 17);
        var kristiHimmelfart = LocalDate.of(2019, 5, 30);
        var andrePinsedag = LocalDate.of(2019, 6, 10);
        var førsteJuledag = LocalDate.of(2019, 12, 25);
        var andreJuledag = LocalDate.of(2019, 12, 26);

        var helligdager2018 = BevegeligeHelligdagerUtil.finnBevegeligeHelligdagerUtenHelg(
                LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));

        assertThat(helligdager2018).hasSize(10);

        assertThat(helligdager2018.get(0)).isEqualTo(førsteNyttårsdag);
        assertThat(helligdager2018.get(1)).isEqualTo(skjærTorsdag);
        assertThat(helligdager2018.get(2)).isEqualTo(langFredag);
        assertThat(helligdager2018.get(3)).isEqualTo(andrePåskedag);
        assertThat(helligdager2018.get(4)).isEqualTo(førsteMai);
        assertThat(helligdager2018.get(5)).isEqualTo(syttendeMai);
        assertThat(helligdager2018.get(6)).isEqualTo(kristiHimmelfart);
        assertThat(helligdager2018.get(7)).isEqualTo(andrePinsedag);
        assertThat(helligdager2018.get(8)).isEqualTo(førsteJuledag);
        assertThat(helligdager2018.get(9)).isEqualTo(andreJuledag);
    }

    @Test
    void skal_gi_samme_virkedag_når_dato_er_virkedag() {
        var dato = LocalDate.of(2018, 9, 18);
        var nesteVirkedag = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(dato);
        assertThat(nesteVirkedag).isEqualTo(dato);
    }

    @Test
    void skal_gi_neste_virkedag_når_dato_er_lørdag() {
        var dato = LocalDate.of(2019, 2, 2);
        var nesteVirkedag = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(dato);
        assertThat(nesteVirkedag).isEqualTo(LocalDate.of(2019, 2, 4));
    }

    @Test
    void skal_gi_neste_virkedag_når_dato_er_søndag() {
        var dato = LocalDate.of(2019, 4, 28);
        var nesteVirkedag = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(dato);
        assertThat(nesteVirkedag).isEqualTo(LocalDate.of(2019, 4, 29));
    }

    @Test
    void skal_gi_neste_virkedag_når_dato_er_helligdag() {
        var dato = LocalDate.of(2019, 5, 17);
        var nesteVirkedag = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(dato);
        assertThat(nesteVirkedag).isEqualTo(LocalDate.of(2019, 5, 20));
    }

    @Test
    void skal_gi_at_det_er_helligdag() {
        var dato = LocalDate.of(2019, 4, 21);
        var resultat = BevegeligeHelligdagerUtil.erDatoHelligdagEllerHelg(dato);
        assertThat(resultat).isTrue();
    }

    @Test
    void skal_gi_at_det_er_helg() {
        var dato = LocalDate.of(2019, 3, 3);
        var resultat = BevegeligeHelligdagerUtil.erDatoHelligdagEllerHelg(dato);
        assertThat(resultat).isTrue();
    }

    @Test
    void skal_gi_at_det_ikke_er_helg_eller_helligdag() {
        var dato = LocalDate.of(2019, 5, 6);
        var resultat = BevegeligeHelligdagerUtil.erDatoHelligdagEllerHelg(dato);
        assertThat(resultat).isFalse();
    }



}
