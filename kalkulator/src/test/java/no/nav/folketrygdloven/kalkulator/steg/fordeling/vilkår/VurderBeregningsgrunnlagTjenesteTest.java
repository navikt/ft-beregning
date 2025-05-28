package no.nav.folketrygdloven.kalkulator.steg.fordeling.vilkår;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.FordelBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.testutilities.TestHjelper;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class VurderBeregningsgrunnlagTjenesteTest {

    private static final Beløp MÅNEDSINNTEKT1 = Beløp.fra(12345);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;

    private TestHjelper testHjelper = new TestHjelper();

    private Collection<InntektsmeldingDto> inntektsmeldinger = List.of();

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);
    private VurderBeregningsgrunnlagTjeneste vurderBeregningsgrunnlagTjeneste = new VurderBeregningsgrunnlagTjeneste();


    @Test
    void testVilkårsvurderingArbeidstakerMedBGOverHalvG() {
        // Arrange
        var beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING,
                OpptjeningAktivitetType.ARBEID);
        var beregningsgrunnlag = lagBeregningsgrunnlag(400_000);
        var grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag);
        var grunnlag = grunnlagDtoBuilder
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        testHjelper.lagBehandlingForSN(MÅNEDSINNTEKT1.multipliser(12), 2015, registerBuilder);
        var ref = lagReferanseMedSkjæringstidspunkt(koblingReferanse);
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var iayGrunnlag = iayGrunnlagBuilder.medData(registerBuilder).medInntektsmeldinger(inntektsmeldinger).build();
        var input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(ref, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);
        var fordelBeregningsgrunnlagInput = new FordelBeregningsgrunnlagInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act
        var resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(fordelBeregningsgrunnlagInput, grunnlag);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getVilkårOppfylt()).isPresent();
	    assertThat(resultat.getVilkårOppfylt().get()).isTrue();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        var vilkårVurdering = resultat.getRegelsporinger().get().regelsporingPerioder().stream()
                .filter(rs -> rs.regelType().equals(BeregningsgrunnlagPeriodeRegelType.VILKÅR_VURDERING))
                .findFirst().get();
        assertThat(vilkårVurdering.regelInput()).isNotNull();
        assertThat(vilkårVurdering.regelEvaluering()).isNotNull();
    }

    @Test
    void testVilkårsvurderingArbeidstakerMedBGUnderHalvG() {
        // Arrange
        var beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING,
                OpptjeningAktivitetType.ARBEID);
        var beregningsgrunnlag = lagBeregningsgrunnlag(40_000);
        var grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag);
        var grunnlag = grunnlagDtoBuilder
                .build(BeregningsgrunnlagTilstand.FORESLÅTT);
        var registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        testHjelper.lagBehandlingForSN(MÅNEDSINNTEKT1.multipliser(12), 2015, registerBuilder);

        var ref = lagReferanseMedSkjæringstidspunkt(koblingReferanse);
        var iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        var iayGrunnlag = iayGrunnlagBuilder.medData(registerBuilder).medInntektsmeldinger(inntektsmeldinger).build();
        var input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(ref, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.FORESLÅTT, iayGrunnlag);

        var fordelBeregningsgrunnlagInput = new FordelBeregningsgrunnlagInput(new StegProsesseringInput(input, BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING));

        // Act
        var resultat = vurderBeregningsgrunnlagTjeneste.vurderBeregningsgrunnlag(fordelBeregningsgrunnlagInput, grunnlag);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
	    assertThat(resultat.getVilkårOppfylt()).isPresent();
        assertThat(resultat.getVilkårOppfylt().get()).isFalse();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        var vilkårVurdering = resultat.getRegelsporinger().get().regelsporingPerioder().stream()
                .filter(rs -> rs.regelType().equals(BeregningsgrunnlagPeriodeRegelType.VILKÅR_VURDERING))
                .findFirst().get();
        assertThat(vilkårVurdering.regelInput()).isNotNull();
        assertThat(vilkårVurdering.regelEvaluering()).isNotNull();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(int inntekt) {
        var sg = SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medRapportertPrÅr(Beløp.ZERO)
                .medAvvikPromilleNy(BigDecimal.ZERO)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)
                .build();
        var bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .medGrunnbeløp(Beløp.fra(600_000))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .leggTilSammenligningsgrunnlag(sg)
                .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(12))
                        .medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT_BEREGNING)
                        .medArbeidsgiver(Arbeidsgiver.virksomhet("1234"))
                        .medRefusjonskravPrÅr(Beløp.fra(inntekt), Utfall.GODKJENT))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
                .medBeregnetPrÅr(Beløp.fra(inntekt))
                .build(periode);
        return bg;
    }

    private static KoblingReferanse lagReferanseMedSkjæringstidspunkt(KoblingReferanse koblingReferanse) {
        return koblingReferanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT_BEREGNING)
                .build());
    }

}
