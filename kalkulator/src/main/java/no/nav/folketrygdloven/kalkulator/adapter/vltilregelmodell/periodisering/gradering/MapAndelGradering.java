package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.gradering;

import static no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste.finnAlleYrkesaktiviteterInkludertFjernetIOverstyring;
import static no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.FinnTidslinjeForErNyAktivitet.finnTidslinjeForNyAktivitet;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapArbeidsforholdFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapAktivitetStatusV2FraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;


public final class MapAndelGradering {
    private MapAndelGradering() {
        // private constructor
    }


    public static AndelGradering mapGradering(no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering andelGradering,
                                              BeregningsgrunnlagDto beregningsgrunnlag,
                                              Collection<InntektsmeldingDto> inntektsmeldinger,
                                              YrkesaktivitetFilterDto filter,
                                              LocalDate skjæringstidspunktBeregning) {

        if (andelGradering.getAktivitetStatus().erArbeidstaker()) {
            return mapGraderingForArbeid(andelGradering, beregningsgrunnlag, inntektsmeldinger, filter, skjæringstidspunktBeregning);
        }
        return mapGraderingForFLSN(beregningsgrunnlag, andelGradering, filter, skjæringstidspunktBeregning);

    }



    private static AndelGradering mapGraderingForArbeid(no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering andelGradering,
                                                        BeregningsgrunnlagDto beregningsgrunnlag,
                                                        Collection<InntektsmeldingDto> inntektsmeldinger,
                                                        YrkesaktivitetFilterDto filter,
                                                        LocalDate skjæringstidspunktBeregning) {
        AndelGradering.Builder builder = AndelGradering.builder();
        builder.medNyAktivitetTidslinje(finnTidslinjeForNyAktivitet(
                beregningsgrunnlag,
                UttakArbeidType.ORDINÆRT_ARBEID,
                finnArbeidsforholdReferanse(inntektsmeldinger, andelGradering),
                Optional.of(andelGradering.getArbeidsgiver())
        ));
        Arbeidsforhold arbeidsforhold = lagArbeidsforhold(inntektsmeldinger, andelGradering, filter, skjæringstidspunktBeregning);
        return builder.medAktivitetStatus(AktivitetStatusV2.AT)
                .medGraderinger(mapGraderingPerioder(andelGradering.getGraderinger()))
                .medArbeidsforhold(arbeidsforhold)
                .build();
    }


    private static Arbeidsforhold lagArbeidsforhold(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                    no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering andelGradering,
                                                    YrkesaktivitetFilterDto filter,
                                                    LocalDate skjæringstidspunktBeregning) {
        Optional<YrkesaktivitetDto> yrkesaktivitet = finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(filter, skjæringstidspunktBeregning)
                .stream()
                .filter(ya -> ya.gjelderFor(andelGradering.getArbeidsgiver(), andelGradering.getArbeidsforholdRef()))
                .findFirst();
        Arbeidsforhold arbeidsforhold = MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
                andelGradering.getArbeidsgiver(),
                finnArbeidsforholdReferanse(inntektsmeldinger, andelGradering));
            yrkesaktivitet.ifPresent(ya -> Arbeidsforhold.builder(arbeidsforhold)
                    .medAnsettelsesPeriode(FinnAnsettelsesPeriode.getMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning)));
            return arbeidsforhold;
    }

    private static InternArbeidsforholdRefDto finnArbeidsforholdReferanse(Collection<InntektsmeldingDto> inntektsmeldinger, no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering andelGradering) {
        Optional<InntektsmeldingDto> matchendeInntektsmelding = inntektsmeldinger.stream()
                .filter(im -> andelGradering.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()))
                .filter(im -> im.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .findFirst();
        return matchendeInntektsmelding.isPresent() ? matchendeInntektsmelding.get().getArbeidsforholdRef() : InternArbeidsforholdRefDto.nullRef();
    }


    public static AndelGradering mapGraderingForFLSN(BeregningsgrunnlagDto beregningsgrunnlag,
                                                         no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering andelGradering,
                                                         YrkesaktivitetFilterDto filter,
                                                         LocalDate skjæringstidspunktBeregning) {
        if (andelGradering.getAktivitetStatus().erArbeidstaker()) {
            throw new IllegalArgumentException("Gradering for arbeidstaker skal ikke mappes her");
        }
        var regelAktivitetStatus = MapAktivitetStatusV2FraVLTilRegel.map(andelGradering.getAktivitetStatus(), null);
        List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.Gradering> graderinger = mapGraderingPerioder(andelGradering.getGraderinger());
        AndelGradering.Builder builder = AndelGradering.builder()
            .medAktivitetStatus(regelAktivitetStatus)
            .medGraderinger(graderinger);


        if (andelGradering.getAktivitetStatus().erFrilanser() || andelGradering.getAktivitetStatus().erSelvstendigNæringsdrivende()){
            settTidslinjeForNyAktivitetForStatus(beregningsgrunnlag, builder, andelGradering.getAktivitetStatus());
        }

        // Finner yrkesaktiviteter inkludert fjernet i overstyring siden vi kun er interessert i å lage nye arbeidsforhold for nye aktiviteter (Disse kan ikke fjernes)
        Optional<YrkesaktivitetDto> yrkesaktivitet = finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(filter, skjæringstidspunktBeregning)
            .stream()
            .filter(ya -> ya.gjelderFor(andelGradering.getArbeidsgiver(), andelGradering.getArbeidsforholdRef()))
            .findFirst();

        if (andelGradering.getArbeidsgiver() != null) {
            Arbeidsforhold arbeidsforhold = MapArbeidsforholdFraVLTilRegel.mapArbeidsforhold(
                andelGradering.getArbeidsgiver(),
                andelGradering.getArbeidsforholdRef());
            yrkesaktivitet.ifPresent(ya -> Arbeidsforhold.builder(arbeidsforhold)
                .medAnsettelsesPeriode(FinnAnsettelsesPeriode.getMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning)));
            builder.medArbeidsforhold(arbeidsforhold);
        }
        return builder.build();
    }

    private static List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.Gradering> mapGraderingPerioder(List<no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering.Gradering> graderingList) {
        return graderingList.stream()
            .map(gradering -> new no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.Gradering(
                Periode.of(gradering.getPeriode().getFomDato(), gradering.getPeriode().getTomDato())))
            .collect(Collectors.toList());
    }


    private static void settTidslinjeForNyAktivitetForStatus(BeregningsgrunnlagDto beregningsgrunnlag,
                                                             AndelGradering.Builder builder,
                                                             no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus status) {
        builder.medNyAktivitetTidslinje(finnNyAndelTidslinje(status, beregningsgrunnlag));
    }

    private static LocalDateTimeline<Boolean> finnNyAndelTidslinje(no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus aktivitetstatus, BeregningsgrunnlagDto beregningsgrunnlag) {
        var eksisterendeAndelSegmenter = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPrStatusOgAndelList()
                        .stream().anyMatch(andel -> andel.getAktivitetStatus().equals(aktivitetstatus)))
                .map(p -> new LocalDateSegment<>(p.getBeregningsgrunnlagPeriodeFom(), p.getBeregningsgrunnlagPeriodeTom(), false))
                .collect(Collectors.toList());
        LocalDateTimeline<Boolean> eksisterendeAndelTidslinje = new LocalDateTimeline<>(eksisterendeAndelSegmenter);
        return new LocalDateTimeline<>(beregningsgrunnlag.getSkjæringstidspunkt(), TIDENES_ENDE, true)
                .crossJoin(eksisterendeAndelTidslinje, StandardCombinators::coalesceRightHandSide);
    }



}
