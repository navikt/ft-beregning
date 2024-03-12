package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import static no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType.ARBEID;
import static no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType.VENTELØNN_VARTPENGER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType.ARBEIDSAVKLARINGSPENGER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.utils.Tuple;


public class AvklarAktiviteterTjenesteImplTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.of(2018, 9, 30);
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("900050001");

    @Test
    public void skal_returnere_false_om_ingen_aktiviteter() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isFalse();
    }

    @Test
    public void skal_returnere_false_om_aktiviteter_som_ikke_er_ventelønn_vartpenger() {
        // Arrange
        BeregningAktivitetDto arbeidAktivitet = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEID);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(
                arbeidAktivitet
            ).build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isFalse();
    }

    @Test
    public void skal_returnere_false_om_ventelønn_vertpenger_ikke_er_siste_aktivitet() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isFalse();
    }

    @Test
    public void skal_returnere_true_om_ventelønn_vertpenger_avslutter_samtidig_med_siste_aktivitet() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING, ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING, VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isTrue();
    }

    @Test
    public void skal_returnere_true_om_ventelønn_vartpenger_avslutter_etter_arbeidsaktivitet_som_slutter_dagen_før_skjæringstidspunkt() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1), VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isTrue();
    }

    @Test
    public void skal_returnere_true_om_ventelønn_vartpenger_sammen_med_arbeid_som_starter_på_skjæringstidspunkt() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10), ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1), VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isTrue();
    }

    @Test
    public void skal_returnere_false_om_ventelønn_vartpenger_starter_på_skjæringstidspunkt() {
        // Arrange
        BeregningAktivitetDto arbeidsperiode = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(10), ARBEID);
        BeregningAktivitetDto ventelønnVartpenger = lagBeregningAktivitetAggregat(SKJÆRINGSTIDSPUNKT_BEREGNING,
            SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1), VENTELØNN_VARTPENGER);
        BeregningAktivitetAggregatDto beregningAktivitetAggregat = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .leggTilAktivitet(arbeidsperiode)
            .leggTilAktivitet(ventelønnVartpenger)
            .build();

        // Act
        boolean harVentelønnEllerVartpenger = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat);

        // Assert
        assertThat(harVentelønnEllerVartpenger).isFalse();
    }

    @Test
    public void skal_returnere_false_når_ikke_AAP() {
        //Arrange
        var aktivitetAggregat = lagBeregningAktivitetAggregatMedType(ARBEID);

        //Act
        boolean harFullAAPMedAndreAktiviteter = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harFullAAPITilleggTilAnnenAktivitet(aktivitetAggregat, Optional.empty());

        //Assert
        assertThat(harFullAAPMedAndreAktiviteter).isFalse();
    }

    @Test
    public void skal_returnere_false_når_bare_AAP_uten_andre_aktiviteter_på_stp() {
        //Arrange
        var aktivitetAggregat = lagBeregningAktivitetAggregatMedType(OpptjeningAktivitetType.AAP);

        //Act
        boolean harFullAAPMedAndreAktiviteter = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harFullAAPITilleggTilAnnenAktivitet(aktivitetAggregat, Optional.empty());

        //Assert
        assertThat(harFullAAPMedAndreAktiviteter).isFalse();
    }

    @Test
    public void skal_returnere_false_når_AAP_med_andre_aktiviteter_på_stp_med_siste_meldekort_uten_full_utbetaling() {
        //Arrange
        var aktivitetAggregatDto = lagBeregningAktivitetAggregatMedType(OpptjeningAktivitetType.AAP, ARBEID);

        Tuple<Periode, Integer> meldekort1 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(15), 200);
        Tuple<Periode, Integer> meldekort2 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), 180);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagAktørYtelse(meldekort1, meldekort2);

        //Act
        boolean harFullAAPMedAndreAktiviteter = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harFullAAPITilleggTilAnnenAktivitet(aktivitetAggregatDto, getAktørYtelseFraRegister(iayGrunnlag));

        //Assert
        assertThat(harFullAAPMedAndreAktiviteter).isFalse();
    }

    @Test
    public void skal_returnere_true_når_AAP_med_andre_aktiviteter_på_stp_med_siste_meldekort_med_full_utbetaling() {
        //Arrange
        var aktivitetAggregatDto = lagBeregningAktivitetAggregatMedType(OpptjeningAktivitetType.AAP, OpptjeningAktivitetType.NÆRING);

        Tuple<Periode, Integer> meldekort1 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(15), 99);
        Tuple<Periode, Integer> meldekort2 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1), 200);
        Tuple<Periode, Integer> meldekort3 = lagMeldekort(SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(1), 179); //Skal ikke tas med siden avsluttes etter stp
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = lagAktørYtelse(meldekort1, meldekort2, meldekort3);

        //Act
        boolean harFullAAPMedAndreAktiviteter = AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP.harFullAAPITilleggTilAnnenAktivitet(aktivitetAggregatDto, getAktørYtelseFraRegister(iayGrunnlag));

        //Assert
        assertThat(harFullAAPMedAndreAktiviteter).isTrue();
    }

    private BeregningAktivitetAggregatDto lagBeregningAktivitetAggregatMedType(OpptjeningAktivitetType... typer) {
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING);
        Stream.of(typer).forEach(type -> builder.leggTilAktivitet(BeregningAktivitetDto.builder()
                .medArbeidsgiver(type.equals(ARBEID) ? ARBEIDSGIVER : null)
                .medOpptjeningAktivitetType(type)
                .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(10), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(10)))
                .build()));
        return builder.build();
    }

    private BeregningAktivitetDto lagBeregningAktivitetAggregat(LocalDate fom, LocalDate tom, OpptjeningAktivitetType type) {
        return BeregningAktivitetDto.builder()
            .medArbeidsgiver(ARBEIDSGIVER)
            .medOpptjeningAktivitetType(type)
            .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
            .build();
    }

    @SafeVarargs
    private InntektArbeidYtelseGrunnlagDto lagAktørYtelse(Tuple<Periode, Integer>... meldekortPerioder) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder();
        YtelseDtoBuilder ytelseBuilder = YtelseDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(6), SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1)))
                .medYtelseType(ARBEIDSAVKLARINGSPENGER);
        if (meldekortPerioder != null && meldekortPerioder.length > 0) {
            Stream.of(meldekortPerioder).forEach(meldekort -> ytelseBuilder.leggTilYtelseAnvist(lagYtelseAnvist(ytelseBuilder, meldekort.getElement1(), meldekort.getElement2())));
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medData(inntektArbeidYtelseAggregatBuilder).build();
    }

    private YtelseAnvistDto lagYtelseAnvist(YtelseDtoBuilder ytelseBuilder, Periode periode, int utbetalingsgrad) {
        return ytelseBuilder.getAnvistBuilder()
            .medAnvistPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
            .medUtbetalingsgradProsent(Stillingsprosent.fra(utbetalingsgrad))
            .medDagsats(Beløp.fra(1000))
            .medBeløp(Beløp.fra(10000))
            .build();
    }

    private Tuple<Periode, Integer> lagMeldekort(LocalDate tom, int utbetalingsgrad) {
        return new Tuple<>(Periode.of(tom.minusDays(13), tom), utbetalingsgrad);
    }

    private Optional<AktørYtelseDto> getAktørYtelseFraRegister(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getAktørYtelseFraRegister();
    }
}
