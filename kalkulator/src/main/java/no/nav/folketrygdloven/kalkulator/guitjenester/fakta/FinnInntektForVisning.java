package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class FinnInntektForVisning {

    private FinnInntektForVisning() {
        // Hide constructor
    }

    public static Beløp finnInntektForPreutfylling(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (Beløp.safeVerdi(andel.getBesteberegningPrÅr()) != null) {
            return andel.getBesteberegningPrÅr().divider(KonfigTjeneste.getMånederIÅr(), 10, RoundingMode.HALF_EVEN);
        }
        return Beløp.safeVerdi(andel.getBeregnetPrÅr()) == null ? null : andel.getBeregnetPrÅr().divider(KonfigTjeneste.getMånederIÅr(), 10, RoundingMode.HALF_EVEN);
    }

    public static Optional<Beløp> finnInntektForKunLese(KoblingReferanse ref,
                                                             BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                             Optional<InntektsmeldingDto> inntektsmeldingForAndel,
                                                             InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                             List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                                             List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler) {
        if (faktaOmBeregningTilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            if (andel.getAktivitetStatus().erFrilanser()) {
                return Optional.empty();
            }
            if (andel.getAktivitetStatus().erArbeidstaker()) {
                if (inntektsmeldingForAndel.isEmpty()) {
                    return Optional.empty();
                }
            }
        }
        if (andel.getAktivitetStatus().erArbeidstaker()) {
            return finnInntektsbeløpForArbeidstaker(ref, andel, inntektsmeldingForAndel, inntektArbeidYtelseGrunnlag, alleAndeler);
        }
        if (andel.getAktivitetStatus().erFrilanser()) {
            return finnMånedsbeløpIBeregningsperiodenForFrilanser(ref, andel, inntektArbeidYtelseGrunnlag);
        }
        if (andel.getAktivitetStatus().erDagpenger()) {
            YtelseFilterDto ytelseFilter = new YtelseFilterDto(inntektArbeidYtelseGrunnlag.getAktørYtelseFraRegister()).før(ref.getSkjæringstidspunktBeregning());
            return FinnInntektFraYtelse.finnÅrbeløpForDagpenger(ref, andel, ytelseFilter, ref.getSkjæringstidspunktBeregning())
                    .map(årsbeløp -> årsbeløp.divider(KonfigTjeneste.getMånederIÅr(), 10, RoundingMode.HALF_EVEN));
        }
        if (andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)) {
            YtelseFilterDto ytelseFilter = new YtelseFilterDto(inntektArbeidYtelseGrunnlag.getAktørYtelseFraRegister()).før(ref.getSkjæringstidspunktBeregning());
            return FinnInntektFraYtelse.finnÅrbeløpFraMeldekortForAndel(ref, andel, ytelseFilter)
                    .map(årsbeløp -> årsbeløp.divider(KonfigTjeneste.getMånederIÅr(), 10, RoundingMode.HALF_EVEN));
        }
        return Optional.empty();
    }

    private static Optional<Beløp> finnInntektsbeløpForArbeidstaker(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                         Optional<InntektsmeldingDto> inntektsmeldingForAndel,
                                                                         InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                         List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler) {
        var inntektsmeldingBeløp = inntektsmeldingForAndel
            .map(InntektsmeldingDto::getInntektBeløp);
        if (inntektsmeldingBeløp.isPresent()) {
            return inntektsmeldingBeløp;
        }
        return inntektsmeldingForAndel.map(InntektsmeldingDto::getInntektBeløp)
                .or(() -> finnMånedsbeløpIBeregningsperiodenForArbeidstaker(ref, andel, inntektArbeidYtelseGrunnlag, alleAndeler));
    }

    private static Optional<Beløp> finnMånedsbeløpIBeregningsperiodenForFrilanser(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                       InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        return InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(
                inntektArbeidYtelseGrunnlag, andel, ref.getSkjæringstidspunktBeregning());
    }

    private static Optional<Beløp> finnMånedsbeløpIBeregningsperiodenForArbeidstaker(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                          InntektArbeidYtelseGrunnlagDto grunnlag, List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler) {
        if (andel.getArbeidsgiver().isEmpty()) {
            // For arbeidstakerandeler uten arbeidsgiver, som etterlønn / sluttpakke.
            return Optional.empty();
        }
        Arbeidsgiver arbeidsgiver = andel.getArbeidsgiver().get();
        List<InntektsmeldingDto> imFraArbeidsgiver = grunnlag.getInntektsmeldinger().stream()
                .flatMap(i -> i.getInntektsmeldingerSomSkalBrukes().stream())
                .filter(im -> im.getArbeidsgiver().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .filter(im -> im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .collect(Collectors.toList());
        var inntektFraInntektsmedlingForAndreArbeidsforholdISammeOrg = imFraArbeidsgiver.stream()
                .map(InntektsmeldingDto::getInntektBeløp)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
        long antallArbeidsforholdUtenIM = finnAntallArbeidsforholdUtenIM(alleAndeler, arbeidsgiver, imFraArbeidsgiver);
        var snittInntektFraAOrdningen = finnSnittinntektForArbeidsgiverPrMåned(ref, andel, grunnlag);
        return snittInntektFraAOrdningen.map(inntekt -> finnAndelAvInntekt(inntektFraInntektsmedlingForAndreArbeidsforholdISammeOrg, antallArbeidsforholdUtenIM, inntekt));
    }

    private static Beløp finnAndelAvInntekt(Beløp inntektFraInntektsmedlingForAndreArbeidsforholdISammeOrg, long antallArbeidsforholdUtenIM, Beløp inntekt) {
        var restInntektForArbeidsforholdUtenIM = inntekt.subtraher(inntektFraInntektsmedlingForAndreArbeidsforholdISammeOrg);
        if (restInntektForArbeidsforholdUtenIM.compareTo(Beløp.ZERO) < 0 || antallArbeidsforholdUtenIM == 0) {
            return Beløp.ZERO;
        } else {
            return restInntektForArbeidsforholdUtenIM.divider(BigDecimal.valueOf(antallArbeidsforholdUtenIM), 10, RoundingMode.HALF_UP);
        }
    }

    private static Optional<Beløp> finnSnittinntektForArbeidsgiverPrMåned(KoblingReferanse ref, BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto grunnlag) {
        return grunnlag.getAktørInntektFraRegister()
            .flatMap(aktørInntekt -> {
                var filter = new InntektFilterDto(aktørInntekt).før(ref.getSkjæringstidspunktBeregning());
                var årsbeløp = InntektForAndelTjeneste.finnSnittinntektPrÅrForArbeidstakerIBeregningsperioden(filter, andel);
                return årsbeløp.map(b -> b.divider(KonfigTjeneste.getMånederIÅr(), 10, RoundingMode.HALF_EVEN));
            });
    }

    private static long finnAntallArbeidsforholdUtenIM(List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler, Arbeidsgiver arbeidsgiver, List<InntektsmeldingDto> imFraArbeidsgiver) {
        return alleAndeler.stream()
                .filter(a -> a.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(a -> a.getArbeidsgiver().isPresent() &&
                a.getArbeidsgiver().get().getIdentifikator().equals(arbeidsgiver.getIdentifikator()) &&
                imFraArbeidsgiver.stream().noneMatch(im -> im.getArbeidsforholdRef().gjelderFor(a.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()))
                )).count();
    }

}
