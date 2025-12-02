package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public record RefusjonSplittAndel(
    Arbeidsgiver arbeidsgiver,
    InternArbeidsforholdRefDto internArbeidsforholdRefDto,
    LocalDate startdatoRefusjon,
    Beløp delvisRefusjonBeløpPrÅr
) {
    public InternArbeidsforholdRefDto getInternArbeidsforholdRefDto() {
        return internArbeidsforholdRefDto == null ? InternArbeidsforholdRefDto.nullRef() : internArbeidsforholdRefDto;
    }

    public boolean gjelderFor(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        var andelAG = andel.getArbeidsgiver().orElse(null);
        var andelRef = andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef());
        // Av og til fastsettes andeler med og uten referanse, må derfor sjekke eksakt match på referanse
        return Objects.equals(andelAG, arbeidsgiver) && Objects.equals(getInternArbeidsforholdRefDto().getReferanse(), andelRef.getReferanse());
    }
}
