package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;


public class BeregningsgrunnlagGrunnlagDtoBuilder {
    private BeregningsgrunnlagGrunnlagDto kladd;
    private boolean built;

    private BeregningsgrunnlagGrunnlagDtoBuilder(BeregningsgrunnlagGrunnlagDto kladd) {
        this.kladd = kladd;
    }

    public static BeregningsgrunnlagGrunnlagDtoBuilder nytt() {
        return new BeregningsgrunnlagGrunnlagDtoBuilder(new BeregningsgrunnlagGrunnlagDto());
    }

    public static BeregningsgrunnlagGrunnlagDtoBuilder oppdatere(BeregningsgrunnlagGrunnlagDto kladd) {
        return new BeregningsgrunnlagGrunnlagDtoBuilder(new BeregningsgrunnlagGrunnlagDto(kladd));
    }

    public static BeregningsgrunnlagGrunnlagDtoBuilder oppdatere(Optional<BeregningsgrunnlagGrunnlagDto> kladd) {
        return kladd.map(BeregningsgrunnlagGrunnlagDtoBuilder::oppdatere).orElseGet(BeregningsgrunnlagGrunnlagDtoBuilder::nytt);
    }

    public BeregningsgrunnlagDto.Builder getBeregningsgrunnlagBuilder() {
        return BeregningsgrunnlagDto.Builder.oppdater(kladd.getBeregningsgrunnlagHvisFinnes());
    }

    /** Henter oppdaterere for fakta-aggregat
     **
     * @return Builder som lager kopi og oppdaterer fakta. Må settes på grunnlaget etter endring.
     */
    public FaktaAggregatDto.Builder getFaktaAggregatBuilder() {
        return kladd.getFaktaAggregat().map(FaktaAggregatDto.Builder::oppdater).orElse(FaktaAggregatDto.builder());
    }

    public BeregningsgrunnlagGrunnlagDtoBuilder medBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
        verifiserKanModifisere();
        kladd.setBeregningsgrunnlag(beregningsgrunnlag);
        return this;
    }

    public BeregningsgrunnlagGrunnlagDtoBuilder medRegisterAktiviteter(BeregningAktivitetAggregatDto registerAktiviteter) {
        verifiserKanModifisere();
        kladd.setRegisterAktiviteter(registerAktiviteter);
        return this;
    }

    public BeregningsgrunnlagGrunnlagDtoBuilder medRefusjonOverstyring(BeregningRefusjonOverstyringerDto beregningRefusjonOverstyringer){
        verifiserKanModifisere();
        kladd.setRefusjonOverstyringer(beregningRefusjonOverstyringer);
        return this;
    }

    public BeregningsgrunnlagGrunnlagDtoBuilder medSaksbehandletAktiviteter(BeregningAktivitetAggregatDto saksbehandletAktiviteter) {
        verifiserKanModifisere();
        kladd.setSaksbehandletAktiviteter(saksbehandletAktiviteter);
        return this;
    }

    public BeregningsgrunnlagGrunnlagDto build(BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        if(built) {
            return kladd;
        }
        Objects.requireNonNull(beregningsgrunnlagTilstand);
        kladd.setBeregningsgrunnlagTilstand(beregningsgrunnlagTilstand);
        built = true;
        return kladd;
    }

    public BeregningsgrunnlagGrunnlagDto buildUtenIdOgTilstand() {
        return kladd;
    }

    public BeregningsgrunnlagGrunnlagDtoBuilder medOverstyring(BeregningAktivitetOverstyringerDto beregningAktivitetOverstyringer) {
        verifiserKanModifisere();
        kladd.setOverstyringer(beregningAktivitetOverstyringer);
        return this;
    }

    public BeregningsgrunnlagGrunnlagDtoBuilder medFaktaAggregat(FaktaAggregatDto faktaAggregatDto) {
        verifiserKanModifisere();
        kladd.setFaktaAggregat(faktaAggregatDto);
        return this;
    }

    private void verifiserKanModifisere() {
        if(built) {
            throw new IllegalStateException("Er allerede bygd, kan ikke oppdatere videre: " + this.kladd);
        }
    }
}
