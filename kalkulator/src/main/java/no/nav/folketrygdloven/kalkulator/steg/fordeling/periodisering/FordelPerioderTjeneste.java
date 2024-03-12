package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.PeriodeModellRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.gradering.MapPerioderForGraderingFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.ytelse.fp.MapRefusjonPerioderFraVLTilRegelFP;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapPerioderForUtbetalingsgradFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapRefusjonPerioderFraVLTilRegelUtbgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;


/**
 * Splitter periode ved endring i refusjon, gradering og utbetalingsgrad
 * <p>
 * Sette refusjon på andeler med gyldig refusjon
 */
public class FordelPerioderTjeneste {
    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad oversetterFraRegelGraderingOgUtbetalingsgrad = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLGraderingOgUtbetalingsgrad();
    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon oversetterFraRegelRefusjon = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLRefusjon();


    public BeregningsgrunnlagRegelResultat fastsettPerioderForRefusjon(BeregningsgrunnlagInput input) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();

        return finnPeriodeModellRefusjon(input, beregningsgrunnlag)
                .map(m -> kjørRegelOgMapTilVLRefusjon(beregningsgrunnlag, m))
                .orElseGet(() -> new BeregningsgrunnlagRegelResultat(beregningsgrunnlag, List.of()));
    }

    private static Optional<PeriodeModellRefusjon> finnPeriodeModellRefusjon(BeregningsgrunnlagInput input,
                                                                             BeregningsgrunnlagDto beregningsgrunnlag) {
        var modell = switch (input.getFagsakYtelseType()) {
            case FORELDREPENGER -> new MapRefusjonPerioderFraVLTilRegelFP().map(input, beregningsgrunnlag);
            case SVANGERSKAPSPENGER, OMSORGSPENGER, PLEIEPENGER_SYKT_BARN, OPPLÆRINGSPENGER, PLEIEPENGER_NÆRSTÅENDE ->
                    new MapRefusjonPerioderFraVLTilRegelUtbgrad().map(input, beregningsgrunnlag);
            default -> null;
        };
        return Optional.ofNullable(modell);
    }

    public BeregningsgrunnlagRegelResultat fastsettPerioderForUtbetalingsgradEllerGradering(BeregningsgrunnlagInput input,
                                                                                            BeregningsgrunnlagDto beregningsgrunnlag) {
        if (input.getFagsakYtelseType().equals(FagsakYtelseType.FORELDREPENGER)) {
            return fastsettPerioderForGradering(input, beregningsgrunnlag);
        } else {
            return fastsettPerioderForUtbetalingsgrad(input, beregningsgrunnlag);
        }
    }

    private BeregningsgrunnlagRegelResultat fastsettPerioderForUtbetalingsgrad(BeregningsgrunnlagInput input,
                                                                               BeregningsgrunnlagDto beregningsgrunnlag) {
        var periodeModell = MapPerioderForUtbetalingsgradFraVLTilRegel.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLUtbetalingsgrad(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagRegelResultat fastsettPerioderForGradering(BeregningsgrunnlagInput input,
                                                                         BeregningsgrunnlagDto beregningsgrunnlag) {
        var periodeModell = MapPerioderForGraderingFraVLTilRegel.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLGradering(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                        PeriodeModellRefusjon input) {
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        RegelResultat regelResultat = KalkulusRegler.fastsettPerioderRefusjon(input, splittedePerioder);
        var nyttBeregningsgrunnlag = oversetterFraRegelRefusjon.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_REFUSJON)));
    }


    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLGradering(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                         PeriodeModellGradering input) {
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        RegelResultat regelResultat = KalkulusRegler.fastsettPerioderGradering(input, splittedePerioder);
        var nyttBeregningsgrunnlag = oversetterFraRegelGraderingOgUtbetalingsgrad.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_GRADERING)));
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLUtbetalingsgrad(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                               PeriodeModellUtbetalingsgrad input) {
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        RegelResultat regelResultat = KalkulusRegler.fastsettPerioderForUtbetalingsgrad(input, splittedePerioder);
        var nyttBeregningsgrunnlag = oversetterFraRegelGraderingOgUtbetalingsgrad.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(
                nyttBeregningsgrunnlag,
                new RegelSporingAggregat(mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_UTBETALINGSGRAD)));
    }
}
