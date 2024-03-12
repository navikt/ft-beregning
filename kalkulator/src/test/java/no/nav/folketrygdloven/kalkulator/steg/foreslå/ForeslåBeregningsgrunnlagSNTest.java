package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fortsettForeslå.FortsettForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.testutilities.TestHjelper;
import no.nav.folketrygdloven.kalkulator.testutilities.behandling.beregningsgrunnlag.BeregningAktivitetTestUtil;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@ExtendWith(MockitoExtension.class)
public class ForeslåBeregningsgrunnlagSNTest {

    private static final Beløp MÅNEDSINNTEKT1 = Beløp.fra(12345);

    private static final Beløp BEREGNINGSGRUNNLAG = Beløp.fra(BigDecimal.valueOf(148989.08d));

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final Beløp GRUNNBELØP = Beløp.fra(90000);

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_BEREGNING);

    private TestHjelper testHjelper = new TestHjelper();
    private FortsettForeslåBeregningsgrunnlag fortsettForeslåBeregningsgrunnlag = new FortsettForeslåBeregningsgrunnlag();

    @Test
    public void testBeregningsgrunnlagSelvstendigNæringsdrivende() {
        // Arrange
        BeregningAktivitetAggregatDto beregningAktiviteter = BeregningAktivitetTestUtil.opprettBeregningAktiviteter(SKJÆRINGSTIDSPUNKT_OPPTJENING,
            OpptjeningAktivitetType.NÆRING);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagDtoBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medRegisterAktiviteter(beregningAktiviteter)
            .medBeregningsgrunnlag(beregningsgrunnlag);
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        testHjelper.lagBehandlingForSN(MÅNEDSINNTEKT1.multipliser(12), 2014, registerBuilder);
        Collection<InntektsmeldingDto> inntektsmeldinger = List.of();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medData(registerBuilder)
            .medInntektsmeldinger(inntektsmeldinger).build();
        KoblingReferanse ref = lagReferanseMedStp(koblingReferanse);
        var input = BeregningsgrunnlagInputTestUtil.lagFortsettForeslåttBeregningsgrunnlagInput(ref, grunnlagDtoBuilder, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag);

        // Act
        BeregningsgrunnlagRegelResultat resultat = fortsettForeslåBeregningsgrunnlag.fortsettForeslåBeregningsgrunnlag(input);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAvklaringsbehov()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriodeDto periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, TIDENES_ENDE, 1);
        verifiserBGSN(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = BeregningsgrunnlagDto.builder();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)));
        return beregningsgrunnlagBuilder.build();
    }

    private void verifiserPeriode(BeregningsgrunnlagPeriodeDto periode, LocalDate fom, LocalDate tom, int antallAndeler) {
        assertThat(periode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(fom);
        assertThat(periode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(tom);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndeler);
        assertThat(periode.getBruttoPrÅr().verdi().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG.verdi().doubleValue(), within(0.01));
        assertThat(periode.getRedusertPrÅr()).isNull();
        assertThat(periode.getAvkortetPrÅr()).isNull();
    }

    private void verifiserBGSN(BeregningsgrunnlagPrStatusOgAndelDto bgpsa) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(bgpsa.getBeregningsperiodeFom()).isEqualTo(LocalDate.of(2014, Month.JANUARY, 1));
        assertThat(bgpsa.getBeregningsperiodeTom()).isEqualTo(LocalDate.of(2016, Month.DECEMBER, 31));
        assertThat(bgpsa.getBgAndelArbeidsforhold()).isEmpty();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.UDEFINERT);
        assertThat(bgpsa.getBeregnetPrÅr().verdi().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG.verdi().doubleValue(), within(0.01));
        assertThat(bgpsa.getBruttoPrÅr().verdi().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG.verdi().doubleValue(), within(0.01));
        assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
    }

    private static KoblingReferanse lagReferanseMedStp(KoblingReferanse koblingReferanse) {
        return koblingReferanse.medSkjæringstidspunkt(Skjæringstidspunkt.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .build());
    }
}
