package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class OpplæringspengerGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    public OpplæringspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    public OpplæringspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad, LocalDate tilkommetInntektHensyntasFom) {
        super(tilretteleggingMedUtbelingsgrad, tilkommetInntektHensyntasFom);
    }
}
