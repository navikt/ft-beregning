package no.nav.folketrygdloven.beregningsgrunnlag.perioder;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGraderingImpl;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MeldekortPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

class FastsettPeriodeRegelTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final String ORGNR = "1234657";
    private static final String ORGNR2 = "89729572935";


    @Test
    void skalLageNyAndelForNyMeldekortperiodeEtterSkjæringstidspunktet() {
        PeriodeModell inputMedMeldekort = lagPeriodeInputMedEnAndelFraStart()
            .medMeldekortPerioder(List.of(new MeldekortPeriode(
                new Periode(SKJÆRINGSTIDSPUNKT.plusDays(12), SKJÆRINGSTIDSPUNKT.plusMonths(1)),
                BigDecimal.TEN,
                BigDecimal.valueOf(1),
                AktivitetStatusV2.DP)))
            .build();
        List<SplittetPeriode> perioder = FastsettPeriodeRegel.fastsett(inputMedMeldekort);
        assertThat(perioder.size()).isEqualTo(3);
        assertThat(perioder.get(1).getNyeAndeler().size()).isEqualTo(1);
        assertThat(perioder.get(2).getNyeAndeler().size()).isEqualTo(0);
    }

    @Test
    void skalLageSplittForEndretMeldekortperiodeEtterSkjæringstidspunktet() {
        PeriodeModell inputMedMeldekort = lagPeriodeInputMedEnDagpengeandelFraStart()
            .medMeldekortPerioder(List.of(
                new MeldekortPeriode(
                new Periode(SKJÆRINGSTIDSPUNKT.minusDays(2), SKJÆRINGSTIDSPUNKT.plusDays(11)),
                BigDecimal.TEN,
                BigDecimal.valueOf(0.5),
                AktivitetStatusV2.DP),
                new MeldekortPeriode(
                new Periode(SKJÆRINGSTIDSPUNKT.plusDays(12), SKJÆRINGSTIDSPUNKT.plusMonths(1)),
                BigDecimal.TEN,
                BigDecimal.valueOf(1),
                AktivitetStatusV2.DP)))
            .build();
        List<SplittetPeriode> perioder = FastsettPeriodeRegel.fastsett(inputMedMeldekort);
        assertThat(perioder.size()).isEqualTo(3);
        assertThat(perioder.get(1).getNyeAndeler().size()).isEqualTo(0);
        assertThat(perioder.get(2).getNyeAndeler().size()).isEqualTo(0);

    }


    @Test
    void skalLageNyAndelForSVPForArbeidsforholdMedSøktYtelseFraSTP() {

        Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(ORGNR2)
                .medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(12))).build();
        PeriodeModell inputMedGraderingFraStartForNyttArbeid = lagPeriodeInputMedEnAndelFraStart()
                .medEndringISøktYtelse(lagGraderingFraSkjæringstidspunkt(arbeidsforhold2))
                .build();
        List<SplittetPeriode> perioder = FastsettPeriodeRegel.fastsett(inputMedGraderingFraStartForNyttArbeid);
        assertThat(perioder.size()).isEqualTo(1);
        assertThat(perioder.get(0).getNyeAndeler().size()).isEqualTo(1);
    }

    private List<AndelGradering> lagGraderingFraSkjæringstidspunkt(Arbeidsforhold arbeidsforhold2) {
        return List.of(AndelGraderingImpl.builder().medAktivitetStatus(AktivitetStatusV2.AT).medArbeidsforhold(arbeidsforhold2).medGraderinger(List.of(new Gradering(Periode.of(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(1)), BigDecimal.valueOf(50)))).build());
    }

    private PeriodeModell.Builder lagPeriodeInputMedEnAndelFraStart() {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
                .medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(12))).medOrgnr(ORGNR).build();
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = BruttoBeregningsgrunnlag.builder().medAktivitetStatus(AktivitetStatusV2.AT).medBruttoBeregningsgrunnlag(BigDecimal.valueOf(500_000)).medArbeidsforhold(arbeidsforhold).build();
        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
                .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
                .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
                .build();
        return PeriodeModell.builder()
                .medGrunnbeløp(BigDecimal.valueOf(90_000))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
                .medEksisterendePerioder(List.of(SplittetPeriode.builder()
                        .medPeriodeÅrsaker(List.of())
                        .medFørstePeriodeAndeler(List.of(BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(arbeidsforhold).medAndelNr(1L).build()))
                        .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
                        .build()));
    }


    private PeriodeModell.Builder lagPeriodeInputMedEnDagpengeandelFraStart() {
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
            .medAnsettelsesPeriode(Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.plusMonths(12))).medOrgnr(ORGNR).build();
        BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag = BruttoBeregningsgrunnlag.builder().medAktivitetStatus(AktivitetStatusV2.AT).medBruttoBeregningsgrunnlag(BigDecimal.valueOf(500_000)).medArbeidsforhold(arbeidsforhold).build();
        BruttoBeregningsgrunnlag dagpengeGrunnlag = BruttoBeregningsgrunnlag.builder().medAktivitetStatus(AktivitetStatusV2.DP).medBruttoBeregningsgrunnlag(BigDecimal.valueOf(500_000)).build();

        PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag = PeriodisertBruttoBeregningsgrunnlag.builder()
            .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
            .leggTilBruttoBeregningsgrunnlag(bruttoBeregningsgrunnlag)
            .leggTilBruttoBeregningsgrunnlag(dagpengeGrunnlag)
            .build();
        return PeriodeModell.builder()
            .medGrunnbeløp(BigDecimal.valueOf(90_000))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medPeriodisertBruttoBeregningsgrunnlag(List.of(periodisertBruttoBeregningsgrunnlag))
            .medEksisterendePerioder(List.of(SplittetPeriode.builder()
                .medPeriodeÅrsaker(List.of())
                .medFørstePeriodeAndeler(List.of(BeregningsgrunnlagPrArbeidsforhold.builder().medArbeidsforhold(arbeidsforhold).medAndelNr(1L).build()))
                .medPeriode(Periode.of(SKJÆRINGSTIDSPUNKT, null))
                .build()));
    }
}
