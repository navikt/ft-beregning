package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;

public class VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer {

    private VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        var nyIArbeidslivetDto = dto.getVurderNyIArbeidslivet();
        var faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        var faktaAktørBuilder = faktaAggregatBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medErNyIArbeidslivetSNFastsattAvSaksbehandler(nyIArbeidslivetDto.erNyIArbeidslivet());
        faktaAggregatBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
    }

}
