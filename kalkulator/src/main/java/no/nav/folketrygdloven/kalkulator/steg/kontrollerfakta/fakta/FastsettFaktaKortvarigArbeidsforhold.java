package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

class FastsettFaktaKortvarigArbeidsforhold {

    private FastsettFaktaKortvarigArbeidsforhold() {
    }

    static List<FaktaArbeidsforholdDto> fastsettFaktaForKortvarigeArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (beregningsgrunnlag.getAktivitetStatuser().stream().noneMatch(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())) {
            Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarigeArbeidsforhold = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, iayGrunnlag);
            return kortvarigeArbeidsforhold.values().stream().map(v ->
                    FaktaArbeidsforholdDto.builder(v.getArbeidsgiver(), v.getArbeidsforholdRef())
                            .medErTidsbegrenset(new FaktaVurdering(true, FaktaVurderingKilde.KALKULATOR))
                            .build()
            ).collect(Collectors.toList());
        }
        return List.of();
    }

}
