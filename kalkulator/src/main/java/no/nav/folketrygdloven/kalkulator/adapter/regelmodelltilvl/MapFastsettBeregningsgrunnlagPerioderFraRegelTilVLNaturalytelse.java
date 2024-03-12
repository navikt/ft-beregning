package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.EksisterendeAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk.MapPeriodeÅrsakFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class MapFastsettBeregningsgrunnlagPerioderFraRegelTilVLNaturalytelse extends MapFastsettBeregningsgrunnlagPerioderFraRegelTilVL {

    @Override
    protected void mapAndeler(SplittetPeriode splittetPeriode, List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        andelListe.forEach(eksisterendeAndel -> mapEksisterendeAndel(splittetPeriode, beregningsgrunnlagPeriode, eksisterendeAndel));
    }

    @Override
    protected void mapSplittetPeriode(BeregningsgrunnlagDto nyttBeregningsgrunnlag,
                                      SplittetPeriode splittetPeriode,
                                      BeregningsgrunnlagDto beregningsgrunnlag) {
        LocalDate periodeTom = utledPeriodeTom(splittetPeriode);

        var originalPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .filter(p -> p.getPeriode().inkluderer(splittetPeriode.getPeriode().getFom()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Ingen matchende perioder"));
        var andelListe = originalPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        var bgPeriodeBuilder = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(splittetPeriode.getPeriode().getFom(), periodeTom);
        splittetPeriode.getPeriodeÅrsaker().stream()
            .map(MapPeriodeÅrsakFraRegelTilVL::map)
            .forEach(bgPeriodeBuilder::leggTilPeriodeÅrsak);
        var beregningsgrunnlagPeriode = bgPeriodeBuilder.build(nyttBeregningsgrunnlag);
        mapAndeler(splittetPeriode, andelListe, beregningsgrunnlagPeriode);
    }

    private void mapEksisterendeAndel(SplittetPeriode splittetPeriode, BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, BeregningsgrunnlagPrStatusOgAndelDto eksisterendeAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(eksisterendeAndel);

        Optional<EksisterendeAndel> regelMatchOpt = splittetPeriode.getEksisterendePeriodeAndeler().stream()
            .filter(andel -> andel.getAndelNr().equals(eksisterendeAndel.getAndelsnr()))
            .findFirst();
        regelMatchOpt.ifPresent(regelAndel -> {
            BGAndelArbeidsforholdDto.Builder andelArbeidsforholdBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder()
                .medNaturalytelseBortfaltPrÅr(Beløp.fra(regelAndel.getNaturalytelseBortfaltPrÅr().orElse(null)))
                .medNaturalytelseTilkommetPrÅr(Beløp.fra(regelAndel.getNaturalytelseTilkommetPrÅr().orElse(null)));
            andelBuilder.medBGAndelArbeidsforhold(andelArbeidsforholdBuilder);
        });
        andelBuilder
            .build(beregningsgrunnlagPeriode);
    }

}
