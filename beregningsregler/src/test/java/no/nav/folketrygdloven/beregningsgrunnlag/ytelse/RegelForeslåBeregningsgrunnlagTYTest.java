package no.nav.folketrygdloven.beregningsgrunnlag.ytelse;

import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.F_14_7;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;

class RegelForeslåBeregningsgrunnlagTYTest {


	private static final LocalDate STP = LocalDate.now();

	@Test
	void skalBeregneFraYtelseVedtakMedKunArbeidstaker() {
		// Arrange
		var beregningsgrunnlagPeriode = lagPeriodeMedInntektskategorier(List.of(Inntektskategori.ARBEIDSTAKER), Periode.of(STP, null));
		var inntektsgrunnlag = lagInntektsgrunnlag(Periode.of(STP.minusMonths(1), STP.minusDays(1)), Map.of(Inntektskategori.ARBEIDSTAKER, BigDecimal.valueOf(1000)));
		var bg = byggBG(beregningsgrunnlagPeriode, inntektsgrunnlag);


		var evaluation = kjørRegel(beregningsgrunnlagPeriode);

		var statuser = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatuser(AktivitetStatus.BA);
		var at = statuser.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.ARBEIDSTAKER)).findFirst().orElseThrow();
		assertThat(at.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(260_000));
		assertThat(bg.getAktivitetStatus(AktivitetStatus.KUN_YTELSE).getHjemmel()).isEqualTo(F_14_7);
	}

	@Test
	void skalBeregneFraYtelseVedtakMedKunFrilanser() {
		// Arrange
		var beregningsgrunnlagPeriode = lagPeriodeMedInntektskategorier(List.of(Inntektskategori.FRILANSER), Periode.of(STP, null));
		var inntektsgrunnlag = lagInntektsgrunnlag(Periode.of(STP.minusMonths(1), STP.minusDays(1)),
				Map.of(Inntektskategori.FRILANSER, BigDecimal.valueOf(1000)));
		var bg = byggBG(beregningsgrunnlagPeriode, inntektsgrunnlag);


		var evaluation = kjørRegel(beregningsgrunnlagPeriode);

		var statuser = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatuser(AktivitetStatus.BA);
		var fl = statuser.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().orElseThrow();
		assertThat(fl.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(260_000));
		assertThat(bg.getAktivitetStatus(AktivitetStatus.KUN_YTELSE).getHjemmel()).isEqualTo(F_14_7);
	}

	@Test
	void skalBeregneFraYtelseVedtakMedArbeidstakerOgFrilanser() {
		// Arrange
		var beregningsgrunnlagPeriode = lagPeriodeMedInntektskategorier(List.of(Inntektskategori.FRILANSER,
				Inntektskategori.ARBEIDSTAKER), Periode.of(STP, null));
		var inntektsgrunnlag = lagInntektsgrunnlag(Periode.of(STP.minusMonths(1), STP.minusDays(1)), Map.of(
				Inntektskategori.ARBEIDSTAKER, BigDecimal.valueOf(1000),
				Inntektskategori.FRILANSER, BigDecimal.valueOf(500)));
		var bg = byggBG(beregningsgrunnlagPeriode, inntektsgrunnlag);


		var evaluation = kjørRegel(beregningsgrunnlagPeriode);

		var statuser = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatuser(AktivitetStatus.BA);
		var at = statuser.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.ARBEIDSTAKER)).findFirst().orElseThrow();
		assertThat(at.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(260_000));

		var fl = statuser.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.FRILANSER)).findFirst().orElseThrow();
		assertThat(fl.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(130_000));
		assertThat(bg.getAktivitetStatus(AktivitetStatus.KUN_YTELSE).getHjemmel()).isEqualTo(F_14_7);
	}

	@Test
	void skalBeregneFraYtelseVedtakMedArbeidstakerOgSjømann() {
		// Arrange
		var beregningsgrunnlagPeriode = lagPeriodeMedInntektskategorier(List.of(Inntektskategori.SJØMANN,
				Inntektskategori.ARBEIDSTAKER), Periode.of(STP, null));
		var inntektsgrunnlag = lagInntektsgrunnlag(Periode.of(STP.minusMonths(1), STP.minusDays(1)), Map.of(
				Inntektskategori.ARBEIDSTAKER, BigDecimal.valueOf(1000),
				Inntektskategori.SJØMANN, BigDecimal.valueOf(500)));
		var bg = byggBG(beregningsgrunnlagPeriode, inntektsgrunnlag);


		var evaluation = kjørRegel(beregningsgrunnlagPeriode);

		var statuser = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatuser(AktivitetStatus.BA);
		var at = statuser.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.ARBEIDSTAKER)).findFirst().orElseThrow();
		assertThat(at.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(260_000));

		var fl = statuser.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.SJØMANN)).findFirst().orElseThrow();
		assertThat(fl.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(130_000));
		assertThat(bg.getAktivitetStatus(AktivitetStatus.KUN_YTELSE).getHjemmel()).isEqualTo(F_14_7);
	}


	@Test
	void skalIkkeBeregneFraYtelsevedtakOmManueltFastsatt() {
		// Arrange
		var beregningsgrunnlagPeriode = lagPeriodeMedInntektskategorier(List.of(Inntektskategori.ARBEIDSTAKER), Periode.of(STP, null));
		BeregningsgrunnlagPrStatus.builder(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.BA)).medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(BigDecimal.valueOf(500_000));
		var inntektsgrunnlag = lagInntektsgrunnlag(Periode.of(STP.minusMonths(1), STP.minusDays(1)), Map.of(Inntektskategori.ARBEIDSTAKER, BigDecimal.valueOf(1000)));
		var bg = byggBG(beregningsgrunnlagPeriode, inntektsgrunnlag);


		var evaluation = kjørRegel(beregningsgrunnlagPeriode);

		var statuser = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatuser(AktivitetStatus.BA);
		var at = statuser.stream().filter(a -> a.getInntektskategori().equals(Inntektskategori.ARBEIDSTAKER)).findFirst().orElseThrow();
		assertThat(at.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(500_000));
		assertThat(bg.getAktivitetStatus(AktivitetStatus.KUN_YTELSE).getHjemmel()).isEqualTo(F_14_7);
	}

	private BeregningsgrunnlagPeriode lagPeriodeMedInntektskategorier(List<Inntektskategori> inntektskategorier, Periode p) {
		var builder = BeregningsgrunnlagPeriode.builder()
				.medPeriode(p);

        var i = 1;
		for (var inntektskategori : inntektskategorier) {
			builder.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
					.medAndelNr((long) i++)
					.medAktivitetStatus(AktivitetStatus.BA).medInntektskategori(inntektskategori).build()
			);
		}
		return builder.build();
	}

	private Inntektsgrunnlag lagInntektsgrunnlag(Periode p, Map<Inntektskategori, BigDecimal> dagsatser) {
        var inntektsgrunnlag = new Inntektsgrunnlag();
		dagsatser.entrySet().stream().map(e -> lagPeriodeInntekt(p, e.getValue(), e.getKey()))
				.forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
		return inntektsgrunnlag;
	}

	private Periodeinntekt lagPeriodeInntekt(Periode p, BigDecimal dagsats, Inntektskategori inntektskategori) {
		return Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.YTELSE_VEDTAK)
				.medPeriode(p)
				.medInntekt(dagsats)
				.medInntektskategori(inntektskategori)
				.build();
	}

	private Beregningsgrunnlag byggBG(BeregningsgrunnlagPeriode periode, Inntektsgrunnlag inntektsgrunnlag) {
		return Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(
						new AktivitetStatusMedHjemmel(AktivitetStatus.KUN_YTELSE, null)))
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medBeregningsgrunnlagPeriode(periode)
				.medSkjæringstidspunkt(STP)
				.medGrunnbeløp(BigDecimal.TEN)
				.medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(false))
				.medGrunnbeløpSatser(List.of(new Grunnbeløp(STP.minusMonths(12), STP, 10L, 10L)))
				.build();
	}

	private Evaluation kjørRegel(BeregningsgrunnlagPeriode periode) {
		return new RegelForeslåBeregningsgrunnlagTY(periode).evaluer(periode);
	}


}
