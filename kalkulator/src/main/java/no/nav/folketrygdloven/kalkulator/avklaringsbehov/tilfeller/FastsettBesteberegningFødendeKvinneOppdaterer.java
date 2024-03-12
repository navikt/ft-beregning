package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.FastsettFaktaOmBeregningVerdierTjeneste;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BesteberegningFødendeKvinneDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.DagpengeAndelLagtTilBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteVerdierForBesteberegningDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RedigerbarAndelFaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;


public class FastsettBesteberegningFødendeKvinneOppdaterer  {

    public static void oppdater(FaktaBeregningLagreDto dto,
                                Optional<BeregningsgrunnlagDto> forrigeBg,
                                BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        BesteberegningFødendeKvinneDto besteberegningDto = dto.getBesteberegningAndeler();
        List<BesteberegningFødendeKvinneAndelDto> andelListe = besteberegningDto.getBesteberegningAndelListe();
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder();
        BeregningsgrunnlagDto nyttBeregningsgrunnlag = beregningsgrunnlagBuilder.getBeregningsgrunnlag();
        for (var periode : nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            Optional<BeregningsgrunnlagPeriodeDto> forrigePeriode = forrigeBg
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
        FaktaAggregatDto.Builder faktaBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        FaktaAktørDto.Builder faktaAktørBuilder = faktaBuilder.getFaktaAktørBuilder();
        boolean skalBesteberegnes = besteberegningDto.getBesteberegningAndelListe().stream().anyMatch(a -> a.getFastsatteVerdier().getSkalHaBesteberegning());
        faktaAktørBuilder.medSkalBesteberegnesFastsattAvSaksbehandler(skalBesteberegnes);
        faktaBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaBuilder.build());
    }

    private static FastsatteVerdierDto mapTilFastsatteVerdier(DagpengeAndelLagtTilBesteberegningDto nyDagpengeAndel) {
        FastsatteVerdierForBesteberegningDto fastsatteVerdier = nyDagpengeAndel.getFastsatteVerdier();
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
        FastsatteVerdierForBesteberegningDto fastsatteVerdier = dtoAndel.getFastsatteVerdier();
        return FastsatteVerdierDto.Builder.ny()
                .medFastsattBeløpPrÅr(fastsatteVerdier.finnFastsattBeløpPrÅr())
                .medInntektskategori(fastsatteVerdier.getInntektskategori())
                .medSkalHaBesteberegning(fastsatteVerdier.getSkalHaBesteberegning())
                .build();
    }

}
