package no.nav.folketrygdloven.kalkulus.migrering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;

public class BeregningsgrunnlagMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private LocalDate skjæringstidspunkt;

    @Valid
    @NotNull
    @Size(max=10)
    private List<BeregningsgrunnlagAktivitetStatusMigreringDto> aktivitetStatuser = new ArrayList<>();

    @Valid
    @NotNull
    @Size(max=100)
    private List<BeregningsgrunnlagPeriodeMigreringDto> beregningsgrunnlagPerioder = new ArrayList<>();

    @Valid
    private BesteberegninggrunnlagMigreringDto besteberegninggrunnlag;

    @Valid
    @Size(max=3)
    private List<SammenligningsgrunnlagPrStatusMigreringDto> sammenligningsgrunnlagPrStatusListe = new ArrayList<>();

    @Valid
    private Beløp grunnbeløp;

    @Valid
    @Size(max=10)
    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller = new ArrayList<>();

    @Valid
    @NotNull
    private boolean overstyrt = false;

    BeregningsgrunnlagMigreringDto() {
    }

    public BeregningsgrunnlagMigreringDto(LocalDate skjæringstidspunkt,
                                          List<BeregningsgrunnlagAktivitetStatusMigreringDto> aktivitetStatuser,
                                          List<BeregningsgrunnlagPeriodeMigreringDto> beregningsgrunnlagPerioder,
                                          BesteberegninggrunnlagMigreringDto besteberegninggrunnlag,
                                          List<SammenligningsgrunnlagPrStatusMigreringDto> sammenligningsgrunnlagPrStatusListe,
                                          Beløp grunnbeløp,
                                          List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller,
                                          boolean overstyrt) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.aktivitetStatuser = aktivitetStatuser;
        this.beregningsgrunnlagPerioder = beregningsgrunnlagPerioder;
        this.besteberegninggrunnlag = besteberegninggrunnlag;
        this.sammenligningsgrunnlagPrStatusListe = sammenligningsgrunnlagPrStatusListe;
        this.grunnbeløp = grunnbeløp;
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
        this.overstyrt = overstyrt;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public List<BeregningsgrunnlagAktivitetStatusMigreringDto> getAktivitetStatuser() {
        return aktivitetStatuser;
    }

    public List<BeregningsgrunnlagPeriodeMigreringDto> getBeregningsgrunnlagPerioder() {
        return beregningsgrunnlagPerioder;
    }

    public BesteberegninggrunnlagMigreringDto getBesteberegninggrunnlag() {
        return besteberegninggrunnlag;
    }

    public List<SammenligningsgrunnlagPrStatusMigreringDto> getSammenligningsgrunnlagPrStatusListe() {
        return sammenligningsgrunnlagPrStatusListe;
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public boolean isOverstyrt() {
        return overstyrt;
    }
}
