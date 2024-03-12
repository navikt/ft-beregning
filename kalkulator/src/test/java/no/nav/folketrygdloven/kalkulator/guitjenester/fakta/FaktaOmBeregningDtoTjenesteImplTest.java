package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public class FaktaOmBeregningDtoTjenesteImplTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.now();
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(LocalDate.now());

    private FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjeneste();

    @Test
    public void skal_kalle_dto_tjenester() {
        // Arrange
        List<FaktaOmBeregningTilfelle> tilfeller = List.of(
            FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL,
            FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON,
            FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE,
            FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD,
            FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING,
            FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE,
            FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(inntektsmeldinger).build();

        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(tilfeller);
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        builder.leggTilAktivitet(BeregningAktivitetDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet("test"))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(10)))
            .build());

        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag);
        oppdatere.medRegisterAktiviteter(builder.build());

        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), null)
            .medBeregningsgrunnlagGrunnlag(oppdatere.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));

        // Act
        Optional<FaktaOmBeregningDto> dto = faktaOmBeregningDtoTjeneste.lagDto(input);

        // Assert
        assertThat(dto.get().getFrilansAndel().getAndelsnr()).isEqualTo(1);
        assertThat(dto.get().getArbeidstakerOgFrilanserISammeOrganisasjonListe()).isEmpty();
        assertThat(dto.get().getKunYtelse().getAndeler()).hasSize(1);
        assertThat(dto.get().getKortvarigeArbeidsforhold()).isEmpty();
        assertThat(dto.get().getArbeidsforholdMedLønnsendringUtenIM()).isNull(); // Fix VurderLønnsendringDtoTjeneste til å sette tom liste?
    }

    @Test
    public void skal_lage_fakta_om_beregning_dto_når_man_har_tilfeller_i_fakta_om_beregning() {
        List<FaktaOmBeregningTilfelle> tilfeller = Collections.singletonList(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(tilfeller);
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        builder.leggTilAktivitet(BeregningAktivitetDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet("test"))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(10)))
            .build());

        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag);
        oppdatere.medRegisterAktiviteter(builder.build());

        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(oppdatere.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));

        Optional<FaktaOmBeregningDto> dto = faktaOmBeregningDtoTjeneste.lagDto(input);
        assertThat(dto.isPresent()).isTrue();
    }

    @Test
    public void skal_lage_fakta_om_beregning_dto_med_avklar_aktiviterer() {
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(inntektsmeldinger).build();
        BeregningsgrunnlagGrunnlagDto grunnlag = lagBeregningsgrunnlag(List.of(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE));

        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder();
        builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        builder.leggTilAktivitet(BeregningAktivitetDto.builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet("test"))
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(10)))
            .build());

        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag);
        oppdatere.medRegisterAktiviteter(builder.build());

        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(oppdatere.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));

        Optional<FaktaOmBeregningDto> dto = faktaOmBeregningDtoTjeneste
            .lagDto(input);
        assertThat(dto).isPresent();
        assertThat(dto.orElseThrow().getAvklarAktiviteter().getAktiviteterTomDatoMapping()).isNotNull();
    }


    private BeregningsgrunnlagGrunnlagDto lagBeregningsgrunnlag(List<FaktaOmBeregningTilfelle> tilfeller) {
        BeregningsgrunnlagGrunnlagDtoBuilder oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty());
        BeregningsgrunnlagDto beregningsgrunnlagDto = oppdatere
            .getBeregningsgrunnlagBuilder()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(Beløp.fra(90000))
            .leggTilFaktaOmBeregningTilfeller(tilfeller)
            .build();

        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlagDto);

        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .build(periode);

        oppdatere.medBeregningsgrunnlag(beregningsgrunnlagDto);
        return oppdatere.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }

}
