package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.FastsettFaktaOmBeregningVerdierTjeneste;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.DagpengeAndelLagtTilBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;


public class FastsettBesteberegningFødendeKvinneOppdaterer  {

    public static void oppdater(FaktaBeregningLagreDto dto,
                                Optional<BeregningsgrunnlagDto> forrigeBg,
                                BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        var besteberegningDto = dto.getBesteberegningAndeler();
        var andelListe = besteberegningDto.getBesteberegningAndelListe();
        var beregningsgrunnlagBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder();
        var nyttBeregningsgrunnlag = beregningsgrunnlagBuilder.getBeregningsgrunnlag();
        for (var periode : nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            var forrigePeriode = forrigeBg
                    .flatMap(beregningsgrunnlag -> beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                            .filter(periode1 -> periode1.getPeriode().overlapper(periode.getPeriode())).findFirst());
            andelListe.forEach(dtoAndel -> FastsettFaktaOmBeregningVerdierTjeneste.fastsettVerdierForAndel(mapTilRedigerbarAndel(dtoAndel), mapTilFastsatteVerdier(dtoAndel), periode, forrigePeriode));
            if (besteberegningDto.getNyDagpengeAndel() != null) {
                FastsettFaktaOmBeregningVerdierTjeneste.fastsettVerdierForAndel(lagRedigerbarAndelDtoForDagpenger(), mapTilFastsatteVerdier(besteberegningDto.getNyDagpengeAndel()), periode, forrigePeriode);
            }
        }
        if (nyttBeregningsgrunnlag.getAktivitetStatuser().stream().noneMatch(status -> AktivitetStatus.DAGPENGER.equals(status.getAktivitetStatus()))) {
            beregningsgrunnlagBuilder
                    .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER));
        }

        // Setter fakta aggregat
        var faktaBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        var faktaAktørBuilder = faktaBuilder.getFaktaAktørBuilder();
        var skalBesteberegnes = besteberegningDto.getBesteberegningAndelListe().stream().anyMatch(a -> a.getFastsatteVerdier().getSkalHaBesteberegning());
        faktaAktørBuilder.medSkalBesteberegnesFastsattAvSaksbehandler(skalBesteberegnes);
        faktaBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaBuilder.build());
    }

    private static FastsatteVerdierDto mapTilFastsatteVerdier(DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        var fastsatteVerdier = nyDagpengeAndel.getFastsatteVerdier();
        return FastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrÅr(fastsatteVerdier.finnFastsattBeløpPrÅr())
                .medInntektskategori(fastsatteVerdier.getInntektskategori())
                .medSkalHaBesteberegning(true)
                .build();
    }

    private static RedigerbarAndelFaktaOmBeregningDto lagRedigerbarAndelDtoForDagpenger() {
        return new RedigerbarAndelFaktaOmBeregningDto(AktivitetStatus.DAGPENGER);
    }

    private static RedigerbarAndelFaktaOmBeregningDto mapTilRedigerbarAndel(BesteberegningFødendeKvinneAndelDto dtoAndel) {
        return new RedigerbarAndelFaktaOmBeregningDto(false, dtoAndel.getAndelsnr(), dtoAndel.getLagtTilAvSaksbehandler());
    }

    private static FastsatteVerdierDto mapTilFastsatteVerdier(BesteberegningFødendeKvinneAndelDto dtoAndel) {
        var fastsatteVerdier = dtoAndel.getFastsatteVerdier();
        return FastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrÅr(fastsatteVerdier.finnFastsattBeløpPrÅr())
                .medInntektskategori(fastsatteVerdier.getInntektskategori())
                .medSkalHaBesteberegning(fastsatteVerdier.getSkalHaBesteberegning())
                .build();
    }

}
