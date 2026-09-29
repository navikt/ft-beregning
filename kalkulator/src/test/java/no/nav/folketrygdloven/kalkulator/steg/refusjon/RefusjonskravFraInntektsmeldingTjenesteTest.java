package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class RefusjonskravFraInntektsmeldingTjenesteTest {

    private static final LocalDate STP = LocalDate.of(2026, 5, 5);
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("999999999");
    private static final Beløp KRAV = Beløp.fra(58483);
    // Perioden der utbetalt refusjon "gjenoppstår" etter et utbetalingsgrad-hull (f.eks. ferie)
    private static final Intervall GJENOPPTAKELSE = Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 7, 27), LocalDate.of(2026, 8, 5));

    @Test
    void uendret_loepende_krav_over_ferie_hull_skal_ikke_regnes_som_oekt_krav() {
        var kravPrArbeidsgiver = List.of(kravperioder(
                periodeForKrav(LocalDate.of(2026, 5, 13), refusjon(STP, LocalDate.of(9999, 12, 31), KRAV))));

        var resultat = RefusjonskravFraInntektsmeldingTjeneste.refusjonskravHarØktInnIPeriode(kravPrArbeidsgiver, ARBEIDSGIVER, GJENOPPTAKELSE, STP);

        assertThat(resultat).isFalse();
    }

    @Test
    void ny_inntektsmelding_med_hoeyere_beloep_i_perioden_skal_regnes_som_oekt_krav() {
        var kravPrArbeidsgiver = List.of(kravperioder(
                periodeForKrav(LocalDate.of(2026, 5, 13), refusjon(STP, LocalDate.of(9999, 12, 31), KRAV)),
                periodeForKrav(LocalDate.of(2026, 8, 1), refusjon(LocalDate.of(2026, 7, 27), LocalDate.of(9999, 12, 31), Beløp.fra(70000)))));

        var resultat = RefusjonskravFraInntektsmeldingTjeneste.refusjonskravHarØktInnIPeriode(kravPrArbeidsgiver, ARBEIDSGIVER, GJENOPPTAKELSE, STP);

        assertThat(resultat).isTrue();
    }

    @Test
    void ny_inntektsmelding_som_hever_beloep_uniformt_fra_stp_skal_regnes_som_oekt_krav() {
        var kravPrArbeidsgiver = List.of(kravperioder(
                periodeForKrav(LocalDate.of(2026, 5, 13), refusjon(STP, LocalDate.of(9999, 12, 31), KRAV)),
                periodeForKrav(LocalDate.of(2026, 8, 1), refusjon(STP, LocalDate.of(9999, 12, 31), Beløp.fra(70000)))));

        var resultat = RefusjonskravFraInntektsmeldingTjeneste.refusjonskravHarØktInnIPeriode(kravPrArbeidsgiver, ARBEIDSGIVER, GJENOPPTAKELSE, STP);

        assertThat(resultat).isTrue();
    }

    @Test
    void tilkommet_krav_uten_tidligere_krav_skal_regnes_som_oekt_krav() {
        var kravPrArbeidsgiver = List.of(kravperioder(
                periodeForKrav(LocalDate.of(2026, 8, 1), refusjon(LocalDate.of(2026, 7, 27), LocalDate.of(9999, 12, 31), Beløp.fra(40000)))));

        var resultat = RefusjonskravFraInntektsmeldingTjeneste.refusjonskravHarØktInnIPeriode(kravPrArbeidsgiver, ARBEIDSGIVER, GJENOPPTAKELSE, STP);

        assertThat(resultat).isTrue();
    }

    @Test
    void manglende_kravdata_skal_beholde_avklaringsbehov_konservativt() {
        assertThat(RefusjonskravFraInntektsmeldingTjeneste.refusjonskravHarØktInnIPeriode(List.of(), ARBEIDSGIVER, GJENOPPTAKELSE, STP)).isTrue();
        assertThat(RefusjonskravFraInntektsmeldingTjeneste.refusjonskravHarØktInnIPeriode(null, ARBEIDSGIVER, GJENOPPTAKELSE, STP)).isTrue();
        assertThat(RefusjonskravFraInntektsmeldingTjeneste.refusjonskravHarØktInnIPeriode(List.of(), null, GJENOPPTAKELSE, STP)).isTrue();
    }

    private static KravperioderPrArbeidsforholdDto kravperioder(PerioderForKravDto... perioder) {
        return new KravperioderPrArbeidsforholdDto(ARBEIDSGIVER, List.of(perioder), List.of());
    }

    private static PerioderForKravDto periodeForKrav(LocalDate innsendingsdato, RefusjonsperiodeDto... refusjonsperioder) {
        return new PerioderForKravDto(innsendingsdato, List.of(refusjonsperioder));
    }

    private static RefusjonsperiodeDto refusjon(LocalDate fom, LocalDate tom, Beløp beløp) {
        return new RefusjonsperiodeDto(Intervall.fraOgMedTilOgMed(fom, tom), beløp);
    }
}
