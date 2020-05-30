package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class FrisinnGrunnlag extends YtelsesSpesifiktGrunnlag {

    private List<FrisinnPeriode> frisinnPerioder;
    private LocalDate skjæringstidspunktOpptjening;

    public FrisinnGrunnlag(List<FrisinnPeriode> frisinnPerioder, LocalDate skjæringstidspunktOpptjening) {
        super("FRISINN");
        Objects.requireNonNull(frisinnPerioder, "frisinnPerioder");
        Objects.requireNonNull(skjæringstidspunktOpptjening, "skjæringstidspunktOpptjening");
        this.frisinnPerioder = frisinnPerioder;
        this.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }

    public List<FrisinnPeriode> getFrisinnPerioder() {
        return frisinnPerioder;
    }

    public boolean søkerYtelseFrilans(LocalDate dato) {
        return frisinnPerioder.stream().anyMatch(p -> p.inneholderDato(dato) && p.getSøkerYtelseFrilans());
    }

    public boolean søkerYtelseFrilans() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerYtelseFrilans);
    }


    public boolean søkerYtelseNæring(LocalDate dato) {
        return frisinnPerioder.stream().anyMatch(p -> p.inneholderDato(dato) && p.getSøkerYtelseNæring());
    }

}
