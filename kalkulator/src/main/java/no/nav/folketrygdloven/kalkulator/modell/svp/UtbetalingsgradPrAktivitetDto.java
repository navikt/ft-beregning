package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.List;

public class UtbetalingsgradPrAktivitetDto {

    private AktivitetDto utbetalingsgradArbeidsforhold;
    private List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad;

    public UtbetalingsgradPrAktivitetDto(AktivitetDto utbetalingsgradArbeidsforhold,
                                         List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad) {
        this.utbetalingsgradArbeidsforhold = utbetalingsgradArbeidsforhold;
        this.periodeMedUtbetalingsgrad = periodeMedUtbetalingsgrad;
    }

    public List<PeriodeMedUtbetalingsgradDto> getPeriodeMedUtbetalingsgrad() {
        return periodeMedUtbetalingsgrad;
    }

    public AktivitetDto getUtbetalingsgradArbeidsforhold() {
        return utbetalingsgradArbeidsforhold;
    }

}
