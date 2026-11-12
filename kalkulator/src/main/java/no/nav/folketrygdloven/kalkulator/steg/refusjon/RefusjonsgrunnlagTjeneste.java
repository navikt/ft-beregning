package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL.mapRegelSporingGrunnlag;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRefusjonsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class RefusjonsgrunnlagTjeneste {

    public RefusjonsgrunnlagResultat fastsettRefusjonsgrunnlag(BeregningsgrunnlagInput input) {
        if (input.getFordelingGrunnlag().isPresent()) {
            throw new IllegalArgumentException("Fordelingsgrunnlag skal ikke være satt ved fastsetting av refusjonsgrunnlag.");
        }
        var modell = MapRefusjonsgrunnlagFraVLTilRegel.map(input);
        if (modell.isEmpty()) {
            return new RefusjonsgrunnlagResultat(input.getBeregningsgrunnlag(), new FordelingGrunnlagDto(List.of()),
                new RegelSporingAggregat(List.of()));
        }
        var resultat = KalkulusRegler.fastsettRefusjonsgrunnlag(modell.get());
        var fordelingGrunnlag = MapRefusjonsgrunnlagFraRegelTilVL.map(resultat);
        var sporing = new RegelSporingAggregat(mapRegelSporingGrunnlag(resultat.regelResultat(),
            BeregningsgrunnlagRegelType.PERIODISERING_REFUSJON));
        var beregningsgrunnlag = PeriodiserBeregningsgrunnlagForRefusjon.periodiser(input, fordelingGrunnlag);
        return new RefusjonsgrunnlagResultat(beregningsgrunnlag, fordelingGrunnlag, sporing);
    }

    public record RefusjonsgrunnlagResultat(BeregningsgrunnlagDto beregningsgrunnlag,
                                           FordelingGrunnlagDto fordelingGrunnlag,
                                           RegelSporingAggregat regelsporinger) {
    }
}
