package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class PleiepengerSyktBarnGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    public PleiepengerSyktBarnGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    public PleiepengerSyktBarnGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad, LocalDate tilkommetInntektHensyntasFom) {
        super(tilretteleggingMedUtbelingsgrad, tilkommetInntektHensyntasFom);
    }
}
