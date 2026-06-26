package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilkommetinntekt;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public record NyttInntektsforholdDto(AktivitetStatus aktivitetStatus, String arbeidsgiverIdentifikator, String arbeidsforholdId,
                                     Integer bruttoInntektPrÅr, boolean skalRedusereUtbetaling) {

    private String sisteTreSiffer(String identifikator) {
        if (identifikator == null) {
            return null;
        }
        var length = identifikator.length();
        if (length <= 3) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 3) + identifikator.substring(length - 3);
    }

    @Override
    public String toString() {
        return "NyttInntektsforholdDto{" + "aktivitetStatus=" + aktivitetStatus + ", arbeidsgiverIdentifikator='" + sisteTreSiffer(arbeidsgiverIdentifikator) + '\''
            + ", arbeidsforholdId='" + sisteTreSiffer(arbeidsforholdId) + '\'' + ", bruttoInntektPrÅr=" + bruttoInntektPrÅr + ", skalRedusereUtbetaling="
            + skalRedusereUtbetaling + '}';
    }
}
