package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

class TilkommetInntektsforholdTjenesteTest {


	public static final String ARBEIDSGIVER_ORGNR = "123456789";
	public static final String ARBEIDSGIVER_ORGNR2 = "123456719";

	public static final LocalDate STP = LocalDate.of(2024, 11, 20);

	@Test
	void skal_slå_sammen_perioder_med_tilkommet_over_helg() {

		var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
		var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
		var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()),
				lagAktivitetsgrader(STP, STP.plusDays(20), 0));

		var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
		var tilkommetDato = STP.plusDays(10);
		var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(
				lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()),
				lagAktivitetsgrader(tilkommetDato, STP.plusDays(20), 50));

		var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(9), InternArbeidsforholdRefDto.nullRef());
		var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, tilkommetDato, STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());


		var utbetalingsgradGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(
				utbetalingsgradFraStart,
				utbetalingsgradNyAndel));

		var iay = lagIAY(List.of(yrkesaktivitet, nyYrkesaktivitet));
		var tidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
				STP,
				List.of(arbeidstakerandelFraStart),
				utbetalingsgradGrunnlag, iay);

		var segmenter = tidslinje.toSegments();

		assertThat(segmenter.size()).isEqualTo(2);
		var iterator = segmenter.iterator();
		var førsteSegment = iterator.next();
		assertThat(førsteSegment.getValue().isEmpty()).isTrue();
		assertThat(førsteSegment.getFom()).isEqualTo(STP);
		assertThat(førsteSegment.getTom()).isEqualTo(tilkommetDato.minusDays(1));

		var andreSegment = iterator.next();
		assertThat(andreSegment.getValue().size()).isEqualTo(1);
		assertThat(andreSegment.getFom()).isEqualTo(tilkommetDato);
		assertThat(andreSegment.getTom()).isEqualTo(STP.plusDays(20));

	}


	@Test
	void skal_slå_sammen_perioder_over_helg_dersom_ny_aktivitet_har_fullt_fravær() {

		var arbeidsgiver = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR);
		var arbeidstakerandelFraStart = lagArbeidstakerandel(arbeidsgiver, 1L, AndelKilde.PROSESS_START, InternArbeidsforholdRefDto.nullRef());
		var utbetalingsgradFraStart = new UtbetalingsgradPrAktivitetDto(lagAktivitet(arbeidsgiver, InternArbeidsforholdRefDto.nullRef()),
				lagAktivitetsgrader(STP, STP.plusDays(20), 0));

		var arbeidsgiver2 = Arbeidsgiver.virksomhet(ARBEIDSGIVER_ORGNR2);
		var tilkommetDato = STP.plusDays(10);
		var utbetalingsgradNyAndel = new UtbetalingsgradPrAktivitetDto(
				lagAktivitet(arbeidsgiver2, InternArbeidsforholdRefDto.nullRef()),
				lagAktivitetsgrader(tilkommetDato, STP.plusDays(20), 0));

		var yrkesaktivitet = lagYrkesaktivitet(arbeidsgiver, STP.minusMonths(10), STP.plusDays(9), InternArbeidsforholdRefDto.nullRef());
		var nyYrkesaktivitet = lagYrkesaktivitet(arbeidsgiver2, tilkommetDato, STP.plusDays(20), InternArbeidsforholdRefDto.nullRef());


		var utbetalingsgradGrunnlag = new PleiepengerSyktBarnGrunnlag(List.of(
				utbetalingsgradFraStart,
				utbetalingsgradNyAndel));

		var iay = lagIAY(List.of(yrkesaktivitet, nyYrkesaktivitet));
		var tidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
				STP,
				List.of(arbeidstakerandelFraStart),
				utbetalingsgradGrunnlag, iay);

		var segmenter = tidslinje.toSegments();

		assertThat(segmenter.size()).isEqualTo(1);
		var iterator = segmenter.iterator();
		var førsteSegment = iterator.next();
		assertThat(førsteSegment.getValue().isEmpty()).isTrue();
		assertThat(førsteSegment.getFom()).isEqualTo(STP);
		assertThat(førsteSegment.getTom()).isEqualTo(STP.plusDays(20));
	}


	private static InntektArbeidYtelseGrunnlagDto lagIAY(List<YrkesaktivitetDto> yrkesaktiviteter) {
		var oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
		var aktørArbeid = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
		yrkesaktiviteter.forEach(aktørArbeid::leggTilYrkesaktivitet);
		oppdatere.leggTilAktørArbeid(aktørArbeid);
		var iay = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medData(oppdatere).build();
		return iay;
	}

	private BeregningsgrunnlagPrStatusOgAndelDto lagArbeidstakerandel(Arbeidsgiver arbeidsgiver2, long andelsnr, AndelKilde kilde, InternArbeidsforholdRefDto arbeidsforholdRef) {
		return BeregningsgrunnlagPrStatusOgAndelDto.ny()
				.medAndelsnr(andelsnr)
				.medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver2).medArbeidsforholdRef(arbeidsforholdRef))
				.medKilde(kilde)
				.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
				.build();
	}

	private YrkesaktivitetDto lagYrkesaktivitet(Arbeidsgiver arbeidsgiver2, LocalDate fom, LocalDate tom, InternArbeidsforholdRefDto arbeidsforholdId) {
		return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
				.medArbeidsgiver(arbeidsgiver2)
				.medArbeidsforholdId(arbeidsforholdId)
				.medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
				.leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
						.medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
						.medErAnsettelsesPeriode(true))
				.build();
	}

	private List<PeriodeMedUtbetalingsgradDto> lagAktivitetsgrader(LocalDate fom, LocalDate tom, int aktivitetsgrad) {
		var tidslinje = new LocalDateTimeline<>(fom, tom, true);
		var utenhelg = fjernHelg(tidslinje);
		var helger = new LocalDateTimeline<>(fom, tom, true).disjoint(utenhelg);

		var utbetalingsgraderForHelger = lagAktivitetsgraderFraTidslinje(helger, 100);
		var utbetalingsgraderForUkedager = lagAktivitetsgraderFraTidslinje(utenhelg, aktivitetsgrad);
		utbetalingsgraderForUkedager.addAll(utbetalingsgraderForHelger);
		return utbetalingsgraderForUkedager;
	}

	private List<PeriodeMedUtbetalingsgradDto> lagAktivitetsgraderFraTidslinje(LocalDateTimeline<Boolean> utenhelg, int aktivitetsgrad) {
		return utenhelg.getLocalDateIntervals().stream()
				.map(p -> lagPeriodeMedAktivitetsgrad(p.getFomDato(), p.getTomDato(), aktivitetsgrad))
				.collect(Collectors.toCollection(ArrayList::new));
	}


	public static <T> LocalDateTimeline<T> fjernHelg(LocalDateTimeline<T> input) {
		List<LocalDateSegment<Void>> helger = new ArrayList<>();
		for (var intervall : input.getLocalDateIntervals()) {
            var d = intervall.getFomDato();
			while (!d.isAfter(intervall.getTomDato())) {
				if (d.getDayOfWeek() == DayOfWeek.SUNDAY) {
					helger.add(new LocalDateSegment<>(d, d, null));
					d = d.plusDays(6);
				} else if (d.getDayOfWeek() == DayOfWeek.SATURDAY) {
					helger.add(new LocalDateSegment<>(d, d.plusDays(1), null));
					d = d.plusWeeks(1);
				} else {
					d = d.plusDays(DayOfWeek.SATURDAY.getValue() - d.getDayOfWeek().getValue());
				}
			}
		}
        var helgetidslinje = new LocalDateTimeline<Void>(helger, StandardCombinators::coalesceLeftHandSide);
		return input.disjoint(helgetidslinje);
	}

	private PeriodeMedUtbetalingsgradDto lagPeriodeMedAktivitetsgrad(LocalDate fom, LocalDate tom, int aktivitetsgrad) {
		return new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMedTilOgMed(fom, tom), null, Aktivitetsgrad.fra(aktivitetsgrad));
	}

	private AktivitetDto lagAktivitet(Arbeidsgiver arbeidsgiver2, InternArbeidsforholdRefDto ref) {
		return new AktivitetDto(arbeidsgiver2, ref, UttakArbeidType.ORDINÆRT_ARBEID);
	}

}
