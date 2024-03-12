package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;

public class SvangerskapspengerGrunnlag extends UtbetalingsgradGrunnlag implements YtelsespesifiktGrunnlag {

    /**
     * Når vi start behandlingen av saken, skal brukes for å vurdere refusjon til arbeidsforhold som er avsluttet
     */
    private LocalDate behandlingstidspunkt;

    public SvangerskapspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbetalingsgrad) {
        super(tilretteleggingMedUtbetalingsgrad);
    }

    public SvangerskapspengerGrunnlag(List<UtbetalingsgradPrAktivitetDto> tilretteleggingMedUtbetalingsgrad, LocalDate tilkommetInntektHensyntasFom) {
        super(tilretteleggingMedUtbetalingsgrad, tilkommetInntektHensyntasFom);
    }

    public Optional<LocalDate> getBehandlingstidspunkt() {
        return Optional.ofNullable(behandlingstidspunkt);
    }

    public void setBehandlingstidspunkt(LocalDate behandlingstidspunkt) {
        this.behandlingstidspunkt = behandlingstidspunkt;
    }
}
