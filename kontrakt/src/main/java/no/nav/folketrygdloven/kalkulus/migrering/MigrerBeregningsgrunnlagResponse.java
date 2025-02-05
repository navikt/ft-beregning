package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.besteberegning.BesteberegningGrunnlagDto;

public record MigrerBeregningsgrunnlagResponse(@Valid @NotNull BeregningsgrunnlagGrunnlagDto grunnlag,
                                               @Valid BesteberegningGrunnlagDto besteberegningGrunnlag,
                                               @Valid @NotNull List<RegelsporingPeriode> sporingerPeriode,
                                               @Valid @NotNull List<RegelsporingGrunnlag> sporingerGrunnlag) {
    public record RegelsporingPeriode(@Valid @NotNull BeregningsgrunnlagPeriodeRegelType type, @Valid @NotNull String regelevaluering, @Valid @NotNull String regelinput, @Valid String regelversjon, @Valid @NotNull Periode periode){}
    public record RegelsporingGrunnlag(@Valid @NotNull BeregningsgrunnlagRegelType type, @Valid @NotNull String regelevaluering, @Valid @NotNull String regelinput, @Valid @NotNull String regelversjon){}
}
