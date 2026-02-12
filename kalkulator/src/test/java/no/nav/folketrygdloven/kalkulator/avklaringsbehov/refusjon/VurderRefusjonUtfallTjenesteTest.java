package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class VurderRefusjonUtfallTjenesteTest {

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto bgPeriode;
    private static Refusjon godkjentRefusjon;
    private static Refusjon underkjentRefusjon;

    private static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet("974760673");
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet("915933149");
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    @BeforeEach
    void setUp() {
        var saksbehandletRefusjon = Beløp.fra(30000);
        var refusjon = Beløp.fra(33333);
        godkjentRefusjon = new Refusjon(refusjon, saksbehandletRefusjon, refusjon, refusjon, Hjemmel.F_14_7_8_28_8_30, Utfall.GODKJENT);
        underkjentRefusjon = new Refusjon(refusjon, saksbehandletRefusjon, refusjon, refusjon, Hjemmel.F_14_7_8_28_8_30, Utfall.UNDERKJENT);
        beregningsgrunnlag = lagBeregningsgrunnlag();
        bgPeriode = lagBeregningsgrunnlagPeriode(beregningsgrunnlag);
    }

    @Test
    void beregningsgrunnlaget_skal_endres_slik_at_refusjonskrav_periode_får_godkjent_kravutfall_hvis_frist_er_utvidet() {
        lagArbeidsgiverAndelerPåPeriode(bgPeriode, ARBEIDSGIVER1, underkjentRefusjon, 1);
        lagArbeidsgiverAndelerPåPeriode(bgPeriode, ARBEIDSGIVER2, underkjentRefusjon, 2);
        var refusjonAndeler = List.of(lagVurderRefusjonAndel(ARBEIDSGIVER1, Boolean.TRUE), lagVurderRefusjonAndel(ARBEIDSGIVER2, Boolean.FALSE));

        var justertBeregningsgrunnlag = VurderRefusjonUtfallTjeneste.justerBeregningsgrunnlagForVurdertRefusjonsfrist(beregningsgrunnlag, refusjonAndeler);
        var refusjonAg1 = getRefusjonForAg(justertBeregningsgrunnlag, ARBEIDSGIVER1);
        var refusjonAg2 = getRefusjonForAg(justertBeregningsgrunnlag, ARBEIDSGIVER2);

        assertThat(refusjonAg1).hasSize(1);
        assertThat(refusjonAg1.getFirst().getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
        assertThat(refusjonAg2).hasSize(1);
        assertThat(refusjonAg2.getFirst().getRefusjonskravFristUtfall()).isEqualTo(Utfall.UNDERKJENT);
    }

    @Test
    void skal_ikke_endre_allerede_godkjent_refusjon() {
        lagArbeidsgiverAndelerPåPeriode(bgPeriode, ARBEIDSGIVER1, godkjentRefusjon, 1);
        var refusjonAndeler = List.of(lagVurderRefusjonAndel(ARBEIDSGIVER1, Boolean.TRUE));

        var justertBeregningsgrunnlag = VurderRefusjonUtfallTjeneste.justerBeregningsgrunnlagForVurdertRefusjonsfrist(beregningsgrunnlag, refusjonAndeler);
        var refusjonAg = getRefusjonForAg(justertBeregningsgrunnlag, ARBEIDSGIVER1);

        assertThat(refusjonAg).hasSize(1);
        assertThat(refusjonAg.getFirst().getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
    }

    @Test
    void skal_ikke_endre_refusjon_der_erFristUtvidet_er_null() {
        lagArbeidsgiverAndelerPåPeriode(bgPeriode, ARBEIDSGIVER1, godkjentRefusjon, 1);
        var refusjonAndeler = List.of(lagVurderRefusjonAndel(ARBEIDSGIVER1, null));

        var justertBeregningsgrunnlag = VurderRefusjonUtfallTjeneste.justerBeregningsgrunnlagForVurdertRefusjonsfrist(beregningsgrunnlag, refusjonAndeler);
        var refusjonAg = getRefusjonForAg(justertBeregningsgrunnlag, ARBEIDSGIVER1);

        assertThat(refusjonAg).hasSize(1);
        assertThat(refusjonAg.getFirst().getRefusjonskravFristUtfall()).isEqualTo(Utfall.GODKJENT);
    }

    @Test
    void skal_kaste_exception_ved_ingen_underkjente_perioder_når_erFristUtvidet_endres_fra_true_til_false() {
        lagArbeidsgiverAndelerPåPeriode(bgPeriode, ARBEIDSGIVER1, godkjentRefusjon, 1);
        var refusjonAndeler = List.of(lagVurderRefusjonAndel(ARBEIDSGIVER1, Boolean.FALSE));

        var feilmelding = assertThrows(IllegalStateException.class,
            () -> VurderRefusjonUtfallTjeneste.justerBeregningsgrunnlagForVurdertRefusjonsfrist(beregningsgrunnlag, refusjonAndeler));
        assertThat(feilmelding.getMessage()).contains("VurderRefusjonUtfallTjeneste: Forventet å finne arbeidsforhold med underkjent refusjon for arbeidsgiver:");
    }

    @Test
    void skal_kaste_exception_hvis_ingen_matchende_arbeidsforhold() {
        lagArbeidsgiverAndelerPåPeriode(bgPeriode, ARBEIDSGIVER1, underkjentRefusjon, 1);

        List<VurderRefusjonAndelBeregningsgrunnlagDto> refusjonAndeler = new ArrayList<>();
        refusjonAndeler.add(lagVurderRefusjonAndel(ARBEIDSGIVER2, Boolean.TRUE));
        var feilmelding = assertThrows(IllegalStateException.class,
            () -> VurderRefusjonUtfallTjeneste.justerBeregningsgrunnlagForVurdertRefusjonsfrist(beregningsgrunnlag, refusjonAndeler));
        assertThat(feilmelding.getMessage()).contains("VurderRefusjonUtfallTjeneste: Fant ingen andel med arbeidsgiver:");
    }

    private static List<Refusjon> getRefusjonForAg(BeregningsgrunnlagDto justertBeregningsgrunnlag, Arbeidsgiver arbeidsgiver) {
        return justertBeregningsgrunnlag.getBeregningsgrunnlagPerioder()
            .stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .flatMap(Optional::stream)
            .filter(arbeidsforhold -> Objects.equals(arbeidsforhold.getArbeidsforholdOrgnr(), arbeidsgiver.getOrgnr()))
            .flatMap(arbeidsforhold -> arbeidsforhold.getRefusjon().stream())
            .toList();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        return BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .build();
    }

    private BeregningsgrunnlagPeriodeDto lagBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(5)).build(beregningsgrunnlag);
    }

    private void lagArbeidsgiverAndelerPåPeriode(BeregningsgrunnlagPeriodeDto periode, Arbeidsgiver arbeidsgiver, Refusjon refusjon, int andelsnr) {
        var bga = lagBGAndelArbeidsforhold(arbeidsgiver, refusjon);
        lagBGPrStatusOgAndel(bga, periode, andelsnr, periode.getPeriode().getFomDato(), periode.getPeriode().getTomDato());
        BeregningsgrunnlagPeriodeDto.oppdater(periode).build();
    }

    private BGAndelArbeidsforholdDto.Builder lagBGAndelArbeidsforhold(Arbeidsgiver arbeidsgiver, Refusjon refusjon) {
        return BGAndelArbeidsforholdDto.builder()
            .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1))
            .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT.plusYears(2))
            .medRefusjon(refusjon)
            .medArbeidsgiver(arbeidsgiver);
    }

    private void lagBGPrStatusOgAndel(BGAndelArbeidsforholdDto.Builder bga,
                                      BeregningsgrunnlagPeriodeDto periode,
                                      int andelsnr,
                                      LocalDate fraDato,
                                      LocalDate tilDato) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAndelsnr((long) andelsnr)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(fraDato, tilDato)
            .build(periode);
    }

    private VurderRefusjonAndelBeregningsgrunnlagDto lagVurderRefusjonAndel(Arbeidsgiver ag, Boolean erFristUtvidet) {
        return new VurderRefusjonAndelBeregningsgrunnlagDto(ag.getOrgnr(), null, null, LocalDate.now(), null, erFristUtvidet);
    }
}
