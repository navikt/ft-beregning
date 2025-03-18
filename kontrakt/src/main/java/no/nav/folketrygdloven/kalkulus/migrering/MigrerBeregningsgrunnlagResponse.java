package no.nav.folketrygdloven.kalkulus.migrering;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.besteberegning.BesteberegningGrunnlagDto;

public record MigrerBeregningsgrunnlagResponse(@Valid @NotNull BeregningsgrunnlagGrunnlagDto grunnlag,
                                               @Valid BesteberegningGrunnlagDto besteberegningGrunnlag,
                                               @Valid @NotNull List<RegelsporingPeriode> sporingerPeriode,
                                               @Valid @NotNull List<RegelsporingGrunnlag> sporingerGrunnlag,
                                               @Valid @NotNull List<Avklaringsbehov> avklaringsbehov) {
    public record RegelsporingPeriode(@Valid @NotNull BeregningsgrunnlagPeriodeRegelType type, @Valid @NotNull String regelevaluering, @Valid @NotNull String regelinput, @Valid String regelversjon, @Valid @NotNull Periode periode){}
    public record RegelsporingGrunnlag(@Valid @NotNull BeregningsgrunnlagRegelType type, @Valid @NotNull String regelevaluering, @Valid @NotNull String regelinput, @Valid @NotNull String regelversjon){}
	public record Avklaringsbehov(@Valid @NotNull AvklaringsbehovDefinisjon definisjon, @Valid @NotNull AvklaringsbehovStatus status, @Valid String begrunnelse, @Valid String vurdertAv, @Valid LocalDateTime vurdertTidspunkt){}
}