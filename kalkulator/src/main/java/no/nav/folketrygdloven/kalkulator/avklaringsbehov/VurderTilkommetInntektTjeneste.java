package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
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
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.NyttInntektsforholdDto;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling.VurderTilkommetInntektHåndteringDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class VurderTilkommetInntektTjeneste {

    public static BeregningsgrunnlagGrunnlagDto løsAvklaringsbehov(VurderTilkommetInntektHåndteringDto vurderDto, HåndterBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        var vurderteInntektsforholdPerioder = vurderDto.getTilkomneInntektsforholdPerioder();
        var segmenter = vurderteInntektsforholdPerioder.stream().map(p -> new LocalDateSegment<>(p.getFom(), p.getTom(), p.getTilkomneInntektsforhold())).toList();
        var tidslinje = new LocalDateTimeline<>(segmenter);
        var splittPeriodeConfig = new SplittPeriodeConfig<>(settVurderingCombinator(input));
        splittPeriodeConfig.setLikhetsPredikatForCompress(StandardPeriodeCompressLikhetspredikat::aldriKomprimer);
        var splitter = new PeriodeSplitter<>(splittPeriodeConfig);
        var splittetGrunnlag = splitter.splittPerioder(input.getBeregningsgrunnlag(), tidslinje);
        grunnlagBuilder.medBeregningsgrunnlag(splittetGrunnlag);
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.VURDERT_TILKOMMET_INNTEKT_UT);
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
                    return new TilkommetInntektDto(tilkommetInntektDto.getAktivitetStatus(), tilkommetInntektDto.getArbeidsgiver().orElse(null), tilkommetInntektDto.getArbeidsforholdRef(), i.getSkalRedusereUtbetaling() ? finnBruttoPrÅr(i, input.getIayGrunnlag()) : null, i.getSkalRedusereUtbetaling() ? utledTilkommetFraBrutto(i, Intervall.fraOgMedTilOgMed(di.getFomDato(), di.getTomDato()), input.getYtelsespesifiktGrunnlag()) : null, i.getSkalRedusereUtbetaling());
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
        return vurderInntektsforhold.stream().filter(v -> v.getAktivitetStatus().equals(i.getAktivitetStatus()) && (ingenHarArbeidsgiver(i, v) || harLikArbeidsgiver(i, v)) && v.getArbeidsforholdRef().gjelderFor(InternArbeidsforholdRefDto.ref(i.getArbeidsforholdId()))).findFirst();
    }

    private static boolean harLikArbeidsgiver(NyttInntektsforholdDto i, TilkommetInntektDto v) {
        return v.getArbeidsgiver().isPresent() && v.getArbeidsgiver().get().getIdentifikator().equals(i.getArbeidsgiverIdentifikator());
    }

    private static boolean ingenHarArbeidsgiver(NyttInntektsforholdDto i, TilkommetInntektDto v) {
        return v.getArbeidsgiver().isEmpty() && i.getArbeidsgiverIdentifikator() == null;
    }


    private static Beløp finnBruttoPrÅr(NyttInntektsforholdDto i, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (i.getBruttoInntektPrÅr() != null) {
            return Beløp.fra(i.getBruttoInntektPrÅr());
        }
        var inntektsmelding = finnInntektsmelding(i, iayGrunnlag);
        return inntektsmelding.map(VurderTilkommetInntektTjeneste::mapTilÅrsinntekt).orElse(null);
    }

    private static Optional<InntektsmeldingDto> finnInntektsmelding(NyttInntektsforholdDto i, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getInntektsmeldinger().stream().flatMap(ims -> ims.getAlleInntektsmeldinger().stream()).filter(im -> Objects.equals(im.getArbeidsgiver().getIdentifikator(), i.getArbeidsgiverIdentifikator()) && InternArbeidsforholdRefDto.ref(i.getArbeidsforholdId()).gjelderFor(im.getArbeidsforholdRef())).findFirst();
    }

    private static Beløp mapTilÅrsinntekt(InntektsmeldingDto inntektsmeldingDto) {
        return inntektsmeldingDto.getInntektBeløp().multipliser(KonfigTjeneste.getMånederIÅr());
    }

    private static Arbeidsgiver mapArbeidsgiver(NyttInntektsforholdDto i) {
        if (i.getArbeidsgiverIdentifikator() == null) {
            return null;
        }
        return OrgNummer.erGyldigOrgnr(i.getArbeidsgiverIdentifikator()) ? Arbeidsgiver.virksomhet(i.getArbeidsgiverIdentifikator()) : Arbeidsgiver.person(new AktørId(i.getArbeidsgiverIdentifikator()));
    }

    private static Beløp utledTilkommetFraBrutto(NyttInntektsforholdDto inntektsforhold,
                                                      Intervall periode,
                                                      YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (inntektsforhold.getAktivitetStatus().erArbeidstaker() && inntektsforhold.getArbeidsgiverIdentifikator() != null) {
                var aktivitetsProsent = UtbetalingsgradTjeneste.finnAktivitetsgradForArbeid(mapArbeidsgiver(inntektsforhold), InternArbeidsforholdRefDto.ref(inntektsforhold.getArbeidsforholdId()), periode, ytelsespesifiktGrunnlag, true);
                var utbetalingsprosent = UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(mapArbeidsgiver(inntektsforhold), InternArbeidsforholdRefDto.ref(inntektsforhold.getArbeidsforholdId()), periode, ytelsespesifiktGrunnlag, true);
                var tilommetGrad = aktivitetsProsent.map(grad -> grad.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.valueOf(1).subtract(utbetalingsprosent.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
                return Beløp.fra(inntektsforhold.getBruttoInntektPrÅr()).multipliser(tilommetGrad);
            } else {
                var utbetalingsprosent = UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(inntektsforhold.getAktivitetStatus(), periode, ytelsespesifiktGrunnlag);
                var aktivitetsProsent = UtbetalingsgradTjeneste.finnAktivitetsgradForStatus(inntektsforhold.getAktivitetStatus(), periode, ytelsespesifiktGrunnlag);
                var tilommetGrad = aktivitetsProsent.map(grad -> grad.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.valueOf(1).subtract(utbetalingsprosent.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)));
                return Beløp.fra(inntektsforhold.getBruttoInntektPrÅr()).multipliser(tilommetGrad);
            }
        }
        throw new IllegalStateException("Kun gyldig ved utbetalingsgradgrunnlag");
    }
}
