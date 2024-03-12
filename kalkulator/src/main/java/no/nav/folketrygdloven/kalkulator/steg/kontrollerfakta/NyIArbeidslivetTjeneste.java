package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.Collections;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;

public class NyIArbeidslivetTjeneste {

    private NyIArbeidslivetTjeneste() {
        // Skjul
    }

    public static boolean erNyIArbeidslivetMedAktivitetStatusSN(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        boolean erSN = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende());
        return erSN
            && iayGrunnlag.getOppgittOpptjening()
            .map(OppgittOpptjeningDto::getEgenNæring)
            .orElse(Collections.emptyList())
            .stream()
            .anyMatch(OppgittEgenNæringDto::getNyIArbeidslivet);
    }
}
