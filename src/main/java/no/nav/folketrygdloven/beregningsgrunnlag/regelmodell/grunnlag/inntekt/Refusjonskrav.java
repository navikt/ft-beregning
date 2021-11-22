package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;

public class Refusjonskrav {

    private Periode periode;
    private BigDecimal månedsbeløp;
    private Utfall fristvurdering = Utfall.IKKE_VURDERT;

    public Refusjonskrav(BigDecimal månedsbeløp, LocalDate fom, LocalDate tom, Utfall fristvurdering) {
	    Objects.requireNonNull(månedsbeløp);
        Objects.requireNonNull(fom);
        this.fristvurdering = fristvurdering;
        this.periode = new Periode(fom, tom);
        this.månedsbeløp = månedsbeløp;
    }

	public Refusjonskrav(BigDecimal månedsbeløp, LocalDate fom, LocalDate tom) {
		Objects.requireNonNull(månedsbeløp);
		Objects.requireNonNull(fom);
		this.periode = new Periode(fom, tom);
		this.månedsbeløp = månedsbeløp;
	}

    public LocalDate getFom() {
        return periode.getFom();
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getMånedsbeløp() {
        return månedsbeløp;
    }

	public BigDecimal getInnvilgetBeløp() {
		return Utfall.GODKJENT.equals(fristvurdering) ? månedsbeløp : BigDecimal.ZERO;
	}


	public Utfall getFristvurdering() {
		return fristvurdering;
	}

	@Override
    public String toString() {
        return "Refusjonskrav{" +
            "periode=" + periode +
            ", månedsbeløp=" + månedsbeløp +
            '}';
    }
}
