package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class PleiepengerNærståendeGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    public PleiepengerNærståendeGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad) {
        super(tilretteleggingMedUtbelingsgrad);
    }

    public PleiepengerNærståendeGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbelingsgrad, LocalDate tilkommetInntektHensyntasFom) {
        super(tilretteleggingMedUtbelingsgrad, tilkommetInntektHensyntasFom);
    }
}
