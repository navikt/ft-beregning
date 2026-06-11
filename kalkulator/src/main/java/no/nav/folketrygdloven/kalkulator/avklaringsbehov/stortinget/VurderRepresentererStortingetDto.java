package no.nav.folketrygdloven.kalkulator.avklaringsbehov.stortinget;

import java.time.LocalDate;

public record VurderRepresentererStortingetDto(LocalDate fom, LocalDate tom, boolean representererStortinget) {
}
