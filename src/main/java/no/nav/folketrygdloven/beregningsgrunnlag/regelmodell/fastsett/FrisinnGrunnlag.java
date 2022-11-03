package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class FrisinnGrunnlag extends YtelsesSpesifiktGrunnlag {

	private final List<FrisinnPeriode> frisinnPerioder;

	public FrisinnGrunnlag(List<FrisinnPeriode> frisinnPerioder) {
		super("FRISINN");
		Objects.requireNonNull(frisinnPerioder, "frisinnPerioder");
		this.frisinnPerioder = frisinnPerioder;
	}

	public boolean søkerYtelseFrilans(LocalDate dato) {
		return frisinnPerioder.stream().anyMatch(p -> p.inneholderDato(dato) && p.getSøkerYtelseFrilans());
	}

	public boolean søkerYtelseNæring(LocalDate dato) {
		return frisinnPerioder.stream().anyMatch(p -> p.inneholderDato(dato) && p.getSøkerYtelseNæring());
	}


}
