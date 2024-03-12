package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class FrisinnPeriode {
	private final Periode periode;
	private final boolean søkerYtelseFrilans;
	private final boolean søkerYtelseNæring;

	public FrisinnPeriode(Periode periode, boolean søkerYtelseFrilans, boolean søkerYtelseNæring) {
		Objects.requireNonNull(periode, "periode");
		this.periode = periode;
		this.søkerYtelseFrilans = søkerYtelseFrilans;
		this.søkerYtelseNæring = søkerYtelseNæring;
	}

	public Periode getPeriode() {
		return periode;
	}

	public boolean getSøkerYtelseFrilans() {
		return søkerYtelseFrilans;
	}

	public boolean getSøkerYtelseNæring() {
		return søkerYtelseNæring;
	}

	public boolean inneholderDato(LocalDate dato) {
		return periode.inneholder(dato);
	}
}
