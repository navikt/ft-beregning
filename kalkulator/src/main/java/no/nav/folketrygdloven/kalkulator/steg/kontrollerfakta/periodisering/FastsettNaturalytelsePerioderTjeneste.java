package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.periodisering;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeModellNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.naturalytelse.MapNaturalytelserFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;


/**
 * Oppretter perioder der det er endring i naturalytelser for ARBEIDSTAKER. Bortfalte naturalytelser skal tas med i beregningsgrunnlaget. Dersom naturalytelsen tilkommer igjen skal den fjernes.
 *
 * § 8-29: "... Fra det tidspunkt arbeidstakeren ikke lenger mottar ytelsene, tas de med ved beregningen med den verdi som nyttes ved forskottstrekk av skatt."
 *
 *
 */
public class FastsettNaturalytelsePerioderTjeneste {
    private final MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse oversetterFraRegelNaturalytelse = new MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse();

    public BeregningsgrunnlagRegelResultat fastsettPerioderForNaturalytelse(BeregningsgrunnlagInput input,
                                                                            BeregningsgrunnlagDto beregningsgrunnlag) {
        PeriodeModellNaturalytelse periodeModell = MapNaturalytelserFraVLTilRegel.map(input, beregningsgrunnlag);
        return kjørRegelOgMapTilVLNaturalytelse(beregningsgrunnlag, periodeModell);
    }

    private BeregningsgrunnlagRegelResultat kjørRegelOgMapTilVLNaturalytelse(BeregningsgrunnlagDto beregningsgrunnlag, PeriodeModellNaturalytelse input) {
        List<SplittetPeriode> splittedePerioder = new ArrayList<>();
        RegelResultat regelResultat = KalkulusRegler.fastsettPerioderNaturalytelse(input, splittedePerioder);
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = oversetterFraRegelNaturalytelse.mapFraRegel(splittedePerioder, beregningsgrunnlag);
        return new BeregningsgrunnlagRegelResultat(nyttBeregningsgrunnlag,
                new RegelSporingAggregat(MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag(regelResultat, BeregningsgrunnlagRegelType.PERIODISERING_NATURALYTELSE)));
    }
}
