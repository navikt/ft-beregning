package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaKortvarigArbeidsforhold.fastsettFaktaForKortvarigeArbeidsforhold;
import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaLønnsendring.fastsettFaktaForLønnsendring;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

public class FastsettFaktaTjenesteOMP {

    public Optional<FaktaAggregatDto> fastsettFakta(BeregningsgrunnlagDto beregningsgrunnlag,
                                                    InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger) {
        FaktaAggregatDto.Builder faktaBuilder = FaktaAggregatDto.builder();
        if (!harRefusjonPåSkjæringstidspunktet(beregningsgrunnlag.getSkjæringstidspunkt(), inntektsmeldinger)) {
            List<FaktaArbeidsforholdDto> faktaArbeidsforholdDtos = fastsettFaktaForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlag);
            faktaArbeidsforholdDtos.forEach(faktaBuilder::kopierTilEksisterendeEllerLeggTil);
        }
        List<FaktaArbeidsforholdDto> faktaLønnsendring = fastsettFaktaForLønnsendring(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger);
        faktaLønnsendring.forEach(faktaBuilder::kopierTilEksisterendeEllerLeggTil);
        if (!faktaBuilder.manglerFakta()) {
            return Optional.of(faktaBuilder.build());
        }
        return Optional.empty();
    }

    private boolean harRefusjonPåSkjæringstidspunktet(LocalDate skjæringstidspunktForBeregning, Collection<InntektsmeldingDto> inntektsmeldinger) {
        return harMinstEnInntektsmeldingMedRefusjonFraSkjæringstidspunktet(inntektsmeldinger, skjæringstidspunktForBeregning);
    }

    private boolean harMinstEnInntektsmeldingMedRefusjonFraSkjæringstidspunktet(Collection<InntektsmeldingDto> inntektsmeldinger, LocalDate skjæringstidspunktForBeregning) {
        return inntektsmeldinger.stream().anyMatch(im -> harInntektsmeldingRefusjonFraStart(im) || harEndringIRefusjonFraSkjæringstidspunktet(im, skjæringstidspunktForBeregning));
    }

    private boolean harEndringIRefusjonFraSkjæringstidspunktet(InntektsmeldingDto im, LocalDate skjæringstidspunktForBeregning) {
        return im.getEndringerRefusjon().stream().anyMatch(er -> er.getFom().equals(skjæringstidspunktForBeregning) && !er.getRefusjonsbeløp().erNullEller0());
    }

    private boolean harInntektsmeldingRefusjonFraStart(InntektsmeldingDto im) {
        return im.getRefusjonBeløpPerMnd() != null
                && !im.getRefusjonBeløpPerMnd().erNullEller0();
    }

}
