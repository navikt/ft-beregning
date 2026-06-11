package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpMock;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLUtbetalingsgradSvpTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 1);
    private static final String ORGNR = "999999999";
    private static final String ORGNR_2 = "888888888";
    private static final String ARBEIDSFORHOLD_ID = UUID.nameUUIDFromBytes("arbeidsforhold1".getBytes()).toString();
    private static final String ARBEIDSFORHOLD_ID_2 = UUID.nameUUIDFromBytes("arbeidsforhold2".getBytes()).toString();
    private static final Beløp GRUNNBELØP = GrunnbeløpMock.finnGrunnbeløp(SKJÆRINGSTIDSPUNKT);

    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLUtbetalingsgradSvp mapper =
        new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLUtbetalingsgradSvp();

    @Test
    void skal_kopiere_eksisterende_andeler_uten_nye_andeler() {
        var bg = lagBeregningsgrunnlag();
        var periode = lagPeriode(bg);
        lagArbeidstakerAndel(periode, ORGNR, null);

        var splittetPeriode = SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(Collections.emptyList())
            .medNyeAndeler(Collections.emptyList())
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build();

        var nyttBg = mapper.mapFraRegel(List.of(splittetPeriode), bg);

        assertThat(nyttBg.getBeregningsgrunnlagPerioder()).hasSize(1);
        var andeler = nyttBg.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.getFirst().getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    void skal_legge_til_ny_frilanser_andel() {
        var bg = lagBeregningsgrunnlag();
        var periode = lagPeriode(bg);
        lagArbeidstakerAndel(periode, ORGNR, null);

        var nyFLAndel = SplittetAndel.builder()
            .medAktivitetstatus(AktivitetStatusV2.FL)
            .build();

        var splittetPeriode = SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(Collections.emptyList())
            .medNyeAndeler(List.of(nyFLAndel))
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build();

        var nyttBg = mapper.mapFraRegel(List.of(splittetPeriode), bg);

        var andeler = nyttBg.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        var frilanserAndel = andeler.stream()
            .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .findFirst();
        assertThat(frilanserAndel).isPresent();
        assertThat(frilanserAndel.get().getKilde()).isEqualTo(AndelKilde.PROSESS_PERIODISERING);
        assertThat(frilanserAndel.get().getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.FRILANS);
    }

    @Test
    void skal_legge_til_ny_selvstendig_næringsdrivende_andel() {
        var bg = lagBeregningsgrunnlag();
        var periode = lagPeriode(bg);
        lagArbeidstakerAndel(periode, ORGNR, null);

        var nySNAndel = SplittetAndel.builder()
            .medAktivitetstatus(AktivitetStatusV2.SN)
            .build();

        var splittetPeriode = SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(Collections.emptyList())
            .medNyeAndeler(List.of(nySNAndel))
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build();

        var nyttBg = mapper.mapFraRegel(List.of(splittetPeriode), bg);

        var andeler = nyttBg.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        var snAndel = andeler.stream()
            .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
            .findFirst();
        assertThat(snAndel).isPresent();
        assertThat(snAndel.get().getKilde()).isEqualTo(AndelKilde.PROSESS_PERIODISERING);
        assertThat(snAndel.get().getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.NÆRING);
    }

    @Test
    void skal_ikke_legge_til_duplikat_frilanser_andel_om_den_allerede_finnes() {
        var bg = lagBeregningsgrunnlag();
        var periode = lagPeriode(bg);
        lagArbeidstakerAndel(periode, ORGNR, null);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.FRILANSER)
            .medArbforholdType(OpptjeningAktivitetType.FRILANS)
            .build(periode);

        var nyFLAndel = SplittetAndel.builder()
            .medAktivitetstatus(AktivitetStatusV2.FL)
            .build();

        var splittetPeriode = SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(Collections.emptyList())
            .medNyeAndeler(List.of(nyFLAndel))
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build();

        var nyttBg = mapper.mapFraRegel(List.of(splittetPeriode), bg);

        var andeler = nyttBg.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        var frilanserAndeler = andeler.stream()
            .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .toList();
        assertThat(frilanserAndeler).hasSize(1);
    }

    @Test
    void skal_legge_til_ny_arbeidstaker_andel_med_arbeidsforhold() {
        var bg = lagBeregningsgrunnlag();
        var periode = lagPeriode(bg);
        lagArbeidstakerAndel(periode, ORGNR, null);

        var nyATAndel = SplittetAndel.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR_2, ARBEIDSFORHOLD_ID))
            .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1))
            .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT.plusYears(1))
            .build();

        var splittetPeriode = SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(Collections.emptyList())
            .medNyeAndeler(List.of(nyATAndel))
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build();

        var nyttBg = mapper.mapFraRegel(List.of(splittetPeriode), bg);

        var andeler = nyttBg.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(2);
        var nyAndel = andeler.stream()
            .filter(a -> a.getBgAndelArbeidsforhold()
                .map(BGAndelArbeidsforholdDto::getArbeidsgiver)
                .filter(ag -> ag.getOrgnr().equals(ORGNR_2))
                .isPresent())
            .findFirst();
        assertThat(nyAndel).isPresent();
        assertThat(nyAndel.get().getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(nyAndel.get().getKilde()).isEqualTo(AndelKilde.PROSESS_PERIODISERING);
    }

    @Test
    void skal_splitte_generell_andel_til_ny_andel_med_arbeidsforholdId() {
        var bg = lagBeregningsgrunnlag();
        var periode = lagPeriode(bg);
        var eksisterendeAndel = lagArbeidstakerAndel(periode, ORGNR, null);

        var nyAndel = SplittetAndel.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR, ARBEIDSFORHOLD_ID_2))
            .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT.minusYears(1))
            .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT.plusYears(1))
            .build();

        var splittetPeriode = SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(List.of(
                EksisterendeAndel.builder()
                    .medAndelNr(eksisterendeAndel.getAndelsnr())
                    .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR))
                    .build()))
            .medNyeAndeler(List.of(nyAndel))
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build();

        var nyttBg = mapper.mapFraRegel(List.of(splittetPeriode), bg);

        var andeler = nyttBg.getBeregningsgrunnlagPerioder().getFirst().getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        var mappedAndel = andeler.getFirst();
        assertThat(mappedAndel.getBgAndelArbeidsforhold()).isPresent();
        assertThat(mappedAndel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().getReferanse()).isEqualTo(ARBEIDSFORHOLD_ID_2);
    }

    @Test
    void skal_kaste_exception_ved_ukjent_aktivitetstatus_mapping() {
        var bg = lagBeregningsgrunnlag();
        var periode = lagPeriode(bg);
        lagArbeidstakerAndel(periode, ORGNR, null);

        var nyAndelMedUkjentStatus = SplittetAndel.builder()
            .medAktivitetstatus(AktivitetStatusV2.AT)
            .build();

        var splittetPeriode = SplittetPeriode.builder()
            .medPeriodeÅrsaker(Collections.emptyList())
            .medFørstePeriodeAndeler(Collections.emptyList())
            .medNyeAndeler(List.of(nyAndelMedUkjentStatus))
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .build();

        assertThatThrownBy(() -> mapper.mapFraRegel(List.of(splittetPeriode), bg))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Klarte ikke mappe nye andeler");
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        var bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(bg);
        return bg;
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto bg) {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagArbeidstakerAndel(BeregningsgrunnlagPeriodeDto periode, String orgnr, String arbeidsforholdId) {
        var arbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr));
        if (arbeidsforholdId != null) {
            arbeidsforholdBuilder.medArbeidsforholdRef(
                no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto.ref(arbeidsforholdId));
        }
        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID)
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBeregnetPrÅr(Beløp.fra(500000))
            .build(periode);
    }
}
