package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.FastsettFaktaLønnsendring.fastsettFaktaForLønnsendring;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;

public class FastsettFaktaTjenesteK14 {

    public Optional<FaktaAggregatDto> fastsettFakta(BeregningsgrunnlagDto beregningsgrunnlag,
                                                    InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                    Collection<InntektsmeldingDto> inntektsmeldinger) {
        if (KonfigurasjonVerdi.instance().get("AUTOMATISK_BEREGNE_LONNSENDRING", false) || KonfigurasjonVerdi.instance().get("AUTOMATISK_BEREGNE_LONNSENDRING_V2", false)) {
            FaktaAggregatDto.Builder faktaBuilder = FaktaAggregatDto.builder();
            List<FaktaArbeidsforholdDto> faktaLønnsendring = fastsettFaktaForLønnsendring(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger);
            faktaLønnsendring.forEach(faktaBuilder::kopierTilEksisterendeEllerLeggTil);
            if (!faktaBuilder.manglerFakta()) {
                return Optional.of(faktaBuilder.build());
            }
        }
        return Optional.empty();
    }

}
