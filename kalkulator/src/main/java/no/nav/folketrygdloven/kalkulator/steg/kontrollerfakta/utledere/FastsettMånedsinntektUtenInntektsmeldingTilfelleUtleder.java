package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return utled(beregningsgrunnlagGrunnlag, input.getInntektsmeldinger());
    }

    private Optional<FaktaOmBeregningTilfelle> utled(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                     Collection<InntektsmeldingDto> inntektsmeldinger) {
        boolean harKunstigVirksomhet = harBeregningsgrunnlagKunstigVirksomhet(beregningsgrunnlagGrunnlag);
        if (harKunstigVirksomhet) {
            return Optional.of(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING);
        }
        boolean harAndelerForSammeVirksomhetMedOgUtenInntektsmelding = harArbeidstakerandelerForSammeVirksomhetMedOgUtenInntektsmelding(beregningsgrunnlagGrunnlag, inntektsmeldinger);
        return harAndelerForSammeVirksomhetMedOgUtenInntektsmelding ? Optional.of(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING) : Optional.empty();
    }

    private boolean harArbeidstakerandelerForSammeVirksomhetMedOgUtenInntektsmelding(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag, Collection<InntektsmeldingDto> inntektsmeldinger) {
        Map<Arbeidsgiver, List<BeregningsgrunnlagPrStatusOgAndelDto>> arbeidsgiverTilAndelerMap = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes()
                .map(bg -> bg.getBeregningsgrunnlagPerioder().get(0))
                .stream()
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .filter(a -> a.getAktivitetStatus().erArbeidstaker() && a.getArbeidsgiver().isPresent())
                .collect(Collectors.groupingBy(a -> a.getArbeidsgiver().orElseThrow(() -> new IllegalStateException("Forventer å ha arbeidsgiver her"))));
        return arbeidsgiverTilAndelerMap.entrySet().stream().anyMatch(entry -> {
            List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = entry.getValue();
            long antallAndelerlerMedInntektsmelding = finnAntallAndelerMedInntektsmelding(inntektsmeldinger, andeler);
            long antallAndelerlerUtenInntektsmelding = finnAntallAndelerUtenInntektsmelding(inntektsmeldinger, andeler);
            return antallAndelerlerMedInntektsmelding > 0 && antallAndelerlerUtenInntektsmelding > 0;
        });
    }

    private long finnAntallAndelerMedInntektsmelding(Collection<InntektsmeldingDto> inntektsmeldinger, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        return andeler.stream()
                .filter(a -> inntektsmeldinger.stream().anyMatch(im -> a.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef())))
                .count();
    }

    private long finnAntallAndelerUtenInntektsmelding(Collection<InntektsmeldingDto> inntektsmeldinger, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        return andeler.stream()
                .filter(a -> inntektsmeldinger.stream().noneMatch(im -> a.gjelderInntektsmeldingFor(im.getArbeidsgiver(), im.getArbeidsforholdRef())))
                .count();
    }


    private boolean harBeregningsgrunnlagKunstigVirksomhet(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        return beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes()
                .stream()
                .flatMap(bg -> bg.getBeregningsgrunnlagPerioder().stream())
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .anyMatch(this::harKunstigArbeidsforhold);
    }


    private boolean harKunstigArbeidsforhold(BeregningsgrunnlagPrStatusOgAndelDto a) {
        if (a.getBgAndelArbeidsforhold().isEmpty()) {
            return false;
        }
        BGAndelArbeidsforholdDto bgAndelArbeidsforhold = a.getBgAndelArbeidsforhold().get();
        Arbeidsgiver arbeidsgiver = bgAndelArbeidsforhold.getArbeidsgiver();
        return arbeidsgiver.getErVirksomhet() && OrgNummer.erKunstig(arbeidsgiver.getOrgnr());
    }
}
