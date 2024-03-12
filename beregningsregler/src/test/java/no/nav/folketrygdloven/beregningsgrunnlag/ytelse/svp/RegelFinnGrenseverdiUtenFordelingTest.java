package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.RegelFinnGrenseverdiUtenFordeling;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.TilkommetInntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

class RegelFinnGrenseverdiUtenFordelingTest {

	public static final String ORGNR = "999999999";
	private static final String ORGNR_2 = "999999998";
	private static final String ORGNR_3 = "999999997";
	public static final Arbeidsforhold AF_1 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
	public static final Arbeidsforhold AF_2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR_2);
	public static final Arbeidsforhold AF_3 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR_3);

	@Test
	void ett_arbeidsforhold_under_6G() {
		//Arrange
		double beregnetPrÅr = 400_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(beregnetPrÅr));
	}

	@Test
	void ett_arbeidsforhold_under_6G_midlertidig_inaktiv_type_A() {
		//Arrange
		double beregnetPrÅr = 400_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medMidlertidigInaktivType(MidlertidigInaktivType.A)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);

		var forventet = 260_000;

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(forventet));
	}


	@Test
	void ett_arbeidsforhold_under_6G_ikkje_søkt_ytelse() {
		//Arrange
		double beregnetPrÅr = 400_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
	}

	@Test
	void ett_arbeidsforhold_over_6G() {
		//Arrange
		double beregnetPrÅr = 800_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}

	@Test
	void to_arbeidsforhold_under_6G() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(550_000));
	}

	@Test
	void to_arbeidsforhold_under_6G_tilkommet_inntekt_i_det_ene() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double tilkommetPrÅr2 = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, null, null, 100D, 0D);
		leggTilTilkommet(periode, AF_2, tilkommetPrÅr2);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
	}

	@Test
	void to_arbeidsforhold_under_6G_tilkommet_inntekt_i_det_ene_mer_enn_totalt_grunnlag_på_stp() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double tilkommetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, null, null, 100D, 0D);
		leggTilTilkommet(periode, AF_2, tilkommetPrÅr2);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void to_arbeidsforhold_under_6G_fordelt_og_tilkommet_inntekt_i_det_ene() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double fordeltPrÅr = 150_000;

		double tilkommetPrÅr2 = 25_000;
		double fordeltPrÅr2 = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, fordeltPrÅr, 100d,0D);
		leggTilArbeidsforhold(periode, AF_2, null, fordeltPrÅr2, 50d,50D);
		leggTilTilkommet(periode, AF_2, tilkommetPrÅr2);
		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(225_000));
	}



	@Test
	void to_arbeidsforhold_over_6G() {
		//Arrange
		double beregnetPrÅr = 350_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}

	@Test
	void to_arbeidsforhold_over_6G_pluss_et_tilkommet() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double beregnetPrÅr2 = 300_000;
		double tilkommetPrÅr = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_3, null, null, 100D, 0D);
		leggTilTilkommet(periode, AF_3, tilkommetPrÅr);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(450_000));
	}


	@Test
	void to_arbeidsforhold_under_6G_søkt_ytelse_for_en() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 0D, 100D);


		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(250_000));
	}

	@Test
	void to_arbeidsforhold_under_6G_søkt_ytelse_for_en_pluss_et_tilkommet() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double beregnetPrÅr2 = 300_000;
		double tilkommetPrÅr = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_3, null, null, 100D, 100D);
		leggTilTilkommet(periode, AF_3, tilkommetPrÅr);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
	}


	@Test
	void to_arbeidsforhold_til_sammen_over_6G_søkt_ytelse_for_en() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double beregnetPrÅr2 = 500_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
	}

	@Test
	void to_arbeidsforhold_til_sammen_over_6G_søkt_ytelse_for_en_pluss_et_tilkommet() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double beregnetPrÅr2 = 500_000;
		double tilkommetPrÅr = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_3, null, null, 0D, 100D);
		leggTilTilkommet(periode, AF_3,tilkommetPrÅr);
		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(180_000));
	}

	@Test
	void to_arbeidsforhold_til_sammen_over_6G_søkt_ytelse_for_begge_gradert_pluss_et_tilkommet() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double beregnetPrÅr2 = 500_000;
		double tilkommetPrÅr = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 20D, 80D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);
		leggTilArbeidsforhold(periode, AF_3, null, null, 0D, 100D);
		leggTilTilkommet(periode, AF_3, tilkommetPrÅr);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi().setScale(2, RoundingMode.HALF_UP)).isEqualByComparingTo(BigDecimal.valueOf(90_000));
	}

	@Test
	void to_arbeidsforhold_den_ene_over_6G_søkt_ytelse_for_en() {
		//Arrange
		double beregnetPrÅr = 800_000;
		double beregnetPrÅr2 = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(480_000));
	}

	@Test
	void to_arbeidsforhold_under_6G_delvis_søkt_ytelse_for_en_full_ytelse_for_en() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
	}

	@Test
	void to_arbeidsforhold_over_6G_for_begge_delvis_søkt_ytelse_for_en_full_ytelse_for_en() {
		//Arrange
		double beregnetPrÅr = 600_000;
		double beregnetPrÅr2 = 600_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(450_000));
	}

	@Test
	void to_arbeidsforhold_over_6G_for_en_delvis_søkt_ytelse_for_en_full_ytelse_for_en() {
		//Arrange
		double beregnetPrÅr = 800_000;
		double beregnetPrÅr2 = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(540_000));
	}

	@Test
	void to_arbeidsforhold_over_6G_for_en_delvis_søkt_ytelse_for_en_full_ytelse_for_den_over_6G() {
		//Arrange
		double beregnetPrÅr = 600_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 50D, 50D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
	}


	@Test
	void to_arbeidsforhold_under_6G_delvis_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 50D, 50D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(275_000));
	}

	@Test
	void to_arbeidsforhold_over_6G_for_begge_delvis_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 600_000;
		double beregnetPrÅr2 = 600_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 50D, 50D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
	}

	@Test
	void to_arbeidsforhold_over_6G_for_en_delvis_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 600_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 50D, 50D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
	}

	@Test
	void to_arbeidsforhold_over_6G_delvis_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 50D, 50D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(275_000));
	}


	@Test
	void tre_arbeidsforhold_over_6G_søkt_ytelse_for_en() {
		//Arrange
		double beregnetPrÅr = 300_000;
		double beregnetPrÅr2 = 300_000;
		double beregnetPrÅr3 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_3, beregnetPrÅr3, null, 0D, 100D);


		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
	}

	@Test
	void tre_arbeidsforhold_over_6G_søkt_ytelse_for_to() {
		//Arrange
		double beregnetPrÅr = 300_000;
		double beregnetPrÅr2 = 300_000;
		double beregnetPrÅr3 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_3, beregnetPrÅr3, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
	}

	@Test
	void tre_arbeidsforhold_over_6G_delvis_søkt_ytelse_for_en_full_for_en() {
		//Arrange
		double beregnetPrÅr = 300_000;
		double beregnetPrÅr2 = 300_000;
		double beregnetPrÅr3 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 50D, 50D);
		leggTilArbeidsforhold(periode, AF_3, beregnetPrÅr3, null, 0D, 100D);


		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
	}

	@Test
	void tre_arbeidsforhold_over_6G_søkt_ytelse_for_alle() {
		//Arrange
		double beregnetPrÅr = 300_000;
		double beregnetPrÅr2 = 300_000;
		double beregnetPrÅr3 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr2, null, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_3, beregnetPrÅr3, null, 100D, 0D);


		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}

	@Test
	void frilans_under_6G_søkt_ytelse() {
		//Arrange
		double beregnetPrÅr = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
	}

	@Test
	void frilans_under_6G_ikkje_søkt_ytelse() {
		//Arrange
		double beregnetPrÅr = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
	}

	@Test
	void frilans_over_6G_søkt_ytelse() {
		//Arrange
		double beregnetPrÅr = 800_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}

	@Test
	void frilans_under_6G_delvis_søkt_ytelse() {
		//Arrange
		double beregnetPrÅr = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
	}

	@Test
	void frilans_over_6G_delvis_søkt_ytelse() {
		//Arrange
		double beregnetPrÅr = 800_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
	}


	@Test
	void frilans_og_et_arbeidsforhold_under_6G_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 300_000;
		double beregnetPrÅr2 = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(500_000));
	}


	@Test
	void et_arbeidsforhold_under_6G_tilkommet_frilansinntekt() {
		//Arrange
		double beregnetPrÅr = 300_000;
		double tilkommetFrilansinntekt = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, null, 100D, 0D);
		leggTilTilkommet(periode, Arbeidsforhold.frilansArbeidsforhold(), tilkommetFrilansinntekt);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi().setScale(2, RoundingMode.HALF_UP)).isEqualByComparingTo(BigDecimal.valueOf(200_000));
	}

	@Test
	void frilans_og_et_arbeidsforhold_under_6G_søkt_ytelse_for_begge_tilkommet_frilansinntekt() {
		//Arrange
		double beregnetPrÅr = 300_000;
		double beregnetPrÅr2 = 200_000;
		double tilkommetFrilansinntekt = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilTilkommet(periode, Arbeidsforhold.frilansArbeidsforhold(), tilkommetFrilansinntekt);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
	}

	@Test
	void frilans_og_et_arbeidsforhold_over_6G_søkt_ytelse_for_begge_tilkommet_frilansinntekt() {
		//Arrange
		double beregnetPrÅr = 800_000;
		double beregnetPrÅr2 = 200_000;
		double tilkommetFrilansinntekt = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilTilkommet(periode, Arbeidsforhold.frilansArbeidsforhold(), tilkommetFrilansinntekt);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(480_000));
	}


	@Test
	void frilans_og_et_arbeidsforhold_over_6G_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 800_000;
		double beregnetPrÅr2 = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}


	@Test
	void frilans_og_et_arbeidsforhold_under_6G_søkt_ytelse_for_kun_frilans() {
		//Arrange
		double beregnetPrÅr = 200_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
	}

	@Test
	void frilans_og_et_arbeidsforhold_under_6G_søkt_ytelse_for_kun_arbeid() {
		//Arrange
		double beregnetPrÅr = 200_000;
		double beregnetPrÅr2 = 300_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
	}

	@Test
	void frilans_og_et_arbeidsforhold_over_6G_til_sammen_søkt_ytelse_for_kun_frilans() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double beregnetPrÅr2 = 500_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}

	@Test
	void frilans_og_et_arbeidsforhold_over_6G_til_sammen_søkt_ytelse_for_kun_arbeid() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double beregnetPrÅr2 = 500_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(500_000));
	}

	@Test
	void frilans_og_et_arbeidsforhold_over_6G_for_arbeidsforhold_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 200_000;
		double beregnetPrÅr2 = 800_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}

	@Test
	void frilans_og_et_arbeidsforhold_over_6G_for_arbeidsforhold_søkt_ytelse_for_kun_frilans() {
		//Arrange
		double beregnetPrÅr = 200_000;
		double beregnetPrÅr2 = 800_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
	}

	@Test
	void frilans_og_et_arbeidsforhold_over_6G_for_arbeidsforhold_søkt_ytelse_for_kun_arbeid() {
		//Arrange
		double beregnetPrÅr = 200_000;
		double beregnetPrÅr2 = 800_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}


	@Test
	void næring_under_6G_søkt_ytelse() {
		//Arrange
		double beregnetPrÅr = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
	}

	@Test
	void næring_under_6G_søkt_delvis_ytelse() {
		//Arrange
		double beregnetPrÅr = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 50D, 50D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}

	@Test
	void næring_under_6G_søkt_delvis_ytelse_tilkommet_inntekt() {
		//Arrange
		double beregnetPrÅr = 200_000;
		double tilkommet = 50_000;


		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 50D, 50D);
		leggTilTilkommet(periode, null, tilkommet);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
	}

	@Test
	void næring_under_6G_ikkje_søkt_ytelse() {
		//Arrange
		double beregnetPrÅr = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
	}

	@Test
	void næring_og_frilans_under_6G_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
	}

	@Test
	void næring_og_frilans_over_6G_søkt_ytelse_for_begge() {
		//Arrange
		double beregnetPrÅr = 500_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}

	@Test
	void næring_og_frilans_over_6G_søkt_ytelse_for_begge_tilkommet_næring() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double tilkommet = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);
		leggTilTilkommet(periode, null, tilkommet);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(540_000));
	}

	@Test
	void næring_og_frilans_over_6G_for_næring_søkt_ytelse_for_næring() {
		//Arrange
		double beregnetPrÅr = 800_000;
		double beregnetPrÅr2 = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr2, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
	}

	@Test
	void næring_og_frilans_over_6G_for_frilans_søkt_ytelse_for_næring() {
		//Arrange
		double beregnetPrÅr = 200_000;
		double beregnetPrÅr2 = 800_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr2, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(0));
	}

	@Test
	void næring_og_frilans_over_6G_for_frilans_søkt_ytelse_for_frilans() {
		//Arrange
		double beregnetPrÅr = 200_000;
		double beregnetPrÅr2 = 800_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 0D, 100D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr2, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
	}

	@Test
	void næring_frilans_og_arbeidsforhold_under_6G_søkt_ytelse_for_alle() {
		//Arrange
		double beregnetPrÅr = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 100D, 0D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(300_000));
	}

	@Test
	void næring_frilans_og_arbeidsforhold_under_6G_søkt_ytelse_for_næring() {
		//Arrange
		double beregnetPrÅr = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}

	@Test
	void næring_frilans_og_arbeidsforhold_under_6G_søkt_ytelse_for_arbeid() {
		//Arrange
		double beregnetPrÅr = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 0D, 100D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}

	@Test
	void næring_frilans_og_arbeidsforhold_over_6G_for_næring_søkt_ytelse_for_arbeid() {
		//Arrange
		double beregnetPrÅr = 800_000;
		double beregnetPrÅr2 = 100_000;
		double beregnetPrÅr3 = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 0D, 100D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr2, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr3, null, 100D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}

	@Test
	void næring_frilans_og_arbeidsforhold_over_6G_for_næring_søkt_ytelse_for_næring() {
		//Arrange
		double beregnetPrÅr = 800_000;
		double beregnetPrÅr2 = 100_000;
		double beregnetPrÅr3 = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 100D, 0D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr2, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr3, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(400_000));
	}

	@Test
	void næring_frilans_og_arbeidsforhold_over_6G_for_næring_søkt_delvis_ytelse_for_næring() {
		//Arrange
		double beregnetPrÅr = 800_000;
		double beregnetPrÅr2 = 100_000;
		double beregnetPrÅr3 = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 50D, 50D);
		leggTilStatus(periode, AktivitetStatus.FL, beregnetPrÅr2, 0D, 100D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr3, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
	}

	@Test
	void næring_og_arbeidsforhold_over_6G_til_sammen_søkt_delvis_ytelse_for_næring_rest_etter_arbeid_mindre_enn_bg_for_frilans() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double beregnetPrÅr2 = 200_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 50D, 50D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 0D, 100D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(200_000));
	}

	@Test
	void næring_og_arbeidsforhold_over_6G_til_sammen_søkt_delvis_ytelse_for_næring_rest_etter_arbeid_mindre_enn_gradert_bg_for_frilans() {
		//Arrange
		double beregnetPrÅr = 500_000;
		double beregnetPrÅr2 = 400_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilStatus(periode, AktivitetStatus.SN, beregnetPrÅr, 50D, 0D);
		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr2, null, 0D, 0D);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}

	@Test
	void to_arbeidsforhold_under_6G_aktivitetsgrad_høyere_enn_utbetalingsgrad() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double tilkommetPrÅr2 = 100_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 50D, 70D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr, null, 50D, 70D);
		leggTilTilkommet(periode, AF_2, tilkommetPrÅr2);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
	}

	@Test
	void to_arbeidsforhold_under_6G_aktivitetsgrad_mye_høyere_enn_utbetalingsgrad() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double tilkommetPrÅr2 = 30_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 50D, 90D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr, null, 30D, 90D);
		leggTilTilkommet(periode, AF_2, tilkommetPrÅr2);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
	}

	@Test
	void to_arbeidsforhold_under_6G_aktivitetsgrad_lavere_enn_utbetalingsgrad() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double tilkommetPrÅr2 = 30_000;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, AF_1, beregnetPrÅr, null, 90D, 20D);
		leggTilArbeidsforhold(periode, AF_2, beregnetPrÅr, null, 95D, 10D);
		leggTilTilkommet(periode, AF_2, tilkommetPrÅr2);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi().setScale(2, RoundingMode.HALF_UP)).isEqualByComparingTo(BigDecimal.valueOf(395_000));
	}


	private RegelResultat kjørRegel(BeregningsgrunnlagPeriode periode) {
		return new RegelFinnGrenseverdiUtenFordeling(periode).evaluerRegel(periode);
	}

	private void leggTilArbeidsforhold(BeregningsgrunnlagPeriode periode,
	                                   Arbeidsforhold arbeidsforhold,
	                                   Double beregnetPrÅr,
	                                   Double fordeltPrÅr,
	                                   Double utbetalingsgrad,
	                                   Double aktivitetsgrad) {
		BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
		if (atfl == null) {
			BeregningsgrunnlagPeriode.oppdater(periode)
					.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
							.builder()
							.medAktivitetStatus(AktivitetStatus.ATFL)
							.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(finnHøyestLedigeAndelsnr(periode), beregnetPrÅr, fordeltPrÅr, 100_000, utbetalingsgrad, aktivitetsgrad, arbeidsforhold))
							.build());
		} else {
			BeregningsgrunnlagPrStatus.builder(atfl)
					.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(finnHøyestLedigeAndelsnr(periode), beregnetPrÅr, fordeltPrÅr, 100_000, utbetalingsgrad, aktivitetsgrad, arbeidsforhold))
					.build();
		}
	}


	private void leggTilTilkommet(BeregningsgrunnlagPeriode periode,
	                              Arbeidsforhold arbeidsforhold,
	                              Double tilkommetPrÅr) {
		AktivitetStatus status;
		if (arbeidsforhold == null) {
			status = AktivitetStatus.SN;
		} else {
			status = arbeidsforhold.erFrilanser() ? AktivitetStatus.FL : AktivitetStatus.AT;
		}
		lagTilkommet(periode, tilkommetPrÅr, arbeidsforhold, status);
	}

	private void lagTilkommet(BeregningsgrunnlagPeriode periode, Double tilkommetPrÅr, Arbeidsforhold arbeidsforhold, AktivitetStatus aktivitetStatus) {
		if (tilkommetPrÅr != null) {
			var tilkommetInntekt = new TilkommetInntekt(aktivitetStatus, arbeidsforhold, BigDecimal.valueOf(tilkommetPrÅr));
			BeregningsgrunnlagPeriode.oppdater(periode).leggTilTilkommetInntektsforhold(List.of(tilkommetInntekt));
		}
	}
	private void leggTilStatus(BeregningsgrunnlagPeriode periode,
	                           AktivitetStatus status,
	                           Double beregnetPrÅr,
	                           Double utbetalingsgrad,
	                           Double aktivitetsgrad) {
		finnHøyestLedigeAndelsnr(periode);
		if (status.equals(AktivitetStatus.FL)) {
			Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
			BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
			if (atfl == null) {
				BeregningsgrunnlagPeriode.oppdater(periode)
						.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
								.builder()
								.medAktivitetStatus(AktivitetStatus.ATFL)
								.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(finnHøyestLedigeAndelsnr(periode), beregnetPrÅr, null, 0, utbetalingsgrad, aktivitetsgrad, arbeidsforhold))
								.build());
			} else {
				BeregningsgrunnlagPrStatus.builder(atfl)
						.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(finnHøyestLedigeAndelsnr(periode), beregnetPrÅr, null, 0, utbetalingsgrad, aktivitetsgrad, arbeidsforhold))
						.build();
			}
		} else if (status.equals(AktivitetStatus.SN)) {
			var bruttoPrÅr = beregnetPrÅr != null ? BigDecimal.valueOf(beregnetPrÅr) : null;
			BeregningsgrunnlagPrStatus prStatus = BeregningsgrunnlagPrStatus
					.builder()
					.medAndelNr(finnHøyestLedigeAndelsnr(periode))
					.medAktivitetStatus(AktivitetStatus.SN)
					.medBruttoPrÅr(bruttoPrÅr)
					.medInntektsgrunnlagPrÅr(bruttoPrÅr)
					.medAktivitetsgrad(aktivitetsgrad == null ? null : BigDecimal.valueOf(aktivitetsgrad))
					.medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
					.build();
			prStatus.setErSøktYtelseFor(utbetalingsgrad > 0);
			BeregningsgrunnlagPeriode.oppdater(periode)
					.medBeregningsgrunnlagPrStatus(prStatus);

		}

	}

	private BeregningsgrunnlagPrArbeidsforhold lagBeregningsgrunnlagPrArbeidsforhold(long andelsnr,
	                                                                                 Double beregnetPrÅr,
	                                                                                 Double fordeltPrÅr,
	                                                                                 double refusjonskrav,
	                                                                                 double utbetalingsgrad,
																					 Double aktivitetsgrad,
	                                                                                 Arbeidsforhold arbeidsforhold) {
		var bruttoPrÅr = finnBrutto(beregnetPrÅr, fordeltPrÅr);
		BeregningsgrunnlagPrArbeidsforhold arb = BeregningsgrunnlagPrArbeidsforhold.builder()
				.medAndelNr(andelsnr)
				.medArbeidsforhold(arbeidsforhold)
				.medInntektsgrunnlagPrÅr(beregnetPrÅr != null ? BigDecimal.valueOf(beregnetPrÅr) : null)
				.medBruttoPrÅr(bruttoPrÅr)
				.medRefusjonPrÅr(BigDecimal.valueOf(refusjonskrav))
				.medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
				.medAktivitetsgrad(aktivitetsgrad == null ? null : BigDecimal.valueOf(aktivitetsgrad))
				.build();
		arb.setErSøktYtelseFor(utbetalingsgrad > 0);
		return arb;
	}

	private BigDecimal finnBrutto(Double beregnetPrÅr, Double fordeltPrÅr) {
		if (fordeltPrÅr != null) {
			return BigDecimal.valueOf(fordeltPrÅr);
		}
		return beregnetPrÅr != null ? BigDecimal.valueOf(beregnetPrÅr) : BigDecimal.ZERO;
	}

	private static long finnHøyestLedigeAndelsnr(BeregningsgrunnlagPeriode periode) {
		return periode.getBeregningsgrunnlagPrStatus().stream()
				.mapToLong(bga -> bga.getAndelNr() != null
						? bga.getAndelNr()
						: bga.getArbeidsforhold().stream().mapToLong(BeregningsgrunnlagPrArbeidsforhold::getAndelNr).max().orElse(0L))
				.max()
				.orElse(0L);
	}


}
