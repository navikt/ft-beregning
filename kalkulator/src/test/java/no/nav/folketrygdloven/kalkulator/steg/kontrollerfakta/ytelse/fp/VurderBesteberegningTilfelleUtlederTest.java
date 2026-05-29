package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ytelse.fp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

class VurderBesteberegningTilfelleUtlederTest {

    public static final LocalDate STP = LocalDate.now();
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(STP);

    @Test
    void skal_ikke_få_besteberegning_om_dagpenger_er_fjernet() {
        // Arrange
        var virksomhet = Arbeidsgiver.virksomhet("28794923");
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
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
        var input = new BeregningsgrunnlagInput(koblingReferanse, null, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).isEmpty();
    }

    @Test
    void skal_ikkje_få_besteberegning_kun_arbeidstaker_og_dagpenger_i_opptjeningsperioden_da_dette_tas_automatisk() {
        // Arrange
        var virksomhet = Arbeidsgiver.virksomhet("28794923");
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
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
        var input = new BeregningsgrunnlagInput(koblingReferanse, null, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).isEmpty();
    }

    @Test
    void skal_få_besteberegning_frilans_arbeid_og_dagpenger_i_opptjeningsperioden() {
        // Arrange
        var virksomhet = Arbeidsgiver.virksomhet("28794923");
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
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
        var input = new BeregningsgrunnlagInput(koblingReferanse, null, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).isNotEmpty();
    }

    @Test
    void skal_få_besteberegning_kun_arbeidstaker_og_dagpenger_når_vlsp_ytelse_siste_10_måneder() {
        // Arrange
        var virksomhet = Arbeidsgiver.virksomhet("28794923");
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
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

        // Sett opp IAY med VLSP ytelse og SP inntekt innenfor siste 10 måneder
        var iayGrunnlag = lagIayMedVlspYtelseOgSPInntekt(STP.minusMonths(3), STP.minusMonths(1));

        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).contains(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING);
    }

    @Test
    void skal_ikke_få_besteberegning_kun_arbeidstaker_og_dagpenger_når_vlsp_ytelse_utenfor_10_måneder() {
        // Arrange
        var virksomhet = Arbeidsgiver.virksomhet("28794923");
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
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

        // Sett opp IAY med VLSP ytelse utenfor siste 10 måneder (mer enn 11 måneder tilbake)
        var iayGrunnlag = lagIayMedVlspYtelseOgSPInntekt(STP.minusMonths(14), STP.minusMonths(12));

        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).isEmpty();
    }

    @Test
    void skal_ikke_få_besteberegning_når_vlsp_ytelse_men_ingen_sp_inntekt_i_perioden() {
        // Arrange
        var virksomhet = Arbeidsgiver.virksomhet("28794923");
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(STP)
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(STP, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);
        var grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
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

        // Sett opp IAY med VLSP ytelse, men UTEN SP inntekt
        var iayGrunnlag = lagIayMedKunVlspYtelse(STP.minusMonths(3), STP.minusMonths(1));

        var input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, opptjeningAktiviteter, null, new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, true));
        input = input.medBeregningsgrunnlagGrunnlag(grunnlag);

        // Act
        var tilfelle = new VurderBesteberegningTilfelleUtleder().utled(new FaktaOmBeregningInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER)), grunnlag);

        // Assert
        assertThat(tilfelle).isEmpty();
    }

    private no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto lagIayMedVlspYtelseOgSPInntekt(LocalDate fom, LocalDate tom) {
        var ytelseBuilder = YtelseDtoBuilder.ny()
                .medYtelseType(YtelseType.SYKEPENGER)
                .medYtelseKilde(YtelseKilde.VLSP)
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørYtelseBuilder = registerBuilder.getAktørYtelseBuilder();
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        registerBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        var aktørInntektBuilder = registerBuilder.getAktørInntektBuilder();
        var inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektskildeType.INNTEKT_BEREGNING, null);
        inntektBuilder.leggTilInntektspost(InntektspostDtoBuilder.ny()
                .medInntektspostType(InntektspostType.YTELSE)
                .medInntektYtelse(InntektYtelseType.SYKEPENGER)
                .medPeriode(fom, tom)
                .medBeløp(Beløp.fra(30000)));
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        registerBuilder.leggTilAktørInntekt(aktørInntektBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .build();
    }

    private no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto lagIayMedKunVlspYtelse(LocalDate fom, LocalDate tom) {
        var ytelseBuilder = YtelseDtoBuilder.ny()
                .medYtelseType(YtelseType.SYKEPENGER)
                .medYtelseKilde(YtelseKilde.VLSP)
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørYtelseBuilder = registerBuilder.getAktørYtelseBuilder();
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        registerBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(registerBuilder)
                .build();
    }

}
