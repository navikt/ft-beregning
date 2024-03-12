package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapPeriodeÅrsakFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;

abstract class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL {

    public BeregningsgrunnlagDto mapFraRegel(List<SplittetPeriode> splittedePerioder,
                                             BeregningsgrunnlagDto vlBeregningsgrunnlag) {

        BeregningsgrunnlagDto nytt = BeregningsgrunnlagDto.builder(vlBeregningsgrunnlag)
                .fjernAllePerioder().build();

        splittedePerioder.forEach(splittetPeriode -> mapSplittetPeriode(nytt, splittetPeriode, vlBeregningsgrunnlag));
        return nytt;
    }

    protected void mapSplittetPeriode(BeregningsgrunnlagDto nyttBeregningsgrunnlag,
                                      SplittetPeriode splittetPeriode,
                                      BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate periodeTom = utledPeriodeTom(splittetPeriode);

        var originalPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getPeriode().inkluderer(splittetPeriode.getPeriode().getFom()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ingen matchende perioder"));
        var andelListe = originalPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var bgPeriodeBuilder = BeregningsgrunnlagPeriodeDto.kopier(originalPeriode)
                .fjernAlleBeregningsgrunnlagPrStatusOgAndeler()
                .medBeregningsgrunnlagPeriode(splittetPeriode.getPeriode().getFom(), periodeTom);
        splittetPeriode.getPeriodeÅrsaker().stream()
                .map(MapPeriodeÅrsakFraRegelTilVL::map)
                .forEach(bgPeriodeBuilder::leggTilPeriodeÅrsak);
        var beregningsgrunnlagPeriode = bgPeriodeBuilder.build(nyttBeregningsgrunnlag);
        mapAndeler(splittetPeriode, andelListe, beregningsgrunnlagPeriode);
    }

    protected abstract void mapAndeler(SplittetPeriode splittetPeriode, List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode);

    LocalDate utledPeriodeTom(SplittetPeriode splittetPeriode) {
        LocalDate tom = splittetPeriode.getPeriode().getTom();
        if (TIDENES_ENDE.equals(tom)) {
            return null;
        }
        return tom;
    }

    Optional<EksisterendeAndel> finnEksisterendeAndelFraRegel(SplittetPeriode splittetPeriode, BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel) {
        return splittetPeriode.getEksisterendePeriodeAndeler().stream()
                .filter(andel -> andel.getAndelNr().equals(eksisterendeAndel.getAndelsnr()))
                .findFirst();
    }

}
