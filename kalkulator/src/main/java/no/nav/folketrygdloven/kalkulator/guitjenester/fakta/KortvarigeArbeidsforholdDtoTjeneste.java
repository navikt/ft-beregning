package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.KortvarigeArbeidsforholdDto;

public class KortvarigeArbeidsforholdDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input,
                       FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        if (!beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD)) {
            return;
        }
        List<KortvarigeArbeidsforholdDto> arbeidsforholdDto = lagKortvarigeArbeidsforholdDto(
                beregningsgrunnlag,
                input.getIayGrunnlag(),
                input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat());
        faktaOmBeregningDto.setKortvarigeArbeidsforhold(arbeidsforholdDto);
    }

    private List<KortvarigeArbeidsforholdDto> lagKortvarigeArbeidsforholdDto(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                             InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                             Optional<FaktaAggregatDto> faktaAggregat) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarige = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, inntektArbeidYtelseGrunnlag);
        return kortvarige.entrySet().stream()
            .map(entry -> mapFraYrkesaktivitet(finnRestDtoForAndel(entry, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()), inntektArbeidYtelseGrunnlag, faktaAggregat))
            .collect(Collectors.toList());
    }

    private BeregningsgrunnlagPrStatusOgAndelDto finnRestDtoForAndel(Map.Entry<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> entry, List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
            .filter(a -> a.getAndelsnr().equals(entry.getKey().getAndelsnr()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikkje matchende blant REST-andeler"));
    }

    private KortvarigeArbeidsforholdDto mapFraYrkesaktivitet(BeregningsgrunnlagPrStatusOgAndelDto prStatusOgAndel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, Optional<FaktaAggregatDto> faktaAggregat) {
        KortvarigeArbeidsforholdDto beregningArbeidsforhold = new KortvarigeArbeidsforholdDto();
        Optional<BGAndelArbeidsforholdDto> bgAndelArbeidsforhold = prStatusOgAndel.getBgAndelArbeidsforhold();
        var faktaArbeidsforholdDto = bgAndelArbeidsforhold.flatMap(arbeidsforhold -> faktaAggregat.flatMap(fa -> fa.getFaktaArbeidsforhold(arbeidsforhold)));
        beregningArbeidsforhold.setErTidsbegrensetArbeidsforhold(faktaArbeidsforholdDto.map(FaktaArbeidsforholdDto::getErTidsbegrensetVurdering).orElse(null));
        beregningArbeidsforhold.setAndelsnr(prStatusOgAndel.getAndelsnr());
        Optional<BeregningsgrunnlagArbeidsforholdDto> arbDto = BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(prStatusOgAndel, Optional.empty(), inntektArbeidYtelseGrunnlag);
        arbDto.ifPresent(beregningArbeidsforhold::setArbeidsforhold);
        return beregningArbeidsforhold;
    }
}
