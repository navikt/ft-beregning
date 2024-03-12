package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ArbeidstakerUtenInntektsmeldingTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.VurderMottarYtelseTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.ArbeidstakerUtenInntektsmeldingAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderMottarYtelseDto;

public class VurderMottarYtelseDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        if (beregningsgrunnlag.getFaktaOmBeregningTilfeller().contains(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE)) {
            LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();
            var iayGrunnlag = input.getIayGrunnlag();
            byggVerdier(iayGrunnlag, beregningsgrunnlag, faktaOmBeregningDto, skjæringstidspunkt, input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat(),
                    input.getInntektsmeldinger());
        }
    }

    private void byggVerdier(InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag, BeregningsgrunnlagDto beregningsgrunnlag,
                             FaktaOmBeregningDto faktaOmBeregningDto, LocalDate skjæringstidspunkt,
                             Optional<FaktaAggregatDto> faktaAggregat, Collection<InntektsmeldingDto> inntektsmeldinger) {
        VurderMottarYtelseDto vurderMottarYtelseDto = new VurderMottarYtelseDto();
        if (VurderMottarYtelseTjeneste.erFrilanser(beregningsgrunnlag)) {
            lagFrilansDel(beregningsgrunnlag, inntektArbeidYtelseGrunnlag, vurderMottarYtelseDto, skjæringstidspunkt, faktaAggregat.flatMap(FaktaAggregatDto::getFaktaAktør));
            if (faktaOmBeregningDto.getFrilansAndel() == null) {
                FaktaOmBeregningAndelDtoTjeneste.lagFrilansAndelDto(beregningsgrunnlag, inntektArbeidYtelseGrunnlag).ifPresent(faktaOmBeregningDto::setFrilansAndel);
            }
        }
        lagArbeidstakerUtenInntektsmeldingDel(inntektArbeidYtelseGrunnlag, beregningsgrunnlag,
                vurderMottarYtelseDto, skjæringstidspunkt,
                faktaAggregat, inntektsmeldinger);
        faktaOmBeregningDto.setVurderMottarYtelse(vurderMottarYtelseDto);
    }

    private void lagArbeidstakerUtenInntektsmeldingDel(InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                       BeregningsgrunnlagDto beregningsgrunnlag,
                                                       VurderMottarYtelseDto vurderMottarYtelseDto,
                                                       LocalDate skjæringstidspunkt,
                                                       Optional<FaktaAggregatDto> faktaAggregat,
                                                       Collection<InntektsmeldingDto> inntektsmeldinger) {

        var filter = new InntektFilterDto(inntektArbeidYtelseGrunnlag.getAktørInntektFraRegister()).før(skjæringstidspunkt);
        var andeler = ArbeidstakerUtenInntektsmeldingTjeneste.finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag,
                inntektsmeldinger);
        andeler.forEach(andelUtenIM -> {
            var dto = new ArbeidstakerUtenInntektsmeldingAndelDto();
            BeregningsgrunnlagPrStatusOgAndelDto andel = finnRestAndel(andelUtenIM, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
            beregnOgSettInntektPrMnd(filter, andel, dto);
            dto.setAndelsnr(andelUtenIM.getAndelsnr());
            dto.setInntektskategori(andelUtenIM.getGjeldendeInntektskategori());
            BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag).ifPresent(dto::setArbeidsforhold);
            Optional<FaktaArbeidsforholdDto> faktaArbeidsforholdDto = andelUtenIM.getBgAndelArbeidsforhold()
                    .flatMap(arb -> faktaAggregat.flatMap(fa -> fa.getFaktaArbeidsforhold(arb)));
            faktaArbeidsforholdDto.map(FaktaArbeidsforholdDto::getHarMottattYtelseVurdering).ifPresent(dto::setMottarYtelse);
            vurderMottarYtelseDto.leggTilArbeidstakerAndelUtenInntektsmelding(dto);
        });
    }

    private BeregningsgrunnlagPrStatusOgAndelDto finnRestAndel(BeregningsgrunnlagPrStatusOgAndelDto andelUtenIM, List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList) {
        return beregningsgrunnlagPrStatusOgAndelList.stream()
                .filter(a -> a.getAndelsnr().equals(andelUtenIM.getAndelsnr()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Fant ikke matchende andel"));
    }

    private void beregnOgSettInntektPrMnd(InntektFilterDto filter, BeregningsgrunnlagPrStatusOgAndelDto andel, ArbeidstakerUtenInntektsmeldingAndelDto dto) {
        var snittIBeregningsperioden = InntektForAndelTjeneste.finnSnittinntektForArbeidstakerIBeregningsperioden(filter, andel);
        snittIBeregningsperioden.map(ModellTyperMapper::beløpTilDto).ifPresent(dto::setInntektPrMnd);
    }

    private void lagFrilansDel(BeregningsgrunnlagDto beregningsgrunnlag,
                               InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                               VurderMottarYtelseDto vurderMottarYtelseDto,
                               LocalDate skjæringstidspunkt,
                               Optional<FaktaAktørDto> faktaAktør) {
        vurderMottarYtelseDto.setErFrilans(VurderMottarYtelseTjeneste.erFrilanser(beregningsgrunnlag));
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(andel -> andel.getAktivitetStatus().erFrilanser()).findFirst()
                .ifPresent(frilansAndel -> {
                    vurderMottarYtelseDto.setFrilansMottarYtelse(faktaAktør.map(FaktaAktørDto::getHarFLMottattYtelseVurdering).orElse(null));
                    InntektForAndelTjeneste.finnSnittAvFrilansinntektIBeregningsperioden(inntektArbeidYtelseGrunnlag, frilansAndel, skjæringstidspunkt)
                            .map(ModellTyperMapper::beløpTilDto).ifPresent(vurderMottarYtelseDto::setFrilansInntektPrMnd);
                });
    }

}
