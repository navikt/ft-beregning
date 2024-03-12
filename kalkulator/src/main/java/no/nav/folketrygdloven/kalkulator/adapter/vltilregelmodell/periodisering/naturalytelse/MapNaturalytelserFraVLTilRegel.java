package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.naturalytelse;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.NaturalytelserPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeModellNaturalytelse;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.MapSplittetPeriodeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

/**
 * Mapper periodemodell for splitting av perioder grunnet naturalytelse.
 * <p>
 * Periodemodellen er den samme modellen som brukes i splitting av perioder grunnet refusjon/gradering/utbetalingsgrad
 */
public class MapNaturalytelserFraVLTilRegel {


    public static PeriodeModellNaturalytelse map(BeregningsgrunnlagInput input,
                                                 BeregningsgrunnlagDto beregningsgrunnlag) {
        precondition(beregningsgrunnlag);
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
        var beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .map(MapSplittetPeriodeFraVLTilRegel::map).collect(Collectors.toList());
        var naturalytelsePrArbeidsforhold = mapInntektsmeldinger(new Input(andeler, input.getInntektsmeldinger()));
        return PeriodeModellNaturalytelse.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medGrunnbeløp(beregningsgrunnlag.getGrunnbeløp().verdi())
                .medInntektsmeldinger(naturalytelsePrArbeidsforhold)
                .medEksisterendePerioder(eksisterendePerioder)
                .build();
    }

    private static void precondition(BeregningsgrunnlagDto beregningsgrunnlag) {
        int antallPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().size();
        if (antallPerioder > 1) {
            throw new IllegalStateException("Forventer kun en periode ved periodisering fra naturalytelse");
        }
    }

    private static List<NaturalytelserPrArbeidsforhold> mapInntektsmeldinger(Input inputTilMapping) {
        return inputTilMapping.getAndeler()
                .stream()
                .filter(MapNaturalytelserFraVLTilRegel::harArbeidsgiver)
                .map(andel -> lagArbeidsforholdOgInntektsmelding(andel, inputTilMapping.getInntektsmeldinger()))
                .collect(Collectors.toList());
    }

    private static boolean harArbeidsgiver(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getArbeidsgiver().isPresent();
    }

    private static Arbeidsforhold lagArbeidsforhold(Collection<InntektsmeldingDto> inntektsmeldinger, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        Optional<InntektsmeldingDto> matchendeInntektsmelding = inntektsmeldinger.stream()
                .filter(im -> andel.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
                .filter(im -> im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .findFirst();
        return MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
                andel.getArbeidsgiver().orElseThrow(() -> new IllegalStateException("Kan ikke mappe arbeidsforhold uten arbeidsgiver")),
                matchendeInntektsmelding.isPresent() ? matchendeInntektsmelding.get().getArbeidsforholdRef() : InternArbeidsforholdRefDto.nullRef());
    }

    private static NaturalytelserPrArbeidsforhold lagArbeidsforholdOgInntektsmelding(BeregningsgrunnlagPrStatusOgAndelDto andel, Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<NaturalYtelse> naturalytelser = mapNaturalytelseFraInntektsmelding(andel, inntektsmeldinger);
        Arbeidsforhold arbeidsforhold = lagArbeidsforhold(inntektsmeldinger, andel);
        return NaturalytelserPrArbeidsforhold.builder()
                .medAndelsnr(andel.getAndelsnr())
                .medNaturalytelser(naturalytelser)
                .medArbeidsforhold(arbeidsforhold)
                .build();
    }

    private static List<NaturalYtelse> mapNaturalytelseFraInntektsmelding(BeregningsgrunnlagPrStatusOgAndelDto andel, Collection<InntektsmeldingDto> inntektsmeldinger) {
        Optional<InntektsmeldingDto> matchendeInntektsmelding = inntektsmeldinger.stream()
                .filter(im -> andel.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
                .findFirst();
        return matchendeInntektsmelding.map(MapNaturalytelser::mapNaturalytelser).orElse(Collections.emptyList());
    }

    public static class Input {
        private final List<BeregningsgrunnlagPrStatusOgAndelDto> andeler;
        private final Collection<InntektsmeldingDto> inntektsmeldinger;

        public Input(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, Collection<InntektsmeldingDto> inntektsmeldinger) {
            this.andeler = Collections.unmodifiableList(andeler);
            this.inntektsmeldinger = inntektsmeldinger;

        }

        public Collection<InntektsmeldingDto> getInntektsmeldinger() {
            return inntektsmeldinger;
        }

        public List<BeregningsgrunnlagPrStatusOgAndelDto> getAndeler() {
            return andeler;
        }

    }

}
