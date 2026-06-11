package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilkommetinntekt;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public record NyttInntektsforholdDto(AktivitetStatus aktivitetStatus, String arbeidsgiverIdentifikator, String arbeidsforholdId,
                                     Integer bruttoInntektPrÅr, boolean skalRedusereUtbetaling) {
}
