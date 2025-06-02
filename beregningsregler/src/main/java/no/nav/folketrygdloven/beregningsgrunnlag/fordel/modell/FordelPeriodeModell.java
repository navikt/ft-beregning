package no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class FordelPeriodeModell {
	private Periode bgPeriode;
	private List<FordelAndelModell> andeler;

	public FordelPeriodeModell(Periode bgPeriode,
	                           List<FordelAndelModell> andeler) {
		Objects.requireNonNull(bgPeriode, "periode");
		Objects.requireNonNull(andeler, "andeler");
		this.bgPeriode = bgPeriode;
		this.andeler = new ArrayList<>(andeler);
	}

	public Periode getBgPeriode() {
		return bgPeriode;
	}

	public List<FordelAndelModell> getAndeler() {
		return andeler;
	}

	// Utility metode som gir optional når man forventer at det aldri finnes mer enn 1 andel for enm status
	public Optional<FordelAndelModell> getEnesteAndelForStatus(AktivitetStatus status) {
        var andelerForStatus = getAlleAndelerForStatus(status);
		if (andelerForStatus.size() > 1) {
			throw new IllegalStateException("Fant " + andelerForStatus.size() + " andeler for status " + status + ". Forventet maks 1");
		}
		return andelerForStatus.stream().findFirst();
	}

	public List<FordelAndelModell> getAlleAndelerForStatus(AktivitetStatus status) {
		return getAndeler().stream()
				.filter(andel -> andel.getAktivitetStatus().equals(status))
				.toList();
	}

	public List<FordelAndelModell> getBeregningsgrunnlagPrStatusSomSkalBrukes() {
		return andeler.stream().filter(FordelAndelModell::erSøktYtelseFor).toList();
	}

	public void leggTilAndel(FordelAndelModell nyAndel) {
		if (!nyAndel.erNytt()) {
			throw new IllegalStateException("Prøver å legge til en andel " + nyAndel + " som ikke er ny på eksisterende periode " + bgPeriode);
		}
		this.andeler.add(nyAndel);
	}

}
