package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilkommetinntekt;

import java.time.LocalDate;
import java.util.List;

public record VurderTilkomneInntektsforholdPeriodeDto (
    List<NyttInntektsforholdDto> tilkomneInntektsforhold,
    LocalDate fom,
    LocalDate tom) {}
