package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SkatteOgAvgiftsregelType;


public class BesteberegningInntektDtoForAndelTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final BigDecimal INNTEKT1 = BigDecimal.valueOf(25000);
    private static final BigDecimal INNTEKT2 = BigDecimal.valueOf(30000);
    private static final BigDecimal INNTEKT3 = BigDecimal.valueOf(35000);
    private static final BigDecimal SNITT_AV_ULIKE_INNTEKTER = INNTEKT1.add(INNTEKT2).add(INNTEKT3).divide(BigDecimal.valueOf(3), RoundingMode.HALF_UP);
    public static final String ORGNR = "379472397427";
    private static final InternArbeidsforholdRefDto ARB_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    private static final String FRILANS_OPPDRAG_ORGNR = "784385345";
    private static final String FRILANS_OPPDRAG_ORGNR2 = "748935793457";

    private Arbeidsgiver arbeidsgiver;
    private Arbeidsgiver frilansArbeidsgiver;
    private Arbeidsgiver frilansArbeidsgiver2;
    private YrkesaktivitetDto arbeidstakerYrkesaktivitet;
    private YrkesaktivitetDto frilansOppdrag;
    private YrkesaktivitetDto frilansOppdrag2;
    private YrkesaktivitetDto frilans;
    private BeregningsgrunnlagPrStatusOgAndelDto arbeidstakerAndel;
    private BeregningsgrunnlagPrStatusOgAndelDto frilansAndel;
    private BeregningsgrunnlagPeriodeDto periode;

    @BeforeEach
    public void setUp() {
        byggArbeidsgiver();
        byggArbeidstakerYrkesaktivitet();
        byggFrilansOppdragAktivitet();
        byggFrilansAktivitet();
        lagBGPeriode();
        lagArbeidstakerAndel();
        lagFrilansAndel();

    }

    @Test
    public void skal_finne_snitt_inntekt_for_arbeidstaker_med_lik_inntekt_pr_mnd() {
        var aktørInntekt = lagAktørInntekt(singletonList(lagLikInntektSiste3Mnd(arbeidsgiver)));
        var filter = new InntektFilterDto(aktørInntekt.build());
        var snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittinntektForArbeidstakerIBeregningsperioden(filter, arbeidstakerAndel).get();
        assertThat(snittIBeregningsperioden).isEqualByComparingTo(Beløp.fra(INNTEKT1));
    }

    @Test
    public void skal_finne_snitt_inntekt_for_arbeidstaker_med_ulik_inntekt_pr_mnd() {
        var aktørInntekt = lagAktørInntekt(singletonList(lagUlikInntektSiste3Mnd(arbeidsgiver)));
        var filter = new InntektFilterDto(aktørInntekt.build());
        var snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittinntektForArbeidstakerIBeregningsperioden(filter, arbeidstakerAndel).get();
        assertThat(snittIBeregningsperioden).isEqualByComparingTo(Beløp.fra(SNITT_AV_ULIKE_INNTEKTER));
    }

    @Test
    public void skal_finne_snitt_inntekt_for_frilans_med_lik_inntekt_pr_mnd() {
        List<InntektDtoBuilder> inntekter = List.of(lagLikInntektSiste3Mnd(arbeidsgiver), lagLikInntektSiste3Mnd(frilansArbeidsgiver));
        List<YrkesaktivitetDto> aktiviteter = List.of(arbeidstakerYrkesaktivitet, frilans, frilansOppdrag);
        var grunnlagEntitet = lagIAYGrunnlagEntitet(inntekter, aktiviteter);
        var snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(grunnlagEntitet, frilansAndel, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        assertThat(snittIBeregningsperioden).hasValueSatisfying(a -> assertThat(a.verdi()).isEqualByComparingTo(INNTEKT1));
    }

    @Test
    public void skal_finne_snitt_inntekt_for_frilans_med_ulik_inntekt_pr_mnd() {
        List<InntektDtoBuilder> inntekter = List.of(lagLikInntektSiste3Mnd(arbeidsgiver), lagUlikInntektSiste3Mnd(frilansArbeidsgiver));
        List<YrkesaktivitetDto> aktiviteter = List.of(arbeidstakerYrkesaktivitet, frilans, frilansOppdrag);
        var grunnlagEntitet = lagIAYGrunnlagEntitet(inntekter, aktiviteter);
        var snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(grunnlagEntitet, frilansAndel, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        assertThat(snittIBeregningsperioden).hasValueSatisfying(a -> assertThat(a.verdi()).isEqualByComparingTo(SNITT_AV_ULIKE_INNTEKTER));
    }

    @Test
    public void skal_finne_snitt_inntekt_for_frilans_med_fleire_oppdragsgivere() {
        List<InntektDtoBuilder> inntekter = List.of(lagLikInntektSiste3Mnd(arbeidsgiver), lagUlikInntektSiste3Mnd(frilansArbeidsgiver), lagUlikInntektSiste3Mnd(frilansArbeidsgiver2));
        List<YrkesaktivitetDto> aktiviteter = List.of(arbeidstakerYrkesaktivitet, frilans, frilansOppdrag, frilansOppdrag2);
        var grunnlagEntitet = lagIAYGrunnlagEntitet(inntekter, aktiviteter);
        var snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(grunnlagEntitet, frilansAndel, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        assertThat(snittIBeregningsperioden).hasValueSatisfying(a -> assertThat(a.verdi()).isEqualByComparingTo(SNITT_AV_ULIKE_INNTEKTER.multiply(BigDecimal.valueOf(2))));
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlagEntitet(List<InntektDtoBuilder> inntekter, List<YrkesaktivitetDto> aktiviteter) {
        var aggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørInntektBuilder = aggregatBuilder.getAktørInntektBuilder();
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
                        .leggTilAktørArbeid(lagAktørArbeid(aktiviteter))
                        .leggTilAktørInntekt(lagAktørInntekt(aktørInntektBuilder, inntekter)));
        return iayGrunnlag.build();
    }

    private InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder lagAktørArbeid(List<YrkesaktivitetDto> aktiviteter) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        aktiviteter.forEach(builder::leggTilYrkesaktivitet);
        return builder;
    }

    private AktivitetsAvtaleDtoBuilder lagAktivitetsavtale() {
        return AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(2)));
    }

    private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder lagAktørInntekt(List<InntektDtoBuilder> inntektList) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        inntektList.forEach(builder::leggTilInntekt);
        return builder;
    }

    private InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder lagAktørInntekt(InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder builder, List<InntektDtoBuilder> inntektList) {
        inntektList.forEach(builder::leggTilInntekt);
        return builder;
    }

    private InntektDtoBuilder lagLikInntektSiste3Mnd(Arbeidsgiver arbeidsgiver) {
        return InntektDtoBuilder.oppdatere(Optional.empty())
                .leggTilInntektspost(lagInntektspost(INNTEKT1, 1))
                .leggTilInntektspost(lagInntektspost(INNTEKT1, 2))
                .leggTilInntektspost(lagInntektspost(INNTEKT1, 3))
                .medArbeidsgiver(arbeidsgiver)
                .medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING);
    }

    private InntektDtoBuilder lagUlikInntektSiste3Mnd(Arbeidsgiver arbeidsgiver) {
        return InntektDtoBuilder.oppdatere(Optional.empty())
                .leggTilInntektspost(lagInntektspost(INNTEKT1, 1))
                .leggTilInntektspost(lagInntektspost(INNTEKT2, 2))
                .leggTilInntektspost(lagInntektspost(INNTEKT3, 3))
                .medArbeidsgiver(arbeidsgiver)
                .medInntektsKilde(InntektskildeType.INNTEKT_BEREGNING);
    }


    private InntektspostDtoBuilder lagInntektspost(BigDecimal inntekt, int mndFørSkjæringstidspunkt) {
        return InntektspostDtoBuilder.ny().medBeløp(Beløp.fra(inntekt))
                .medInntektspostType(InntektspostType.LØNN)
                .medPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(mndFørSkjæringstidspunkt).withDayOfMonth(1),
                        SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(mndFørSkjæringstidspunkt - 1).withDayOfMonth(1).minusDays(1))
                .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.UDEFINERT);
    }

    private void lagArbeidstakerAndel() {
        arbeidstakerAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(arbeidsgiver))
                .medAndelsnr(1L)
                .build(periode);
    }

    private void lagBGPeriode() {
        BeregningsgrunnlagDto beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(91425))
                .build();
        periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlag);
    }

    private void byggArbeidstakerYrkesaktivitet() {
        arbeidstakerYrkesaktivitet = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidsforholdId(ARB_ID)
                .leggTilAktivitetsAvtale(lagAktivitetsavtale())
                .build();
    }

    private void byggArbeidsgiver() {
        arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        frilansArbeidsgiver = Arbeidsgiver.virksomhet(FRILANS_OPPDRAG_ORGNR);
        frilansArbeidsgiver2 = Arbeidsgiver.virksomhet(FRILANS_OPPDRAG_ORGNR2);
    }

    private void lagFrilansAndel() {
        frilansAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .medAndelsnr(2L)
                .build(periode);
    }

    private void byggFrilansAktivitet() {
        frilans = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.FRILANSER)
                .build();
    }

    private void byggFrilansOppdragAktivitet() {
        frilansOppdrag = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.FRILANSER_OPPDRAGSTAKER)
                .medArbeidsgiver(frilansArbeidsgiver)
                .leggTilAktivitetsAvtale(lagAktivitetsavtale())
                .build();
        frilansOppdrag2 = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidType(ArbeidType.FRILANSER_OPPDRAGSTAKER)
                .medArbeidsgiver(frilansArbeidsgiver2)
                .leggTilAktivitetsAvtale(lagAktivitetsavtale())
                .build();
    }

}
