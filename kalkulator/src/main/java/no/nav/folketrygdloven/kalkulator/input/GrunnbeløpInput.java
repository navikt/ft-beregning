package no.nav.folketrygdloven.kalkulator.input;

import java.time.LocalDate;

public record GrunnbeløpInput(LocalDate fom, LocalDate tom, Long gVerdi, Long gSnitt) {

}
