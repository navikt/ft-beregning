package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class YtelseDtoBuilder {

    private final YtelseDto ytelse;
    private final boolean oppdaterer;

    private YtelseDtoBuilder(YtelseDto ytelse, boolean oppdaterer) {
        this.ytelse = ytelse;
        this.oppdaterer = oppdaterer;
    }

    public static YtelseDtoBuilder ny() {
        return new YtelseDtoBuilder(new YtelseDto(), false);
    }

    public YtelseDtoBuilder medYtelseType(YtelseType relatertYtelseType) {
        ytelse.setYtelseType(relatertYtelseType);
        return this;
    }

    public YtelseDtoBuilder medPeriode(Intervall intervallEntitet) {
        ytelse.setPeriode(intervallEntitet);
        return this;
    }

    public YtelseDtoBuilder medYtelseGrunnlag(YtelseGrunnlagDto ytelseGrunnlag) {
        ytelse.setYtelseGrunnlag(ytelseGrunnlag);
        return this;
    }


    public YtelseDtoBuilder medVedtaksDagsats(Beløp vedtakDagsats) {
        ytelse.setVedtaksDagsats(vedtakDagsats);
        return this;
    }

    public YtelseDtoBuilder leggTilYtelseAnvist(YtelseAnvistDto ytelseAnvist) {
        ytelse.leggTilYtelseAnvist(ytelseAnvist);
        return this;
    }

    public Intervall getPeriode() {
        return ytelse.getPeriode();
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public YtelseDto build() {
        validerUtbetalingsgrader();
        return ytelse;
    }

    private void validerUtbetalingsgrader() {
        if (ytelse.getYtelseType().erArenaytelse()) {
            Optional<YtelseAnvistDto> ulovligMeldekort = ytelse.getYtelseAnvist().stream()
                    .filter(this::harUtbetalingsgradOver200)
                    .findFirst();
            if (ulovligMeldekort.isPresent()) {
                throw new IllegalStateException("Finnes meldekort " + ulovligMeldekort.get() +
                        " med utbetalingsgrad som overskrider maksimalt tillatte verdi");
            }
        }
    }

    private boolean harUtbetalingsgradOver200(YtelseAnvistDto ya) {
        return ya.getUtbetalingsgradProsent().orElse(Stillingsprosent.ZERO).compareTo(Stillingsprosent.fra(200)) > 0;
    }

    public YtelseAnvistDtoBuilder getAnvistBuilder() {
        return YtelseAnvistDtoBuilder.ny();
    }


}
