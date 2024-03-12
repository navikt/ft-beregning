package no.nav.folketrygdloven.kalkulator.input;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;


public class InntektsmeldingFilter {

    private InntektArbeidYtelseGrunnlagDto iayGrunnlag;

    public InntektsmeldingFilter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        this.iayGrunnlag = iayGrunnlag;
    }

    /**
     * Henter alle inntektsmeldinger for beregning
     * Tar hensyn til inaktive arbeidsforhold, dvs. fjerner de
     * inntektsmeldingene som er koblet til inaktive arbeidsforhold.
     * Spesial håndtering i forbindelse med beregning.
     *
     * @param skjæringstidspunktForOpptjening datoen arbeidsforhold må inkludere eller starte etter for å bli regnet som aktive
     * @return Liste med inntektsmeldinger {@link InntektsmeldingDto}
     */
    public List<InntektsmeldingDto> hentInntektsmeldingerBeregning(LocalDate skjæringstidspunktForOpptjening) {
        LocalDate sistedagForInkluderteAktiviteter = BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktForOpptjening);
        List<InntektsmeldingDto> inntektsmeldinger = iayGrunnlag.getInntektsmeldinger().map(InntektsmeldingAggregatDto::getInntektsmeldingerSomSkalBrukes)
            .orElse(emptyList());

        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        Collection<YrkesaktivitetDto> yrkesaktiviteter = filter.getYrkesaktiviteter();

        // kan ikke filtrere når det ikke finnes yrkesaktiviteter
        if (yrkesaktiviteter.isEmpty()) {
            return inntektsmeldinger;
        }
        return filtrerVekkInntektsmeldingPåInaktiveArbeidsforhold(filter, yrkesaktiviteter, inntektsmeldinger, sistedagForInkluderteAktiviteter);
    }

    /**
     * Filtrer vekk inntektsmeldinger som er knyttet til et arbeidsforhold som har en tom dato som slutter før STP.
     */
    private List<InntektsmeldingDto> filtrerVekkInntektsmeldingPåInaktiveArbeidsforhold(YrkesaktivitetFilterDto filter, Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                        Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                                        LocalDate skjæringstidspunktet) {
        List<InntektsmeldingDto> resultat = new ArrayList<>();

        inntektsmeldinger.forEach(im -> {
            boolean skalLeggeTil = yrkesaktiviteter.stream()
                .anyMatch(y -> {
                    boolean gjelderFor = y.gjelderFor(im);
                    var ansettelsesPerioder = filter.getAnsettelsesPerioder(y);
                    return gjelderFor && ansettelsesPerioder.stream()
                        .anyMatch(ap -> ap.getPeriode().inkluderer(skjæringstidspunktet) || ap.getPeriode().getFomDato().isAfter(skjæringstidspunktet));
                });
            if (skalLeggeTil) {
                resultat.add(im);
            }
        });
        return Collections.unmodifiableList(resultat);
    }
}
