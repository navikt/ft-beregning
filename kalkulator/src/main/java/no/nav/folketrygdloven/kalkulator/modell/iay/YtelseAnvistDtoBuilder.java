package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class YtelseAnvistDtoBuilder {
    private final YtelseAnvistDto ytelseAnvist;

    YtelseAnvistDtoBuilder(YtelseAnvistDto ytelseAnvist) {
        this.ytelseAnvist = ytelseAnvist;
    }

    public static YtelseAnvistDtoBuilder ny() {
        return new YtelseAnvistDtoBuilder(new YtelseAnvistDto());
    }

    public YtelseAnvistDtoBuilder medBeløp(Beløp beløp) {
        if (beløp != null) {
            this.ytelseAnvist.setBeløp(beløp);
        }
        return this;
    }

    public YtelseAnvistDtoBuilder medDagsats(Beløp dagsats) {
        if (dagsats != null) {
            this.ytelseAnvist.setDagsats(dagsats);
        }
        return this;
    }

    public YtelseAnvistDtoBuilder medAnvistPeriode(Intervall intervallEntitet){
        this.ytelseAnvist.setAnvistPeriode(intervallEntitet);
        return this;
    }

    public YtelseAnvistDtoBuilder medAnvisteAndeler(List<AnvistAndel> anvisteAndeler){
        this.ytelseAnvist.setAnvisteAndeler(anvisteAndeler);
        return this;
    }

    public YtelseAnvistDtoBuilder medUtbetalingsgradProsent(Stillingsprosent utbetalingsgradProsent) {
        this.ytelseAnvist.setUtbetalingsgradProsent(utbetalingsgradProsent);
        return this;
    }

    public YtelseAnvistDto build() {
        return ytelseAnvist;
    }

}
