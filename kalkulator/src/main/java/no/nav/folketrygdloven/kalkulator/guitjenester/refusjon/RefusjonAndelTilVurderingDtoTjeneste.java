package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.TimelineWeekendCompressor;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;

public final class RefusjonAndelTilVurderingDtoTjeneste {
    private RefusjonAndelTilVurderingDtoTjeneste() {
        // Skjuler default
    }

    public static List<RefusjonAndelTilVurderingDto> lagDtoListe(Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon,
                                                                 BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag,
                                                                 List<BeregningsgrunnlagDto> originaleGrunnlag,
                                                                 List<BeregningRefusjonOverstyringDto> gjeldendeOverstyringer,
                                                                 Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon
                                                                 ) {
        var andelerMedØktRefusjonNavigable = new TreeMap<>(andelerMedØktRefusjon);
        return andelerMedØktRefusjonNavigable.entrySet().stream().flatMap(entry -> {
            var forrigeEntry = andelerMedØktRefusjonNavigable.lowerEntry(entry.getKey());
            return entry.getValue()
                .stream()
                .filter(andel -> forrigeEntry == null || !forrigeEntry.getValue().contains(andel))
                .map(andel -> lagAndel(entry.getKey(), andel, gjeldendeBeregningsgrunnlag, originaleGrunnlag, gjeldendeOverstyringer,
                    arbeidsforholdInformasjon));
        }).toList();
    }

    private static RefusjonAndelTilVurderingDto lagAndel(Intervall periode,
                                                         RefusjonAndel andel,
                                                         BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag,
                                                         List<BeregningsgrunnlagDto> originaleGrunnlag,
                                                         List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer,
                                                         Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        var dto = new RefusjonAndelTilVurderingDto();
        // Visningsfelter
        dto.setArbeidsgiver(mapArbeidsgiver(andel));
        dto.setInternArbeidsforholdRef(andel.getArbeidsforholdRef().getReferanse());
        dto.setAktivitetStatus(AktivitetStatus.ARBEIDSTAKER); // Hardkoder denne til vi ser en grunn til å bruke andre statuser, er uansett kun AT som har inntektsmeldinger.
        dto.setNyttRefusjonskravFom(periode.getFomDato());
        dto.setTidligereUtbetalinger(finnTidligereUtbetalinger(andel.getArbeidsgiver(), originaleGrunnlag));
        mapEksternReferanse(andel, arbeidsforholdInformasjon).ifPresent(ref -> dto.setEksternArbeidsforholdRef(ref.getReferanse()));

        // Sjekk om delvis refusjon skal settes og avklar evt valideringer
        var tidligereRefusjonForAndelIPeriode = finnTidligereUtbetaltRefusjonForAndelIPeriode(periode, andel, originaleGrunnlag);
        var skalFastsetteDelvisRefusjon = erTilkommetAndelEllerRefusjonTidligereInnvilgetMedLavereBeløpStørreEnnNull(tidligereRefusjonForAndelIPeriode, andel);
        dto.setSkalKunneFastsetteDelvisRefusjon(skalFastsetteDelvisRefusjon);

        // Valideringsfelter
        getTidligsteMuligeRefusjonsdato(andel, gjeldendeOvertyringer)
                .ifPresentOrElse(dto::setTidligsteMuligeRefusjonsdato, () -> dto.setTidligsteMuligeRefusjonsdato(gjeldendeBeregningsgrunnlag.getSkjæringstidspunkt()));
        dto.setMaksTillattDelvisRefusjonPrMnd(ModellTyperMapper.beløpTilDto(månedsbeløp(tidligereRefusjonForAndelIPeriode.orElse(andel.getRefusjon()))));

        // Tidligere fastsatte verdier som brukes til preutfylling av gui
        finnFastsattDelvisRefusjon(gjeldendeBeregningsgrunnlag, andel, periode).map(ModellTyperMapper::beløpTilDto).ifPresent(dto::setFastsattDelvisRefusjonPrMnd);
        finnTidligereOverstyringForAndel(gjeldendeOvertyringer, andel).map(BeregningRefusjonPeriodeDto::getStartdatoRefusjon).ifPresent(dto::setFastsattNyttRefusjonskravFom);

        return dto;
    }

    private static Optional<EksternArbeidsforholdRef> mapEksternReferanse(RefusjonAndel andel, Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        return arbeidsforholdInformasjon.map(d -> d.finnEkstern(andel.getArbeidsgiver(), andel.getArbeidsforholdRef()));
    }

    private static Optional<Beløp> finnFastsattDelvisRefusjon(BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag, RefusjonAndel andel, Intervall periode) {
        var bgPeriode = gjeldendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(bgp -> bgp.getPeriode().inkluderer(periode.getFomDato()))
                .findFirst();
        var bgAndelerIPeriode = bgPeriode
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
                .orElse(Collections.emptyList());
        var matchetAndel = bgAndelerIPeriode.stream()
                .filter(bgAndel -> bgAndel.getArbeidsgiver().isPresent() && bgAndel.getArbeidsgiver().get().equals(andel.getArbeidsgiver())
                && bgAndel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()).gjelderFor(andel.getArbeidsforholdRef()))
                .findFirst();
        var tidligereFastsattRefusjonPrÅr = matchetAndel.flatMap(bga -> bga.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getSaksbehandletRefusjonPrÅr));
        return tidligereFastsattRefusjonPrÅr.map(RefusjonAndelTilVurderingDtoTjeneste::månedsbeløp);
    }

    private static Optional<BeregningRefusjonPeriodeDto> finnTidligereOverstyringForAndel(List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer, RefusjonAndel andel) {
        var refusjonperioderForAG = gjeldendeOvertyringer.stream()
                .filter(os -> os.getArbeidsgiver().getIdentifikator().equals(andel.getArbeidsgiver().getIdentifikator()))
                .findFirst()
                .map(BeregningRefusjonOverstyringDto::getRefusjonPerioder)
                .orElse(Collections.emptyList());
        return refusjonperioderForAG.stream()
                .filter(refusjon -> refusjon.getArbeidsforholdRef().gjelderFor(andel.getArbeidsforholdRef()))
                .findFirst();
    }

    private static Beløp månedsbeløp(Beløp årsbeløp) {
        if (Beløp.safeVerdi(årsbeløp) == null) {
            return Beløp.ZERO;
        }
        return årsbeløp.divider(KonfigTjeneste.getMånederIÅr(), 0, RoundingMode.HALF_UP);
    }

    private static boolean erTilkommetAndelEllerRefusjonTidligereInnvilgetMedLavereBeløpStørreEnnNull(Optional<Beløp> tidligereRefusjonForAndelIPeriode, RefusjonAndel andel) {
        return tidligereRefusjonForAndelIPeriode.map(ref -> ref.compareTo(Beløp.ZERO) > 0 && andel.getRefusjon().compareTo(ref) > 0).orElse(true);
    }

    private static Optional<Beløp> finnTidligereUtbetaltRefusjonForAndelIPeriode(Intervall periode, RefusjonAndel refusjonAndel, List<BeregningsgrunnlagDto> originaleGrunnlag) {
        var andelerIOrginalPeriode = finnOriginaleGrunnlagPeriode(periode.getFomDato(), originaleGrunnlag).getBeregningsgrunnlagPrStatusOgAndelList();
        var matchedeAndeler = andelerIOrginalPeriode.stream()
                .filter(bga -> erSammeArbeidsgiverOgArbeidsforhold(bga, refusjonAndel))
                .toList();
        return matchedeAndeler.stream()
                .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getGjeldendeRefusjonPrÅr() != null)
                .map(a -> a.getBgAndelArbeidsforhold().get().getGjeldendeRefusjonPrÅr())
                .reduce(Beløp::adder);
    }

    private static BeregningsgrunnlagPeriodeDto finnOriginaleGrunnlagPeriode(LocalDate fomDato, List<BeregningsgrunnlagDto> originaleGrunnlag) {
        var alleOriginalePerioderMedUtbetaling = originaleGrunnlag.stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(p -> p.getDagsats() != null && p.getDagsats() > 0)
                .toList();

        return alleOriginalePerioderMedUtbetaling.stream().filter(bgp -> bgp.getPeriode().inkluderer(fomDato)).findFirst().or(() ->
            // Hvis ingen overlapper, finn siste som overlapper eller første periode
            Optional.of(finnSistePeriodeSomOverlapperEllerFørste(fomDato, originaleGrunnlag))).orElseThrow();
    }

    private static BeregningsgrunnlagPeriodeDto finnSistePeriodeSomOverlapperEllerFørste(LocalDate fomDato, List<BeregningsgrunnlagDto> originaleGrunnlag) {
        return originaleGrunnlag.stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(p -> p.getPeriode().inkluderer(fomDato))
                .max(Comparator.comparing(p -> p.getPeriode().getFomDato()))
                .orElseGet(() -> finnFørstePeriode(originaleGrunnlag));
    }

    private static BeregningsgrunnlagPeriodeDto finnFørstePeriode(List<BeregningsgrunnlagDto> originaleGrunnlag) {
        return originaleGrunnlag.stream()
                .min(Comparator.comparing(BeregningsgrunnlagDto::getSkjæringstidspunkt))
                .orElseThrow().getBeregningsgrunnlagPerioder().getFirst();
    }

    private static boolean erSammeArbeidsgiverOgArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto bga, RefusjonAndel refusjonAndel) {
        var bgAndelReferanse = bga.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef());
        var bgAndelAG = bga.getArbeidsgiver().orElse(null);
        return Objects.equals(bgAndelAG, refusjonAndel.getArbeidsgiver()) && bgAndelReferanse.gjelderFor(refusjonAndel.getArbeidsforholdRef());
    }

    private static Optional<LocalDate> getTidligsteMuligeRefusjonsdato(RefusjonAndel andel, List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer) {
        return gjeldendeOvertyringer.stream()
                .filter(os -> os.getArbeidsgiver().getIdentifikator().equals(andel.getArbeidsgiver().getIdentifikator()))
                .findFirst()
                .flatMap(BeregningRefusjonOverstyringDto::getFørsteMuligeRefusjonFom);
    }

    private static Arbeidsgiver mapArbeidsgiver(RefusjonAndel andel) {
        return andel.getArbeidsgiver().erAktørId()
                ? new Arbeidsgiver(null, andel.getArbeidsgiver().getAktørId().getId())
                : new Arbeidsgiver(andel.getArbeidsgiver().getOrgnr(), null);
    }

    private static List<TidligereUtbetalingDto> finnTidligereUtbetalinger(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver arbeidsgiver, List<BeregningsgrunnlagDto> originaleGrunnlag) {
        var tidligereUtbetalinger = originaleGrunnlag.stream()
            // alle perioder med dagsats > 0
            .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
            .filter(p -> p.getDagsats() != null && p.getDagsats() > 0)
            // hent alle andeler for arbeidsgiver
            .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> andel.getArbeidsgiver().isPresent())
                .filter(andel -> andel.getArbeidsgiver().get().equals(arbeidsgiver))
                .flatMap(andel -> lagTidligereUtbetaling(andel).stream()))
            .toList();

        return komprimerForHelg(tidligereUtbetalinger);
    }

    private static List<TidligereUtbetalingDto> komprimerForHelg(List<TidligereUtbetalingDto> utbetalinger) {
        var utbetalingSegmenter = utbetalinger.stream().map(t -> new LocalDateSegment<>(t.getFom(), t.getTom(), t.getErTildeltRefusjon()));
        var factory = new TimelineWeekendCompressor.CompressorFactory<Boolean>(Objects::equals, (i, lhs, rhs) -> new LocalDateSegment<>(i, lhs.getValue()));
        var compressor = utbetalingSegmenter.collect(factory::get, TimelineWeekendCompressor::accept, TimelineWeekendCompressor::combine);
        return compressor.getSegmenter().stream().map(s -> new TidligereUtbetalingDto(s.getFom(), s.getTom(), s.getValue())).toList();
    }

    private static Optional<TidligereUtbetalingDto> lagTidligereUtbetaling(BeregningsgrunnlagPrStatusOgAndelDto andelIOrginaltGrunnlag) {
        if (andelIOrginaltGrunnlag.getDagsats() == null || andelIOrginaltGrunnlag.getDagsats() == 0) {
            return Optional.empty();
        }
        var periode = andelIOrginaltGrunnlag.getBeregningsgrunnlagPeriode().getPeriode();
        if (andelIOrginaltGrunnlag.getDagsatsArbeidsgiver() != null && andelIOrginaltGrunnlag.getDagsatsArbeidsgiver() > 0) {
            return Optional.of(new TidligereUtbetalingDto(periode.getFomDato(), periode.getTomDato(), true));
        }
        return Optional.of(new TidligereUtbetalingDto(periode.getFomDato(), periode.getTomDato(), false));
    }
}
