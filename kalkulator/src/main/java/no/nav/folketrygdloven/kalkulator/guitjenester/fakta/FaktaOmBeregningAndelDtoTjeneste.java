package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.guitjenester.BeregningsgrunnlagDtoUtil;
import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.KontrollerFaktaBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.ATogFLISammeOrganisasjonDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningAndelDto;

// TODO (Safir) Denne bør splittes opp til tre klasser: ei klasse for ATFL i samme org, ei for frilanser og ei for AT med lønnsendring (uten inntektsmelding)
public class FaktaOmBeregningAndelDtoTjeneste {

    private FaktaOmBeregningAndelDtoTjeneste() {
    }

    static Optional<FaktaOmBeregningAndelDto> lagFrilansAndelDto(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().isEmpty()) {
            return Optional.empty();
        }
        BeregningsgrunnlagPeriodeDto førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndelDto frilansAndel = førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
                .findFirst()
                .orElse(null);
        if (frilansAndel != null) {
            FaktaOmBeregningAndelDto dto = new FaktaOmBeregningAndelDto();
            BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(frilansAndel, Optional.empty(), inntektArbeidYtelseGrunnlag)
                    .ifPresent(dto::setArbeidsforhold);
            dto.setInntektskategori(frilansAndel.getGjeldendeInntektskategori());
            dto.setAndelsnr(frilansAndel.getAndelsnr());
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    /// ATFL I samme organisasjon
    static List<ATogFLISammeOrganisasjonDto> lagATogFLISAmmeOrganisasjonListe(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                              Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                              InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        Set<Arbeidsgiver> arbeidsgivere = KontrollerFaktaBeregningFrilanserTjeneste
                .brukerErArbeidstakerOgFrilanserISammeOrganisasjon(beregningsgrunnlag, inntektArbeidYtelseGrunnlag);
        if (arbeidsgivere.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> arbeidsgivereSomErVirksomheter = arbeidsgivere
                .stream()
                .filter(Arbeidsgiver::getErVirksomhet)
                .map(Arbeidsgiver::getOrgnr)
                .collect(Collectors.toSet());

        Map<String, List<InntektsmeldingDto>> inntektsmeldingMap = KontrollerFaktaBeregningTjeneste
                .hentInntektsmeldingerForVirksomheter(arbeidsgivereSomErVirksomheter, inntektsmeldinger);
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(andel -> andel.getAktivitetStatus().erArbeidstaker())
                .collect(Collectors.toList());

        List<ATogFLISammeOrganisasjonDto> resultatListe = new ArrayList<>();
        for (Arbeidsgiver arbeidsgiver : arbeidsgivere) {
            andeler.stream()
                    .filter(
                            andel -> andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver).map(a -> a.equals(arbeidsgiver)).orElse(false))
                    .forEach(andel -> resultatListe.add(lagATogFLISAmmeOrganisasjon(andel, inntektsmeldingMap, inntektArbeidYtelseGrunnlag)));
        }
        return resultatListe;
    }

    private static ATogFLISammeOrganisasjonDto lagATogFLISAmmeOrganisasjon(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                           Map<String, List<InntektsmeldingDto>> inntektsmeldingMap,
                                                                           InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        ATogFLISammeOrganisasjonDto dto = new ATogFLISammeOrganisasjonDto();
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
                .ifPresent(dto::setArbeidsforhold);
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setInntektskategori(andel.getGjeldendeInntektskategori());
        dto.setAktivitetStatus(andel.getAktivitetStatus());

        // Privapersoner sender ikke inntektsmelding, disse må alltid fastsettes
        if (andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver).map(Arbeidsgiver::getErVirksomhet).orElse(false)) {
            Optional<InntektsmeldingDto> inntektsmelding = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver)
                    .filter(Arbeidsgiver::getErVirksomhet)
                    .flatMap(arbeidsgiver -> finnRiktigInntektsmelding(
                            inntektsmeldingMap,
                            arbeidsgiver.getOrgnr(),
                            andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)));
            inntektsmelding.map(InntektsmeldingDto::getInntektBeløp).map(ModellTyperMapper::beløpTilDto).ifPresent(dto::setInntektPrMnd);
        }
        return dto;
    }

    /// Arbeidsforhold uten inntektsmelding
    static List<FaktaOmBeregningAndelDto> lagArbeidsforholdUtenInntektsmeldingDtoList(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                      Optional<FaktaAggregatDto> faktaAggregat,
                                                                                      InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                                      Collection<InntektsmeldingDto> inntektsmeldinger) {
        List<YrkesaktivitetDto> aktiviteterMedLønnsendring = LønnsendringTjeneste.finnAktiviteterMedLønnsendringUtenInntektsmeldingIBeregningsperiodenOgTilStp(beregningsgrunnlag, inntektArbeidYtelseGrunnlag, inntektsmeldinger);
        var aktiviteterMedLønnsendringUtenVurdering = aktiviteterMedLønnsendring.stream().filter(ya -> harIkkeBlittVurdertAutomatisk(faktaAggregat, ya))
                .toList();
        if (aktiviteterMedLønnsendringUtenVurdering.isEmpty()) {
            return Collections.emptyList();
        }
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(andel -> andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver).isPresent())
                .collect(Collectors.toList());
        List<FaktaOmBeregningAndelDto> arbeidsforholdMedLønnsendringUtenIMDtoList = new ArrayList<>();
        for (YrkesaktivitetDto yrkesaktivitet : aktiviteterMedLønnsendringUtenVurdering) {
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> korrektAndel = finnKorrektAndelFraArbeidsgiver(andeler, yrkesaktivitet.getArbeidsgiver());
            if (korrektAndel.isEmpty()) {
                throw new IllegalStateException("Utviklerfeil: Finner ikke korrekt andel for yrkesaktiviteten.");
            }
            FaktaOmBeregningAndelDto dto = lagArbeidsforholdUtenInntektsmeldingDto(korrektAndel.get(), inntektArbeidYtelseGrunnlag);
            arbeidsforholdMedLønnsendringUtenIMDtoList.add(dto);
        }
        return arbeidsforholdMedLønnsendringUtenIMDtoList;
    }

    private static boolean harIkkeBlittVurdertAutomatisk(Optional<FaktaAggregatDto> faktaAggregat, YrkesaktivitetDto ya) {
        return faktaAggregat.stream().flatMap(f -> f.getFaktaArbeidsforhold().stream())
                .noneMatch(a ->
                        a.getArbeidsgiver().equals(ya.getArbeidsgiver()) &&
                                a.getArbeidsforholdRef().gjelderFor(ya.getArbeidsforholdRef()) &&
                                a.getHarLønnsendringIBeregningsperiodenVurdering() != null &&
                                a.getHarLønnsendringIBeregningsperioden().getKilde().equals(FaktaVurderingKilde.KALKULATOR));
    }

    public static FaktaOmBeregningAndelDto lagArbeidsforholdUtenInntektsmeldingDto(BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        FaktaOmBeregningAndelDto dto = new FaktaOmBeregningAndelDto();
        BeregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel, Optional.empty(), inntektArbeidYtelseGrunnlag)
                .ifPresent(dto::setArbeidsforhold);
        dto.setAndelsnr(andel.getAndelsnr());
        dto.setInntektskategori(andel.getGjeldendeInntektskategori());
        return dto;
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnKorrektAndelFraArbeidsgiver(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler, Arbeidsgiver arbeidsgiver) {
        return andeler.stream()
                .filter(andel -> andel.getBgAndelArbeidsforhold()
                        .map(BGAndelArbeidsforholdDto::getArbeidsgiver)
                        .map(ag -> ag.equals(arbeidsgiver))
                        .orElse(false))
                .findFirst();
    }

    private static Optional<InntektsmeldingDto> finnRiktigInntektsmelding(Map<String, List<InntektsmeldingDto>> inntektsmeldingMap, String virksomhetOrgnr,
                                                                          Optional<InternArbeidsforholdRefDto> arbeidsforholdRef) {
        if (!inntektsmeldingMap.containsKey(virksomhetOrgnr)) {
            return Optional.empty();
        }
        Collection<InntektsmeldingDto> inntektsmeldinger = inntektsmeldingMap.get(virksomhetOrgnr);
        if (inntektsmeldinger.size() == 1) {
            return Optional.ofNullable(List.copyOf(inntektsmeldinger).get(0));
        }
        return inntektsmeldinger.stream()
                .filter(im -> arbeidsforholdRef.map(ref -> ref.equals(im.getArbeidsforholdRef()))
                        .orElse(false))
                .findFirst();
    }

}
