package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

public class MapRefusjonskravFraVLTilRegelTest {

    public static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet("1234786124");
    public static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet("09872335");
    public BeregningRefusjonOverstyringerDto.Builder refusjonOverstyringer = BeregningRefusjonOverstyringerDto.builder();

    @Test
    public void refusjonFraSenereDato() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate endringFom = skjæringstidspunkt.plusMonths(1);
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.ZERO)
                .leggTil(new RefusjonDto(Beløp.fra(11), endringFom))
                .build();

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet,
                skjæringstidspunkt,
                Optional.empty(),
                List.of(Intervall.fraOgMed(skjæringstidspunkt.minusMonths(12))));

        // Assert
        assertThat(resultat).hasSize(2);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt, endringFom.minusDays(1)));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        });
        assertThat(resultat).anySatisfy(endring -> {
            assertThat(endring.getPeriode()).isEqualTo(Periode.of(endringFom, null));
            assertThat(endring.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(11));
        });
    }

    @Test
    public void refusjonFraSenereDatoUnderGyldigGrense() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate endringFom = skjæringstidspunkt.plusMonths(1);
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.ZERO)
                .leggTil(new RefusjonDto(Beløp.fra(10), endringFom))
                .build();

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet,
                skjæringstidspunkt,
                Optional.empty(),
                List.of(Intervall.fraOgMed(skjæringstidspunkt.minusMonths(12))));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt, null));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }


    @Test
    public void refusjonFraStpUnderGyldigGrense() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(10))
                .build();

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet,
                skjæringstidspunkt,
                Optional.empty(),
                List.of(Intervall.fraOgMed(skjæringstidspunkt.minusMonths(12))));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt, null));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }



    @Test
    public void skal_finne_refusjonskrav_på_stp_med_uten_refusjon_fra_start() {
        // Arrange
        LocalDate idag = LocalDate.now();
        InntektsmeldingAggregatDtoBuilder inntektsmeldingAggregatDtoBuilder = InntektsmeldingAggregatDtoBuilder.ny();
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medRefusjon(Beløp.ZERO)
                .leggTil(new RefusjonDto(Beløp.fra(10000), idag.plusMonths(1)));
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder2 = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder2.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjon(Beløp.ZERO)
                .leggTil(new RefusjonDto(Beløp.fra(10000), idag.plusMonths(1)));

        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder.build());
        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder2.build());
        InntektsmeldingAggregatDto inntektsmeldingAggregatDto = inntektsmeldingAggregatDtoBuilder.build();

        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(
                new UtbetalingsgradPrAktivitetDto(
                        new AktivitetDto(ARBEIDSGIVER1, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(idag), Utbetalingsgrad.valueOf(100)))),
                new UtbetalingsgradPrAktivitetDto(
                        new AktivitetDto(ARBEIDSGIVER2, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(idag), Utbetalingsgrad.valueOf(50))))), List.of());

        // Act
        var refusjonPåStp = MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet(inntektsmeldingAggregatDto.getInntektsmeldingerSomSkalBrukes(), idag, omsorgspengerGrunnlag);

        // Assert
        assertThat(refusjonPåStp).isEqualByComparingTo(Beløp.ZERO);
    }


    @Test
    public void skal_finne_refusjonskrav_på_stp_med_endring_i_refusjonskrav() {
        // Arrange
        LocalDate idag = LocalDate.now();
        InntektsmeldingAggregatDtoBuilder inntektsmeldingAggregatDtoBuilder = InntektsmeldingAggregatDtoBuilder.ny();
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medRefusjon(Beløp.fra(12000))
                .leggTil(new RefusjonDto(Beløp.fra(10000), idag.plusMonths(1)));
        InntektsmeldingDtoBuilder inntektsmeldingDtoBuilder2 = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingDtoBuilder2.medStartDatoPermisjon(idag)
                .medArbeidsgiver(ARBEIDSGIVER2)
                .medRefusjon(Beløp.fra(12000))
                .leggTil(new RefusjonDto(Beløp.fra(10000), idag.plusMonths(1)));

        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder.build());
        inntektsmeldingAggregatDtoBuilder.leggTil(inntektsmeldingDtoBuilder2.build());
        InntektsmeldingAggregatDto inntektsmeldingAggregatDto = inntektsmeldingAggregatDtoBuilder.build();

        OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(List.of(
                new UtbetalingsgradPrAktivitetDto(
                        new AktivitetDto(ARBEIDSGIVER1, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(idag), Utbetalingsgrad.valueOf(100)))),
                new UtbetalingsgradPrAktivitetDto(
                        new AktivitetDto(ARBEIDSGIVER2, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.ORDINÆRT_ARBEID),
                        List.of(new PeriodeMedUtbetalingsgradDto(Intervall.fraOgMed(idag), Utbetalingsgrad.valueOf(50))))), List.of());

        // Act
        var refusjonPåStp = MapRefusjonskravFraVLTilRegel.finnGradertRefusjonskravPåSkjæringstidspunktet(inntektsmeldingAggregatDto.getInntektsmeldingerSomSkalBrukes(), idag, omsorgspengerGrunnlag);

        // Assert
        assertThat(refusjonPåStp).isEqualByComparingTo(Beløp.fra(216_000));
    }

    @Test
    public void skal_bruke_overstyrt_dato_om_denne_finnes_og_matcher_arbeidsforholdet() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        LocalDate overstyrtDato = skjæringstidspunkt.plusDays(15);
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(11))
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidsforholdId(ref)
                .build();
        lagRefusjonoverstyring(ARBEIDSGIVER1, ref, overstyrtDato);

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet,
                skjæringstidspunkt,
                Optional.of(refusjonOverstyringer.build()),
                List.of(Intervall.fraOgMed(skjæringstidspunkt.minusMonths(12))));

        // Assert
        assertThat(resultat).hasSize(2);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(overstyrtDato, TIDENES_ENDE));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(11));
        });
    }

    @Test
    public void skal_ikke_bruke_overstyrt_dato_om_denne_finnes_og_ikke_matcher_arbeidsforholdet() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        InternArbeidsforholdRefDto refIM = InternArbeidsforholdRefDto.nyRef();
        InternArbeidsforholdRefDto refOverstyring = InternArbeidsforholdRefDto.nyRef();
        LocalDate overstyrtDato = skjæringstidspunkt.plusDays(15);
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(11))
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidsforholdId(refIM)
                .build();
        lagRefusjonoverstyring(ARBEIDSGIVER1, refOverstyring, overstyrtDato);

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet,
                skjæringstidspunkt, Optional.of(refusjonOverstyringer.build()),
                List.of(Intervall.fraOgMed(skjæringstidspunkt.minusMonths(12))));

        // Assert
        assertThat(resultat).hasSize(1);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt, TIDENES_ENDE));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(11));
        });
    }


    @Test
    public void skal_finne_refusjonskrav_for_perioder_med_opphør_og_restart() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.now();
        LocalDate endringFom = skjæringstidspunkt.plusMonths(1);
        InntektsmeldingDto inntektsmeldingEntitet = InntektsmeldingDtoBuilder.builder()
                .medRefusjon(Beløp.fra(15))
                .leggTil(new RefusjonDto(Beløp.fra(11), endringFom))
                .build();

        // Act
        List<Refusjonskrav> resultat = MapRefusjonskravFraVLTilRegel.periodiserRefusjonsbeløp(inntektsmeldingEntitet,
                skjæringstidspunkt,
                Optional.empty(),
                List.of(
                        Intervall.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(12), skjæringstidspunkt.plusWeeks(2)),
                        Intervall.fraOgMed(skjæringstidspunkt.plusMonths(2))
                ));

        // Assert
        assertThat(resultat).hasSize(3);
        assertThat(resultat).anySatisfy(start -> {
            assertThat(start.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt, skjæringstidspunkt.plusWeeks(2)));
            assertThat(start.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(15));
        });
        assertThat(resultat).anySatisfy(endring -> {
            assertThat(endring.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt.plusWeeks(2).plusDays(1), skjæringstidspunkt.plusMonths(2).minusDays(1)));
            assertThat(endring.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        });
        assertThat(resultat).anySatisfy(endring -> {
            assertThat(endring.getPeriode()).isEqualTo(Periode.of(skjæringstidspunkt.plusMonths(2), null));
            assertThat(endring.getMånedsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(11));
        });
    }


    private void lagRefusjonoverstyring(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, LocalDate dato) {
        BeregningRefusjonPeriodeDto periodeOverstyring = new BeregningRefusjonPeriodeDto(ref, dato);
        BeregningRefusjonOverstyringDto overstyring = new BeregningRefusjonOverstyringDto(ag, null, Collections.singletonList(periodeOverstyring), null);
        refusjonOverstyringer.leggTilOverstyring(overstyring);
    }

}
