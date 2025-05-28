package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.RegelFinnGrenseverdiMedFordeling;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

class RegelFullføreBeregningsgrunnlagTest {
	public static final String ORGNR1 = "999999999";
	private static final String ORGNR2 = "999999998";
	private static final String ORGNR3 = "999999997";
	private static final String ARB_ID_1 = "arbeidsforholdId_1";
	private static final String ARB_ID_2 = "arbeidsforholdId_2";
	private static final String ARB_ID_3 = "arbeidsforholdId_3";
	private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(100_000);

	private static BeregningsgrunnlagPeriode PERIODE = BeregningsgrunnlagPeriode.builder()
			.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
			.build();


	@BeforeEach
	void setup() {
		PERIODE = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();
		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(PERIODE)
				.medGrunnbeløp(GRUNNBELØP);
	}

	@Test
	void to_arbeidsforhold_hel_og_halv_utbetaling_kun_penger_til_refusjon() {
		//Arrange
		double bruttoEn = 624_000;
		double refusjonEn = 600_000;
		var utbetalingsgradEn = 50;

		double bruttoTo = 576_000;
		double refusjonTo = 480_000;
		var utbetalingsgradTo = 100;

        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		var grunnbeløp = BigDecimal.valueOf(99_858L);
		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(grunnbeløp);

		leggTilArbeidsforhold(periode, 1L, ORGNR1, bruttoEn, refusjonEn, utbetalingsgradEn);
		leggTilArbeidsforhold(periode, 2L, ORGNR2, bruttoTo, refusjonTo, utbetalingsgradTo);

		//Act
		kjørRegelFinnGrenseverdi(periode);
		kjørRegelFullførBeregningsgrunnlag(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(443369.52));
        var arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();

		verifiserArbfor(arbeidsforhold, ORGNR1, 0, 599);
		verifiserArbfor(arbeidsforhold, ORGNR2, 0, 1106);
	}

	@Test
	void to_arbeidsforhold_hel_og_halv_utbetaling_penger_til_bruker_og_refusjon() {
		//Arrange
		double bruttoEn = 624_000;
		double refusjonEn = 300_000;

		double bruttoTo = 576_000;
		double refusjonTo = 200_000;

        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(99_858L));

		leggTilArbeidsforhold(periode, 1L, ORGNR1, bruttoEn, refusjonEn, 50);
		leggTilArbeidsforhold(periode, 2L, ORGNR2, bruttoTo, refusjonTo, 100);

		//Act
		kjørRegelFinnGrenseverdi(periode);
		kjørRegelFullførBeregningsgrunnlag(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(443369.52));
        var arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();

		verifiserArbfor(arbeidsforhold, ORGNR1, 22, 577);
		verifiserArbfor(arbeidsforhold, ORGNR2, 337, 769);
	}

	@Test
	void tre_arbeidsforhold_halv_og_halv_og_ingen_utbetaling_penger_til_bruker_og_refusjon() {
		//Arrange
		double bruttoEn = 600_000;
        var refusjonEn = 560_000.04;

		double bruttoTo = 750_000;
        var refusjonTo = 333_333.36;

		double bruttoTre = 250_000;
		double refusjonTre = 0;

        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(99_858L));

		leggTilArbeidsforhold(periode, 1L, ORGNR1, bruttoEn, refusjonEn, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR2, bruttoTo, refusjonTo, 60);
		leggTilArbeidsforhold(periode, 3L, ORGNR3, bruttoTre, refusjonTre, 0);

		//Act
		kjørRegelFinnGrenseverdi(periode);
		kjørRegelFullførBeregningsgrunnlag(periode);
		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(393190.875));
        var arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();

		verifiserArbfor(arbeidsforhold, ORGNR1, 0, 864);
		verifiserArbfor(arbeidsforhold, ORGNR2, 0, 648);
		verifiserArbfor(arbeidsforhold, ORGNR3, 0, 0);
	}

	@Test
	void skal_teste_et_arbeidsforhold_med_refusjon_over_6G() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 612_000, 612_000, 100);

		// Act
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(612_000, PERIODE.getGrenseverdi(), PERIODE.getGrenseverdi());
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, PERIODE.getGrenseverdi(), PERIODE.getGrenseverdi());
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_beregningsgrunnlag_under_6G_full_refusjon_gradert() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 200_000, 200_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 300_000, 300_000, 50);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(500_000, 350_000, 350_000);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.valueOf(200_000), BigDecimal.valueOf(200_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.valueOf(150_000), BigDecimal.valueOf(150_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void næring_med_beregningsgrunnlag_under_6G() {
		// Arrange
		leggTilNæring(300_000, 100, 1L);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(300_000, 300_000, 300_000);
		assertAndel(BigDecimal.valueOf(300_000), BigDecimal.valueOf(300_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void næring_med_beregningsgrunnlag_under_6G_ikke_søkt_ytelse() {
		// Arrange
		leggTilNæring(300_000, 0, 1L);

		// Act
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(300_000, 0, 0);
		assertAndel(BigDecimal.ZERO, BigDecimal.ZERO);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void næring_med_beregningsgrunnlag_under_6G_delvis_søkt_ytelse() {
		// Arrange
		leggTilNæring(300_000, 50, 1L);

		// Act
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(300_000, 150_000, 150_000);
		assertAndel(BigDecimal.valueOf(150_000), BigDecimal.valueOf(150_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void brukers_andel_med_tilkommet_næring_uten_fordeling() {
		// Arrange
		var andel = BeregningsgrunnlagPrStatus
				.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medBruttoPrÅr(BigDecimal.valueOf(300_000))
				.medUtbetalingsprosent(BigDecimal.valueOf(100))
				.medAndelNr(1L)
				.build();
		andel.setErSøktYtelseFor(true);

		BeregningsgrunnlagPeriode.oppdater(PERIODE)
				.medBeregningsgrunnlagPrStatus(andel);

		var snAndel = BeregningsgrunnlagPrStatus
				.builder()
				.medAktivitetStatus(AktivitetStatus.SN)
				.medBruttoPrÅr(null)
				.medUtbetalingsprosent(BigDecimal.valueOf(50))
				.medAndelNr(2L)
				.build();
		snAndel.setErSøktYtelseFor(true);
		BeregningsgrunnlagPeriode.oppdater(PERIODE)
				.medBeregningsgrunnlagPrStatus(snAndel);


		// Act
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(300_000, 300_000, 300_000);

		Assertions.assertThat(snAndel.getAvkortetPrÅr().compareTo(BigDecimal.ZERO)).isZero();
		Assertions.assertThat(snAndel.getRedusertPrÅr().compareTo(BigDecimal.ZERO)).isZero();

		Assertions.assertThat(andel.getAvkortetPrÅr().compareTo(BigDecimal.valueOf(300000))).isZero();
		Assertions.assertThat(andel.getRedusertPrÅr().compareTo(BigDecimal.valueOf(300000))).isZero();


		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void næring_med_beregningsgrunnlag_over_6G_delvis_søkt_ytelse() {
		// Arrange
		leggTilNæring(800_000, 50, 1L);

		// Act
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		//assertPeriode(800_000‬, 299_574, 299_574);
		assertAndel(BigDecimal.valueOf(300_000), BigDecimal.valueOf(300_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_beregningsgrunnlag_over_6G_refusjon_under_6G_gradert() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 800_000, 200_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 200_000, 200_000, 50);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(1_000_000, 540_000, 540_000);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.valueOf(240_000), BigDecimal.valueOf(200_000), BigDecimal.valueOf(440_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.valueOf(0), BigDecimal.valueOf(100_000), BigDecimal.valueOf(100_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_med_refusjon_over_6G() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 600_000, 560_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 750_000, 200_000, 100);
		leggTilArbeidsforhold(PERIODE, 3L, ORGNR3, ARB_ID_3, 250_000, 0, 0);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(1_600_000, 506_250, 506_250);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.valueOf(306_250), null);
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.valueOf(200_000), null);
		assertArbeidsforhold(arbeidsforhold, ORGNR3, BigDecimal.ZERO, BigDecimal.ZERO, null);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_med_refusjon_under_6G() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 600_000, 150_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 750_000, 200_000, 100);
		leggTilArbeidsforhold(PERIODE, 3L, ORGNR3, ARB_ID_3, 250_000, 0, 0);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(1_600_000, 506_250, 506_250);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.valueOf(75_000), BigDecimal.valueOf(150_000), BigDecimal.valueOf(225_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.valueOf(81_250), BigDecimal.valueOf(200_000), BigDecimal.valueOf(281_250));
		assertArbeidsforhold(arbeidsforhold, ORGNR3, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_med_refusjon_og_inntekt_under_6G() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 150_000, 100_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 200_000, 200_000, 100);
		leggTilArbeidsforhold(PERIODE, 3L, ORGNR3, ARB_ID_3, 100_000, 0, 0);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(450_000, 350_000, 350_000);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.valueOf(50_000), BigDecimal.valueOf(100_000), BigDecimal.valueOf(150_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.valueOf(200_000), BigDecimal.valueOf(200_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR3, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_med_refusjon_lik_6G_og_inntekt_over_6G_gradert() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 600_000, 600_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 600_000, 0, 50);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(1_200_000, 450_000, 450_000);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.valueOf(450_000), BigDecimal.valueOf(450_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_med_refusjon_lik_6G_for_begge_og_inntekt_over_6G_gradert() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 600_000, 600_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 600_000, 600_000, 50);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(1_200_000, 450_000, 450_000);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.valueOf(300_000), BigDecimal.valueOf(300_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.valueOf(150_000), BigDecimal.valueOf(150_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_med_refusjon_lik_6G_og_1G_og_inntekt_over_6G_gradert() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 600_000, 600_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 600_000, 100_000, 50);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(1_200_000, 450_000, 450_000);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.valueOf(400_000), BigDecimal.valueOf(400_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.valueOf(50_000), BigDecimal.valueOf(50_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_arbeidsforhold_med_refusjon_uten_tilrettelegging_og_tilrettelegging_uten_refusjon() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 300_000, 150_000, 100);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR2, ARB_ID_2, 210_000, 210_000, 0);
		leggTilArbeidsforhold(PERIODE, 3L, ORGNR3, ARB_ID_3, 240_000, 0, 100);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(750000, 432_000, 432_000);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.valueOf(90_000), BigDecimal.valueOf(150_000), BigDecimal.valueOf(240_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		assertArbeidsforhold(arbeidsforhold, ORGNR3, BigDecimal.valueOf(192_000), BigDecimal.ZERO, BigDecimal.valueOf(192_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_arbeidstaker_med_delvis_søkt_ytelse() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 1L, ORGNR1, ARB_ID_1, 300_000, 0, 45);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertPeriode(300_000, 135_000, 135_000);
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.valueOf(135_000), BigDecimal.ZERO, BigDecimal.valueOf(135_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_med_beregningsgrunnlag_under_6G() {
		// Arrange
		leggTilFrilans(300_000, 100, 1L);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(300_000, 300_000, 300_000);
		assertFrilans(BigDecimal.valueOf(300_000), BigDecimal.valueOf(300_000), 1154L);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_med_beregningsgrunnlag_under_6G_delvis_søkt_ytelse() {
		// Arrange
		leggTilFrilans(300_000, 50, 1L);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(300_000, 150_000, 150_000);
		assertFrilans(BigDecimal.valueOf(150_000), BigDecimal.valueOf(150_000), 577L);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_med_beregningsgrunnlag_under_6G_ikke_søkt_ytelse() {
		// Arrange
		leggTilFrilans(300_000, 0, 1L);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(300_000, 0, 0);
		assertFrilans(BigDecimal.ZERO, BigDecimal.ZERO, 0L);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_og_arbeid_med_beregningsgrunnlag_under_6G_søkt_ytelse_for_alle() {
		// Arrange
		leggTilFrilans(200_000, 100, 1L);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR1, ARB_ID_1, 200_000, 0, 100);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(400_000, 400_000, 400_000);
		assertFrilans(BigDecimal.valueOf(200_000), BigDecimal.valueOf(200_000), null);
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.valueOf(200_000), BigDecimal.ZERO, BigDecimal.valueOf(200_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_og_arbeid_med_beregningsgrunnlag_over_6G_for_arbeid_søkt_ytelse_for_alle() {
		// Arrange
		leggTilFrilans(200_000, 100, 1L);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR1, ARB_ID_1, 800_000, 0, 100);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(1_000_000, 600_000, 600_000);
		assertFrilans(BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertArbeidsforhold(arbeidsforhold, ORGNR1, PERIODE.getGrenseverdi(), BigDecimal.ZERO, PERIODE.getGrenseverdi());
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_og_arbeid_med_beregningsgrunnlag_over_6G_for_arbeid_søkt_ytelse_for_frilans() {
		// Arrange
		leggTilFrilans(200_000, 100, 1L);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR1, ARB_ID_1, 800_000, 0, 0);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(1_000_000, 0, 0);
		assertFrilans(BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_og_arbeid_med_beregningsgrunnlag_over_6G_til_sammen_søkt_delvis_ytelse_for_frilans() {
		// Arrange
		leggTilFrilans(500_000, 50, 1L);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR1, ARB_ID_1, 500_000, 0, 0);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(1_000_000, 50_000, 50_000);
		assertFrilans(BigDecimal.valueOf(50_000), BigDecimal.valueOf(50_000), 192L);
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_og_to_arbeid_med_beregningsgrunnlag_over_6G_til_sammen_søkt_ytelse_for_alle_med_refusjonkrav_som_overstiger_total_avkortet_for_arbeid() {
		// Arrange
		leggTilFrilans(500_000, 100, 1L);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR1, ARB_ID_1, 500_000, 500_000, 100);
		leggTilArbeidsforhold(PERIODE, 3L, ORGNR2, ARB_ID_2, 500_000, 200_000, 100);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(1_500_000, 600_000, 600_000);
		assertFrilans(BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.valueOf(400_000), BigDecimal.valueOf(400_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.valueOf(200_000), BigDecimal.valueOf(200_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_frilans_og_to_arbeid_med_beregningsgrunnlag_over_6G_til_sammen_søkt_ytelse_for_alle_med_refusjonkrav_som_overstiger_total_avkortet_for_arbeid_med_fordeling_av_refusjonskrav() {
		// Arrange
		leggTilFrilans(500_000, 100, 1L);
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR1, ARB_ID_1, 500_000, 500_000, 100);
		leggTilArbeidsforhold(PERIODE, 3L, ORGNR2, ARB_ID_2, 500_000, 300_000, 100);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(1_500_000, 600_000, 600_000);
		assertFrilans(BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.valueOf(300_000), BigDecimal.valueOf(300_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.valueOf(300_000), BigDecimal.valueOf(300_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_hos_en_arbeidsgiver_med_beregningsgrunnlag_over_6G() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR1, ARB_ID_1, 400_000, 300_000, 100);
		leggTilArbeidsforhold(PERIODE, 3L, ORGNR2, ARB_ID_2, 300_000, 300_000, 100);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(700_000, 600_000, 600_000);
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.ZERO, BigDecimal.valueOf(300_000), BigDecimal.valueOf(300_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.ZERO, BigDecimal.valueOf(300_000), BigDecimal.valueOf(300_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	@Test
	void skal_teste_to_arbeidsforhold_hos_en_arbeidsgiver_med_beregningsgrunnlag_over_6G_ikke_full_utbetaling() {
		// Arrange
		leggTilArbeidsforhold(PERIODE, 2L, ORGNR1, ARB_ID_1, 624_000, 300_000, 50);
		leggTilArbeidsforhold(PERIODE, 3L, ORGNR2, ARB_ID_2, 576_000, 200_000, 100);

		// Assert
		var grenseverdiRegelesultat = kjørRegelFinnGrenseverdi(PERIODE);
		var regelResultat = kjørRegelFullførBeregningsgrunnlag(PERIODE);

		// Assert
		assertPeriode(1_200_000, 444_000, 444_000);
        var arbeidsforhold = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		assertArbeidsforhold(arbeidsforhold, ORGNR1, BigDecimal.valueOf(6_000), BigDecimal.valueOf(150_000), BigDecimal.valueOf(156_000));
		assertArbeidsforhold(arbeidsforhold, ORGNR2, BigDecimal.valueOf(88_000), BigDecimal.valueOf(200_000), BigDecimal.valueOf(288_000));
		assertThat(grenseverdiRegelesultat).isNotNull();
		assertThat(regelResultat).isNotNull();
	}

	private void assertPeriode(int brutto, int avkortet, int redusert) {
		assertPeriode(BigDecimal.valueOf(brutto), BigDecimal.valueOf(avkortet), BigDecimal.valueOf(redusert));
	}

	private void assertPeriode(int brutto, BigDecimal avkortet, BigDecimal redusert) {
		assertPeriode(BigDecimal.valueOf(brutto), avkortet, redusert);
	}

	private void assertPeriode(BigDecimal brutto, BigDecimal avkortet, BigDecimal redusert) {
		Assertions.assertThat(PERIODE.getBruttoPrÅr()).isEqualByComparingTo(brutto);
		Assertions.assertThat(PERIODE.getAvkortetPrÅr()).isEqualByComparingTo(avkortet);
		Assertions.assertThat(PERIODE.getRedusertPrÅr()).isEqualByComparingTo(redusert);
	}

	private void assertAndel(BigDecimal bruker, BigDecimal avkortet) {
		var andel = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        var total = bruker.add(BigDecimal.ZERO);
		Assertions.assertThat(andel.getAvkortetPrÅr()).isEqualByComparingTo(Objects.requireNonNullElse(avkortet, total));
		Assertions.assertThat(andel.getRedusertPrÅr()).isEqualByComparingTo(total);
	}

	private void assertFrilans(BigDecimal bruker, BigDecimal avkortet, Long dagsats) {
		var andel = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        var total = bruker.add(BigDecimal.ZERO);
        var frilansOpt = andel.getArbeidsforhold().stream().filter(BeregningsgrunnlagPrArbeidsforhold::erFrilanser).findFirst();
		assertThat(frilansOpt).isPresent();
        var frilansAndel = frilansOpt.get();
		assertThat(frilansAndel.getAvkortetPrÅr()).isEqualByComparingTo(Objects.requireNonNullElse(avkortet, total));
		assertThat(frilansAndel.getRedusertPrÅr()).isEqualByComparingTo(total);
		if (dagsats != null) {
			assertThat(frilansAndel.getDagsats()).isEqualByComparingTo(dagsats);
		}
	}

	private void assertArbeidsforhold(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold, String orgnr, BigDecimal bruker, BigDecimal refusjon, BigDecimal avkortet) {
		Function<BigDecimal, Long> calcDagsats = a -> a.divide(BigDecimal.valueOf(260), 0, RoundingMode.HALF_UP).longValue();
        var arbforOpt = arbeidsforhold.stream().filter(a -> Objects.equals(a.getArbeidsgiverId(), orgnr)).findFirst();
        var total = bruker.add(refusjon);

		assertThat(arbforOpt).isPresent();
        var arbfor = arbforOpt.get();

		Assertions.assertThat(arbfor.getAvkortetPrÅr()).isEqualByComparingTo(Objects.requireNonNullElse(avkortet, total));
		assertThat(arbfor.getRedusertPrÅr()).isEqualByComparingTo(total);

        var dagsatsRefusjon = calcDagsats.apply(refusjon);
        var dagsatsBruker = calcDagsats.apply(bruker);
		assertThat(arbfor.getDagsatsArbeidsgiver()).isEqualTo(dagsatsRefusjon);
		assertThat(arbfor.getDagsatsBruker()).isEqualTo(dagsatsBruker);
		assertThat(arbfor.getDagsats()).isEqualTo(dagsatsBruker + dagsatsRefusjon);
	}

	private void verifiserArbfor(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold, String orgnr, int forventetDagsatsBrukersAndel, int forventetDagsatsRefusjon) {
        var arbforOpt = arbeidsforhold.stream().filter(a -> a.getArbeidsgiverId().equals(orgnr)).findFirst();
		assertThat(arbforOpt).isPresent();
        var arbfor = arbforOpt.get();
		assertThat(arbfor.getDagsatsArbeidsgiver()).isEqualTo(forventetDagsatsRefusjon);
		assertThat(arbfor.getDagsatsBruker()).isEqualTo(forventetDagsatsBrukersAndel);
		assertThat(arbfor.getDagsats()).isEqualTo(forventetDagsatsBrukersAndel + forventetDagsatsRefusjon);
	}

	private void leggTilNæring(int brutto, int utbetaingsgrad, Long andelsnr) {
		var andel = BeregningsgrunnlagPrStatus
				.builder()
				.medAktivitetStatus(AktivitetStatus.SN)
				.medBruttoPrÅr(BigDecimal.valueOf(brutto))
				.medUtbetalingsprosent(BigDecimal.valueOf(utbetaingsgrad))
				.medAndelNr(andelsnr)
				.build();
		andel.setErSøktYtelseFor(utbetaingsgrad != 0);
		BeregningsgrunnlagPeriode.oppdater(PERIODE)
				.medBeregningsgrunnlagPrStatus(andel);
	}

	private void leggTilArbeidsforhold(BeregningsgrunnlagPeriode periode,
	                                   long andelsnr,
	                                   String orgnr,
	                                   double beregnetPrÅr,
	                                   double refusjonPrÅr,
	                                   double utbetalingsgrad) {
		leggTilArbeidsforhold(periode, andelsnr, orgnr, null, beregnetPrÅr, refusjonPrÅr, utbetalingsgrad);
	}

	private void leggTilFrilans(int brutto, int utbetaingsgrad, Long andelsnr) {
        var arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
        var atfl = PERIODE.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

		if (atfl == null) {
			BeregningsgrunnlagPeriode.oppdater(PERIODE)
					.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
							.builder()
							.medAktivitetStatus(AktivitetStatus.ATFL)
							.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, brutto, 0, utbetaingsgrad, arbeidsforhold))
							.build());
		} else {
			BeregningsgrunnlagPrStatus.builder(atfl)
					.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, brutto, 0, utbetaingsgrad, arbeidsforhold))
					.build();
		}
	}

	private void leggTilArbeidsforhold(BeregningsgrunnlagPeriode periode,
	                                   long andelsnr,
	                                   String orgnr,
	                                   String arbeidsforholdId,
	                                   double beregnetPrÅr,
	                                   double refusjonPrÅr,
	                                   double utbetalingsgrad) {
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, arbeidsforholdId);
        var atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

		if (atfl == null) {
			BeregningsgrunnlagPeriode.oppdater(periode)
					.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
							.builder()
							.medAktivitetStatus(AktivitetStatus.ATFL)
							.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, refusjonPrÅr, utbetalingsgrad, arbeidsforhold))
							.build());
		} else {
			BeregningsgrunnlagPrStatus.builder(atfl)
					.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, refusjonPrÅr, utbetalingsgrad, arbeidsforhold))
					.build();
		}
	}

	private BeregningsgrunnlagPrArbeidsforhold lagBeregningsgrunnlagPrArbeidsforhold(long andelsnr,
	                                                                                 double beregnetPrÅr,
	                                                                                 double refusjonskrav,
	                                                                                 double utbetalingsgrad,
	                                                                                 Arbeidsforhold arbeidsforhold) {
        var arb = BeregningsgrunnlagPrArbeidsforhold.builder()
				.medAndelNr(andelsnr)
				.medArbeidsforhold(arbeidsforhold)
				.medBruttoPrÅr(BigDecimal.valueOf(beregnetPrÅr))
				.medRefusjonPrÅr(BigDecimal.valueOf(refusjonskrav))
				.medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
				.build();
		arb.setErSøktYtelseFor(utbetalingsgrad > 0);
		return arb;
	}


	private RegelResultat kjørRegelFinnGrenseverdi(BeregningsgrunnlagPeriode grunnlag) {
		return new RegelFinnGrenseverdiMedFordeling(grunnlag).evaluerRegel(grunnlag);
	}

	private RegelResultat kjørRegelFullførBeregningsgrunnlag(BeregningsgrunnlagPeriode grunnlag) {
		return new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
	}
}
