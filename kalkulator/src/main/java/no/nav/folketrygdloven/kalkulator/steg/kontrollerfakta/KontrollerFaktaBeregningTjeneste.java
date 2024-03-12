package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class KontrollerFaktaBeregningTjeneste {

    private KontrollerFaktaBeregningTjeneste() {
        // Skjul
    }

    public static boolean harAktivitetStatusKunYtelse(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream()
            .allMatch(bgStatus -> bgStatus.getAktivitetStatus().equals(AktivitetStatus.KUN_YTELSE));
    }

    /** Map av inntektsmeldinger per orgnr. */
    public static Map<String, List<InntektsmeldingDto>> hentInntektsmeldingerForVirksomheter(Set<String> virksomheterOrgnr, Collection<InntektsmeldingDto>inntektsmeldinger) {
        return inntektsmeldinger.stream()
            .filter(im -> im.getArbeidsgiver().getErVirksomhet())
            .filter(im -> virksomheterOrgnr.contains(im.getArbeidsgiver().getOrgnr()))
            .collect(Collectors.groupingBy(im-> im.getArbeidsgiver().getOrgnr()));
    }

}
