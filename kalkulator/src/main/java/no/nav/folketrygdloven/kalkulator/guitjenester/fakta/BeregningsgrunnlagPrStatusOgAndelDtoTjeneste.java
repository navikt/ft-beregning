package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.felles.FinnInntektsmeldingForAndel;
import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelDto;

public class BeregningsgrunnlagPrStatusOgAndelDtoTjeneste {

    public List<BeregningsgrunnlagPrStatusOgAndelDto> lagBeregningsgrunnlagPrStatusOgAndelDto(BeregningsgrunnlagGUIInput input,
                                                                                              List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList) {

        List<BeregningsgrunnlagPrStatusOgAndelDto> usortertDtoList = new ArrayList<>();
        for (no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel : beregningsgrunnlagPrStatusOgAndelList) {
            BeregningsgrunnlagPrStatusOgAndelDto dto = lagDto(input, andel);
            usortertDtoList.add(dto);
        }
        // Følgende gjøres for å sortere arbeidsforholdene etter beregnet årsinntekt og deretter arbedsforholdId
        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidsarbeidstakerAndeler = usortertDtoList.stream().filter(dto -> dto.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)).collect(toList());
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndreAndeler = usortertDtoList.stream().filter(dto -> !dto.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)).collect(toList());
        if (dtoKanSorteres(arbeidsarbeidstakerAndeler)) {
            arbeidsarbeidstakerAndeler.sort(comparatorEtterBeregnetOgArbeidsforholdId());
        }
        List<BeregningsgrunnlagPrStatusOgAndelDto> dtoList = new ArrayList<>(arbeidsarbeidstakerAndeler);
        dtoList.addAll(alleAndreAndeler);

        return dtoList;
    }

    private BeregningsgrunnlagPrStatusOgAndelDto lagDto(BeregningsgrunnlagGUIInput input,
                                                        no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var iayGrunnlag = input.getIayGrunnlag();
        var inntektsmeldinger = input.getInntektsmeldinger();
        var ref = input.getKoblingReferanse();
        Optional<FaktaAggregatDto> faktaAggregat = input.getFaktaAggregat();
        BeregningsgrunnlagPrStatusOgAndelDto dto = LagTilpassetDtoTjeneste.opprettTilpassetDTO(ref, andel, iayGrunnlag, faktaAggregat);
        Optional<InntektsmeldingDto> inntektsmelding = FinnInntektsmeldingForAndel.finnInntektsmelding(andel, inntektsmeldinger);
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, inntektsmelding, iayGrunnlag).ifPresent(dto::setArbeidsforhold);
        dto.setDagsats(andel.getDagsats());
        dto.setOriginalDagsatsFraTilstøtendeYtelse(andel.getOrginalDagsatsFraTilstøtendeYtelse());
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setAktivitetStatus(andel.getAktivitetStatus());
        dto.setBeregningsperiodeFom(andel.getBeregningsperiodeFom());
        dto.setBeregningsperiodeTom(andel.getBeregningsperiodeTom());
        dto.setBruttoPrAar(ModellTyperMapper.beløpTilDto(andel.getBruttoPrÅr()));
        dto.setFordeltPrAar(ModellTyperMapper.beløpTilDto(andel.getFordeltPrÅr()));
        dto.setAvkortetPrAar(ModellTyperMapper.beløpTilDto(andel.getAvkortetPrÅr()));
        dto.setRedusertPrAar(ModellTyperMapper.beløpTilDto(andel.getRedusertPrÅr()));
        dto.setOverstyrtPrAar(ModellTyperMapper.beløpTilDto(andel.getOverstyrtPrÅr()));
        dto.setBeregnetPrAar(ModellTyperMapper.beløpTilDto(andel.getBeregnetPrÅr()));
        dto.setInntektskategori(andel.getGjeldendeInntektskategori());
        dto.setBesteberegningPrAar(ModellTyperMapper.beløpTilDto(andel.getBesteberegningPrÅr()));
        dto.setFastsattAvSaksbehandler(andel.getFastsattAvSaksbehandler());
        faktaAggregat.flatMap(fa -> fa.getFaktaArbeidsforhold(andel))
                .map(FaktaArbeidsforholdDto::getErTidsbegrensetVurdering).ifPresent(dto::setErTidsbegrensetArbeidsforhold);
        faktaAggregat.flatMap(FaktaAggregatDto::getFaktaAktør).map(FaktaAktørDto::getErNyIArbeidslivetSNVurdering).ifPresent(dto::setErNyIArbeidslivet);
        faktaAggregat.flatMap(fa -> fa.getFaktaArbeidsforhold(andel))
                .map(FaktaArbeidsforholdDto::getHarLønnsendringIBeregningsperiodenVurdering).ifPresent(dto::setLonnsendringIBeregningsperioden);
        dto.setLagtTilAvSaksbehandler(andel.erLagtTilAvSaksbehandler());
        dto.setErTilkommetAndel(!andel.getKilde().equals(AndelKilde.PROSESS_START));
        if (andel.getAktivitetStatus().erFrilanser()
                || andel.getAktivitetStatus().erArbeidstaker()
                || andel.getAktivitetStatus().erSelvstendigNæringsdrivende()
                || andel.getAktivitetStatus().equals(AktivitetStatus.BRUKERS_ANDEL)) {
            dto.setSkalFastsetteGrunnlag(skalGrunnlagFastsettesForYtelse(input, andel));
        }
        return dto;
    }

    private boolean skalGrunnlagFastsettesForYtelse(BeregningsgrunnlagGUIInput input,
                                                    no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (FagsakYtelseType.OMSORGSPENGER.equals(input.getFagsakYtelseType())) {
            return new FastsettGrunnlagOmsorgspenger().skalGrunnlagFastsettes(input, andel);
        } else {
            return new FastsettGrunnlagGenerell().skalGrunnlagFastsettes(input, andel);
        }
    }

    private static boolean dtoKanSorteres(List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidsarbeidstakerAndeler) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> listMedNull = arbeidsarbeidstakerAndeler
                .stream()
                .filter(a -> a.getBeregnetPrAar() == null)
                .collect(toList());
        return listMedNull.isEmpty();
    }

    private static Comparator<BeregningsgrunnlagPrStatusOgAndelDto> comparatorEtterBeregnetOgArbeidsforholdId() {
        return Comparator.comparing(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrAar)
                .reversed();
    }

}
