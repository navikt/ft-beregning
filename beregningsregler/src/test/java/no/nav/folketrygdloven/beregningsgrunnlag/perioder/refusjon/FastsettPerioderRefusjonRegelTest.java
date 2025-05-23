package no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon;


import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

import no.nav.fpsak.tidsserie.LocalDateInterval;

import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FastsettPerioderRefusjonRegelTest {
	private static final LocalDate STP = LocalDate.of(2023,6,1);

	@Test
	void skal_periodisere_et_refusjonskrav() {
		var arbfor = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999");
		var periode = lagEksisterendePeriode(STP, etterStp(100), lagEksisterendeAndel(arbfor, 1L));
		var regel = new FastsettPerioderRefusjonRegel();
		var utfall = Arrays.asList(new UtfallPeriode(STP, etterStp(50), Utfall.UNDERKJENT),
				new UtfallPeriode(etterStp(51), LocalDateInterval.TIDENES_ENDE, Utfall.GODKJENT));
        var utfallMap = Map.of(Arbeidsgiver.medOrgnr("999999999"), lagUtfallPrAg(utfall));
		var arbeidOgInntektsmelding = Arrays.asList(lagArbeidOgInntektsmelding(arbfor, new Refusjonskrav(BigDecimal.valueOf(50000), STP, LocalDateInterval.TIDENES_ENDE)));
		var modell = PeriodeModellRefusjon.builder()
				.medUtfallPrArbeidsgiver(utfallMap)
				.medEksisterendePerioder(Collections.singletonList(periode))
				.medSkjæringstidspunkt(STP)
				.medInntektsmeldinger(arbeidOgInntektsmelding)
				.build();

		var liste = new ArrayList<SplittetPeriode>();
		regel.evaluerRegel(modell, liste);

		assertThat(liste).hasSize(2);
		assertPeriode(liste, STP, etterStp(50), Utfall.UNDERKJENT);
		assertPeriode(liste, etterStp(51), LocalDateInterval.TIDENES_ENDE, Utfall.GODKJENT);
	}

	@Test
	void skal_periodisere_flere_refusjonskrav() {
		var arbfor = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999");
		var periode = lagEksisterendePeriode(STP, etterStp(100), lagEksisterendeAndel(arbfor, 1L));
		var regel = new FastsettPerioderRefusjonRegel();
		var utfall = Arrays.asList(new UtfallPeriode(STP, etterStp(50), Utfall.UNDERKJENT),
				new UtfallPeriode(etterStp(51), etterStp(60), Utfall.GODKJENT),
				new UtfallPeriode(etterStp(61), etterStp(70), Utfall.UNDERKJENT),
				new UtfallPeriode(etterStp(71), LocalDateInterval.TIDENES_ENDE, Utfall.GODKJENT));
        var utfallMap = Map.of(Arbeidsgiver.medOrgnr("999999999"), lagUtfallPrAg(utfall));
		var arbeidOgInntektsmelding = Arrays.asList(lagArbeidOgInntektsmelding(arbfor, new Refusjonskrav(BigDecimal.valueOf(50000), STP, LocalDateInterval.TIDENES_ENDE)));
		var modell = PeriodeModellRefusjon.builder()
				.medUtfallPrArbeidsgiver(utfallMap)
				.medEksisterendePerioder(Collections.singletonList(periode))
				.medSkjæringstidspunkt(STP)
				.medInntektsmeldinger(arbeidOgInntektsmelding)
				.build();

		var liste = new ArrayList<SplittetPeriode>();
		regel.evaluerRegel(modell, liste);

		assertThat(liste).hasSize(4);
		assertPeriode(liste, STP, etterStp(50), Utfall.UNDERKJENT);
		assertPeriode(liste, etterStp(51), etterStp(60), Utfall.GODKJENT);
		assertPeriode(liste, etterStp(61), etterStp(70), Utfall.UNDERKJENT);
		assertPeriode(liste, etterStp(71), LocalDateInterval.TIDENES_ENDE, Utfall.GODKJENT);
	}

	private void assertPeriode(ArrayList<SplittetPeriode> perioder, LocalDate fom, LocalDate tom, Utfall utfall) {
		var match = perioder.stream().filter(p -> p.getPeriode().getFom().equals(fom) && p.getPeriode().getTom().equals(tom)).findFirst();
		assertThat(match).isPresent();
		if (utfall.equals(Utfall.UNDERKJENT)) {
			assertThat(match.get().getNyeAndeler().stream().allMatch(andel -> andel.getInnvilgetRefusjonskravPrÅr().compareTo(BigDecimal.ZERO) == 0)).isTrue();
		} else {
			assertThat(match.get().getNyeAndeler().stream().allMatch(andel -> andel.getInnvilgetRefusjonskravPrÅr().compareTo(BigDecimal.ZERO) == 1)).isTrue();
		}
	}

	private ArbeidsforholdOgInntektsmelding lagArbeidOgInntektsmelding(Arbeidsforhold arbfor, Refusjonskrav... refusjonskrav) {
		return ArbeidsforholdOgInntektsmelding.builder()
				.medArbeidsforhold(arbfor)
				.medStartdatoPermisjon(STP)
				.medAnsettelsesperiode(Periode.of(LocalDateInterval.TIDENES_BEGYNNELSE, LocalDateInterval.TIDENES_ENDE))
				.medRefusjonskrav(Arrays.asList(refusjonskrav))
				.build();

	}

	private SplittetPeriode lagEksisterendePeriode(LocalDate fom, LocalDate tom, EksisterendeAndel... andel) {
		return SplittetPeriode.builder()
				.medPeriode(Periode.of(fom, tom))
				.medPeriodeÅrsaker(Collections.emptyList())
				.medFørstePeriodeAndeler(Arrays.asList(andel))
				.build();
	}

	private LocalDate etterStp(int dager) {
		return STP.plusDays(dager);
	}

	private EksisterendeAndel lagEksisterendeAndel(Arbeidsforhold arbeidsforhold, Long andelsnr) {
		return EksisterendeAndel.builder()
				.medArbeidsforhold(arbeidsforhold)
				.medAndelNr(andelsnr)
				.build();
	}

	private LocalDateTimeline<Utfall> lagUtfallPrAg(List<UtfallPeriode> alleUtfall) {
		var segmenter = alleUtfall.stream().map(ut -> new LocalDateSegment<>(ut.fom, ut.tom, ut.utfall)).toList();
		return new LocalDateTimeline<>(segmenter);
	}

	private record UtfallPeriode(LocalDate fom, LocalDate tom, Utfall utfall){};

}
