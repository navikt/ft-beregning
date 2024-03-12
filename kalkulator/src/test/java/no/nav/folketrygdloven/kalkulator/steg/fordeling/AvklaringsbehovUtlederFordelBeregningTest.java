package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.AvklaringsbehovUtlederFordelBeregning;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


public class AvklaringsbehovUtlederFordelBeregningTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MARCH, 23);

    private BeregningAktivitetAggregatDto.Builder beregningAktivitetBuilder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);


    @Test
    public void skal_ikke_lage_avklaringsbehov_dersom_det_ikke_er_endret_bg() {
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlagutenNyttArbeidsforhold();

        List<BeregningAvklaringsbehovResultat> avklaringsbehovResultats = utledAvklaringsbehov(koblingReferanse, grunnlag);

        assertThat(avklaringsbehovResultats).isEmpty();
    }

    private List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(KoblingReferanse ref, BeregningsgrunnlagGrunnlagDto grunnlag) {
        ForeldrepengerGrunnlag foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        List<BeregningAvklaringsbehovResultat> avklaringsbehovResultats = AvklaringsbehovUtlederFordelBeregning.utledAvklaringsbehovFor(ref, grunnlag, foreldrepengerGrunnlag, InntektArbeidYtelseGrunnlagDtoBuilder.nytt().build(), Collections.emptyList());
        return avklaringsbehovResultats;
    }

    @Test
    public void skal_lage_avklaringsbehov_når_det_er_endring() {
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlagMedNyttArbeidsforhold();

        List<BeregningAvklaringsbehovResultat> avklaringsbehovResultats = utledAvklaringsbehov(koblingReferanse, grunnlag);

        assertThat(avklaringsbehovResultats).hasSize(1);
        assertThat(avklaringsbehovResultats.get(0).getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.FORDEL_BG);
    }


    private BeregningsgrunnlagGrunnlagDto lagGrunnlagMedNyttArbeidsforhold(FaktaOmBeregningTilfelle... tilfeller) {
        List<FaktaOmBeregningTilfelle> listeMedTilfeller = Arrays.asList(tilfeller);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
                .leggTilFaktaOmBeregningTilfeller(listeMedTilfeller)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medKilde(AndelKilde.PROSESS_PERIODISERING)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet("1234534")))
                .build(periode);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(beregningAktivitetBuilder.build())
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        return grunnlag;
    }

    private BeregningsgrunnlagGrunnlagDto lagGrunnlagutenNyttArbeidsforhold(FaktaOmBeregningTilfelle... tilfeller) {
        List<FaktaOmBeregningTilfelle> listeMedTilfeller = Arrays.asList(tilfeller);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
                .leggTilFaktaOmBeregningTilfeller(listeMedTilfeller)
                .build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlag);
        Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet("1234534");
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(virksomhet))
                .build(periode);

        beregningAktivitetBuilder.leggTilAktivitet(BeregningAktivitetDto.builder()
                .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2)))
                .medArbeidsgiver(virksomhet)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef()).build());

        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(beregningAktivitetBuilder.build())
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        return grunnlag;
    }


}
