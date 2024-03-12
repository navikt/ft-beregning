package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;

public class InntektsmeldingForAndel {

    /**
     * Finner ut det finnes en inntektsmelding for en aktivitet som går over skjæringstidspunktet som gitt andel/arbeidsforhold kan kobles til.
     *
     * Dette avhenger av startdato for yrkesaktiviteten som inntektsmeldingen gjelder for, og om andelen er en aggregatandel (gjelder for flere arbeidsforhorhold)
     * eller om det er en andel med referanse som gjelder for et spesifikt arbeidsforhold.
     *
     * Denne metoden er kan gi ulikt resultat det som gis fra harInntektsmelding på andelen, fordi harInntektsmelding
     * ikke tar hensyn til aktiviteter som går over skjæringstidspunktet i kombinasjon med aktiviteter som starter etter skjæringstidspunktet.
     *
     * @param arbeidsforholdDto arbeidsforhold for en andel
     * @param inntektsmeldinger alle inntektsmeldinger for saken
     * @param yrkesaktiviteter alle yrkesaktiviteter for beregning
     * @param skjæringstidspunkt skjæringstidspunkt for beregning
     * @return har gitt arbeidsforhold en inntektsmelding
     */
    public static boolean harInntektsmeldingForAndel(BGAndelArbeidsforholdDto arbeidsforholdDto,
                                                     Collection<InntektsmeldingDto> inntektsmeldinger,
                                                     Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                     LocalDate skjæringstidspunkt) {
        var yrkesaktiviteterForAndel = finnYrkesaktiviteterForAndelSomStarterFørOgSlutterPåEllerEtterStp(
                arbeidsforholdDto,
                yrkesaktiviteter,
                skjæringstidspunkt);
        return harMottattInntektsmeldingForMinstEnYrkesaktivitet(yrkesaktiviteterForAndel, inntektsmeldinger);
    }

    private static boolean harMottattInntektsmeldingForMinstEnYrkesaktivitet(Collection<YrkesaktivitetDto> yrkesaktiviteterForAndel,
                                                                             Collection<InntektsmeldingDto> inntektsmeldinger) {
        return yrkesaktiviteterForAndel.stream().anyMatch(ya ->
                inntektsmeldinger.stream().anyMatch(im -> matcherInntektsmeldingYrkesaktivitet(ya, im)));
    }

    private static boolean matcherInntektsmeldingYrkesaktivitet(YrkesaktivitetDto ya, InntektsmeldingDto im) {
        return ya.gjelderFor(im);
    }

    private static List<YrkesaktivitetDto> finnYrkesaktiviteterForAndelSomStarterFørOgSlutterPåEllerEtterStp(BGAndelArbeidsforholdDto arbeidsforhold,
                                                                                                             Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                                             LocalDate skjæringstidspunkt) {
        LocalDate beregningstidspunkt = BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt);
        return yrkesaktiviteter.stream()
                .filter(ya -> ya.getArbeidsgiver() != null &&
                        arbeidsforhold.getArbeidsgiver().getIdentifikator().equals(ya.getArbeidsgiver().getIdentifikator()) &&
                        arbeidsforhold.getArbeidsforholdRef().gjelderFor(ya.getArbeidsforholdRef()))
                .filter(ya -> getAnsettelsesperioder(ya).stream().anyMatch(a -> !a.getPeriode().getFomDato().isAfter(beregningstidspunkt) && a.getPeriode().getTomDato().isAfter(beregningstidspunkt)))
                .collect(Collectors.toList());
    }

    private static List<AktivitetsAvtaleDto> getAnsettelsesperioder(YrkesaktivitetDto ya) {
        return ya.getAlleAktivitetsAvtaler().stream().filter(AktivitetsAvtaleDto::erAnsettelsesPeriode).collect(Collectors.toList());
    }

}
