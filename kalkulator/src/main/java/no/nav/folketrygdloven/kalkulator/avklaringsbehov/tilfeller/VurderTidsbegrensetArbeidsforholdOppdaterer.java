package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderteArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;

public class VurderTidsbegrensetArbeidsforholdOppdaterer {

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderTidsbegrensetArbeidsforholdDto tidsbegrensetDto = dto.getVurderTidsbegrensetArbeidsforhold();
        FaktaAggregatDto.Builder faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        BeregningsgrunnlagPeriodeDto periode = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold = tidsbegrensetDto.getFastsatteArbeidsforhold();
        for (VurderteArbeidsforholdDto arbeidsforhold : fastsatteArbeidsforhold) {
            BeregningsgrunnlagPrStatusOgAndelDto korrektAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(a -> a.getAndelsnr().equals(arbeidsforhold.getAndelsnr()))
                    .findFirst()
                    .orElseThrow(() -> new KalkulatorException("FT-238175", "Finner ikke andelen for eksisterende grunnlag"));

            // Setter Fakta-aggregat
            BGAndelArbeidsforholdDto arbeidsforholdDto = korrektAndel.getBgAndelArbeidsforhold()
                    .orElseThrow(() -> new KalkulatorException("FT-238176", "Finner ikke arbeidsforhold for eksisterende andel"));
            FaktaArbeidsforholdDto.Builder faktaArbBuilder = faktaAggregatBuilder.getFaktaArbeidsforholdBuilderFor(arbeidsforholdDto.getArbeidsgiver(), arbeidsforholdDto.getArbeidsforholdRef())
                    .medErTidsbegrensetFastsattAvSaksbehandler(arbeidsforhold.isTidsbegrensetArbeidsforhold());
            faktaAggregatBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
            grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
        }
    }


}
