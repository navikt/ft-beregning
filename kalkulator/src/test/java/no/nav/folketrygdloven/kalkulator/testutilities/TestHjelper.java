package no.nav.folketrygdloven.kalkulator.testutilities;

import java.time.LocalDate;
import java.time.Month;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
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

    private InntektsmeldingDto lagInntektsmelding(Beløp beløp,
                                                  Arbeidsgiver arbeidsgiver,
                                                  Beløp refusjonskrav, NaturalYtelseDto naturalYtelse) {
        InntektsmeldingDtoBuilder inntektsmeldingBuilder = InntektsmeldingDtoBuilder.builder();
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

    public InntektArbeidYtelseAggregatBuilder initBehandlingFor_AT_SN(Beløp skattbarInntekt,
                                                                      int førsteÅr, LocalDate skjæringstidspunkt, String virksomhetOrgnr,
                                                                      Beløp inntektSammenligningsgrunnlag,
                                                                      Beløp inntektBeregningsgrunnlag,
                                                                      InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseBuilder) {
        InntektArbeidYtelseAggregatBuilder register = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        for (LocalDate året = LocalDate.of(førsteÅr, Month.JANUARY, 1); året.getYear() < førsteÅr + 3; året = året.plusYears(1)) {
            lagInntektForSN(register, året, skattbarInntekt);
        }
        LocalDate fraOgMed = skjæringstidspunkt.minusYears(1).withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhetOrgnr);
        lagAktørArbeid(register, arbeidsgiver, fraOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            lagInntektForSammenligning(register, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    arbeidsgiver);
            lagInntektForArbeidsforhold(register, dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                    arbeidsgiver);
            lagInntektForOpptjening(register, dt, dt.plusMonths(1), inntektBeregningsgrunnlag,
                    virksomhetOrgnr);
        }
        return register;
    }

    public void lagBehandlingForSN(Beløp skattbarInntekt,
                                   int førsteÅr, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        for (LocalDate året = LocalDate.of(førsteÅr, Month.JANUARY, 1); året.getYear() < førsteÅr + 3; året = året.plusYears(1)) {
            lagInntektForSN(inntektArbeidYtelseAggregatBuilder, året, skattbarInntekt);
        }
    }

    private void lagInntektForSN(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                 LocalDate år, Beløp årsinntekt) {
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();
        InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(InntektskildeType.SIGRUN, null);
        InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
                .medBeløp(årsinntekt)
                .medPeriode(år.withMonth(1).withDayOfMonth(1), år.withMonth(12).withDayOfMonth(31))
                .medInntektspostType(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE);
        inntektBuilder.leggTilInntektspost(inntektspost);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public InntektArbeidYtelseAggregatBuilder initBehandlingFL(Beløp inntektSammenligningsgrunnlag,
                                                               Beløp inntektFrilans,
                                                               String virksomhetOrgnr, LocalDate fraOgMed, LocalDate tilOgMed, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {

        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(virksomhetOrgnr);
        lagAktørArbeid(inntektArbeidYtelseAggregatBuilder, arbeidsgiver, fraOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER);
        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            lagInntektForArbeidsforhold(inntektArbeidYtelseAggregatBuilder, dt, dt.plusMonths(1), inntektFrilans,
                    arbeidsgiver);
            lagInntektForSammenligning(inntektArbeidYtelseAggregatBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    arbeidsgiver);
            lagInntektForOpptjening(inntektArbeidYtelseAggregatBuilder, dt, dt.plusMonths(1), inntektSammenligningsgrunnlag,
                    virksomhetOrgnr);
        }
        return inntektArbeidYtelseAggregatBuilder;
    }

    public YrkesaktivitetDtoBuilder lagAktørArbeid(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                                   Arbeidsgiver arbeidsgiver,
                                                   LocalDate fom, ArbeidType arbeidType) {
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder
                .getAktørArbeidBuilder();

        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = aktørArbeidBuilder
                .getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsnøkkel, arbeidType);
        AktivitetsAvtaleDtoBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        AktivitetsAvtaleDtoBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder.medPeriode(Intervall.fraOgMed(fom));
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtale)
                .medArbeidType(arbeidType)
                .medArbeidsgiver(arbeidsgiver);

        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);

        return yrkesaktivitetBuilder;
    }

    public void lagInntektForSammenligning(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                           LocalDate fom,
                                           LocalDate tom, Beløp månedsbeløp, Arbeidsgiver arbeidsgiver) {
        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

        InntektskildeType kilde = InntektskildeType.INNTEKT_SAMMENLIGNING;
        InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
                .medBeløp(månedsbeløp)
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(arbeidsgiver);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public void lagInntektForArbeidsforhold(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                            LocalDate fom,
                                            LocalDate tom, Beløp månedsbeløp, Arbeidsgiver arbeidsgiver) {
        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forArbeidsgiver(arbeidsgiver);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

        InntektskildeType kilde = InntektskildeType.INNTEKT_BEREGNING;
        InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
                .medBeløp(månedsbeløp)
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost).medArbeidsgiver(arbeidsgiver);
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    void lagInntektForOpptjening(InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                                 LocalDate fom,
                                 LocalDate tom, Beløp månedsbeløp, String virksomhetOrgnr) {
        OpptjeningsnøkkelDto opptjeningsnøkkel = OpptjeningsnøkkelDto.forOrgnummer(virksomhetOrgnr);

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntektBuilder = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

        InntektskildeType kilde = InntektskildeType.INNTEKT_OPPTJENING;
        InntektDtoBuilder inntektBuilder = aktørInntektBuilder.getInntektBuilder(kilde, opptjeningsnøkkel);
        InntektspostDtoBuilder inntektspost = InntektspostDtoBuilder.ny()
                .medBeløp(månedsbeløp)
                .medPeriode(fom, tom)
                .medInntektspostType(InntektspostType.LØNN);
        inntektBuilder.leggTilInntektspost(inntektspost)
                .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhetOrgnr));
        aktørInntektBuilder.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntektBuilder);
    }

    public InntektsmeldingDto opprettInntektsmeldingMedRefusjonskrav(Arbeidsgiver arbeidsgiver, Beløp inntektInntektsmelding,
                                                                     NaturalYtelseDto naturalYtelse,
                                                                     Beløp refusjonskrav) {
        return lagInntektsmelding(inntektInntektsmelding,
                arbeidsgiver,
                refusjonskrav,
                naturalYtelse);
    }


}
