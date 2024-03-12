package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
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
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.TidligereUtbetalingDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;

public final class LagVurderRefusjonDto {
    private LagVurderRefusjonDto() {
        // Skjuler default
    }

    public static Optional<RefusjonTilVurderingDto> lagDto(Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon,
                                                           BeregningsgrunnlagGUIInput input) {
        List<BeregningsgrunnlagGrunnlagDto> originaleGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (originaleGrunnlag.isEmpty() || originaleGrunnlag.stream().anyMatch(bg -> bg.getBeregningsgrunnlagHvisFinnes().isEmpty())) {
            return Optional.empty();
        }
        var originaleBg = originaleGrunnlag.stream().flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream()).collect(Collectors.toList());

        List<BeregningRefusjonOverstyringDto> gjeldendeOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());

        Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon = input.getIayGrunnlag().getArbeidsforholdInformasjon();
        List<RefusjonAndelTilVurderingDto> dtoer = new ArrayList<>();
        NavigableMap<Intervall, List<RefusjonAndel>> navMap = new TreeMap<>(andelerMedØktRefusjon);
        BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag = input.getBeregningsgrunnlag();
        for (Map.Entry<Intervall, List<RefusjonAndel>> e : navMap.entrySet()) {
            Map.Entry<Intervall, List<RefusjonAndel>> forrigeEntry = navMap.lowerEntry(e.getKey());
            if (forrigeEntry == null) {
                // Første periode, alle andeler skal legges til
                List<RefusjonAndelTilVurderingDto> andeler = e.getValue().stream()
                        .map(andel -> lagAndel(e.getKey(), andel, gjeldendeBeregningsgrunnlag, originaleBg, gjeldendeOverstyringer, arbeidsforholdInformasjon))
                        .collect(Collectors.toList());
                dtoer.addAll(andeler);
            } else {
                // Senere perioden, kun legg til andeler som ikke var i forrige periode (vi vet periodene er sammenhengende)
                List<RefusjonAndelTilVurderingDto> andeler = e.getValue().stream().filter(a -> !forrigeEntry.getValue().contains(a))
                        .map(andel -> lagAndel(e.getKey(), andel, gjeldendeBeregningsgrunnlag, originaleBg, gjeldendeOverstyringer, arbeidsforholdInformasjon))
                        .collect(Collectors.toList());
                dtoer.addAll(andeler);
            }
        }
        return dtoer.isEmpty() ? Optional.empty() : Optional.of(new RefusjonTilVurderingDto(dtoer));
    }

    private static RefusjonAndelTilVurderingDto lagAndel(Intervall periode,
                                                         RefusjonAndel andel,
                                                         BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag,
                                                         List<BeregningsgrunnlagDto> orginalBG,
                                                         List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer,
                                                         Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        RefusjonAndelTilVurderingDto dto = new RefusjonAndelTilVurderingDto();
        // Visningsfelter
        Arbeidsgiver ag = mapArbeidsgiver(andel);
        dto.setArbeidsgiver(ag);
        dto.setInternArbeidsforholdRef(andel.getArbeidsforholdRef().getReferanse());
        dto.setAktivitetStatus(AktivitetStatus.ARBEIDSTAKER); // Hardkoder denne til vi ser en grunn til å bruke andre statuser, er uansett kun AT som har inntektsmeldinger.
        dto.setNyttRefusjonskravFom(periode.getFomDato());
        dto.setTidligereUtbetalinger(finnTidligereUtbetalinger(andel.getArbeidsgiver(), orginalBG));
        mapEksternReferanse(andel, arbeidsforholdInformasjon).ifPresent(ref -> dto.setEksternArbeidsforholdRef(ref.getReferanse()));

        // Sjekk om delvis refusjon skal settes og avklar evt valideringer
        var tidligereRefusjonForAndelIPeriode = finnTidligereUtbetaltRefusjonForAndelIPeriode(periode, andel, orginalBG);
        boolean skalFastsetteDelvisRefusjon = erTilkommetAndelEllerRefusjonTidligereInnvilgetMedLavereBeløpStørreEnnNull(tidligereRefusjonForAndelIPeriode, andel);
        dto.setSkalKunneFastsetteDelvisRefusjon(skalFastsetteDelvisRefusjon);

        // Valideringsfelter
        getTidligsteMuligeRefusjonsdato(andel, gjeldendeOvertyringer)
                .ifPresentOrElse(dto::setTidligsteMuligeRefusjonsdato, () -> dto.setTidligsteMuligeRefusjonsdato(gjeldendeBeregningsgrunnlag.getSkjæringstidspunkt()));
        dto.setMaksTillattDelvisRefusjonPrMnd(ModellTyperMapper.beløpTilDto(månedsbeløp(tidligereRefusjonForAndelIPeriode.orElse(andel.getRefusjon()))));

        // Tidligere fastsatte verdier som brukes til preutfylling av gui
        finnFastsattDelvisRefusjon(gjeldendeBeregningsgrunnlag, andel, periode).map(ModellTyperMapper::beløpTilDto).ifPresent(dto::setFastsattDelvisRefusjonPrMnd);
        getFastsattRefusjonStartdato(gjeldendeOvertyringer, andel).ifPresent(dto::setFastsattNyttRefusjonskravFom);

        return dto;
    }

    private static Optional<EksternArbeidsforholdRef> mapEksternReferanse(RefusjonAndel andel, Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        return arbeidsforholdInformasjon.map(d -> d.finnEkstern(andel.getArbeidsgiver(), andel.getArbeidsforholdRef()));
    }

    private static Optional<Beløp> finnFastsattDelvisRefusjon(BeregningsgrunnlagDto gjeldendeBeregningsgrunnlag, RefusjonAndel andel, Intervall periode) {
        Optional<BeregningsgrunnlagPeriodeDto> bgPeriode = gjeldendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(bgp -> bgp.getPeriode().inkluderer(periode.getFomDato()))
                .findFirst();
        List<BeregningsgrunnlagPrStatusOgAndelDto> bgAndelerIPeriode = bgPeriode
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
                .orElse(Collections.emptyList());
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> matchetAndel = bgAndelerIPeriode.stream()
                .filter(bgAndel -> bgAndel.getArbeidsgiver().isPresent() && bgAndel.getArbeidsgiver().get().equals(andel.getArbeidsgiver())
                && bgAndel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()).gjelderFor(andel.getArbeidsforholdRef()))
                .findFirst();
        var tidligereFastsattRefusjonPrÅr = matchetAndel.flatMap(bga -> bga.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getSaksbehandletRefusjonPrÅr));
        return tidligereFastsattRefusjonPrÅr.map(LagVurderRefusjonDto::månedsbeløp);
    }

    private static Optional<BeregningRefusjonPeriodeDto> finnTidligereOverstyringForAndel(List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer, RefusjonAndel andel) {
        List<BeregningRefusjonPeriodeDto> refusjonperioderForAG = gjeldendeOvertyringer.stream()
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

    private static Optional<Beløp> finnTidligereUtbetaltRefusjonForAndelIPeriode(Intervall periode, RefusjonAndel refusjonAndel, List<BeregningsgrunnlagDto> orginalBG) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andelerIOrginalPeriode = finnOrginalBGPeriode(periode.getFomDato(), orginalBG).getBeregningsgrunnlagPrStatusOgAndelList();
        List<BeregningsgrunnlagPrStatusOgAndelDto> matchedeAndeler = andelerIOrginalPeriode.stream()
                .filter(bga -> {
                    InternArbeidsforholdRefDto bgAndelReferanse = bga.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef());
                    no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver bgAndelAG = bga.getArbeidsgiver().orElse(null);
                    return Objects.equals(bgAndelAG, refusjonAndel.getArbeidsgiver()) && bgAndelReferanse.gjelderFor(refusjonAndel.getArbeidsforholdRef());
                })
                .collect(Collectors.toList());
        return matchedeAndeler.stream()
                .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getGjeldendeRefusjonPrÅr() != null)
                .map(a -> a.getBgAndelArbeidsforhold().get().getGjeldendeRefusjonPrÅr())
                .reduce(Beløp::adder);
    }

    private static BeregningsgrunnlagPeriodeDto finnOrginalBGPeriode(LocalDate fomDato, List<BeregningsgrunnlagDto> orginalBG) {
        var alleOriginalePerioderMedUtbetaling = orginalBG.stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(p -> p.getDagsats() != null && p.getDagsats() > 0)
                .collect(Collectors.toList());
        if (harIngenOverlappMedUtbetaling(fomDato, alleOriginalePerioderMedUtbetaling)) {
            return finnSistePeriodeSomOverlapperEllerFørste(fomDato, orginalBG);
        }
        return alleOriginalePerioderMedUtbetaling.stream()
                .filter(bgp -> bgp.getPeriode().inkluderer(fomDato))
                .findFirst()
                .orElseThrow();
    }

    private static boolean harIngenOverlappMedUtbetaling(LocalDate fomDato, List<BeregningsgrunnlagPeriodeDto> alleOriginalePerioderMedUtbetaling) {
        return alleOriginalePerioderMedUtbetaling.stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).noneMatch(p -> p.inkluderer(fomDato));
    }

    private static BeregningsgrunnlagPeriodeDto finnSistePeriodeSomOverlapperEllerFørste(LocalDate fomDato, List<BeregningsgrunnlagDto> orginalBG) {
        return orginalBG.stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(p -> p.getPeriode().inkluderer(fomDato))
                .max(Comparator.comparing(p -> p.getPeriode().getFomDato()))
                .orElse(finnFørstePeriode(orginalBG));
    }

    private static BeregningsgrunnlagPeriodeDto finnFørstePeriode(List<BeregningsgrunnlagDto> orginalBG) {
        return orginalBG.stream()
                .min(Comparator.comparing(BeregningsgrunnlagDto::getSkjæringstidspunkt))
                .orElseThrow().getBeregningsgrunnlagPerioder().get(0);
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

    private static List<TidligereUtbetalingDto> finnTidligereUtbetalinger(no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver ag, List<BeregningsgrunnlagDto> orginaltBG) {
        List<TidligereUtbetalingDto> tidligereUtbetalinger = new ArrayList<>();
        List<BeregningsgrunnlagPeriodeDto> alleOrginalePerioder = orginaltBG.stream().flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .filter(p -> p.getDagsats() != null && p.getDagsats() > 0)
                .collect(Collectors.toList());
        alleOrginalePerioder.forEach(p -> {
            List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedSammeAG = p.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(andel -> andel.getArbeidsgiver().isPresent())
                    .filter(andel -> andel.getArbeidsgiver().get().equals(ag))
                    .collect(Collectors.toList());
            andelerMedSammeAG.forEach(a -> lagTidligereUtbetaling(a).ifPresent(tidligereUtbetalinger::add));
        });
        return komprimerForHelg(tidligereUtbetalinger);
    }

    private static List<TidligereUtbetalingDto> komprimerForHelg(List<TidligereUtbetalingDto> utbetalinger) {
        var utbetalingSegmenter = utbetalinger.stream().map(t -> new LocalDateSegment<>(t.getFom(), t.getTom(), t.getErTildeltRefusjon()));
        var factory = new TimelineWeekendCompressor.CompressorFactory<Boolean>(Objects::equals, (i, lhs, rhs) -> new LocalDateSegment<>(i, lhs.getValue()));
        TimelineWeekendCompressor<Boolean> compressor = utbetalingSegmenter.collect(factory::get, TimelineWeekendCompressor::accept, TimelineWeekendCompressor::combine);
        return compressor.getSegmenter().stream().map(s -> new TidligereUtbetalingDto(s.getFom(), s.getTom(), s.getValue())).collect(Collectors.toList());
    }

    private static Optional<LocalDate> getFastsattRefusjonStartdato(List<BeregningRefusjonOverstyringDto> gjeldendeOvertyringer, RefusjonAndel andel) {
        return finnTidligereOverstyringForAndel(gjeldendeOvertyringer, andel).map(BeregningRefusjonPeriodeDto::getStartdatoRefusjon);
    }


    private static Optional<TidligereUtbetalingDto> lagTidligereUtbetaling(BeregningsgrunnlagPrStatusOgAndelDto andelIOrginaltGrunnlag) {
        if (andelIOrginaltGrunnlag.getDagsats() == null || andelIOrginaltGrunnlag.getDagsats() == 0) {
            // Ingen utbetaling for andelen på orginalt grunnlag
            return Optional.empty();
        }
        Intervall periode = andelIOrginaltGrunnlag.getBeregningsgrunnlagPeriode().getPeriode();
        if (andelIOrginaltGrunnlag.getDagsatsArbeidsgiver() != null && andelIOrginaltGrunnlag.getDagsatsArbeidsgiver() > 0) {
            return Optional.of(new TidligereUtbetalingDto(periode.getFomDato(), periode.getTomDato(), true));
        }
        return Optional.of(new TidligereUtbetalingDto(periode.getFomDato(), periode.getTomDato(), false));
    }

}
