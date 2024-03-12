package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndelNøkkel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriodeEndring;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.TimelineWeekendCompressor;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

/**
 * Tjeneste for å vurdere refusjonskrav for beregning
 * I noen tilfeller, f.eks ved innsending av refusjonskrav etter allerede utbetalt periode, skal ikke alltid
 * dette refusjonskravet gjelde fra den perioden arbeidsgiver ber om det. Da kan saksbehandler måtte fastsette startdato
 * for refusjon selv.
 */
public final class BeregningRefusjonTjeneste {

    private BeregningRefusjonTjeneste() {
        // SKjuler default
    }

    /**
     * @param revurderingBeregningsgrunnlag - nytt beregningsgrunnlag
     * @param originaltBeregningsgrunnlag   - beregningsgrunnlag fra forrige behandling
     * @param alleredeUtbetaltTOM           - datoen ytelse er utbetalt til, det er kun relevant å se på perioder frem til denne datoen
     * @param ytelsespesifiktGrunnlag
     * @return - Ser på revurderingBeregningsgrunnlag og sjekker hvilke andeler i hvilke perioder
     * frem til alleredeUtbetaltTOM som har hatt økt refusjon i forhold til originaltBeregningsgrunnlag
     */
    public static Map<Intervall, List<RefusjonAndel>> finnUtbetaltePerioderMedAndelerMedØktRefusjon(BeregningsgrunnlagDto revurderingBeregningsgrunnlag,
                                                                                                    BeregningsgrunnlagDto originaltBeregningsgrunnlag,
                                                                                                    LocalDate alleredeUtbetaltTOM,
                                                                                                    Beløp grenseverdi,
                                                                                                    YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (alleredeUtbetaltTOM.isBefore(revurderingBeregningsgrunnlag.getSkjæringstidspunkt())) {
            return Collections.emptyMap();
        }
        LocalDateTimeline<RefusjonPeriode> alleredeUtbetaltPeriode = finnAlleredeUtbetaltPeriode(alleredeUtbetaltTOM);
        LocalDateTimeline<RefusjonPeriode> originalUtbetaltTidslinje = RefusjonTidslinjeTjeneste.lagTidslinje(originaltBeregningsgrunnlag, true, ytelsespesifiktGrunnlag).intersection(alleredeUtbetaltPeriode);
        LocalDateTimeline<RefusjonPeriode> revurderingTidslinje = RefusjonTidslinjeTjeneste.lagTidslinje(revurderingBeregningsgrunnlag, false, ytelsespesifiktGrunnlag);
        LocalDateTimeline<RefusjonPeriodeEndring> endringTidslinje = RefusjonTidslinjeTjeneste.kombinerTidslinjer(originalUtbetaltTidslinje, revurderingTidslinje);
        var helgekomprimertTidslinje = komprimerForHelg(endringTidslinje);
        return vurderPerioder(helgekomprimertTidslinje, grenseverdi);
    }

    private static LocalDateTimeline<RefusjonPeriodeEndring> komprimerForHelg(LocalDateTimeline<RefusjonPeriodeEndring> endringTidslinje) {
        var factory = new TimelineWeekendCompressor.CompressorFactory<RefusjonPeriodeEndring>(Objects::equals, (i, lhs, rhs) -> new LocalDateSegment<>(i, lhs.getValue()));
        TimelineWeekendCompressor<RefusjonPeriodeEndring> compressor = endringTidslinje.toSegments().stream()
                .collect(factory::get, TimelineWeekendCompressor::accept, TimelineWeekendCompressor::combine);
        return new LocalDateTimeline<>(compressor.getSegmenter());
    }

    private static LocalDateTimeline<RefusjonPeriode> finnAlleredeUtbetaltPeriode(LocalDate alleredeUtbetaltTOM) {
        return new LocalDateTimeline<>(
                TIDENES_BEGYNNELSE,
                alleredeUtbetaltTOM,
                null);
    }

    private static Map<Intervall, List<RefusjonAndel>> vurderPerioder(LocalDateTimeline<RefusjonPeriodeEndring> endringTidslinje, Beløp grenseverdi) {
        Map<Intervall, List<RefusjonAndel>> andelerIPeriode = new HashMap<>();
        endringTidslinje.toSegments().forEach(segment -> {
            RefusjonPeriodeEndring refusjonsendring = segment.getValue();
            if (erMindreAndelTilgjengeligForBruker(refusjonsendring, grenseverdi)) {
                // Bruker vil få mindre andel av beregningsgrunnlaget, sjekk om noen andeler har fått økt refusjon
                List<RefusjonAndel> andelerMedØktRefusjon = finnAndelerMedØktRefusjon(refusjonsendring);
                if (!andelerMedØktRefusjon.isEmpty()) {
                    Intervall interval = Intervall.fraOgMedTilOgMed(segment.getFom(), segment.getTom());
                    andelerIPeriode.put(interval, andelerMedØktRefusjon);
                }
            }
        });
        return andelerIPeriode;
    }

    private static List<RefusjonAndel> finnAndelerMedØktRefusjon(RefusjonPeriodeEndring refusjonsendring) {
        Map<RefusjonAndelNøkkel, List<RefusjonAndel>> revurderingAndeler = refusjonsendring.getRevurderingAndelerMap();
        Map<RefusjonAndelNøkkel, List<RefusjonAndel>> originaleAndelMap = refusjonsendring.getOriginaleAndelerMap();
        List<RefusjonAndel> andelerMedØktRefusjon = new ArrayList<>();
        revurderingAndeler.forEach((nøkkel, andeler) -> andelerMedØktRefusjon.addAll(sjekkOmAndelerPåSammeNøkkelHarØktRefusjon(originaleAndelMap, nøkkel, andeler)));
        return andelerMedØktRefusjon;
    }

    private static List<RefusjonAndel> sjekkOmAndelerPåSammeNøkkelHarØktRefusjon(Map<RefusjonAndelNøkkel, List<RefusjonAndel>> originalAndelMap, RefusjonAndelNøkkel nøkkel, List<RefusjonAndel> revurderingAndeler) {
        List<RefusjonAndel> originaleAndelerPåNøkkel = originalAndelMap.getOrDefault(nøkkel, Collections.emptyList());

        // Tilkommet arbeidsgiver
        var totalRefusjonRevurdering = totalRefusjon(revurderingAndeler);
        if (nøkkel.getAktivitetStatus().erArbeidstaker() && originaleAndelerPåNøkkel.isEmpty()) {
            if (totalRefusjonRevurdering.compareTo(Beløp.ZERO) > 0) {
                return revurderingAndeler;
            }
        }

        var totalRefusjonOriginal = totalRefusjon(originaleAndelerPåNøkkel);
        boolean refusjonINøkkelHarØkt = totalRefusjonRevurdering.compareTo(totalRefusjonOriginal) > 0;
        if (refusjonINøkkelHarØkt) {
            return FinnAndelerMedØktRefusjonTjeneste.finnAndelerPåSammeNøkkelMedØktRefusjon(revurderingAndeler, originaleAndelerPåNøkkel);
        }

        return Collections.emptyList();
    }

    private static Beløp totalRefusjon(List<RefusjonAndel> andeler) {
        return andeler.stream()
                .map(RefusjonAndel::getRefusjon)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private static boolean erMindreAndelTilgjengeligForBruker(RefusjonPeriodeEndring refusjonsendring, Beløp grenseverdi) {
        var originalBrutto = refusjonsendring.getOriginalBrutto().min(grenseverdi);
        var revurderingBrutto = refusjonsendring.getRevurderingBrutto().min(grenseverdi);
        var originalAndelTilBruker = originalBrutto.subtraher(refusjonsendring.getOriginalRefusjon()).max(Beløp.ZERO);
        Beløp revurderingAndelTilBruker = revurderingBrutto.subtraher(refusjonsendring.getRevurderingRefusjon()).max(Beløp.ZERO);
        return revurderingAndelTilBruker.compareTo(originalAndelTilBruker) < 0;
    }

}
