package no.nav.folketrygdloven.kalkulator.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;

public class BeregningsgrunnlagRegelResultat {
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningAktivitetAggregatDto registerAktiviteter;
    private FaktaAggregatDto faktaAggregatDto;
    private List<BeregningAvklaringsbehovResultat> avklaringsbehov = new ArrayList<>();
    private List<BeregningVilkårResultat> vilkårsresultat = new ArrayList<>();
    private RegelSporingAggregat regelsporinger;

    public BeregningsgrunnlagRegelResultat(BeregningAktivitetAggregatDto registerAktiviteter) {
        this.registerAktiviteter = registerAktiviteter;
    }

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag,
                                           List<BeregningAvklaringsbehovResultat> avklaringsbehovResultatListe) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.avklaringsbehov = avklaringsbehovResultatListe;
    }

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag, RegelSporingAggregat regelsporinger) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.regelsporinger = regelsporinger;
    }

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag,
                                           FaktaAggregatDto faktaAggregatDto,
                                           RegelSporingAggregat regelsporinger) {
        this.faktaAggregatDto = faktaAggregatDto;
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.regelsporinger = regelsporinger;
    }

    public BeregningsgrunnlagRegelResultat(BeregningsgrunnlagDto beregningsgrunnlag,
                                           List<BeregningAvklaringsbehovResultat> avklaringsbehov,
                                           RegelSporingAggregat regelsporinger) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.avklaringsbehov = avklaringsbehov;
        this.regelsporinger = regelsporinger;
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public Optional<BeregningsgrunnlagDto> getBeregningsgrunnlagHvisFinnes() {
        return Optional.ofNullable(beregningsgrunnlag);
    }

    public BeregningAktivitetAggregatDto getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public List<BeregningAvklaringsbehovResultat> getAvklaringsbehov() {
        return avklaringsbehov;
    }

    public Boolean getVilkårOppfylt() {

        if (vilkårsresultat != null) {
            return vilkårsresultat.stream().allMatch(BeregningVilkårResultat::getErVilkårOppfylt);
        }

        return null;
    }


    public void setVilkårsresultat(List<BeregningVilkårResultat> vilkårsresultat) {
        this.vilkårsresultat = vilkårsresultat;
    }

    public void setRegisterAktiviteter(BeregningAktivitetAggregatDto registerAktiviteter) {
        this.registerAktiviteter = registerAktiviteter;
    }

    public List<BeregningVilkårResultat> getVilkårsresultat() {
        return vilkårsresultat;
    }

    public Optional<RegelSporingAggregat> getRegelsporinger() {
        return Optional.ofNullable(regelsporinger);
    }

    public FaktaAggregatDto getFaktaAggregatDto() {
        return faktaAggregatDto;
    }
}
