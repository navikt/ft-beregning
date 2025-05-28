package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;

public class VurderTidsbegrensetArbeidsforholdOppdaterer {

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        var tidsbegrensetDto = dto.getVurderTidsbegrensetArbeidsforhold();
        var faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        var periode = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        var fastsatteArbeidsforhold = tidsbegrensetDto.getFastsatteArbeidsforhold();
        for (var arbeidsforhold : fastsatteArbeidsforhold) {
            var korrektAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(a -> a.getAndelsnr().equals(arbeidsforhold.getAndelsnr()))
                    .findFirst()
                    .orElseThrow(() -> new KalkulatorException("FT-238175", "Finner ikke andelen for eksisterende grunnlag"));

            // Setter Fakta-aggregat
            var arbeidsforholdDto = korrektAndel.getBgAndelArbeidsforhold()
                    .orElseThrow(() -> new KalkulatorException("FT-238176", "Finner ikke arbeidsforhold for eksisterende andel"));
            var faktaArbBuilder = faktaAggregatBuilder.getFaktaArbeidsforholdBuilderFor(arbeidsforholdDto.getArbeidsgiver(), arbeidsforholdDto.getArbeidsforholdRef())
                    .medErTidsbegrensetFastsattAvSaksbehandler(arbeidsforhold.isTidsbegrensetArbeidsforhold());
            faktaAggregatBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
            grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
        }
    }


}
