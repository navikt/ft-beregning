package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

class FastsettFaktaLønnsendring {

    private FastsettFaktaLønnsendring() {
    }

    static List<FaktaArbeidsforholdDto> fastsettFaktaForLønnsendring(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                     Collection<InntektsmeldingDto> inntektsmeldinger) {
        var aktiviteterMedLønnsendring = LønnsendringTjeneste.finnAktiviteterMedLønnsendringUtenInntektsmeldingIStandardBeregningsperiodeOgTilStp(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger);
        var andelerMedLønnsendring = finnAndelerMedLønnsendring(aktiviteterMedLønnsendring, beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));
        return andelerMedLønnsendring.stream().map(a ->
                FaktaArbeidsforholdDto.builder(a.getArbeidsgiver().orElseThrow(),
                                a.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).orElse(InternArbeidsforholdRefDto.nullRef()))
                        .medHarLønnsendringIBeregningsperioden(new FaktaVurdering(true, FaktaVurderingKilde.KALKULATOR))
                        .build()
        ).collect(Collectors.toList());
    }

    public static List<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelerMedLønnsendring(List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring, BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> harLønnsendring(yrkesaktiviteterMedLønnsendring, a))
                .collect(Collectors.toList());
    }
    private static boolean harLønnsendring(List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring, BeregningsgrunnlagPrStatusOgAndelDto a) {
        return a.getAktivitetStatus().erArbeidstaker() &&
                !finnMatchendeYrkesaktiviteterMedLønnsendring(yrkesaktiviteterMedLønnsendring, a).isEmpty();
    }

    private static List<YrkesaktivitetDto> finnMatchendeYrkesaktiviteterMedLønnsendring(List<YrkesaktivitetDto> yrkesaktiviteterMedLønnsendring, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().isPresent() ? yrkesaktiviteterMedLønnsendring.stream().filter(y -> y.gjelderFor(andel.getBgAndelArbeidsforhold().get().getArbeidsgiver(), andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef())).collect(Collectors.toList()) :
                Collections.emptyList();
    }
}
