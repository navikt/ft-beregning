package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn;

import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class FrisinnGrunnlag extends YtelsesSpesifiktGrunnlag {

    private boolean erSøktYtelseForFrilans;
    private LocalDate skjæringstidspunktOpptjening;

    public FrisinnGrunnlag(boolean erSøktYtelseForFrilans, LocalDate skjæringstidspunktOpptjening) {
        super("FRISINN");
        this.erSøktYtelseForFrilans = erSøktYtelseForFrilans;
        this.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
    }

    public boolean isErSøktYtelseForFrilans() {
        return erSøktYtelseForFrilans;
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }
}
