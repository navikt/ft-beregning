package no.nav.folketrygdloven.kalkulator.testutilities;

import java.time.LocalDate;
import java.time.Month;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.NaturalYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;


public class TestHjelper {

    static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);

    public TestHjelper() {
    }

    private static InntektsmeldingDto lagInntektsmelding(Beløp beløp,
                                                  Arbeidsgiver arbeidsgiver,
                                                  Beløp refusjonskrav, NaturalYtelseDto naturalYtelse) {
        var inntektsmeldingBuilder = InntektsmeldingDtoBuilder.builder();
        inntektsmeldingBuilder.medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingBuilder.medBeløp(beløp);
        if (naturalYtelse != null) {
            inntektsmeldingBuilder.leggTil(naturalYtelse);
        }
        if (refusjonskrav != null) {
            inntektsmeldingBuilder.medRefusjon(refusjonskrav);
        }
        inntektsmeldingBuilder.medArbeidsgiver(arbeidsgiver);
        return inntektsmeldingBuilder.build();
    }

    public static InntektArbeidYtelseAggregatBuilder initBehandlingFor_AT_SN(Beløp skattbarInntekt,
                                                                      int førsteÅr, LocalDate skjæringstidspunkt, String virksomhetOrgnr,
                                                                      Beløp inntektSammenligningsgrunnlag,
                                                                      Beløp inntektBeregningsgrunnlag,
                                                                      InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseBuilder) {
        var register = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        for (var året = LocalDate.of(førsteÅr, Month.JANUARY, 1); året.getYear() < førsteÅr + 3; året = året.plusYears(1)) {
            lagInntektForSN(register, året, skattbarInntekt);
        }
        var fraOgMed = skjæringstidspunkt.minusYears(1).withDayOfMonth(1);
        var tilOgMed = fraOgMed.plusYears(1);
        var arbeidsgiver = Arbeidsgiver.virksomhet(virksomhetOrgnr);
        lagAktørArbeid(register, arbeidsgiver, fraOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        for (var dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            lagInntektForSammenligning(register, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    arbeidsgiver);
            lagInntektForArbeidsforhold(register, dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                    arbeidsgiver);
            lagInntektForOpptjening(register, dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                    virksomhetOrgnr);
        }
        return register;
    }

    public static void lagBehandlingForSN(Beløp skattbarInntekt,
                                   int førsteÅr, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        for (var året = LocalDate.of(førsteÅr, Month.JANUARY, 1); året.getYear() < førsteÅr + 3; året = året.plusYears(1)) {
            lagInntektForSN(inntektArbeidYtelseAggregatBuilder, året, skattbarInntekt);
        }
    }

    private static void lagInntektForSN(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                 LocalDate år, Beløp årsinntekt) {
        var aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();
        var inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektskildeType.SIGRUN, null);
        var inntektspost = InntektspostDtoBuilder.ny()
                .medBeløp(årsinntekt)
                .medPeriode(år.withMonth(1).withDayOfMonth(1), år.withMonth(12).withDayOfMonth(31))
                .medInntektspostType(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE);
        inntektBuilder.leggTilInntektspost(inntektspost);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public static InntektArbeidYtelseAggregatBuilder initBehandlingFL(Beløp inntektSammenligningsgrunnlag,
                                                               Beløp inntektFrilans,
                                                               String virksomhetOrgnr, LocalDate fraOgMed, LocalDate tilOgMed, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {

        var arbeidsgiver = Arbeidsgiver.virksomhet(virksomhetOrgnr);
        lagAktørArbeid(inntektArbeidYtelseAggregatBuilder, arbeidsgiver, fraOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER);
        for (var dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            lagInntektForArbeidsforhold(inntektArbeidYtelseAggregatBuilder, dt, dt.plusMonths(1), inntektFrilans,
                    arbeidsgiver);
            lagInntektForSammenligning(inntektArbeidYtelseAggregatBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    arbeidsgiver);
            lagInntektForOpptjening(inntektArbeidYtelseAggregatBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    virksomhetOrgnr);
        }
        return inntektArbeidYtelseAggregatBuilder;
    }

    public static YrkesaktivitetDtoBuilder lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                                   Arbeidsgiver arbeidsgiver,
                                                   LocalDate fom, ArbeidType arbeidType) {
        var aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
                .getAktørArbeidBuilder();

        var opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        var yrkesaktivitetBuilder = aktørArbeidBuilder
                .getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        var aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        var aktivitetsAvtale = aktivitetsAvtaleBuilder.medPeriode(Intervall.fraOgMed(fom));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
                .medArbeidType(arbeidType)
                .medArbeidsgiver(arbeidsgiver);

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);

        return yrkesaktivitetBuilder;
    }

    public static void lagInntektForSammenligning(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                           LocalDate fom,
                                           LocalDate tom, Beløp månedsbeløp, Arbeidsgiver arbeidsgiver) {
        var opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        var aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

        var kilde = InntektskildeType.INNTEKT_SAMMENLIGNING;
        var inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        var inntektspost = InntektspostDtoBuilder.ny()
                .medBeløp(månedsbeløp)
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(arbeidsgiver);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public static void lagInntektForArbeidsforhold(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                            LocalDate fom,
                                            LocalDate tom, Beløp månedsbeløp, Arbeidsgiver arbeidsgiver) {
        var opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        var aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

        var kilde = InntektskildeType.INNTEKT_BEREGNING;
        var inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        var inntektspost = InntektspostDtoBuilder.ny()
                .medBeløp(månedsbeløp)
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(arbeidsgiver);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    static void lagInntektForOpptjening(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                 LocalDate fom,
                                 LocalDate tom, Beløp månedsbeløp, String virksomhetOrgnr) {
        var opptjeningsnøkkel = OpptjeningsnøkkelDto.forOrgnummer(virksomhetOrgnr);

        var aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

        var kilde = InntektskildeType.INNTEKT_OPPTJENING;
        var inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        var inntektspost = InntektspostDtoBuilder.ny()
                .medBeløp(månedsbeløp)
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhetOrgnr));
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public static InntektsmeldingDto opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver arbeidsgiver, Beløp inntektInntektsmelding,
                                                                     NaturalYtelseDto naturalYtelse,
                                                                     Beløp refusjonskrav) {
        return lagInntektsmelding(inntektInntektsmelding,
                arbeidsgiver,
                refusjonskrav,
                naturalYtelse);
    }


}
