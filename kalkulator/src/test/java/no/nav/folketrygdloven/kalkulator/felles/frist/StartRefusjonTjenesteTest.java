package no.nav.folketrygdloven.kalkulator.felles.frist;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class StartRefusjonTjenesteTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = LocalDate.now();
    public static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet("37432232");
    public static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet("432342342");


    @Test
    void skal_finne_dag_med_refusjon_lik_stp_ved_eksisterende_aktivitet() {
        // Arrange
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .leggTilAktivitet(lagBeregningsaktivitet(ansattPeriode, ARBEIDSGIVER1))
                .build();
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(lagAnsettelsesPeriode(ansattPeriode))
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef())
                .build();

        // Act
        LocalDate førsteDatoRefusjon = StartRefusjonTjeneste.finnFørsteMuligeDagRefusjon(
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                yrkesaktivitet);

        // Assert
        assertThat(førsteDatoRefusjon).isEqualTo(SKJÆRINGSTIDSPUNKT_BEREGNING);

    }

    @Test
    void første_dag_med_refusjon_lik_startdato_ansattforhold_ved_tilkommet_aktivitet_etter_stp() {
        // Arrange
        LocalDate startdatoArbeid = SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(2);
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(startdatoArbeid, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter();
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(lagAnsettelsesPeriode(ansattPeriode))
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef())
                .build();


        // Act
        LocalDate førsteDatoRefusjon = StartRefusjonTjeneste.finnFørsteMuligeDagRefusjon(
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                yrkesaktivitet);

        //Assert
        assertThat(førsteDatoRefusjon).isEqualTo(startdatoArbeid);
    }

    @Test
    void første_dag_med_refusjon_lik_startdato_ansattforhold_ved_tilkommet_aktivitet_etter_stp_med_flere_ansattperioder() {
        // Arrange
        LocalDate startdatoArbeid = SKJÆRINGSTIDSPUNKT_BEREGNING.plusDays(2);
        Intervall ansattPeriodeEtterStp = Intervall.fraOgMedTilOgMed(startdatoArbeid, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        LocalDate startdatoArbeidFørStp = SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(2);
        Intervall ansattPeriodeFørStp = Intervall.fraOgMedTilOgMed(startdatoArbeidFørStp, SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(2));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter();
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(lagAnsettelsesPeriode(ansattPeriodeEtterStp))
                .leggTilAktivitetsAvtale(lagAnsettelsesPeriode(ansattPeriodeFørStp))
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef())
                .build();


        // Act
        LocalDate førsteDatoRefusjon = StartRefusjonTjeneste.finnFørsteMuligeDagRefusjon(
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                yrkesaktivitet);

        //Assert
        assertThat(førsteDatoRefusjon).isEqualTo(startdatoArbeid);
    }

    @Test
    void første_dag_med_refusjon_lik_stp_ved_fjernet_ved_overstyring_og_start_før_stp() {
        // Arrange
        LocalDate startdatoArbeid = SKJÆRINGSTIDSPUNKT_BEREGNING.minusDays(1);
        Intervall ansattPeriode = Intervall.fraOgMedTilOgMed(startdatoArbeid, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = lagGjeldendeAktiviteter();
        YrkesaktivitetDto yrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(ARBEIDSGIVER1)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(lagAnsettelsesPeriode(ansattPeriode))
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef())
                .build();

        // Act
        LocalDate førsteDatoRefusjon = StartRefusjonTjeneste.finnFørsteMuligeDagRefusjon(
                gjeldendeAktiviteter,
                SKJÆRINGSTIDSPUNKT_BEREGNING,
                yrkesaktivitet);

        //Assert
        assertThat(førsteDatoRefusjon).isEqualTo(SKJÆRINGSTIDSPUNKT_BEREGNING);
    }

    @Test
    void skal_finne_riktig_dato_med_innsending_på_den_første_i_måneden() {
        LocalDate innsendingsdato = LocalDate.of(2021, 6, 1);
        LocalDate førsteGyldigeDato = StartRefusjonTjeneste.finnFørsteGyldigeDatoMedRefusjon(innsendingsdato);
        assertThat(førsteGyldigeDato).isEqualTo(LocalDate.of(2021, 3, 1));
    }

    @Test
    void skal_finne_riktig_dato_med_innsending_midt_i_måneden() {
        LocalDate innsendingsdato = LocalDate.of(2021, 6, 15);
        LocalDate førsteGyldigeDato = StartRefusjonTjeneste.finnFørsteGyldigeDatoMedRefusjon(innsendingsdato);
        assertThat(førsteGyldigeDato).isEqualTo(LocalDate.of(2021, 3, 1));
    }

    private BeregningAktivitetAggregatDto lagGjeldendeAktiviteter() {
        return BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .leggTilAktivitet(lagBeregningsaktivitet(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(12)), ARBEIDSGIVER2))
                .build();
    }

    private AktivitetsAvtaleDtoBuilder lagAnsettelsesPeriode(Intervall ansattPeriode) {
        return AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(ansattPeriode)
                .medErAnsettelsesPeriode(true);
    }

    private BeregningAktivitetDto lagBeregningsaktivitet(Intervall periode, Arbeidsgiver arbeidsgiver) {
        return BeregningAktivitetDto.builder()
                .medPeriode(periode)
                .medArbeidsgiver(arbeidsgiver)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef())
                .build();
    }


}
