package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilArbeidsforholdMedInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

public class BeregningsgrunnlagFlerePerioderTest {

    private static final String ORGNR2 = "456";
    private LocalDate skjæringstidspunkt;

    @BeforeEach
    void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    }

    @Test
    void skalBeregneGrunnlagMedToPerioder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(20000d);
        BigDecimal refusjonskrav = BigDecimal.valueOf(20000d);
        BigDecimal naturalytelse = BigDecimal.valueOf(2000d);
        LocalDate naturalytelseOpphørFom = skjæringstidspunkt.plusMonths(3);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt, refusjonskrav, naturalytelse, naturalytelseOpphørFom);
        leggTilMånedsinntekter(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt,
            Collections.singletonList(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, null);

        BeregningsgrunnlagPeriode førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPeriode.builder(førstePeriode)
            .medPeriode(Periode.of(skjæringstidspunkt, naturalytelseOpphørFom.minusDays(1)))
            .build();
        BeregningsgrunnlagPeriode andrePeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(naturalytelseOpphørFom, null))
            .build();

        kopierBeregningsgrunnlagPeriode(førstePeriode, andrePeriode);

        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medBeregningsgrunnlagPeriode(andrePeriode)
            .build();

        // Act
	    var resultat = new RegelForeslåBeregningsgrunnlag(førstePeriode).evaluerRegel(førstePeriode);
	    new RegelForeslåBeregningsgrunnlag(andrePeriode).evaluerRegel(andrePeriode);
        // Assert
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(førstePeriode, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isZero();

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(andrePeriode, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        assertThat(andrePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualTo(naturalytelse.multiply(BigDecimal.valueOf(12)));

        verifiserRegelresultat(beregningsgrunnlag, resultat);
    }

    @Test
    void skalBeregneGrunnlagMedTrePerioder() {
        // Arrange
        BigDecimal månedsinntekt1 = BigDecimal.valueOf(20000);
        BigDecimal refusjonskrav1 = BigDecimal.valueOf(20000);
        BigDecimal månedsinntekt2 = BigDecimal.valueOf(10000);
        BigDecimal refusjonskrav2 = BigDecimal.valueOf(10000);
        BigDecimal naturalytelse1 = BigDecimal.valueOf(2000);
        BigDecimal naturalytelse2 = BigDecimal.valueOf(500);
        final BigDecimal tolv = BigDecimal.valueOf(12);
        BigDecimal månedsinntekt = månedsinntekt1.add(månedsinntekt2);
        LocalDate naturalytelseOpphørFom1 = skjæringstidspunkt.plusMonths(3);
        LocalDate naturalytelseOpphørFom2 = skjæringstidspunkt.plusMonths(5);

        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt1, refusjonskrav1, naturalytelse1, naturalytelseOpphørFom1);
        LocalDate startdatoArbeidsforhold = skjæringstidspunkt.minusYears(2);
		Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(startdatoArbeidsforhold, ORGNR2);

        BeregningsgrunnlagPeriode periode1 = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        leggTilArbeidsforholdMedInntektsmelding(periode1, skjæringstidspunkt, månedsinntekt2, refusjonskrav2, arbeidsforhold2, naturalytelse2, naturalytelseOpphørFom2);
        leggTilMånedsinntekter(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt,
            Collections.singletonList(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, null);

        BeregningsgrunnlagPeriode.builder(periode1)
            .medPeriode(Periode.of(skjæringstidspunkt, naturalytelseOpphørFom1.minusDays(1)))
            .build();
        BeregningsgrunnlagPeriode periode2 = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(naturalytelseOpphørFom1, naturalytelseOpphørFom2.minusDays(1)))
            .build();

        kopierBeregningsgrunnlagPeriode(periode1, periode2);

        BeregningsgrunnlagPeriode periode3 = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(naturalytelseOpphørFom2, null))
            .build();

        kopierBeregningsgrunnlagPeriode(periode1, periode3);

        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medBeregningsgrunnlagPeriode(periode2)
            .medBeregningsgrunnlagPeriode(periode3)
            .build();

        // Act
	    var resultat = new RegelForeslåBeregningsgrunnlag(periode1).evaluerRegel(periode1);
	    new RegelForeslåBeregningsgrunnlag(periode2).evaluerRegel(periode2);
	    new RegelForeslåBeregningsgrunnlag(periode3).evaluerRegel(periode3);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(periode1, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(periode2, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(periode3, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        assertThat(periode1.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isZero();
        assertThat(periode2.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualTo(naturalytelse1.multiply(tolv));
        assertThat(periode3.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualTo(naturalytelse1.add(naturalytelse2).multiply(tolv));

        verifiserRegelresultat(beregningsgrunnlag, resultat);


    }

    private void verifiserRegelresultat(Beregningsgrunnlag beregningsgrunnlag, RegelResultat resultat) {
        assertThat(resultat.getBeregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
	    assertThat(beregningsgrunnlag.getSammenligningsgrunnlagForStatus(SammenligningGrunnlagType.AT_FL).orElseThrow().getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(11000));
    }

    private void kopierBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagPeriode kopi) {
        for (BeregningsgrunnlagPrStatus forrigeStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (forrigeStatus.erArbeidstakerEllerFrilanser()) {
                BeregningsgrunnlagPrStatus ny = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .build();
                for (BeregningsgrunnlagPrArbeidsforhold kopierFraArbeidsforhold : forrigeStatus.getArbeidsforhold()) {
                    BeregningsgrunnlagPrArbeidsforhold kopiertArbeidsforhold = BeregningsgrunnlagPrArbeidsforhold.builder()
                        .medArbeidsforhold(kopierFraArbeidsforhold.getArbeidsforhold())
                        .medAndelNr(kopierFraArbeidsforhold.getAndelNr())
                        .build();
                    BeregningsgrunnlagPrStatus.builder(ny).medArbeidsforhold(kopiertArbeidsforhold).build();

                }
            }
        }
    }
}
