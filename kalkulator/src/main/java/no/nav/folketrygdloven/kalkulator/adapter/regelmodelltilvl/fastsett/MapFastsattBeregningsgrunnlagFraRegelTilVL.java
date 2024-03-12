package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.fastsett;


import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class MapFastsattBeregningsgrunnlagFraRegelTilVL {


    public BeregningsgrunnlagDto mapFastsettBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag resultatGrunnlag,
                                                               BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        return map(resultatGrunnlag, eksisterendeVLGrunnlag);
    }

    private BeregningsgrunnlagDto map(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag resultatGrunnlag, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(eksisterendeVLGrunnlag).build();
        Objects.requireNonNull(resultatGrunnlag, "resultatGrunnlag");
        mapPerioder(nyttBeregningsgrunnlag, resultatGrunnlag.getBeregningsgrunnlagPerioder());
        return nyttBeregningsgrunnlag;
    }

    private void mapPerioder(BeregningsgrunnlagDto eksisterendeVLGrunnlag,
                             List<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder) {

        for (var resultatBGPeriode : beregningsgrunnlagPerioder) {
            BeregningsgrunnlagPeriodeDto eksisterendePeriode = eksisterendeVLGrunnlag.getBeregningsgrunnlagPerioder().stream().filter(p -> p.getPeriode().getFomDato().equals(resultatBGPeriode.getPeriodeFom())).findFirst().orElseThrow();
            for (BeregningsgrunnlagPrStatus regelAndel : resultatBGPeriode.getBeregningsgrunnlagPrStatus()) {
                if (regelAndel.getAndelNr() == null) {
                    mapAndelMedArbeidsforhold(eksisterendePeriode, regelAndel);
                } else {
                    mapAndel(eksisterendePeriode, regelAndel);
                }
            }
            fastsettAgreggerteVerdier(eksisterendePeriode, eksisterendeVLGrunnlag);
            BeregningsgrunnlagPeriodeDto.oppdater(eksisterendePeriode)
                    .medInntektsgraderingsprosentBrutto(resultatBGPeriode.getInntektsgraderingFraBruttoBeregningsgrunnlag())
                    .medTotalUtbetalingsgradFraUttak(resultatBGPeriode.getTotalUtbetalingsgradFraUttak())
                    .medTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(resultatBGPeriode.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt())
                    .medReduksjonsfaktorInaktivTypeA(resultatBGPeriode.getReduksjonsfaktorInaktivTypeA());
        }
    }

    private static void mapAndel(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndel.getAndelNr().equals(bgpsa.getAndelsnr()))
                .forEach(resultatAndel -> mapBeregningsgrunnlagPrStatus(mappetPeriode, regelAndel, resultatAndel));
    }

    private void mapAndelMedArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode, BeregningsgrunnlagPrStatus regelAndel) {
        for (BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold : regelAndel.getArbeidsforhold()) {
            mapEksisterendeAndelForArbeidsforhold(mappetPeriode, regelAndelForArbeidsforhold);
        }
    }

    private void mapEksisterendeAndelForArbeidsforhold(BeregningsgrunnlagPeriodeDto mappetPeriode,
                                                       BeregningsgrunnlagPrArbeidsforhold regelAndelForArbeidsforhold) {
        Optional<BeregningsgrunnlagPrStatusOgAndelDto> andelOpt = mappetPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgpsa -> regelAndelForArbeidsforhold.getAndelNr().equals(bgpsa.getAndelsnr()))
                .findFirst();
        if (andelOpt.isPresent()) {
            BeregningsgrunnlagPrStatusOgAndelDto kalkulatorAndel = andelOpt.get();
            mapBeregningsgrunnlagPrStatusForATKombinert(mappetPeriode, kalkulatorAndel, regelAndelForArbeidsforhold);
        } else {
            throw new IllegalStateException("Forventer ikke ny andel fra fastsett beregning steg.");
        }
    }

    private static void fastsettAgreggerteVerdier(BeregningsgrunnlagPeriodeDto periode, BeregningsgrunnlagDto eksisterendeVLGrunnlag) {
        var bruttoPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder);
        var avkortetPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getAvkortetPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder);
        var redusertPrÅr = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getRedusertPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder);
        BeregningsgrunnlagPeriodeDto.oppdater(periode)
                .medBruttoPrÅr(bruttoPrÅr.orElse(null))
                .medAvkortetPrÅr(avkortetPrÅr.orElse(null))
                .medRedusertPrÅr(redusertPrÅr.orElse(null))
                .build(eksisterendeVLGrunnlag);
    }

    private void mapBeregningsgrunnlagPrStatusForATKombinert(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                             BeregningsgrunnlagPrStatusOgAndelDto vlBGPAndel,
                                                             BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(vlBGPAndel));
        settVerdierFraFastsettRegel(andelBuilder, regelArbeidsforhold);
        andelBuilder.build(vlBGPeriode);
    }

    protected static void settVerdierFraFastsettRegel(BeregningsgrunnlagPrStatusOgAndelDto.Builder builder, BeregningsgrunnlagPrArbeidsforhold regelResultat) {

        builder.medAvkortetPrÅr(Beløp.fra(regelResultat.getAvkortetPrÅr()))
                .medRedusertPrÅr(Beløp.fra(regelResultat.getRedusertPrÅr()))
                .medMaksimalRefusjonPrÅr(Beløp.fra(regelResultat.getMaksimalRefusjonPrÅr()))
                .medAvkortetRefusjonPrÅr(Beløp.fra(regelResultat.getAvkortetRefusjonPrÅr()))
                .medRedusertRefusjonPrÅr(Beløp.fra(regelResultat.getRedusertRefusjonPrÅr()))
                .medAvkortetBrukersAndelPrÅr(Beløp.fra(regelResultat.getAvkortetBrukersAndelPrÅr()))
                .medRedusertBrukersAndelPrÅr(Beløp.fra(regelResultat.getRedusertBrukersAndelPrÅr()))
                .medAvkortetFørGraderingPrÅr(Beløp.fra(regelResultat.getAndelsmessigFørGraderingPrAar() == null ? BigDecimal.ZERO : regelResultat.getAndelsmessigFørGraderingPrAar()));
    }

    private static void mapBeregningsgrunnlagPrStatus(BeregningsgrunnlagPeriodeDto vlBGPeriode,
                                                      BeregningsgrunnlagPrStatus resultatBGPStatus,
                                                      BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatusOgAndel) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(vlBGPStatusOgAndel));
        settVerdierFraFastsettRegel(builder, resultatBGPStatus);
        builder.build(vlBGPeriode);
    }

    private static void settVerdierFraFastsettRegel(BeregningsgrunnlagPrStatusOgAndelDto.Builder builder, BeregningsgrunnlagPrStatus regelResultat) {
        builder
                .medAvkortetPrÅr(Beløp.fra(regelResultat.getAvkortetPrÅr()))
                .medRedusertPrÅr(Beløp.fra(regelResultat.getRedusertPrÅr()))
                .medAvkortetBrukersAndelPrÅr(Beløp.fra(regelResultat.getAvkortetPrÅr()))
                .medRedusertBrukersAndelPrÅr(Beløp.fra(regelResultat.getRedusertPrÅr()))
                .medMaksimalRefusjonPrÅr(Beløp.ZERO)
                .medAvkortetRefusjonPrÅr(Beløp.ZERO)
                .medRedusertRefusjonPrÅr(Beløp.ZERO)
                .medAvkortetFørGraderingPrÅr(Optional.ofNullable(regelResultat.getAndelsmessigFørGraderingPrAar()).map(Beløp::fra).orElse(Beløp.ZERO));
    }

}
