package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderATogFLiSammeOrganisasjonDto;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

class FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer {

    private FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderATogFLiSammeOrganisasjonDto vurderATFLISammeOrgDto = dto.getVurderATogFLiSammeOrganisasjon();
        vurderATFLISammeOrgDto.getVurderATogFLiSammeOrganisasjonAndelListe().forEach(dtoAndel ->
        {
            BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
            BeregningsgrunnlagPrStatusOgAndelDto andelIFørstePeriode = finnAndelIFørstePeriode(beregningsgrunnlag, dtoAndel);
            int årsinntekt = dtoAndel.getArbeidsinntekt() * KonfigTjeneste.getMånederIÅrInt();
            beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
                BeregningsgrunnlagPrStatusOgAndelDto matchendeAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.equals(andelIFørstePeriode)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Fant ingen mactchende andel i periode med fom " + periode.getBeregningsgrunnlagPeriodeFom()));
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(matchendeAndel)
                    .medBeregnetPrÅr(Beløp.fra(årsinntekt))
                    .medFastsattAvSaksbehandler(true);
            });
        });
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto finnAndelIFørstePeriode(BeregningsgrunnlagDto nyttBeregningsgrunnlag, VurderATogFLiSammeOrganisasjonAndelDto dtoAndel) {
        return nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
                    .getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> bpsa.getAndelsnr().equals(dtoAndel.getAndelsnr()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Fant ikke andel i første periode med andelsnr " + dtoAndel.getAndelsnr()));
    }

}
