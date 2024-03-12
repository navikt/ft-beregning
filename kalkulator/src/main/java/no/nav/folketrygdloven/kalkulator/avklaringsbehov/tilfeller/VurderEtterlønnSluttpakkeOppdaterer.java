package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderEtterlønnSluttpakkeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class VurderEtterlønnSluttpakkeOppdaterer {

    private VurderEtterlønnSluttpakkeOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderEtterlønnSluttpakkeDto vurderDto = dto.getVurderEtterlønnSluttpakke();
        List<BeregningsgrunnlagPrStatusOgAndelDto> etterlønnSluttpakkeAndel = finnEtterlønnSluttpakkeAndeler(grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag());
        if (!vurderDto.erEtterlønnSluttpakke()) {
            etterlønnSluttpakkeAndel.forEach(andel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel)
                    .medFastsattAvSaksbehandler(true)
                    .medBeregnetPrÅr(Beløp.ZERO));
        }

        // Setter fakta aggregat
        FaktaAggregatDto.Builder faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        FaktaAktørDto.Builder faktaAktørBuilder = faktaAggregatBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medMottarEtterlønnSluttpakkeFastsattAvSaksbehandler(vurderDto.erEtterlønnSluttpakke());
        faktaAggregatBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> finnEtterlønnSluttpakkeAndeler(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(bpsa -> bpsa.getArbeidsforholdType().equals(OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE))
                .collect(Collectors.toList());
    }
}
