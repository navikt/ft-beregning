package no.nav.folketrygdloven.kalkulator.testutilities;

import static java.util.Collections.singletonList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdOverstyringDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittAnnenAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OpptjeningsnøkkelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;


public class BeregningIAYTestUtil {

    /**
     * Lager oppgitt opptjening for Selvstending næringsdrivende 6 måneder før skjæringstidspunkt med endringsdato en måned før
     * skjæringstidspunkt.
     *
     * Setter virksomhetstype til udefinert som mapper til inntektskategori SELVSTENDING_NÆRINGSDRIVENDE.
     *  @param skjæringstidspunktOpptjening skjæringstidpunkt for opptjening
     * @param nyIArbeidslivet spesifiserer om bruker er ny i arbeidslivet
     * @param iayGrunnlagBuilder IayGrunnlag
     */
    public static void lagOppgittOpptjeningForSN(LocalDate skjæringstidspunktOpptjening, boolean nyIArbeidslivet, InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder) {
        iayGrunnlagBuilder.medOppgittOpptjening(lagOppgittOpptjeningForSN(skjæringstidspunktOpptjening, nyIArbeidslivet));
    }

    /**
     * Lager oppgitt opptjening for Selvstending næringsdrivende 6 måneder før skjæringstidspunkt med endringsdato en måned før
     * skjæringstidspunkt.
     *  @param skjæringstidspunktOpptjening skjæringstidpunkt for opptjening
     * @param nyIArbeidslivet spesifiserer om bruker er ny i arbeidslivet
     * @return Oppgitt opptjening
     */
    private static OppgittOpptjeningDtoBuilder lagOppgittOpptjeningForSN(LocalDate skjæringstidspunktOpptjening, boolean nyIArbeidslivet) {
        return lagOppgittOpptjeningForSN(skjæringstidspunktOpptjening, nyIArbeidslivet,
                singletonList(Periode.of(skjæringstidspunktOpptjening.minusMonths(6), skjæringstidspunktOpptjening)));
    }

    /**
     * Lager oppgitt opptjening for Selvstending næringsdrivende 6 måneder før skjæringstidspunkt med endringsdato en måned før
     * skjæringstidspunkt.
     *  @param skjæringstidspunktOpptjening skjæringstidpunkt for opptjening
     * @param nyIArbeidslivet spesifiserer om bruker er ny i arbeidslivet
     * @param perioder spesifiserer perioder
     * @return Oppgitt opptjening
     */
    private static OppgittOpptjeningDtoBuilder lagOppgittOpptjeningForSN(LocalDate skjæringstidspunktOpptjening, boolean nyIArbeidslivet,
                                                                         Collection<Periode> perioder) {
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> næringBuilders = new ArrayList<>();
        perioder.forEach(periode -> næringBuilders.add(OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
            .medBruttoInntekt(Beløp.fra(10000))
            .medNyIArbeidslivet(nyIArbeidslivet)
            .medPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
            .medVirksomhetType(VirksomhetType.UDEFINERT)
            .medEndringDato(skjæringstidspunktOpptjening.minusMonths(1))));
        oppgittOpptjeningBuilder.leggTilEgneNæringer(næringBuilders);
        return oppgittOpptjeningBuilder;
    }

    /**
     * Lager oppgitt opptjening for frilans.
     *
     * @param erNyOppstartet spesifiserer om frilans er nyoppstartet
     */
    public static OppgittOpptjeningDtoBuilder leggTilOppgittOpptjeningForFL(boolean erNyOppstartet, LocalDate fom) {
        OppgittOpptjeningDtoBuilder oppgittOpptjeningBuilder = OppgittOpptjeningDtoBuilder.ny();
        OppgittFrilansDto frilans = new OppgittFrilansDto();
        frilans.setErNyoppstartet(erNyOppstartet);
        OppgittAnnenAktivitetDto annenAktivitet = new OppgittAnnenAktivitetDto(Intervall.fraOgMed(fom), ArbeidType.FRILANSER);
        oppgittOpptjeningBuilder.leggTilAnnenAktivitet(annenAktivitet);
        oppgittOpptjeningBuilder.leggTilFrilansOpplysninger(frilans);
        return oppgittOpptjeningBuilder;
    }

    public static AktørYtelseDto leggTilAktørytelse(InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder,
                                                    LocalDate fom,
                                                    LocalDate tom, // NOSONAR - brukes bare til test
                                                    YtelseType ytelseType,
                                                    Periode... meldekortPerioder) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder
            .getAktørYtelseBuilder();
        YtelseDtoBuilder ytelseBuilder = YtelseDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medYtelseType(ytelseType);
        if (meldekortPerioder != null) {
            Arrays.asList(meldekortPerioder).forEach(meldekortPeriode -> {
                YtelseAnvistDto ytelseAnvist = lagYtelseAnvist(meldekortPeriode, ytelseBuilder);
                ytelseBuilder.leggTilYtelseAnvist(ytelseAnvist);
            });
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        inntektArbeidYtelseGrunnlagBuilder.medData(inntektArbeidYtelseAggregatBuilder);
        return aktørYtelseBuilder.build();
    }

    private static YtelseAnvistDto lagYtelseAnvist(Periode periode, YtelseDtoBuilder ytelseBuilder) {
        return ytelseBuilder.getAnvistBuilder()
            .medAnvistPeriode(Intervall.fraOgMedTilOgMed(periode.getFom(), periode.getTom()))
            .medUtbetalingsgradProsent(Stillingsprosent.HUNDRED)
            .medDagsats(Beløp.fra(1000))
            .medBeløp(Beløp.fra(10000))
            .build();
    }

    public static void byggArbeidForBehandling(LocalDate skjæringstidspunktOpptjening,
                                               LocalDate fraOgMed,
                                               LocalDate tilOgMed,
                                               InternArbeidsforholdRefDto arbId,
                                               Arbeidsgiver arbeidsgiver,
                                               InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver, Beløp.fra(10), inntektArbeidYtelseGrunnlagBuilder);
    }

    public static void byggArbeidForBehandling(LocalDate skjæringstidspunktOpptjening,
                                               Intervall arbeidsperiode,
                                               InternArbeidsforholdRefDto arbId,
                                               Arbeidsgiver arbeidsgiver,
                                               InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(skjæringstidspunktOpptjening, arbeidsperiode.getFomDato(), arbeidsperiode.getTomDato(), arbId, arbeidsgiver,
                Beløp.fra(10), inntektArbeidYtelseGrunnlagBuilder);
    }

    public static void byggArbeidForBehandling(LocalDate skjæringstidspunktOpptjening,
                                               LocalDate fraOgMed,
                                               LocalDate tilOgMed,
                                               InternArbeidsforholdRefDto arbId,
                                               Arbeidsgiver arbeidsgiver,
                                               Beløp inntektPrMnd,
                                               InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            singletonList(inntektPrMnd),
            arbeidsgiver != null,
            Optional.empty(),
            inntektArbeidYtelseGrunnlagBuilder);
    }


    public static void byggArbeidForBehandling(LocalDate skjæringstidspunktOpptjening, // NOSONAR - brukes bare til test
                                               LocalDate fraOgMed,
                                               LocalDate tilOgMed,
                                               InternArbeidsforholdRefDto arbId,
                                               Arbeidsgiver arbeidsgiver,
                                               Optional<LocalDate> lønnsendringsdato,
                                               InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidForBehandling(skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            singletonList(Beløp.fra(10)), arbeidsgiver != null, lønnsendringsdato, inntektArbeidYtelseGrunnlagBuilder);
    }

    public static void byggArbeidForBehandling(LocalDate skjæringstidspunktOpptjening,
                                                LocalDate fraOgMed,
                                                LocalDate tilOgMed,
                                                InternArbeidsforholdRefDto arbId,
                                                Arbeidsgiver arbeidsgiver,
                                                ArbeidType arbeidType,
                                                List<Beløp> inntektPrMnd,
                                                boolean virksomhetPåInntekt,
                                                Optional<LocalDate> lønnsendringsdato,
                                                InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        byggArbeidInntekt(skjæringstidspunktOpptjening, fraOgMed, tilOgMed, arbId, arbeidsgiver, arbeidType, inntektPrMnd,
            virksomhetPåInntekt, lønnsendringsdato, inntektArbeidYtelseGrunnlagBuilder);
        if (lønnsendringsdato.isPresent()) {
            brukUtenInntektsmelding(arbeidType, arbeidsgiver, skjæringstidspunktOpptjening, inntektArbeidYtelseGrunnlagBuilder);
        }
    }

    private static void byggArbeidInntekt(LocalDate skjæringstidspunktOpptjening,
                                          LocalDate fraOgMed,
                                          LocalDate tilOgMed,
                                          InternArbeidsforholdRefDto arbId,
                                          Arbeidsgiver arbeidsgiver,
                                          ArbeidType arbeidType,
                                          List<Beløp> inntektPrMnd,
                                          boolean virksomhetPåInntekt,
                                          Optional<LocalDate> lønnsendringsdato,
                                          InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(inntektArbeidYtelseGrunnlagBuilder.getKladd().getRegisterVersjon(), VersjonTypeDto.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = inntektArbeidYtelseAggregatBuilder.getAktørArbeidBuilder();
        YrkesaktivitetDtoBuilder yrkesaktivitetBuilder = hentYABuilder(aktørArbeidBuilder, arbeidType, arbeidsgiver, arbId);

        AktivitetsAvtaleDtoBuilder aktivitetsAvtale = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(tilOgMed == null ? Intervall.fraOgMed(fraOgMed) : Intervall.fraOgMedTilOgMed(fraOgMed, tilOgMed))
            .medErAnsettelsesPeriode(false)
            .medSisteLønnsendringsdato(lønnsendringsdato.orElse(null));
        AktivitetsAvtaleDtoBuilder arbeidsperiode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(tilOgMed == null ? Intervall.fraOgMed(fraOgMed) : Intervall.fraOgMedTilOgMed(fraOgMed, tilOgMed));

        yrkesaktivitetBuilder
            .medArbeidType(arbeidType)
            .medArbeidsgiver(arbeidsgiver)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilAktivitetsAvtale(arbeidsperiode);
        if (arbId != null) {
            yrkesaktivitetBuilder.medArbeidsforholdId(arbId);
        }

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørArbeid(aktørArbeid);
        byggInntektForBehandling(skjæringstidspunktOpptjening, inntektArbeidYtelseAggregatBuilder, inntektPrMnd,
            virksomhetPåInntekt, arbeidsgiver);

        inntektArbeidYtelseGrunnlagBuilder.medData(inntektArbeidYtelseAggregatBuilder);
    }

    private static void brukUtenInntektsmelding(ArbeidType arbeidType,
                                                Arbeidsgiver arbeidsgiver,
                                                LocalDate skjæringstidspunktOpptjening,
                                                InntektArbeidYtelseGrunnlagDtoBuilder inntektArbeidYtelseGrunnlagBuilder) {
        var filter = new YrkesaktivitetFilterDto(inntektArbeidYtelseGrunnlagBuilder.getArbeidsforholdInformasjon(), inntektArbeidYtelseGrunnlagBuilder.getKladd().getAktørArbeidFraRegister())
            .før(skjæringstidspunktOpptjening);

        if (!filter.getYrkesaktiviteter().isEmpty()) {
            YrkesaktivitetDto yrkesaktivitet = finnKorresponderendeYrkesaktivitet(filter, arbeidType, arbeidsgiver);
            final ArbeidsforholdInformasjonDtoBuilder informasjonBuilder =
                ArbeidsforholdInformasjonDtoBuilder
                .oppdatere(inntektArbeidYtelseGrunnlagBuilder.getInformasjon());

            final ArbeidsforholdOverstyringDtoBuilder overstyringBuilderFor = informasjonBuilder.getOverstyringBuilderFor(yrkesaktivitet.getArbeidsgiver(),
                yrkesaktivitet.getArbeidsforholdRef());
            overstyringBuilderFor.medHandling(ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING);
            informasjonBuilder.leggTil(overstyringBuilderFor);
            inntektArbeidYtelseGrunnlagBuilder.medInformasjon(informasjonBuilder.build());
        }

    }

    public static YrkesaktivitetDto finnKorresponderendeYrkesaktivitet(YrkesaktivitetFilterDto filter, ArbeidType arbeidType, Arbeidsgiver arbeidsgiver) {
        Collection<YrkesaktivitetDto> yrkesaktiviteter = finnKorresponderendeYrkesaktiviteter(filter, arbeidType);
        return yrkesaktiviteter
            .stream()
            .filter(ya -> ya.getArbeidsgiver().equals(arbeidsgiver))
            .findFirst().get();
    }

    private static Collection<YrkesaktivitetDto> finnKorresponderendeYrkesaktiviteter(YrkesaktivitetFilterDto filter, ArbeidType arbeidType) {
        if (ArbeidType.FRILANSER_OPPDRAGSTAKER.equals(arbeidType)) {
            return filter.getFrilansOppdrag();
        } else {
            return filter.getYrkesaktiviteter();
        }
    }

    private static YrkesaktivitetDtoBuilder hentYABuilder(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder, ArbeidType arbeidType,
                                                          Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbId) {
        if (arbId == null) {
            return aktørArbeidBuilder.getYrkesaktivitetBuilderForType(arbeidType);
        } else {
            return aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(new OpptjeningsnøkkelDto(arbId, arbeidsgiver), arbeidType);
        }

    }

    public static void byggInntektForBehandling(LocalDate skjæringstidspunktOpptjening,
                                                InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder, List<Beløp> inntektPrMnd,
                                                boolean virksomhetPåInntekt, Arbeidsgiver arbeidsgiver) {

        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder();

        InntektDtoBuilder inntektBeregningBuilder = aktørInntekt
            .getInntektBuilder(InntektskildeType.INNTEKT_BEREGNING, OpptjeningsnøkkelDto.forArbeidsforholdIdMedArbeidgiver(null, arbeidsgiver));

        // Lager et år (12 mnd) med inntekt for beregning
        byggInntekt(inntektBeregningBuilder, skjæringstidspunktOpptjening, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        aktørInntekt.leggTilInntekt(inntektBeregningBuilder);

        InntektDtoBuilder inntektSammenligningBuilder = aktørInntekt
            .getInntektBuilder(InntektskildeType.INNTEKT_SAMMENLIGNING, OpptjeningsnøkkelDto.forArbeidsforholdIdMedArbeidgiver(null, arbeidsgiver));

        // Lager et år (12 mnd) med inntekt for sammenligningsgrunnlag
        byggInntekt(inntektSammenligningBuilder, skjæringstidspunktOpptjening, inntektPrMnd, virksomhetPåInntekt, arbeidsgiver);
        aktørInntekt.leggTilInntekt(inntektSammenligningBuilder);

        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
    }

    private static void byggInntekt(InntektDtoBuilder builder, LocalDate skjæringstidspunktOpptjening, List<Beløp> inntektPrMnd, boolean virksomhetPåInntekt,
                                    Arbeidsgiver arbeidsgiver) {
        if (virksomhetPåInntekt) {
            for (int i = 0; i <= 12; i++) {
                var inntekt = getInntekt(inntektPrMnd, i);
                builder
                    .leggTilInntektspost(
                        lagInntektspost(skjæringstidspunktOpptjening.minusMonths(i + 1L).plusDays(1), skjæringstidspunktOpptjening.minusMonths(i), inntekt))
                    .medArbeidsgiver(arbeidsgiver);
            }
        } else {
            for (int i = 0; i <= 12; i++) {
                var inntekt = getInntekt(inntektPrMnd, i);
                builder.leggTilInntektspost(
                    lagInntektspost(skjæringstidspunktOpptjening.minusMonths(i + 1L).plusDays(1), skjæringstidspunktOpptjening.minusMonths(i), inntekt));
            }
        }
    }

    private static Beløp getInntekt(List<Beløp> inntektPrMnd, int i) {
        Beløp inntekt;
        if (inntektPrMnd.size() >= i + 1) {
            inntekt = inntektPrMnd.get(i);
        } else {
            inntekt = inntektPrMnd.get(inntektPrMnd.size() - 1);
        }
        return inntekt;
    }

    private static InntektspostDtoBuilder lagInntektspost(LocalDate fom, LocalDate tom, Beløp lønn) {
        return InntektspostDtoBuilder.ny()
            .medBeløp(lønn)
            .medPeriode(fom, tom)
            .medInntektspostType(InntektspostType.LØNN);
    }
}
