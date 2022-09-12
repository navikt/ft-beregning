package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2019;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnPeriode;
import no.nav.fpsak.nare.ServiceArgument;

class BeregnPrArbeidsforholdFraAOrdningenFRISINNTest {
    private static final LocalDate STP = LocalDate.of(2020,3,1);
    private static final Arbeidsforhold ARBFOR_MED_REF = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999", "ARBFOR-REF");
    private static final Arbeidsforhold ARBFOR_UTEN_REF = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("999999999");
    private static final Arbeidsforhold ARBFOR_FL = Arbeidsforhold.frilansArbeidsforhold();
    public static final Arbeidsforhold FRILANS_ARBEIDSFORHOLD = Arbeidsforhold.frilansArbeidsforhold();

    private static Inntektsgrunnlag inntektsgrunnlag;

    @BeforeEach
    void setup() {
        inntektsgrunnlag = new Inntektsgrunnlag();
    }

    @Test
    public void nyoppstartet_fl_uten_ytelse() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(FRILANS_ARBEIDSFORHOLD).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,7,1), LocalDate.of(2019,7,31)), FRILANS_ARBEIDSFORHOLD, 8));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        List<FrisinnPeriode> frisinnPerioder = Collections.singletonList(new FrisinnPeriode(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode(),
            true, false));

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(frisinnPerioder, List.of(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode()), STP))
            .build(), andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(12);
    }

    @Test
    public void nyoppstartet_fl_uten_ytelse_inntekt_første_mars_2019() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(FRILANS_ARBEIDSFORHOLD).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,3,1), LocalDate.of(2019,3,1)), FRILANS_ARBEIDSFORHOLD, 12));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        List<FrisinnPeriode> frisinnPerioder = Collections.singletonList(new FrisinnPeriode(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode(),
            true, false));

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(frisinnPerioder, List.of(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode()), STP))
            .build(), andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(12);
    }

    @Test
    public void nyoppstartet_fl_uten_ytelse_ingen_inntekt_før_stp() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(FRILANS_ARBEIDSFORHOLD).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,3,1), LocalDate.of(2020,3,31)), FRILANS_ARBEIDSFORHOLD, 12));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        List<FrisinnPeriode> frisinnPerioder = Collections.singletonList(new FrisinnPeriode(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode(),
            true, false));

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(frisinnPerioder, List.of(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode()), STP))
            .build(), andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(0);
    }

    @Test
    public void nyoppstartet_fl_med_ytelse() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(FRILANS_ARBEIDSFORHOLD).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,3,1), LocalDate.of(2019,3,31)), FRILANS_ARBEIDSFORHOLD, 10));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,3,31))));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);
        List<FrisinnPeriode> frisinnPerioder = Collections.singletonList(new FrisinnPeriode(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode(),
            true, false));

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(frisinnPerioder, List.of(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode()), STP))
            .build(), andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(12);
    }

    @Test
    public void kun_inntekt_før_stp_ingen_ytelser() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,12,1), LocalDate.of(2019,12,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,11,1), LocalDate.of(2019,11,30)), ARBFOR_MED_REF, 1));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(4), Percentage.withPercentage(0.001));
    }

    @Test
    public void skal_ignorere_inntekt_fra_frilans() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31)), ARBFOR_FL, 100_000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,12,1), LocalDate.of(2019,12,31)), ARBFOR_FL, 100_000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,11,1), LocalDate.of(2019,11,30)), ARBFOR_FL, 100_000));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(1), Percentage.withPercentage(0.001));
    }


    @Test
    public void skal_ignorere_inntekt_etter_stp() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,3,1), LocalDate.of(2020,3,31)), ARBFOR_MED_REF, 100_000)); // Etter STP, skal ignoreres
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,12,1), LocalDate.of(2019,12,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,11,1), LocalDate.of(2019,11,30)), ARBFOR_MED_REF, 1));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(4), Percentage.withPercentage(0.001));
    }

    @Test
    public void skal_ikke_matche_på_inntekt_som_ikke_har_arbfor_ref() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_UTEN_REF, 100000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31)), ARBFOR_UTEN_REF, 100000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,12,1), LocalDate.of(2019,12,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,11,1), LocalDate.of(2019,11,30)), ARBFOR_MED_REF, 1));
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(2), Percentage.withPercentage(0.001));
    }

    @Test
    public void skal_ikke_ta_med_inntekt_som_overlapper_med_ytelser() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,15), LocalDate.of(2020,2,15)), ARBFOR_MED_REF, 100000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,12,1), LocalDate.of(2019,12,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,11,1), LocalDate.of(2019,11,30)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,10,1), LocalDate.of(2019,10,31)), ARBFOR_MED_REF, 1));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(4.363636), Percentage.withPercentage(0.001));
    }

    @Test
    public void skal_ta_inntekt_langt_tilbake_hvis_alle_nylige_måneder_har_hatt_ytelse() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_MED_REF).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019,1,1), LocalDate.of(2019,12,31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,12,1), LocalDate.of(2019,12,31)), ARBFOR_MED_REF, 100000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,11,1), LocalDate.of(2019,11,30)), ARBFOR_MED_REF, 100000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,12,1), LocalDate.of(2018,12,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,11,1), LocalDate.of(2018,11,30)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,10,1), LocalDate.of(2018,10,31)), ARBFOR_MED_REF, 1));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,9,1), LocalDate.of(2018,9,30)), ARBFOR_MED_REF, 1));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(12);
    }

    @Test
    public void skal_ta_inntekt_langt_tilbake_hvis_alle_nylige_måneder_har_hatt_ytelse_frilans() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_FL).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_FL, 2));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31)), ARBFOR_FL, 2));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019,1,1), LocalDate.of(2019,12,31))));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,12,1), LocalDate.of(2019,12,31)), ARBFOR_FL, 100000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,11,1), LocalDate.of(2019,11,30)), ARBFOR_FL, 100000));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,12,1), LocalDate.of(2018,12,31)), ARBFOR_FL, 2));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,11,1), LocalDate.of(2018,11,30)), ARBFOR_FL, 2));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,10,1), LocalDate.of(2018,10,31)), ARBFOR_FL, 2));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,9,1), LocalDate.of(2018,9,30)), ARBFOR_FL, 2));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, true, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(24);
    }

    @Test
    public void skal_teste_at_nyoppstartet_fl_ikke_bruker_helt_år_som_snitt() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_FL).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_FL, 6));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31)), ARBFOR_FL, 6));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, true, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(72);
    }

    @Test
    public void skal_teste_at_nyoppstartet_fl_beregnes_vanlig_når_det_finne_eldre_frilansinntekter() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_FL).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_FL, 6));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,1,1), LocalDate.of(2020,1,31)), ARBFOR_FL, 6));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,1,1), LocalDate.of(2019,1,31)), ARBFOR_FL, 6));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, true, STP);

        // Act
        kjørRegel(beregningsgrunnlag, andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(12);
    }

    @Test
    public void arbeid_med_ytelse_siste_9_mnd() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(ARBFOR_UTEN_REF).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,5,1), LocalDate.of(2019,5,31)), ARBFOR_UTEN_REF, 10));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,4,1), LocalDate.of(2019,4,30)), ARBFOR_UTEN_REF, 10));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,3,1), LocalDate.of(2019,3,31)), ARBFOR_UTEN_REF, 10));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,2,1), LocalDate.of(2019,2,28)), ARBFOR_UTEN_REF, 10));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,1,1), LocalDate.of(2019,1,31)), ARBFOR_UTEN_REF, 10));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2018,12,1), LocalDate.of(2018,12,31)), ARBFOR_UTEN_REF, 10));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2019,6,1), LocalDate.of(2020,3,31))));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, LocalDate.of(2019,6,1));
        List<FrisinnPeriode> frisinnPerioder = Collections.singletonList(new FrisinnPeriode(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode(),
            true, false));

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(frisinnPerioder, List.of(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode()), STP))
            .build(), andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(120);
    }


    @Test
    public void skal_bruke_oppgitt_inntekt_om_fastsatt_manuelt() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder().medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(BigDecimal.valueOf(20_000))
            .medArbeidsforhold(ARBFOR_UTEN_REF).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_UTEN_REF, 10));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);
        List<FrisinnPeriode> frisinnPerioder = Collections.singletonList(new FrisinnPeriode(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode(),
            true, false));

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(frisinnPerioder, List.of(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode()), STP))
            .build(), andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(20_000);
    }

    @Test
    public void skal_håndtere_frilans_uten_inntekt() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(FRILANS_ARBEIDSFORHOLD).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2020,2,1), LocalDate.of(2020,2,29)), ARBFOR_UTEN_REF, 10));

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);
        List<FrisinnPeriode> frisinnPerioder = Collections.singletonList(new FrisinnPeriode(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode(),
            true, false));

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(frisinnPerioder, List.of(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode()), STP))
            .build(), andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(0);
    }

    @Test
    public void skal_håndtere_nyoppstartet_frilans_med_ytelse_siste_24_måneder() {
        // Arrange
        BeregningsgrunnlagPrArbeidsforhold andel = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(FRILANS_ARBEIDSFORHOLD).medAndelNr(1L).build();
        inntektsgrunnlag.leggTilPeriodeinntekt(byggInntekt(Periode.of(LocalDate.of(2019,3,1), LocalDate.of(2019,3,31)), FRILANS_ARBEIDSFORHOLD, 24));
        inntektsgrunnlag.leggTilPeriodeinntekt(byggYtelse(Periode.of(LocalDate.of(2018,3,1), LocalDate.of(2020,3,31))));


        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag(inntektsgrunnlag, STP);
        List<FrisinnPeriode> frisinnPerioder = Collections.singletonList(new FrisinnPeriode(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode(),
            true, false));

        // Act
        kjørRegel(Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(frisinnPerioder, List.of(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPeriode()), STP))
            .build(), andel);

        // Assert
        assertThat(andel.getBeregnetPrÅr().intValue()).isEqualTo(24);
    }

    private Periodeinntekt byggYtelse(Periode periode) {
        return Periodeinntekt.builder()
            .medPeriode(periode)
            .medInntekt(BigDecimal.ONE)
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .build();
    }

    private Periodeinntekt byggInntekt(Periode periode, Arbeidsforhold arbeidsforhold, int beløp) {
        return Periodeinntekt.builder()
            .medPeriode(periode)
            .medArbeidsgiver(arbeidsforhold)
            .medInntekt(BigDecimal.valueOf(beløp))
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
            .build();
    }

    private void kjørRegel(Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagPrArbeidsforhold andel) {
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        new BeregnPrArbeidsforholdFraAOrdningenFRISINN().evaluate(grunnlag, new ServiceArgument("arbeidsforhold", andel));
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(Inntektsgrunnlag ig, LocalDate skjæringstidspunkt) {
        return lagBeregningsgrunnlag(ig, false, skjæringstidspunkt);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(Inntektsgrunnlag ig, boolean erNyoppstartetFL, LocalDate skjæringstidspunkt) {
        Periode interval = new Periode(STP, null);
        BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(interval);
        BeregningsgrunnlagPeriode periode = periodeBuilder.build();
        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(interval, erNyoppstartetFL, false);
        FrisinnGrunnlag frisinnGrunnlag = new FrisinnGrunnlag(Collections.singletonList(frisinnPeriode), List.of(interval), STP);
        return Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(ig)
            .medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2019))
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medAntallGMinstekravVilkår(BigDecimal.valueOf(0.75))
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL_SN, null)))
            .medBeregningsgrunnlagPeriode(periode)
            .medYtelsesSpesifiktGrunnlag(frisinnGrunnlag)
            .build();
    }
}
