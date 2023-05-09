package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

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

public class RegelFinnGrenseverdiTest {

	public static final String ORGNR = "910";
	private static final String ORGNR_2 = "974760673";
	private static final String ORGNR_3 = "976967631";

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 0);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, null, tilkommetPrÅr2, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, null, tilkommetPrÅr2, 100);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void to_arbeidsforhold_under_6G_fordelt_og_tilkommet_inntekt_i_det_ene() {
		//Arrange
		double beregnetPrÅr = 250_000;
		double fordeltPrÅr = 150_000;

		double tilkommetPrÅr2 = 100_000;
		double fordeltPrÅr2 = tilkommetPrÅr2;

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, fordeltPrÅr, null,100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, null, fordeltPrÅr2, tilkommetPrÅr2, 50);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(150_000));
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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);
		leggTilArbeidsforhold(periode, 3L, ORGNR_3, null, tilkommetPrÅr, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);
		leggTilArbeidsforhold(periode, 3L, ORGNR_3, null, tilkommetPrÅr, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);
		leggTilArbeidsforhold(periode, 3L, ORGNR_3, null,tilkommetPrÅr, 0);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 20);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);
		leggTilArbeidsforhold(periode, 3L, ORGNR_3, null,tilkommetPrÅr, 0);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 50);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);

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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 0);
		leggTilArbeidsforhold(periode, 3L, ORGNR_3, beregnetPrÅr3, 0);


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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);
		leggTilArbeidsforhold(periode, 3L, ORGNR_3, beregnetPrÅr3, 0);


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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 50);
		leggTilArbeidsforhold(periode, 3L, ORGNR_3, beregnetPrÅr3, 0);


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

		leggTilArbeidsforhold(periode, 1L, ORGNR, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR_2, beregnetPrÅr2, 100);
		leggTilArbeidsforhold(periode, 3L, ORGNR_3, beregnetPrÅr3, 100);


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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 0);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 50);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 50);


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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

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

		leggTilFrilans(periode, 1L, null, tilkommetFrilansinntekt, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, tilkommetFrilansinntekt,100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, tilkommetFrilansinntekt, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 0);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 0);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

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

		leggTilFrilans(periode, 1L, beregnetPrÅr, 0);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 50);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, tilkommet, 50);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 0);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 100);
		leggTilFrilans(periode, 2L, beregnetPrÅr, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 100);
		leggTilFrilans(periode, 2L, beregnetPrÅr, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, tilkommet, 100);
		leggTilFrilans(periode, 2L, beregnetPrÅr, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 100);
		leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 100);
		leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 0);
		leggTilFrilans(periode, 2L, beregnetPrÅr2, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 100);
		leggTilFrilans(periode, 2L, beregnetPrÅr, 100);
		leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 100);
		leggTilFrilans(periode, 2L, beregnetPrÅr, 0);
		leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr, 0);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 0);
		leggTilFrilans(periode, 2L, beregnetPrÅr, 0);
		leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 0);
		leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);
		leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr3, 100);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 100);
		leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);
		leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr3, 0);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 50);
		leggTilFrilans(periode, 2L, beregnetPrÅr2, 0);
		leggTilArbeidsforhold(periode, 3L, ORGNR, beregnetPrÅr3, 0);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 50);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

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

		leggTilNæring(periode, 1L, beregnetPrÅr, 50);
		leggTilArbeidsforhold(periode, 2L, ORGNR, beregnetPrÅr2, 0);

		//Act
		kjørRegel(periode);

		assertThat(periode.getGrenseverdi()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}

	private RegelResultat kjørRegel(BeregningsgrunnlagPeriode periode) {
		return new RegelFinnGrenseverdi(periode).evaluerRegel(periode);
	}


	private void leggTilArbeidsforhold(BeregningsgrunnlagPeriode periode,
	                                   long andelsnr,
	                                   String orgnr,
	                                   double beregnetPrÅr,
	                                   double utbetalingsgrad) {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
		BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

		if (atfl == null) {
			BeregningsgrunnlagPeriode.oppdater(periode)
					.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
							.builder()
							.medAktivitetStatus(AktivitetStatus.ATFL)
							.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, null, 100_000, utbetalingsgrad, arbeidsforhold))
							.build());
		} else {
			BeregningsgrunnlagPrStatus.builder(atfl)
					.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, null, 100_000, utbetalingsgrad, arbeidsforhold))
					.build();
		}
	}


	private void leggTilArbeidsforhold(BeregningsgrunnlagPeriode periode,
	                                   long andelsnr,
	                                   String orgnr,
	                                   Double beregnetPrÅr,
									   Double tilkommetPrÅr,
	                                   double utbetalingsgrad) {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
		BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
		leggTilTilkommet(periode, tilkommetPrÅr, arbeidsforhold, AktivitetStatus.AT);
		if (atfl == null) {
			BeregningsgrunnlagPeriode.oppdater(periode)
					.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
							.builder()
							.medAktivitetStatus(AktivitetStatus.ATFL)
							.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, null, 100_000, utbetalingsgrad, arbeidsforhold))
							.build());
		} else {
			BeregningsgrunnlagPrStatus.builder(atfl)
					.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, null, 100_000, utbetalingsgrad, arbeidsforhold))
					.build();
		}
	}

	private void leggTilTilkommet(BeregningsgrunnlagPeriode periode, Double tilkommetPrÅr, Arbeidsforhold arbeidsforhold, AktivitetStatus aktivitetStatus) {
		if (tilkommetPrÅr != null) {
			var tilkommetInntekt = new TilkommetInntekt(aktivitetStatus, arbeidsforhold, BigDecimal.valueOf(tilkommetPrÅr));
			BeregningsgrunnlagPeriode.oppdater(periode).leggTilTilkommetInntektsforhold(List.of(tilkommetInntekt));
		}
	}

	private void leggTilArbeidsforhold(BeregningsgrunnlagPeriode periode,
	                                   long andelsnr,
	                                   String orgnr,
	                                   Double beregnetPrÅr,
									   double fordeltPrÅr,
	                                   Double tilkommetPrÅr,
	                                   double utbetalingsgrad) {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
		BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

		var beregningsgrunnlagPrArbeidsforhold = lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, fordeltPrÅr, 100_000, utbetalingsgrad, arbeidsforhold);
		leggTilTilkommet(periode, tilkommetPrÅr, arbeidsforhold, AktivitetStatus.AT);
		if (atfl == null) {
			BeregningsgrunnlagPeriode.oppdater(periode)
					.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
							.builder()
							.medAktivitetStatus(AktivitetStatus.ATFL)
							.medArbeidsforhold(beregningsgrunnlagPrArbeidsforhold)
							.build());
		} else {
			BeregningsgrunnlagPrStatus.builder(atfl)
					.medArbeidsforhold(beregningsgrunnlagPrArbeidsforhold)
					.build();
		}
	}

	private void leggTilFrilans(BeregningsgrunnlagPeriode periode,
	                            long andelsnr,
	                            double beregnetPrÅr, double utbetalingsgrad) {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
		BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

		if (atfl == null) {
			BeregningsgrunnlagPeriode.oppdater(periode)
					.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
							.builder()
							.medAktivitetStatus(AktivitetStatus.ATFL)
							.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, null, 0, utbetalingsgrad, arbeidsforhold))
							.build());
		} else {
			BeregningsgrunnlagPrStatus.builder(atfl)
					.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, null, 0, utbetalingsgrad, arbeidsforhold))
					.build();
		}
	}

	private void leggTilFrilans(BeregningsgrunnlagPeriode periode,
	                            long andelsnr,
	                            Double beregnetPrÅr,
								Double tilkommetPrÅr,
	                            double utbetalingsgrad) {
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
		BeregningsgrunnlagPrStatus atfl = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
		leggTilTilkommet(periode, tilkommetPrÅr, arbeidsforhold, AktivitetStatus.FL);
		if (atfl == null) {
			BeregningsgrunnlagPeriode.oppdater(periode)
					.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
							.builder()
							.medAktivitetStatus(AktivitetStatus.ATFL)
							.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, null, 0, utbetalingsgrad, arbeidsforhold))
							.build());
		} else {
			BeregningsgrunnlagPrStatus.builder(atfl)
					.medArbeidsforhold(lagBeregningsgrunnlagPrArbeidsforhold(andelsnr, beregnetPrÅr, null, 0, utbetalingsgrad, arbeidsforhold))
					.build();
		}
	}

	private void leggTilNæring(BeregningsgrunnlagPeriode periode,
	                           long andelsnr,
	                           double beregnetPrÅr, double utbetalingsgrad) {
		BeregningsgrunnlagPrStatus status = BeregningsgrunnlagPrStatus
				.builder()
				.medAndelNr(andelsnr)
				.medAktivitetStatus(AktivitetStatus.SN)
				.medBruttoPrÅr(BigDecimal.valueOf(beregnetPrÅr))
				.medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
				.build();
		status.setErSøktYtelseFor(utbetalingsgrad > 0);
		BeregningsgrunnlagPeriode.oppdater(periode)
				.medBeregningsgrunnlagPrStatus(status);
	}

	private void leggTilNæring(BeregningsgrunnlagPeriode periode,
	                           long andelsnr,
	                           Double beregnetPrÅr,
	                           double tilkommet,
	                           double utbetalingsgrad) {
		var bruttoPrÅr = beregnetPrÅr != null ? BigDecimal.valueOf(beregnetPrÅr) : null;
		BeregningsgrunnlagPrStatus status = BeregningsgrunnlagPrStatus
				.builder()
				.medAndelNr(andelsnr)
				.medAktivitetStatus(AktivitetStatus.SN)
				.medBruttoPrÅr(bruttoPrÅr)
				.medInntektsgrunnlagPrÅr(bruttoPrÅr)
				.medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
				.build();
		status.setErSøktYtelseFor(utbetalingsgrad > 0);
		BeregningsgrunnlagPeriode.oppdater(periode)
				.leggTilTilkommetInntektsforhold(List.of(new TilkommetInntekt(AktivitetStatus.SN, null, BigDecimal.valueOf(tilkommet))))
				.medBeregningsgrunnlagPrStatus(status);
	}

	private BeregningsgrunnlagPrArbeidsforhold lagBeregningsgrunnlagPrArbeidsforhold(long andelsnr,
	                                                                                 Double beregnetPrÅr,
	                                                                                 Double fordeltPrÅr,
	                                                                                 double refusjonskrav,
	                                                                                 double utbetalingsgrad,
	                                                                                 Arbeidsforhold arbeidsforhold) {
		var bruttoPrÅr = finnBrutto(beregnetPrÅr, fordeltPrÅr);
		BeregningsgrunnlagPrArbeidsforhold arb = BeregningsgrunnlagPrArbeidsforhold.builder()
				.medAndelNr(andelsnr)
				.medArbeidsforhold(arbeidsforhold)
				.medInntektsgrunnlagPrÅr(beregnetPrÅr != null ? BigDecimal.valueOf(beregnetPrÅr) : null)
				.medBruttoPrÅr(bruttoPrÅr)
				.medRefusjonPrÅr(BigDecimal.valueOf(refusjonskrav))
				.medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
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


}
