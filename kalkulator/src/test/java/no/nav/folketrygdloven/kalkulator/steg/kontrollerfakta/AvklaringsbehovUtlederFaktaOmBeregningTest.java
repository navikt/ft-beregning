package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

public class AvklaringsbehovUtlederFaktaOmBeregningTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MARCH, 23);
    private final InternArbeidsforholdRefDto arbId = InternArbeidsforholdRefDto.namedRef("A");
    private final String orgnr = "974760673";
    private final InternArbeidsforholdRefDto arbId2 = InternArbeidsforholdRefDto.namedRef("B");
    private final String orgnr2 = "974761424";

    private AvklaringsbehovUtlederFaktaOmBeregning avklaringsbehovUtlederFaktaOmBeregning;


    private BeregningAktivitetAggregatDto.Builder beregningAktivitetBuilder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private Arbeidsgiver arbeidsgiver;
    private Arbeidsgiver arbeidsgiver2;

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);

    @BeforeEach
    public void setup() {
        avklaringsbehovUtlederFaktaOmBeregning = new AvklaringsbehovUtlederFaktaOmBeregning();
        arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
    }

    /**
     * orgnr gradering, orgnr2 med refusjon over 6G
     * SN ny i Arbeidslivet:
     */
    @Test
    public void skalUtledeAvklaringsbehovForSNNyIArbeidslivet() {
        // Arrange
        LocalDate graderingStart = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(9);
        int refusjonskravAndel2 = 50000;

        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        opptjeningMap.put(orgnr, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));
        opptjeningMap.put(orgnr2, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));

        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.lagOppgittOpptjeningForSN(SKJÆRINGSTIDSPUNKT_OPPTJENING, true, iayGrunnlagBuilder);

        BeregningIAYTestUtil.byggArbeidForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, arbeidsgiver, iayGrunnlagBuilder);
        arbeidsgiver2 = Arbeidsgiver.virksomhet(orgnr2);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), arbId2, arbeidsgiver2, iayGrunnlagBuilder);

        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7)
                .build(beregningsgrunnlag);
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medHjemmel(Hjemmel.F_14_7)
                .build(beregningsgrunnlag);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, graderingStart.minusDays(1))
                .build(beregningsgrunnlag);

        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, 0);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, orgnr, 0);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, orgnr2, refusjonskravAndel2 * 12);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(graderingStart, null)
                .leggTilPeriodeÅrsak(PeriodeÅrsak.GRADERING)
                .build(beregningsgrunnlag);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, null, 0);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, orgnr, 0);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, orgnr2, refusjonskravAndel2 * 12);

        BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING
        );
        BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, 50000
        );
        leggTilAktivitet(arbId, orgnr, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10));
        leggTilAktivitet(arbId2, orgnr2, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(beregningAktivitetBuilder.build())
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        var input = lagInput(iayGrunnlagBuilder);
        var resultat = avklaringsbehovUtlederFaktaOmBeregning.utledAvklaringsbehovFor(input, grunnlag, false);

        // Assert
        assertThat(resultat.getBeregningAvklaringsbehovResultatList()).hasSize(1);
        assertThat(resultat.getBeregningAvklaringsbehovResultatList().get(0).getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN);
        List<FaktaOmBeregningTilfelle> tilfeller = resultat.getFaktaOmBeregningTilfeller();
        assertThat(tilfeller).containsExactlyInAnyOrder(FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET);
    }

    @Test
    public void skalUtledeAvklaringsbehovATFLSammeOrgLønnsendringNyoppstartetFL() {
        // Arrange
        var arbId3 = InternArbeidsforholdRefDto.namedRef("3");
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        final String orgnr3 = "567755757";
        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        Periode periode = Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12);
        opptjeningMap.put(orgnr, periode);
        opptjeningMap.put(orgnr3, periode);
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        iayGrunnlag.medOppgittOpptjening(BeregningIAYTestUtil.leggTilOppgittOpptjeningForFL(true, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2)));
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, arbeidsgiver, Optional.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1L)), iayGrunnlag);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId3, Arbeidsgiver.virksomhet(orgnr3), iayGrunnlag);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), null, arbeidsgiver,
                ArbeidType.FRILANSER_OPPDRAGSTAKER, singletonList(Beløp.fra(10)), false, Optional.empty(), iayGrunnlag);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedATFL(periode);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                        .leggTilAktivitet(lagBeregningAktivitetArbeid(periode, arbeidsgiver, arbId))
                        .leggTilAktivitet(lagBeregningAktivitetArbeid(periode, Arbeidsgiver.virksomhet(orgnr3), InternArbeidsforholdRefDto.nullRef()))
                        .leggTilAktivitet(lagBeregningAktivitetFL(periode))
                        .build())
                .medBeregningsgrunnlag(beregningsgrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = grunnlagBuilder.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        var input = lagInput(iayGrunnlag);
        var resultat = avklaringsbehovUtlederFaktaOmBeregning.utledAvklaringsbehovFor(input, grunnlag, false);

        // Assert
        assertThat(resultat.getBeregningAvklaringsbehovResultatList()).hasSize(1);
        assertThat(resultat.getBeregningAvklaringsbehovResultatList().get(0).getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN);
        List<FaktaOmBeregningTilfelle> tilfeller = resultat.getFaktaOmBeregningTilfeller();
        assertThat(tilfeller).containsExactlyInAnyOrder(
                FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON,
                FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING,
                FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL);
    }

    /**
     * orgnr har gradering fra og med STP+2 uker
     * orgnr2 er kortvarig arbeidsforhold med slutt STP+1 måned, søker refusjon
     */
    @Test
    public void skalUtledeAvklaringsbehovKortvarigeArbeidsforhold() {
        // Arrange
        LocalDate graderingStart = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(9);
        int refusjonskravAndel2 = 50000;

        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        opptjeningMap.put(orgnr, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));
        opptjeningMap.put(orgnr2, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12));
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlag);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), arbId2, Arbeidsgiver.virksomhet(orgnr2), iayGrunnlag);
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(GrunnbeløpTestKonstanter.GRUNNBELØP_2018))
                .build();
        BeregningsgrunnlagAktivitetStatusDto.builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medHjemmel(Hjemmel.F_14_7)
                .build(beregningsgrunnlag);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode1 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, graderingStart.minusDays(1))
                .build(beregningsgrunnlag);

        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, orgnr, 0);
        leggTilAndel(beregningsgrunnlagPeriode1, AktivitetStatus.ARBEIDSTAKER, orgnr2, refusjonskravAndel2 * 12);

        BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode2 = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(graderingStart, null)
                .leggTilPeriodeÅrsak(PeriodeÅrsak.GRADERING)
                .build(beregningsgrunnlag);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, orgnr, 0);
        leggTilAndel(beregningsgrunnlagPeriode2, AktivitetStatus.ARBEIDSTAKER, orgnr2, refusjonskravAndel2 * 12);

        BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        BeregningInntektsmeldingTestUtil.opprettInntektsmelding(orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjonskravAndel2);
        leggTilAktivitet(arbId, orgnr, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10));
        leggTilAktivitet(arbId2, orgnr2, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(beregningAktivitetBuilder.build())
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        var input = lagInput(iayGrunnlag);
        var resultat = avklaringsbehovUtlederFaktaOmBeregning.utledAvklaringsbehovFor(input, grunnlag, false);

        // Assert
        assertThat(resultat.getBeregningAvklaringsbehovResultatList()).hasSize(1);
        assertThat(resultat.getBeregningAvklaringsbehovResultatList().get(0).getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN);
        List<FaktaOmBeregningTilfelle> tilfeller = resultat.getFaktaOmBeregningTilfeller();
        assertThat(tilfeller).containsExactlyInAnyOrder(
                FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD);
    }

    private void leggTilAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, AktivitetStatus aktivitetStatus, String orgnr, int refusjonskravPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(aktivitetStatus);
        if (orgnr != null) {
            BGAndelArbeidsforholdDto.Builder bgAndelArbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                    .medRefusjonskravPrÅr(Beløp.fra(refusjonskravPrÅr), Utfall.GODKJENT);
            andelBuilder
                    .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                    .medBGAndelArbeidsforhold(bgAndelArbeidsforholdBuilder);
        }
        andelBuilder.build(beregningsgrunnlagPeriode);
    }

    private void leggTilAktivitet(InternArbeidsforholdRefDto ref, String orgnr, LocalDate fom, LocalDate tom) {
        beregningAktivitetBuilder.leggTilAktivitet(BeregningAktivitetDto.builder()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                .medArbeidsforholdRef(ref).build());
    }

    @Test
    public void skalUtledeAvklaringsbehovForFellesTilfeller() {
        // Act
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.lagOppgittOpptjeningForSN(SKJÆRINGSTIDSPUNKT_OPPTJENING, true, iayGrunnlagBuilder);
        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        Periode periode = Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12);
        opptjeningMap.put(orgnr, periode);

        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
        BeregningsgrunnlagDto bg = lagBeregningsgrunnlagMedATSN(periode);


        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                        .leggTilAktivitet(lagBeregningAktivitetArbeid(periode, arbeidsgiver, arbId))
                        .leggTilAktivitet(lagBeregningAktivitetSN(periode))
                        .build())
                .medBeregningsgrunnlag(bg);
        BeregningsgrunnlagGrunnlagDto grunnlag = grunnlagBuilder.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        var input = lagInput(iayGrunnlagBuilder);
        var resultater = avklaringsbehovUtlederFaktaOmBeregning.utledAvklaringsbehovFor(input, grunnlag, false);

        // Assert
        assertThat(resultater.getBeregningAvklaringsbehovResultatList()).hasSize(1);
        assertThat(resultater.getBeregningAvklaringsbehovResultatList())
                .anySatisfy(resultat -> assertThat(resultat.getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.VURDER_FAKTA_ATFL_SN));
    }

    @Test
    public void skalUtledeAvklaringsbehovForFellesTilfellerOgReturnereOverstyring() {
        // Act
        InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BeregningIAYTestUtil.lagOppgittOpptjeningForSN(SKJÆRINGSTIDSPUNKT_OPPTJENING, true, iayGrunnlagBuilder);
        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        Periode periode = Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 12);
        opptjeningMap.put(orgnr, periode);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedATSN(periode);

        // Act
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                        .leggTilAktivitet(lagBeregningAktivitetArbeid(periode, arbeidsgiver, arbId))
                        .leggTilAktivitet(lagBeregningAktivitetSN(periode))
                        .build())
                .medBeregningsgrunnlag(beregningsgrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = grunnlagBuilder.build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        var input = lagInput(iayGrunnlagBuilder);
        var resultater = avklaringsbehovUtlederFaktaOmBeregning.utledAvklaringsbehovFor(input, grunnlag, true);

        // Assert
        assertThat(resultater.getBeregningAvklaringsbehovResultatList()).hasSize(1);
        assertThat(resultater.getBeregningAvklaringsbehovResultatList())
                .anySatisfy(resultat -> assertThat(resultat.getBeregningAvklaringsbehovDefinisjon()).isEqualTo(AvklaringsbehovDefinisjon.OVST_INNTEKT));
        assertThat(resultater.getFaktaOmBeregningTilfeller()).containsExactly(FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET);

    }

    private BeregningAktivitetDto lagBeregningAktivitetArbeid(Periode periode, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbId) {
        return BeregningAktivitetDto.builder().medPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdRef(arbId)
                .build();
    }

    private BeregningAktivitetDto lagBeregningAktivitetSN(Periode periode) {
        return BeregningAktivitetDto.builder().medPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.NÆRING)
                .build();
    }

    private BeregningAktivitetDto lagBeregningAktivitetFL(Periode periode) {
        return BeregningAktivitetDto.builder().medPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.FRILANS)
                .build();
    }

    @Test
    public void skalReturnereIngenAvklaringsbehov() {
        // Arrange
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlagMedAT(Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2)));

        // Act
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlag)
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING).build())
                .build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        var input = lagInput(InntektArbeidYtelseGrunnlagDtoBuilder.nytt());
        var resultater = avklaringsbehovUtlederFaktaOmBeregning.utledAvklaringsbehovFor(input, grunnlag, false);

        // Assert
        assertThat(resultater.getBeregningAvklaringsbehovResultatList()).isEmpty();
    }

    private FaktaOmBeregningInput lagInput(InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        return new FaktaOmBeregningInput(koblingReferanse, inntektArbeidYtelseGrunnlagBuilder.build(), new OpptjeningAktiviteterDto(), List.of(), foreldrepengerGrunnlag);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedATSN(Periode periode) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_SN))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsperiodeFom(periode.getFom()).medArbeidsperiodeTom(periode.getTom()).medArbeidsforholdRef(arbId))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(bgPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .build(bgPeriode);
        return bg;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedATFL(Periode periode) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_FL))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsperiodeFom(periode.getFom()).medArbeidsperiodeTom(periode.getTom()).medArbeidsforholdRef(arbId))
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .build(bgPeriode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(bgPeriode);
        return bg;
    }


    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedAT(Periode periode) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_FL))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver).medArbeidsperiodeFom(periode.getFom()).medArbeidsperiodeTom(periode.getTom()).medArbeidsforholdRef(arbId))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(bgPeriode);
        return bg;
    }

}
