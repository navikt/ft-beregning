package no.nav.folketrygdloven.besteberegning;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelseAktivitetType;
import no.nav.folketrygdloven.besteberegning.modell.input.Ytelsegrunnlag;

import no.nav.folketrygdloven.besteberegning.modell.input.YtelsegrunnlagAndel;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelsegrunnlagPeriode;

import no.nav.folketrygdloven.besteberegning.modell.output.AktivitetNøkkel;

import no.nav.folketrygdloven.besteberegning.modell.output.Inntekt;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Java6Assertions.assertThat;

class FastsettYtelseFordelingTest {
	private List<Ytelsegrunnlag> grunnlag = new ArrayList<>();

	@Test
	public void skal_fordele_sykepenger_ved_ett_vedtak_perfekt_overlapp() {
		// Arrange
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 1), RelatertYtelseType.SYKEPENGER, BigDecimal.valueOf(10000));
		lagSykepengeperiode(LocalDate.of(2021,1,1), LocalDate.of(2021,1,31), YtelseAktivitetType.YTELSE_FOR_DAGPENGER);

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(1);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_DAGPENGER), 10000);
	}

	@Test
	public void skal_fordele_sykepenger_ved_ett_vedtak_delvis_overlapp() {
		// Arrange
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.SYKEPENGER, BigDecimal.valueOf(10000));
		lagSykepengeperiode(LocalDate.of(2021,2,20), LocalDate.of(2021,3,12), YtelseAktivitetType.YTELSE_FOR_DAGPENGER);

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(1);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_DAGPENGER), 10000);
	}

	@Test
	public void skal_fordele_sykepenger_ved_ett_vedtak_gjeldende_måned_før_utbetaling() {
		// Arrange
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.SYKEPENGER, BigDecimal.valueOf(10000));
		lagSykepengeperiode(LocalDate.of(2021,2,1), LocalDate.of(2021,2,28), YtelseAktivitetType.YTELSE_FOR_DAGPENGER);

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(1);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_DAGPENGER), 10000);
	}

	@Test
	public void skal_fordele_sykepenger_ved_kun_en_dag_utvidet_overlapp() {
		// Arrange
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.SYKEPENGER, BigDecimal.valueOf(10000));
		lagSykepengeperiode(LocalDate.of(2021,1,1), LocalDate.of(2021,2,1), YtelseAktivitetType.YTELSE_FOR_ARBEID);

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(1);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_ARBEID), 10000);
	}

	@Test
	public void skal_fordele_sykepenger_ved_flere_vedtak_likt_grunnlag() {
		// Arrange
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.SYKEPENGER, BigDecimal.valueOf(10000));
		lagSykepengeperiode(LocalDate.of(2021,3,1), LocalDate.of(2021,3,10), YtelseAktivitetType.YTELSE_FOR_ARBEID);
		lagSykepengeperiode(LocalDate.of(2021,3,21), LocalDate.of(2021,3,30), YtelseAktivitetType.YTELSE_FOR_DAGPENGER);

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(2);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_ARBEID), 5000);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_DAGPENGER), 5000);
	}

	@Test
	public void skal_fordele_sykepenger_ved_flere_vedtak_ulikt_grunnlag() {
		// Arrange
		// 21 dager totalt, 1/3 på arbeid of 2/3 på dagpenger
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.SYKEPENGER, BigDecimal.valueOf(15000));
		lagSykepengeperiode(LocalDate.of(2021,3,1), LocalDate.of(2021,3,7), YtelseAktivitetType.YTELSE_FOR_ARBEID);
		lagSykepengeperiode(LocalDate.of(2021,3,18), LocalDate.of(2021,3,31), YtelseAktivitetType.YTELSE_FOR_DAGPENGER);

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(2);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_ARBEID), 5000);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_DAGPENGER), 10000);
	}

	@Test
	public void skal_fordele_foreldrepenger_et_grunnlag_en_andel() {
		// Arrange
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.FORELDREPENGER, BigDecimal.valueOf(15000));
		lagForeldrepengeperiode(LocalDate.of(2021,3,1), LocalDate.of(2021,3,31), lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_ARBEID, 500));

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(1);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.FORELDREPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_ARBEID), 15000);
	}

	@Test
	public void skal_fordele_foreldrepenger_et_grunnlag_flere_like_andeler() {
		// Arrange
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.FORELDREPENGER, BigDecimal.valueOf(20000));
		lagForeldrepengeperiode(LocalDate.of(2021,3,1), LocalDate.of(2021,3,31), lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_ARBEID, 500));
		lagForeldrepengeperiode(LocalDate.of(2021,3,1), LocalDate.of(2021,3,31), lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_DAGPENGER, 500));

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(2);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.FORELDREPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_ARBEID), 10000);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.FORELDREPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_DAGPENGER), 10000);
	}

	@Test
	public void skal_fordele_foreldrepenger_flere_perioder_flere_ulike_andeler() {
		// Arrange
		// AT 11 * 500 = 5500
		// DP 11 * 500 = 5500
		// DP 12 * 1000 = 12000
		// Sum: 23000
		// AT sum: 20000 * (5500 / 23000) = 4782.60
		// DP sum: 20000 * (17500 / 23000) = 15217.39
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.FORELDREPENGER, BigDecimal.valueOf(20000));
		lagForeldrepengeperiode(LocalDate.of(2021,3,1), LocalDate.of(2021,3,15), lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_ARBEID, 500),
				lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_DAGPENGER, 500));
		lagForeldrepengeperiode(LocalDate.of(2021,3,16), LocalDate.of(2021,3,31), lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_DAGPENGER, 1000));

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(2);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.FORELDREPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_ARBEID), 4783);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.FORELDREPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_DAGPENGER), 15217);
	}

	@Test
	public void skal_fordele_foreldrepenger_starter_måned_før_inntekt() {
		// Arrange
		// AT 11 * 350 + 9 * 500 = 8350
		// DP 11 * 400 + 9 * 700 = 10700
		// Sum: 19050
		// AT sum: 25000 * (8350 / 19050) = 10958.00
		// DP sum: 25000 * (10700 / 19050) = 14041.99
		Periodeinntekt inntekt = lagPeriodeinntekt(YearMonth.of(2021, 3), RelatertYtelseType.FORELDREPENGER, BigDecimal.valueOf(25000));
		lagForeldrepengeperiode(LocalDate.of(2021,2,1), LocalDate.of(2021,2,15), lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_ARBEID, 350),
				lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_DAGPENGER, 400));
		lagForeldrepengeperiode(LocalDate.of(2021,2,16), LocalDate.of(2021,2,28), lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_ARBEID, 500),
				lagYtelseAndel(YtelseAktivitetType.YTELSE_FOR_DAGPENGER, 700));

		// Act
		List<Inntekt> fordeltInntekt = fordelInntekt(inntekt);

		// Assert
		assertThat(fordeltInntekt).hasSize(2);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.FORELDREPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_ARBEID), 10958);
		assertInntekt(fordeltInntekt, AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.FORELDREPENGER_MOTTAKER, YtelseAktivitetType.YTELSE_FOR_DAGPENGER), 14042);
	}

	private void assertInntekt(List<Inntekt> fordeltInntekt, AktivitetNøkkel forventetNøkkel, int forventetInntekt) {
		List<Inntekt> alleMatchendeInntekter = fordeltInntekt.stream().filter(inntekt -> inntekt.getAktivitetNøkkel().equals(forventetNøkkel)).collect(Collectors.toList());
		assertThat(alleMatchendeInntekter).hasSize(1);
		assertThat(alleMatchendeInntekter.get(0).getInntektPrMåned().setScale(0, RoundingMode.HALF_UP).intValue()).isEqualTo(forventetInntekt);
	}

	private List<Inntekt> fordelInntekt(Periodeinntekt inntekt) {
		return FastsettYtelseFordeling.fordelYtelse(grunnlag, inntekt);
	}

	private void lagSykepengeperiode(LocalDate fom, LocalDate tom, YtelseAktivitetType ytelsegrunnlag) {
		Optional<Ytelsegrunnlag> eksisterendeYG = grunnlag.stream().filter(yg -> yg.getYtelse().equals(RelatertYtelseType.SYKEPENGER)).findFirst();
		if (eksisterendeYG.isPresent()) {
			YtelsegrunnlagPeriode nyPeriode = new YtelsegrunnlagPeriode(Periode.of(fom, tom), new ArrayList<>(Collections.singletonList(new YtelsegrunnlagAndel(ytelsegrunnlag))));
			eksisterendeYG.get().getPerioder().add(nyPeriode);
		} else {
			YtelsegrunnlagPeriode nyPeriode = new YtelsegrunnlagPeriode(Periode.of(fom, tom), new ArrayList<>(Collections.singletonList(new YtelsegrunnlagAndel(ytelsegrunnlag))));
			grunnlag.add(new Ytelsegrunnlag(RelatertYtelseType.SYKEPENGER, new ArrayList<>(Collections.singletonList(nyPeriode))));
		}
	}

	private void lagForeldrepengeperiode(LocalDate fom, LocalDate tom, YtelsegrunnlagAndel... andeler) {
		Optional<Ytelsegrunnlag> eksisterendeYG = grunnlag.stream().filter(yg -> yg.getYtelse().equals(RelatertYtelseType.FORELDREPENGER)).findFirst();
		if (eksisterendeYG.isPresent()) {
			YtelsegrunnlagPeriode nyPeriode = new YtelsegrunnlagPeriode(Periode.of(fom, tom), Arrays.asList(andeler));
			eksisterendeYG.get().getPerioder().add(nyPeriode);
		} else {
			YtelsegrunnlagPeriode nyPeriode = new YtelsegrunnlagPeriode(Periode.of(fom, tom), Arrays.asList(andeler));
			grunnlag.add(new Ytelsegrunnlag(RelatertYtelseType.FORELDREPENGER, new ArrayList<>(Collections.singletonList(nyPeriode))));
		}
	}

	private YtelsegrunnlagAndel lagYtelseAndel(YtelseAktivitetType ytelseAktivitetType, int dagsats) {
		return new YtelsegrunnlagAndel(ytelseAktivitetType, BigDecimal.valueOf(dagsats));
	}

	private Periodeinntekt lagPeriodeinntekt(YearMonth måned, RelatertYtelseType ytelseType, BigDecimal inntekt) {
		return Periodeinntekt.builder()
				.medYtelse(ytelseType)
				.medPeriode(Periode.of(måned.atDay(1), måned.atEndOfMonth()))
				.medInntekt(inntekt)
				.medInntektskildeOgPeriodeType(Inntektskilde.ANNEN_YTELSE)
				.medAktivitetStatus(AktivitetStatus.KUN_YTELSE)
				.build();
	}

}