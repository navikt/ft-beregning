package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.ArbeidstakerUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderMottarYtelseDto;


public class VurderMottarYtelseDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final String ORGNR = "973093681";
    private static final EksternArbeidsforholdRef EKSTERN_ARB_ID = EksternArbeidsforholdRef.ref("TEST_REF1");
    private static final Beløp INNTEKT1 = Beløp.fra(10000);
    private static final Beløp INNTEKT2 = Beløp.fra(20000);
    private static final Beløp INNTEKT3 = Beløp.fra(30000);
    private static final List<Beløp> INNTEKT_PR_MND = List.of(INNTEKT1, INNTEKT2, INNTEKT3);
    private static final Beløp INNTEKT_SNITT = INNTEKT1.adder(INNTEKT2.adder(INNTEKT3)).divider(3, 10, RoundingMode.HALF_EVEN);
    private static final String FRILANS_ORGNR = "853498598934";

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT_OPPTJENING);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;
    private VurderMottarYtelseDtoTjeneste dtoTjeneste;
    private BeregningsgrunnlagGrunnlagDto grunnlag;
    private InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag;
    private final Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);

    @BeforeEach
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(91425))
                .leggTilFaktaOmBeregningTilfeller(Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE))
                .build();
        periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlag);
        grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(beregningsgrunnlag)
                .build(BeregningsgrunnlagTilstand.OPPRETTET);
        dtoTjeneste = new VurderMottarYtelseDtoTjeneste();
    }

    @Test
    public void skal_lage_dto_for_mottar_ytelse_uten_mottar_ytelse_satt() {
        // Arrange
        FaktaOmBeregningDto dto = new FaktaOmBeregningDto();
        byggFrilansAndel();
        BeregningsgrunnlagPrStatusOgAndelDto arbeidsforholdAndel = byggArbeidsforholdMedBgAndel();

        // Act
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, inntektArbeidYtelseGrunnlag, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(grunnlag);

        dtoTjeneste.lagDto(input, dto);

        // Assert
        VurderMottarYtelseDto mottarYtelseDto = dto.getVurderMottarYtelse();
        assertThat(mottarYtelseDto.getErFrilans()).isTrue();
        assertThat(mottarYtelseDto.getFrilansMottarYtelse()).isNull();
        assertThat(mottarYtelseDto.getFrilansInntektPrMnd()).isEqualByComparingTo(ModellTyperMapper.beløpTilDto(INNTEKT_SNITT));
        assertThat(mottarYtelseDto.getArbeidstakerAndelerUtenIM()).hasSize(1);
        ArbeidstakerUtenInntektsmeldingAndelDto andelUtenIM = mottarYtelseDto.getArbeidstakerAndelerUtenIM().get(0);
        assertThat(andelUtenIM.getMottarYtelse()).isNull();
        assertThat(andelUtenIM.getArbeidsforhold().getArbeidsgiverIdent()).isEqualTo(ORGNR);
        assertThat(andelUtenIM.getAndelsnr()).isEqualTo(arbeidsforholdAndel.getAndelsnr());
        assertThat(andelUtenIM.getInntektPrMnd()).isEqualByComparingTo(ModellTyperMapper.beløpTilDto(INNTEKT_SNITT));
    }

    @Test
    public void skal_lage_dto_for_mottar_ytelse_med_mottar_ytelse_satt() {
        // Arrange
        FaktaOmBeregningDto dto = new FaktaOmBeregningDto();
        byggFrilansAndel();
        BeregningsgrunnlagPrStatusOgAndelDto arbeidsforholdAndel = byggArbeidsforholdMedBgAndel();

        // Act
        FaktaAggregatDto fakta = FaktaAggregatDto.builder()
                .medFaktaAktør(FaktaAktørDto.builder().medHarFLMottattYtelseFastsattAvSaksbehandler(false).build())
                .erstattEksisterendeEllerLeggTil(FaktaArbeidsforholdDto.builder(arbeidsgiver, InternArbeidsforholdRefDto.nullRef())
                        .medHarMottattYtelseFastsattAvSaksbehandler(true)
                        .build()).build();
        var input = new BeregningsgrunnlagGUIInput(koblingReferanse, inntektArbeidYtelseGrunnlag, List.of(), null)
                .medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag)
                        .medFaktaAggregat(fakta)
                        .build(BeregningsgrunnlagTilstand.KOFAKBER_UT));
        dtoTjeneste.lagDto(input, dto);

        // Assert
        VurderMottarYtelseDto mottarYtelseDto = dto.getVurderMottarYtelse();
        assertThat(mottarYtelseDto.getErFrilans()).isTrue();
        assertThat(mottarYtelseDto.getFrilansMottarYtelse()).isFalse();
        assertThat(mottarYtelseDto.getFrilansInntektPrMnd()).isEqualByComparingTo(ModellTyperMapper.beløpTilDto(INNTEKT_SNITT));
        assertThat(mottarYtelseDto.getArbeidstakerAndelerUtenIM()).hasSize(1);
        ArbeidstakerUtenInntektsmeldingAndelDto andelUtenIM = mottarYtelseDto.getArbeidstakerAndelerUtenIM().get(0);
        assertThat(andelUtenIM.getMottarYtelse()).isTrue();
        assertThat(andelUtenIM.getArbeidsforhold().getArbeidsgiverIdent()).isEqualTo(ORGNR);
        assertThat(andelUtenIM.getAndelsnr()).isEqualTo(arbeidsforholdAndel.getAndelsnr());
        assertThat(andelUtenIM.getInntektPrMnd()).isEqualByComparingTo(ModellTyperMapper.beløpTilDto(INNTEKT_SNITT));
    }

    private void byggFrilansAndel() {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(FRILANS_ORGNR);
        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        Intervall frilansPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10));
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilderForType = aktørArbeidBuilder
                .getYrkesaktivitetBuilderForType(ArbeidType.FRILANSER_OPPDRAGSTAKER);
        yrkesaktivitetBuilderForType
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(frilansPeriode))
                .medArbeidsgiver(arbeidsgiver);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilderForType);
        oppdatere.leggTilAktørArbeid(aktørArbeidBuilder);
        BeregningIAYTestUtil.byggInntektForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING, oppdatere, INNTEKT_PR_MND, true, arbeidsgiver);

        inntektArbeidYtelseGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(oppdatere)
                .medOppgittOpptjening(BeregningIAYTestUtil.leggTilOppgittOpptjeningForFL(false, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10)))
                .build();

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusMonths(3), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .build(this.periode);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto byggArbeidsforholdMedBgAndel() {
        InntektArbeidYtelseAggregatBuilder oppdatere = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseGrunnlag.getRegisterVersjon(), VersjonTypeDto.REGISTER);
        Intervall ansettelsesPeriode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10));
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = oppdatere.getAktørArbeidBuilder();
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilderForType = aktørArbeidBuilder
                .getYrkesaktivitetBuilderForType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilderForType
                .medArbeidsgiver(arbeidsgiver)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesPeriode))
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(ansettelsesPeriode)
                        .medErAnsettelsesPeriode(false)
                        .medSisteLønnsendringsdato(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2)));
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilderForType);
        oppdatere.leggTilAktørArbeid(aktørArbeidBuilder);
        oppdatere.medNyInternArbeidsforholdRef(arbeidsgiver, EKSTERN_ARB_ID);

        ArbeidsforholdOverstyringDtoBuilder arbeidsforholdOverstyringDtoBuilder = ArbeidsforholdOverstyringDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medHandling(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING);

        BeregningIAYTestUtil.byggInntektForBehandling(
                SKJÆRINGSTIDSPUNKT_OPPTJENING, oppdatere, INNTEKT_PR_MND, true, arbeidsgiver);

        inntektArbeidYtelseGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(inntektArbeidYtelseGrunnlag)
                .medData(oppdatere)
                .medInformasjon(ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty()).leggTil(arbeidsforholdOverstyringDtoBuilder).build())
                .build();

        return BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusMonths(3), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
                .build(periode);
    }


}
