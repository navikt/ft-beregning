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
     * @param beregningsgrunnlag - nytt beregningsgrunnlag
     * @param forrigeGrunnlag   - beregningsgrunnlag fra forrige behandling
     * @param alleredeUtbetaltTOM           - datoen ytelse er utbetalt til, det er kun relevant å se på perioder frem til denne datoen
     * @param ytelsespesifiktGrunnlag
     * @return - Ser på revurderingBeregningsgrunnlag og sjekker hvilke andeler i hvilke perioder
     * frem til alleredeUtbetaltTOM som har hatt økt refusjon i forhold til originaltBeregningsgrunnlag
     */
    public static Map<Intervall, List<RefusjonAndel>> finnUtbetaltePerioderMedAndelerMedØktRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                    BeregningsgrunnlagDto forrigeGrunnlag,
                                                                                                    LocalDate alleredeUtbetaltTOM,
                                                                                                    Beløp grenseverdi,
                                                                                                    YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (alleredeUtbetaltTOM.isBefore(beregningsgrunnlag.getSkjæringstidspunkt())) {
            return Collections.emptyMap();
        }
	    var alleredeUtbetaltPeriode = finnAlleredeUtbetaltPeriode(alleredeUtbetaltTOM);
	    var forrigeUtbetaltTidslinje = RefusjonTidslinjeTjeneste.lagTidslinje(forrigeGrunnlag, true, ytelsespesifiktGrunnlag).intersection(alleredeUtbetaltPeriode);
	    var utbetaltTidslinje = RefusjonTidslinjeTjeneste.lagTidslinje(beregningsgrunnlag, false, ytelsespesifiktGrunnlag);
	    var endringTidslinje = RefusjonTidslinjeTjeneste.kombinerTidslinjer(forrigeUtbetaltTidslinje, utbetaltTidslinje);
        var helgekomprimertTidslinje = komprimerForHelg(endringTidslinje);
        return vurderPerioder(helgekomprimertTidslinje, grenseverdi);
    }

    private static LocalDateTimeline<RefusjonPeriodeEndring> komprimerForHelg(LocalDateTimeline<RefusjonPeriodeEndring> endringTidslinje) {
        var factory = new TimelineWeekendCompressor.CompressorFactory<RefusjonPeriodeEndring>(Objects::equals, (i, lhs, rhs) -> new LocalDateSegment<>(i, lhs.getValue()));
	    var compressor = endringTidslinje.toSegments().stream()
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
	        var refusjonsendring = segment.getValue();
            if (erMindreAndelTilgjengeligForBruker(refusjonsendring, grenseverdi)) {
                // Bruker vil få mindre andel av beregningsgrunnlaget, sjekk om noen andeler har fått økt refusjon
	            var andelerMedØktRefusjon = finnAndelerMedØktRefusjon(refusjonsendring);
                if (!andelerMedØktRefusjon.isEmpty()) {
	                var interval = Intervall.fraOgMedTilOgMed(segment.getFom(), segment.getTom());
                    andelerIPeriode.put(interval, andelerMedØktRefusjon);
                }
            }
        });
        return andelerIPeriode;
    }

    private static List<RefusjonAndel> finnAndelerMedØktRefusjon(RefusjonPeriodeEndring refusjonsendring) {
	    var revurderingAndeler = refusjonsendring.getRevurderingAndelerMap();
	    var forrigeAndeler = refusjonsendring.getForrigeAndelerMap();
        List<RefusjonAndel> andelerMedØktRefusjon = new ArrayList<>();
        revurderingAndeler.forEach((nøkkel, andeler) -> andelerMedØktRefusjon.addAll(sjekkOmAndelerPåSammeNøkkelHarØktRefusjon(forrigeAndeler, nøkkel, andeler)));
        return andelerMedØktRefusjon;
    }

    private static List<RefusjonAndel> sjekkOmAndelerPåSammeNøkkelHarØktRefusjon(Map<RefusjonAndelNøkkel, List<RefusjonAndel>> forrigeAndeler, RefusjonAndelNøkkel nøkkel, List<RefusjonAndel> revurderingAndeler) {
	    var forrigeAndelerPåNøkkel = forrigeAndeler.getOrDefault(nøkkel, Collections.emptyList());

        // Tilkommet arbeidsgiver
        var totalRefusjonRevurdering = totalRefusjon(revurderingAndeler);
        if (nøkkel.getAktivitetStatus().erArbeidstaker() && forrigeAndelerPåNøkkel.isEmpty()) {
            if (totalRefusjonRevurdering.compareTo(Beløp.ZERO) > 0) {
                return revurderingAndeler;
            }
        }

        var totalRefusjonForrige = totalRefusjon(forrigeAndelerPåNøkkel);
	    var refusjonINøkkelHarØkt = totalRefusjonRevurdering.compareTo(totalRefusjonForrige) > 0;
        if (refusjonINøkkelHarØkt) {
            return FinnAndelerMedØktRefusjonTjeneste.finnAndelerPåSammeNøkkelMedØktRefusjon(revurderingAndeler, forrigeAndelerPåNøkkel);
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
        var forrigeBrutto = refusjonsendring.getBruttoForForrigeAndeler().min(grenseverdi);
        var revurderingBrutto = refusjonsendring.getBruttoForAndeler().min(grenseverdi);
        var forrigeAndelTilBruker = forrigeBrutto.subtraher(refusjonsendring.getRefusjonForForrigeAndeler()).max(Beløp.ZERO);
	    var revurderingAndelTilBruker = revurderingBrutto.subtraher(refusjonsendring.getRefusjonForAndeler()).max(Beløp.ZERO);
        return revurderingAndelTilBruker.compareTo(forrigeAndelTilBruker) < 0;
    }

}
