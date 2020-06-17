package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
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

    public boolean søkerFrilansISøkandsperiode(LocalDate dato) {
        return søkerStatusISøknadsperiode(dato, AktivitetStatus.FL);
    }

    public boolean søkerNæringISøkandsperiode(LocalDate dato) {
        return søkerStatusISøknadsperiode(dato, AktivitetStatus.SN);
    }

    private boolean søkerStatusISøknadsperiode(LocalDate dato, AktivitetStatus status) {
        YearMonth ym = YearMonth.from(dato);
        if (ym.equals(YearMonth.of(2020,3)) && søkerIMars2020()) {
            return frisinnPerioder.stream()
                .filter(p -> YearMonth.from(p.getPeriode().getTom()).equals(YearMonth.of(2020,4)))
                .anyMatch(p -> AktivitetStatus.FL.equals(status) ? p.getSøkerYtelseFrilans() : p.getSøkerYtelseNæring());
        } else {
            return frisinnPerioder.stream()
                .filter(p -> YearMonth.from(p.getPeriode().getTom()).equals(ym))
                .anyMatch(p -> AktivitetStatus.FL.equals(status) ? p.getSøkerYtelseFrilans() : p.getSøkerYtelseNæring());
        }
    }

    public boolean søkerYtelseFrilans() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerYtelseFrilans);
    }

    public boolean søkerYtelseNæring() {
        return frisinnPerioder.stream().anyMatch(FrisinnPeriode::getSøkerYtelseNæring);
    }

    private boolean søkerIMars2020() {
        return frisinnPerioder.stream().anyMatch(p -> YearMonth.from(p.getPeriode().getFom()).equals(YearMonth.of(2020,3)));
    }
}
