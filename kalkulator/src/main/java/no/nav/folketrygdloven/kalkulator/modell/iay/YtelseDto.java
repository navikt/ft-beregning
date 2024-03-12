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
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class YtelseDto {

    private Beløp vedtaksDagsats;
    private YtelseType ytelseType = YtelseType.UDEFINERT;
    private Intervall periode;
    // Brukes til å skille ulike ytelser med samme ytelsetype
    private Set<YtelseAnvistDto> ytelseAnvist = new LinkedHashSet<>();
    private YtelseGrunnlagDto ytelseGrunnlag;

    public YtelseDto() {
        // hibernate
    }

    public YtelseDto(YtelseDto ytelse) {
        this.ytelseType = ytelse.getYtelseType();
        this.periode = ytelse.getPeriode();
        this.ytelseGrunnlag = ytelse.getYtelseGrunnlag().orElse(null);
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

    public Optional<YtelseGrunnlagDto> getYtelseGrunnlag() {
        return Optional.ofNullable(ytelseGrunnlag);
    }

    void setYtelseGrunnlag(YtelseGrunnlagDto ytelseGrunnlag) {
        this.ytelseGrunnlag = ytelseGrunnlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof YtelseDto))
            return false;
        YtelseDto that = (YtelseDto) o;
        return Objects.equals(ytelseType, that.ytelseType) &&
                Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ytelseType, periode);
    }

    @Override
    public String toString() {
        return "YtelseEntitet{" + //$NON-NLS-1$
                "relatertYtelseType=" + ytelseType + //$NON-NLS-1$
                ", periode=" + periode + //$NON-NLS-1$
                '}';
    }

}
