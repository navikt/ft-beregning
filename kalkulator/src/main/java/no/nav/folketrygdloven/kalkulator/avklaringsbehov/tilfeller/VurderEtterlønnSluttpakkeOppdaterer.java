package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class VurderEtterlønnSluttpakkeOppdaterer {

    private VurderEtterlønnSluttpakkeOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        var vurderDto = dto.getVurderEtterlønnSluttpakke();
        var etterlønnSluttpakkeAndel = finnEtterlønnSluttpakkeAndeler(grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag());
        if (!vurderDto.erEtterlønnSluttpakke()) {
            etterlønnSluttpakkeAndel.forEach(andel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel)
                    .medFastsattAvSaksbehandler(true)
                    .medBeregnetPrÅr(Beløp.ZERO));
        }

        // Setter fakta aggregat
        var faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        var faktaAktørBuilder = faktaAggregatBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medMottarEtterlønnSluttpakkeFastsattAvSaksbehandler(vurderDto.erEtterlønnSluttpakke());
        faktaAggregatBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> finnEtterlønnSluttpakkeAndeler(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .getFirst()
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(bpsa -> bpsa.getArbeidsforholdType().equals(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE))
                .toList();
    }
}
