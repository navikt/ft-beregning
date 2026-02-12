package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
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
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregnetPrAarKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagPrStatusOgAndelDto;

public class BeregningsgrunnlagPrStatusOgAndelDtoTjeneste {

    public List<BeregningsgrunnlagPrStatusOgAndelDto> lagBeregningsgrunnlagPrStatusOgAndelDto(BeregningsgrunnlagGUIInput input,
                                                                                              List<no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList) {

        List<BeregningsgrunnlagPrStatusOgAndelDto> usortertDtoList = new ArrayList<>();
        for (var andel : beregningsgrunnlagPrStatusOgAndelList) {
            var dto = lagDto(input, andel);
            usortertDtoList.add(dto);
        }
        // Følgende gjøres for å sortere arbeidsforholdene etter beregnet årsinntekt og deretter arbedsforholdId
        var arbeidsarbeidstakerAndeler = usortertDtoList.stream().filter(dto -> dto.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)).collect(toList());
        var alleAndreAndeler = usortertDtoList.stream().filter(dto -> !dto.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)).collect(toList());
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
        var stp = input.getSkjæringstidspunkt();
        var faktaAggregat = input.getFaktaAggregat();
        var dto = LagTilpassetDtoTjeneste.opprettTilpassetDTO(ref, andel, iayGrunnlag, faktaAggregat);
        var inntektsmelding = FinnInntektsmeldingForAndel.finnInntektsmelding(andel, inntektsmeldinger);
        BeregningsgrunnlagDtoUtil.lagUtvidetArbeidsforholdDto(andel, inntektsmelding, iayGrunnlag, stp).ifPresent(dto::setArbeidsforhold);
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
        dto.setBeregnetPrAarKilde(
            finnBeregnetPrAarKilde(andel.getArbeidsgiver(), andel.getBeregnetPrÅr(), inntektsmelding, iayGrunnlag.getAktørInntektFraRegister(),
                andel.getFastsattAvSaksbehandler()));
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
        var listMedNull = arbeidsarbeidstakerAndeler
                .stream()
                .filter(a -> a.getBeregnetPrAar() == null)
                .collect(toList());
        return listMedNull.isEmpty();
    }

    private static Comparator<BeregningsgrunnlagPrStatusOgAndelDto> comparatorEtterBeregnetOgArbeidsforholdId() {
        return Comparator.comparing(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrAar)
                .reversed();
    }

    private BeregnetPrAarKilde finnBeregnetPrAarKilde(Optional<Arbeidsgiver> arbeidsgiver, Beløp beregnetPrÅr, Optional<InntektsmeldingDto> inntektsmelding, Optional<AktørInntektDto> aktørInntekt, Boolean erFastsattAvSb) {
        if (Boolean.TRUE.equals(erFastsattAvSb)) {
            if (inntektsmelding.isPresent()) {
                return erBeregnetPrÅrLikInntektsmelding(beregnetPrÅr,
                    inntektsmelding) ? BeregnetPrAarKilde.INNTEKTSMELDING : BeregnetPrAarKilde.SAKSBEHANDLER;
            } else if (aktørInntekt.isPresent() && arbeidsgiver.isPresent()) {
                Optional<Beløp> beregnetÅrsinntektFraBeregningsgrunnlaget = aktørInntekt.get()
                    .getInntekt()
                    .stream()
                    .filter(inntekt -> inntekt.getInntektsKilde().equals(InntektskildeType.INNTEKT_BEREGNING)
                        && inntekt.getArbeidsgiver().equals(arbeidsgiver.get()))
                    .findFirst()
                    .map(inntekt -> {
                        var allePoster = inntekt.getAlleInntektsposter();
                        // TODO: Filtrere på beregningsperioden fra andelen i stedet for dette
                        var sammenlikningsgrunnlag = allePoster.stream()
                            .skip(Math.max(0, allePoster.size() - 3))
                            .toList();

                        Beløp sum = sammenlikningsgrunnlag.stream()
                            .map(InntektspostDto::getBeløp)
                            .reduce(Beløp::adder)
                            .orElse(Beløp.ZERO);

                        return sum.divider(sammenlikningsgrunnlag.size(), 10, RoundingMode.HALF_UP).multipliser(12);
                    });
                if (beregnetÅrsinntektFraBeregningsgrunnlaget.isPresent() &&
                    beregnetPrÅr.equals(beregnetÅrsinntektFraBeregningsgrunnlaget.get())) {
                    return BeregnetPrAarKilde.A_ORDNING;
                } else {
                    return BeregnetPrAarKilde.SAKSBEHANDLER;
                }
            }
            return BeregnetPrAarKilde.SAKSBEHANDLER;

        } else {
            return inntektsmelding.isPresent() ? BeregnetPrAarKilde.INNTEKTSMELDING : BeregnetPrAarKilde.A_ORDNING;
        }
    }

    private boolean erBeregnetPrÅrLikInntektsmelding(Beløp beregnetPrÅr, Optional<InntektsmeldingDto> inntektsmelding) {
        return inntektsmelding.filter(inntektsmeldingDto -> beregnetPrÅr.equals(inntektsmeldingDto.getInntektBeløp().multipliser(12))).isPresent();
    }
}
