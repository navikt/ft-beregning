package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class YtelseDto {

    private Beløp vedtaksDagsats;
    private YtelseType ytelseType = YtelseType.UDEFINERT;
	private YtelseKilde ytelseKilde = YtelseKilde.UDEFINERT;
    private Intervall periode;
    // Brukes til å skille ulike ytelser med samme ytelsetype
    private Set<YtelseAnvistDto> ytelseAnvist = new LinkedHashSet<>();

    public YtelseDto() {
        // hibernate
    }

    public YtelseDto(YtelseDto ytelse) {
        this.ytelseType = ytelse.getYtelseType();
		this.ytelseKilde = ytelse.getYtelseKilde().orElse(YtelseKilde.UDEFINERT);
        this.periode = ytelse.getPeriode();
        this.ytelseAnvist = ytelse.getYtelseAnvist().stream().map(YtelseAnvistDto::new).collect(Collectors.toCollection(LinkedHashSet::new));
        ytelse.getVedtaksDagsats().ifPresent(dagsats -> this.vedtaksDagsats = dagsats);
    }

    public Optional<Beløp> getVedtaksDagsats() {
        return Optional.ofNullable(vedtaksDagsats);
    }

    public void setVedtaksDagsats(Beløp vedtaksDagsats) {
        this.vedtaksDagsats = vedtaksDagsats;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    void setYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

	public Optional<YtelseKilde> getYtelseKilde() {
		return Optional.ofNullable(ytelseKilde).filter(k -> !k.equals(YtelseKilde.UDEFINERT));
	}

	public boolean harKildeKelvin() {
		return getYtelseKilde().map(k -> k.equals(YtelseKilde.KELVIN)).orElse(false);
	}

	public void setYtelseKilde(YtelseKilde ytelseKilde) {
		this.ytelseKilde = ytelseKilde;
	}

	public Intervall getPeriode() {
        return periode;
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    public Collection<YtelseAnvistDto> getYtelseAnvist() {
        return Collections.unmodifiableCollection(ytelseAnvist);
    }

    void leggTilYtelseAnvist(YtelseAnvistDto ytelseAnvist) {
        this.ytelseAnvist.add(ytelseAnvist);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        return o instanceof YtelseDto that &&
	            Objects.equals(ytelseType, that.ytelseType) &&
		        Objects.equals(ytelseKilde, that.ytelseKilde) &&
                Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ytelseType, ytelseKilde, periode);
    }

    @Override
    public String toString() {
        return "YtelseEntitet{" + //$NON-NLS-1$
                "relatertYtelseType=" + ytelseType + //$NON-NLS-1$
                ", periode=" + periode + //$NON-NLS-1$
                '}';
    }

}
