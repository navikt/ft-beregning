package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste.finnAktiviteterMedLønnsendringUtenInntektsmelding;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class FastsettBeregningsperiodeForLønnsendring {

    private FastsettBeregningsperiodeForLønnsendring() {

    }

    static BeregningsgrunnlagDto fastsettBeregningsperiodeForLønnsendring(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                          InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                          Collection<InntektsmeldingDto> inntektsmeldinger) {
        if (KonfigurasjonVerdi.instance().get("AUTOMATISK_BEREGNE_LONNSENDRING_V2", false)) {
            return fastsettBeregningsperiodeForLønnsendringV2(beregningsgrunnlag, inntektArbeidYtelseGrunnlag, inntektsmeldinger);
        } else {
            return fastsettBeregningsperiodeForLønnsendringV1(beregningsgrunnlag, inntektArbeidYtelseGrunnlag, inntektsmeldinger);
        }
    }

    static BeregningsgrunnlagDto fastsettBeregningsperiodeForLønnsendringV1(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                            InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                            Collection<InntektsmeldingDto> inntektsmeldinger) {
        Intervall beregningsperiodeATFL = new BeregningsperiodeTjeneste().fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt());
        Intervall toFørsteMåneder = Intervall.fraOgMedTilOgMed(beregningsperiodeATFL.getFomDato().plusDays(1), beregningsperiodeATFL.getTomDato().withDayOfMonth(1));
        Intervall sisteMåned = Intervall.fraOgMedTilOgMed(beregningsperiodeATFL.getTomDato().withDayOfMonth(2), beregningsgrunnlag.getSkjæringstidspunkt());

        List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringUtenInntektsmelding(beregningsgrunnlag, inntektArbeidYtelseGrunnlag, toFørsteMåneder, inntektsmeldinger);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        if (!yrkesaktiviteterMedLønnsendring.isEmpty()) {
            nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
                Map<BeregningsgrunnlagPrStatusOgAndelDto, List<YrkesaktivitetDto>> andelLønnsendringMap = finnAndelAktivitetMap(yrkesaktiviteterMedLønnsendring, periode);
                andelLønnsendringMap.forEach((andel, yrkesaktiviteter) -> {
                    LocalDate sisteLønnsendring = finnSisteLønnsendringIBeregningsperioden(yrkesaktiviteter, beregningsperiodeATFL);
                    if (!sisteMåned.inkluderer(sisteLønnsendring)) {
                        LocalDate beregningsperiodeTom = andel.getBeregningsperiodeTom();
                        LocalDate beregningsperiodeFom = andel.getBeregningsperiodeFom();
                        LocalDate nyFom = sisteLønnsendring.isBefore(beregningsperiodeFom) ? beregningsperiodeFom : sisteLønnsendring;
                        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel).medBeregningsperiode(nyFom, beregningsperiodeTom);
                    }
                });
            });
        }
        return nyttBeregningsgrunnlag;
    }

    static BeregningsgrunnlagDto fastsettBeregningsperiodeForLønnsendringV2(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                            InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                            Collection<InntektsmeldingDto> inntektsmeldinger) {
        Intervall beregningsperiodeATFL = new BeregningsperiodeTjeneste().fastsettBeregningsperiodeForATFLAndeler(beregningsgrunnlag.getSkjæringstidspunkt());

        List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringUtenInntektsmelding(beregningsgrunnlag, inntektArbeidYtelseGrunnlag, beregningsperiodeATFL, inntektsmeldinger);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        if (!yrkesaktiviteterMedLønnsendring.isEmpty()) {
            nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
                Map<BeregningsgrunnlagPrStatusOgAndelDto, List<YrkesaktivitetDto>> andelLønnsendringMap = finnAndelAktivitetMap(yrkesaktiviteterMedLønnsendring, periode);
                andelLønnsendringMap.forEach((andel, yrkesaktiviteter) -> {
                    LocalDate sisteLønnsendring = finnSisteLønnsendringFørStp(yrkesaktiviteter, beregningsgrunnlag.getSkjæringstidspunkt());
                    boolean harIkkeLønnsendringIMånedenFør = harIkkeLønnsendringIMånedenFør(beregningsperiodeATFL, yrkesaktiviteter, sisteLønnsendring);
                    if (beregningsperiodeATFL.inkluderer(sisteLønnsendring) && harIkkeLønnsendringIMånedenFør) {
                        LocalDate beregningsperiodeTom = andel.getBeregningsperiodeTom();
                        LocalDate beregningsperiodeFom = andel.getBeregningsperiodeFom();
                        LocalDate nyFom = sisteLønnsendring.isBefore(beregningsperiodeFom) ? beregningsperiodeFom : sisteLønnsendring;
                        BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel).medBeregningsperiode(nyFom, beregningsperiodeTom);
                    }
                });
            });
        }
        return nyttBeregningsgrunnlag;
    }

    private static boolean harIkkeLønnsendringIMånedenFør(Intervall beregningsperiodeATFL, List<YrkesaktivitetDto> yrkesaktiviteter, LocalDate sisteLønnsendring) {
        Set<LocalDate> lønnsendringer = finnLønnsendringerIBeregningsperioden(yrkesaktiviteter, beregningsperiodeATFL);
        return lønnsendringer.stream().noneMatch(endring -> endring.getMonth().equals(sisteLønnsendring.minusMonths(1).getMonth()));
    }


    public static Map<BeregningsgrunnlagPrStatusOgAndelDto, List<YrkesaktivitetDto>> finnAndelAktivitetMap(List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring, BeregningsgrunnlagPeriodeDto periode) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, List<YrkesaktivitetDto>> andelLønnsendringMap = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> harLønnsendring(yrkesaktiviteterMedLønnsendring, a))
                .collect(Collectors.toMap(a -> a, a -> finnMatchendeYrkesaktiviteterMedLønnsendring(yrkesaktiviteterMedLønnsendring, a)));
        return andelLønnsendringMap;
    }

    public static LocalDate finnSisteLønnsendringIBeregningsperioden(List<YrkesaktivitetDto> yrkesaktiviteter, Intervall beregningsperiode) {
        return yrkesaktiviteter.stream().flatMap(y -> y.getAlleAktivitetsAvtaler().stream())
                .map(AktivitetsAvtaleDto::getSisteLønnsendringsdato)
                .filter(Objects::nonNull)
                .filter(beregningsperiode::inkluderer)
                .max(Comparator.naturalOrder())
                .orElse(beregningsperiode.getFomDato());
    }

    public static LocalDate finnSisteLønnsendringFørStp(List<YrkesaktivitetDto> yrkesaktiviteter, LocalDate stp) {
        return yrkesaktiviteter.stream().flatMap(y -> y.getAlleAktivitetsAvtaler().stream())
                .map(AktivitetsAvtaleDto::getSisteLønnsendringsdato)
                .filter(Objects::nonNull)
                .filter(lønnsendingdato -> lønnsendingdato.isBefore(stp))
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }

    public static Set<LocalDate> finnLønnsendringerIBeregningsperioden(List<YrkesaktivitetDto> yrkesaktiviteter, Intervall beregningsperiode) {
        return yrkesaktiviteter.stream().flatMap(y -> y.getAlleAktivitetsAvtaler().stream())
                .map(AktivitetsAvtaleDto::getSisteLønnsendringsdato)
                .filter(Objects::nonNull)
                .filter(beregningsperiode::inkluderer)
                .collect(Collectors.toSet());
    }

    private static boolean harLønnsendring(List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return a.getAktivitetStatus().erArbeidstaker() &&
                !finnMatchendeYrkesaktiviteterMedLønnsendring(yrkesaktiviteterMedLønnsendring, a).isEmpty();
    }

    private static List<YrkesaktivitetDto> finnMatchendeYrkesaktiviteterMedLønnsendring(List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().isPresent() ? yrkesaktiviteterMedLønnsendring.stream().filter(y -> y.gjelderFor(andel.getBgAndelArbeidsforhold().get().getArbeidsgiver(), andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef())).collect(Collectors.toList()) :
                Collections.emptyList();
    }


}
