package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ytelse.fp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class VurderBesteberegningTilfelleUtlederTest {

    public static final LocalDate STP = LocalDate.now();
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(STP);

    @Test
    void skal_ikke_få_besteberegning_om_dagpenger_er_fjernet() {
        // Arrange
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet("28794923");
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.DAGPENGER)
                                .build())
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .medArbeidsgiver(virksomhet)
                                .build())
                        .build())
                .medSaksbehandletAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .medArbeidsgiver(virksomhet)
                                .build())
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMed(STP.minusMonths(10)), virksomhet.getIdentifikator()),
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.DAGPENGER, Intervall.fraOgMed(STP.minusMonths(10))),
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS, Intervall.fraOgMed(STP.minusMonths(10)))));
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        Optional<FaktaOmBeregningTilfelle> tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).isEmpty();
    }

    @Test
    void skal_ikkje_få_besteberegning_kun_arbeidstaker_og_dagpenger_i_opptjeningsperioden_da_dette_tas_automatisk() {
        // Arrange
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet("28794923");
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.DAGPENGER)
                                .build())
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .medArbeidsgiver(virksomhet)
                                .build())
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMed(STP.minusMonths(10)), virksomhet.getIdentifikator()),
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.DAGPENGER, Intervall.fraOgMed(STP.minusMonths(10)))));
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        Optional<FaktaOmBeregningTilfelle> tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).isEmpty();
    }

    @Test
    void skal_få_besteberegning_frilans_arbeid_og_dagpenger_i_opptjeningsperioden() {
        // Arrange
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet("28794923");
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(STP)
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.DAGPENGER)
                                .build())
                        .leggTilAktivitet(BeregningAktivitetDto.builder()
                                .medPeriode(Intervall.fraOgMedTilOgMed(STP.minusMonths(12), STP))
                                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                                .medArbeidsgiver(virksomhet)
                                .build())
                        .build())
                .medBeregningsgrunnlag(bg)
                .build(BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER);
        var opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(
                OpptjeningAktiviteterDto.nyPeriodeOrgnr(OpptjeningAktivitetType.ARBEID, Intervall.fraOgMed(STP.minusMonths(10)), virksomhet.getIdentifikator()),
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.DAGPENGER, Intervall.fraOgMed(STP.minusMonths(10))),
                OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS, Intervall.fraOgMed(STP.minusMonths(10)))));
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        Optional<FaktaOmBeregningTilfelle> tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).isNotEmpty();
    }

}
