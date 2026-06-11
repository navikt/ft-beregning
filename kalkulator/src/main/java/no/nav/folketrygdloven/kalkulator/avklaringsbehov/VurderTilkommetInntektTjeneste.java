package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilkommetinntekt.NyttInntektsforholdDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilkommetinntekt.VurderTilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.FinnUttaksgradInntektsgradering;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.StandardPeriodeCompressLikhetspredikat;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class VurderTilkommetInntektTjeneste {

    public static VurdertTilkommetInntektResultat løsAvklaringsbehov(VurderTilkommetInntektDto vurderDto, HåndterBeregningsgrunnlagInput input) {
        var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        var vurderteInntektsforholdPerioder = vurderDto.tilkomneInntektsforholdPerioder();
        var segmenter = vurderteInntektsforholdPerioder.stream().map(p -> new LocalDateSegment<>(p.fom(), p.tom(), p.tilkomneInntektsforhold())).toList();
        var tidslinje = new LocalDateTimeline<>(segmenter);
        var splittPeriodeConfig = new SplittPeriodeConfig<>(settVurderingCombinator(input));
        splittPeriodeConfig.setLikhetsPredikatForCompress(StandardPeriodeCompressLikhetspredikat::aldriKomprimer);
        var splitter = new PeriodeSplitter<>(splittPeriodeConfig);
        var splittetGrunnlag = splitter.splittPerioder(input.getBeregningsgrunnlag(), tidslinje);
        grunnlagBuilder.medBeregningsgrunnlag(splittetGrunnlag);

        if (harPeriodeMedReduksjon(vurderDto)) {
            var inputMedTilkommetInntekt = input.medBeregningsgrunnlagGrunnlag(grunnlagBuilder.buildUtenIdOgTilstand());
            var beregningsgrunnlagMedGradering = FinnUttaksgradInntektsgradering.finnInntektsgradering(inputMedTilkommetInntekt);
            return new VurdertTilkommetInntektResultat(
                grunnlagBuilder.medBeregningsgrunnlag(beregningsgrunnlagMedGradering.getBeregningsgrunnlag()).build(BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT),
                beregningsgrunnlagMedGradering.getRegelsporinger().orElseThrow());
        } else {
            return new VurdertTilkommetInntektResultat(grunnlagBuilder.build(BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT), null);
        }


    }

    private static boolean harPeriodeMedReduksjon(VurderTilkommetInntektDto vurderDto) {
        return vurderDto.tilkomneInntektsforholdPerioder()
            .stream()
            .anyMatch(p -> p.tilkomneInntektsforhold().stream().anyMatch(NyttInntektsforholdDto::skalRedusereUtbetaling));
    }

    private static LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, List<NyttInntektsforholdDto>, BeregningsgrunnlagPeriodeDto> settVurderingCombinator(HåndterBeregningsgrunnlagInput input) {
        return (LocalDateInterval di, LocalDateSegment<BeregningsgrunnlagPeriodeDto> lhs, LocalDateSegment<List<NyttInntektsforholdDto>> rhs) -> {
            if (lhs != null && rhs != null) {
                var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                        .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato());
                if (erLagtTilAvSaksbehandler(di, lhs.getValue().getPeriode()) && !rhs.getValue().isEmpty()) {
                    nyPeriode.leggTilPeriodeÅrsak(PeriodeÅrsak.TILKOMMET_INNTEKT_MANUELT);
                }
                rhs.getValue().stream().map(i -> {
                    var tilkommetInntektDto = finnTilkommetInntektTilVurdering(lhs.getValue().getTilkomneInntekter(), i).orElseThrow();
                    return new TilkommetInntektDto(tilkommetInntektDto.getAktivitetStatus(), tilkommetInntektDto.getArbeidsgiver().orElse(null), tilkommetInntektDto.getArbeidsforholdRef(), i.skalRedusereUtbetaling() ? finnBruttoPrÅr(i, input.getIayGrunnlag()) : null, i.skalRedusereUtbetaling() ? utledTilkommetFraBrutto(i, Intervall.fraOgMedTilOgMed(di.getFomDato(), di.getTomDato()), input.getYtelsespesifiktGrunnlag()) : null, i.skalRedusereUtbetaling());
                }).forEach(nyPeriode::leggTilTilkommetInntekt);

                var build = nyPeriode.build();

                if (build.getTilkomneInntekter().stream().anyMatch(it -> it.skalRedusereUtbetaling() == null)) {
                    throw new IllegalStateException(String.format("Periode %s har tilkomne inntektsforhold som ikke har blitt vurdert", build.getPeriode()));
                }

                return new LocalDateSegment<>(di, build);
            } else if (lhs != null) {
                var nyPeriode = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue()).medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato()).build();
                return new LocalDateSegment<>(di, nyPeriode);
            }
            throw new IllegalStateException("Fant inntektsforhold utenfor gyldig periode. Periode var " + di);
        };
    }

    private static boolean erLagtTilAvSaksbehandler(LocalDateInterval di, Intervall periode) {
        return !di.getFomDato().equals(periode.getFomDato());
    }

    private static Optional<TilkommetInntektDto> finnTilkommetInntektTilVurdering(Collection<TilkommetInntektDto> vurderInntektsforhold, NyttInntektsforholdDto i) {
        return vurderInntektsforhold.stream().filter(v -> v.getAktivitetStatus().equals(i.aktivitetStatus()) && (ingenHarArbeidsgiver(i, v) || harLikArbeidsgiver(i, v)) && v.getArbeidsforholdRef().gjelderFor(InternArbeidsforholdRefDto.ref(i.arbeidsforholdId()))).findFirst();
    }

    private static boolean harLikArbeidsgiver(NyttInntektsforholdDto i, TilkommetInntektDto v) {
        return v.getArbeidsgiver().isPresent() && v.getArbeidsgiver().get().getIdentifikator().equals(i.arbeidsgiverIdentifikator());
    }

    private static boolean ingenHarArbeidsgiver(NyttInntektsforholdDto i, TilkommetInntektDto v) {
        return v.getArbeidsgiver().isEmpty() && i.arbeidsgiverIdentifikator() == null;
    }


    private static Beløp finnBruttoPrÅr(NyttInntektsforholdDto i, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (i.bruttoInntektPrÅr() != null) {
            return Beløp.fra(i.bruttoInntektPrÅr());
        }
        var inntektsmelding = finnInntektsmelding(i, iayGrunnlag);
        return inntektsmelding.map(VurderTilkommetInntektTjeneste::mapTilÅrsinntekt).orElse(null);
    }

    private static Optional<InntektsmeldingDto> finnInntektsmelding(NyttInntektsforholdDto i, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getInntektsmeldinger().stream().flatMap(ims -> ims.getAlleInntektsmeldinger().stream()).filter(im -> Objects.equals(im.getArbeidsgiver().getIdentifikator(), i.arbeidsgiverIdentifikator()) && InternArbeidsforholdRefDto.ref(i.arbeidsforholdId()).gjelderFor(im.getArbeidsforholdRef())).findFirst();
    }

    private static Beløp mapTilÅrsinntekt(InntektsmeldingDto inntektsmeldingDto) {
        return inntektsmeldingDto.getInntektBeløp().multipliser(KonfigTjeneste.getMånederIÅr());
    }

    private static Arbeidsgiver mapArbeidsgiver(NyttInntektsforholdDto i) {
        if (i.arbeidsgiverIdentifikator() == null) {
            return null;
        }
        return OrgNummer.erGyldigOrgnr(i.arbeidsgiverIdentifikator()) ? Arbeidsgiver.virksomhet(i.arbeidsgiverIdentifikator()) : Arbeidsgiver.person(new AktørId(i.arbeidsgiverIdentifikator()));
    }

    private static Beløp utledTilkommetFraBrutto(NyttInntektsforholdDto inntektsforhold,
                                                      Intervall periode,
                                                      YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (inntektsforhold.aktivitetStatus().erArbeidstaker() && inntektsforhold.arbeidsgiverIdentifikator() != null) {
                var aktivitetsProsent = UtbetalingsgradTjeneste.finnAktivitetsgradForArbeid(mapArbeidsgiver(inntektsforhold), InternArbeidsforholdRefDto.ref(inntektsforhold.arbeidsforholdId()), periode, ytelsespesifiktGrunnlag, true);
                var utbetalingsprosent = UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(mapArbeidsgiver(inntektsforhold), InternArbeidsforholdRefDto.ref(inntektsforhold.arbeidsforholdId()), periode, ytelsespesifiktGrunnlag, true);
                var tilommetGrad = aktivitetsProsent.map(grad -> grad.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.valueOf(1).subtract(utbetalingsprosent.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
                return Beløp.fra(inntektsforhold.bruttoInntektPrÅr()).multipliser(tilommetGrad);
            } else {
                var utbetalingsprosent = UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(inntektsforhold.aktivitetStatus(), periode, ytelsespesifiktGrunnlag);
                var aktivitetsProsent = UtbetalingsgradTjeneste.finnAktivitetsgradForStatus(inntektsforhold.aktivitetStatus(), periode, ytelsespesifiktGrunnlag);
                var tilommetGrad = aktivitetsProsent.map(grad -> grad.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.valueOf(1).subtract(utbetalingsprosent.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
                return Beløp.fra(inntektsforhold.bruttoInntektPrÅr()).multipliser(tilommetGrad);
            }
        }
        throw new IllegalStateException("Kun gyldig ved utbetalingsgradgrunnlag");
    }

    public record VurdertTilkommetInntektResultat(
        BeregningsgrunnlagGrunnlagDto grunnlag,
        RegelSporingAggregat regelSporingAggregat
    ) {}


}
