package no.nav.folketrygdloven.kalkulus.response.v1.besteberegning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

import java.util.List;

/**
 * Responsobjekt som viser en koblings besteberegningsgrunnlag
 */
public record BesteberegningGrunnlagDto(@Valid @NotNull List<BesteberegningMånedDto> seksBesteMåneder, @Valid @NotNull Beløp avvikFørsteOgTredjeLedd){

    public record BesteberegningMånedDto(@Valid @NotNull Periode periode, @Valid @NotNull List<BesteberegningInntektDto> inntekter){}

    public record BesteberegningInntektDto(@Valid @NotNull OpptjeningAktivitetType opptjeningAktiviteterDto, @Valid @NotNull Beløp inntekt,
                                           @Valid Arbeidsgiver arbeidsgiver, @Valid InternArbeidsforholdRefDto internArbeidsforholdRefDto){}
}

