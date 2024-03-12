package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class BeregningsgrunnlagGrunnlagDto {

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningAktivitetAggregatDto registerAktiviteter;
    private BeregningAktivitetAggregatDto saksbehandletAktiviteter;
    private BeregningAktivitetOverstyringerDto overstyringer;
    private FaktaAggregatDto faktaAggregat;
    private BeregningRefusjonOverstyringerDto refusjonOverstyringer;
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    public BeregningsgrunnlagGrunnlagDto() {
    }

    BeregningsgrunnlagGrunnlagDto(BeregningsgrunnlagGrunnlagDto grunnlag) {
        grunnlag.getBeregningsgrunnlagHvisFinnes().ifPresent(this::setBeregningsgrunnlag);
        this.setRegisterAktiviteter(grunnlag.getRegisterAktiviteter());
        grunnlag.getSaksbehandletAktiviteter().ifPresent(this::setSaksbehandletAktiviteter);
        grunnlag.getOverstyring().ifPresent(this::setOverstyringer);
        grunnlag.getRefusjonOverstyringer().ifPresent(this::setRefusjonOverstyringer);
        grunnlag.getFaktaAggregat().ifPresent(this::setFaktaAggregat);
        this.beregningsgrunnlagTilstand = grunnlag.getBeregningsgrunnlagTilstand();
    }

    public Optional<BeregningsgrunnlagDto> getBeregningsgrunnlagHvisFinnes() {
        return Optional.ofNullable(beregningsgrunnlag);
    }

    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }
    public BeregningAktivitetAggregatDto getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public Optional<BeregningAktivitetAggregatDto> getSaksbehandletAktiviteter() {
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<BeregningAktivitetAggregatDto> getOverstyrteEllerSaksbehandletAktiviteter() {
        Optional<BeregningAktivitetAggregatDto> overstyrteAktiviteter = getOverstyrteAktiviteter();
        if (overstyrteAktiviteter.isPresent()) {
            return overstyrteAktiviteter;
        }
        return Optional.ofNullable(saksbehandletAktiviteter);
    }

    public Optional<BeregningAktivitetOverstyringerDto> getOverstyring() {
        return Optional.ofNullable(overstyringer);
    }

    public Optional<FaktaAggregatDto> getFaktaAggregat() {
        return Optional.ofNullable(faktaAggregat);
    }

    public BeregningAktivitetAggregatDto getGjeldendeAktiviteter() {
        return getOverstyrteAktiviteter()
                .or(this::getSaksbehandletAktiviteter)
                .orElse(registerAktiviteter);
    }

    public BeregningAktivitetAggregatDto getOverstyrteEllerRegisterAktiviteter() {
        Optional<BeregningAktivitetAggregatDto> overstyrteAktiviteter = getOverstyrteAktiviteter();
        if (overstyrteAktiviteter.isPresent()) {
            return overstyrteAktiviteter.get();
        }
        return registerAktiviteter;
    }

    public BeregningsgrunnlagTilstand getBeregningsgrunnlagTilstand() {
        return beregningsgrunnlagTilstand;
    }

    void setBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    void setRegisterAktiviteter(BeregningAktivitetAggregatDto registerAktiviteter) {
        this.registerAktiviteter = registerAktiviteter;
    }

    void setSaksbehandletAktiviteter(BeregningAktivitetAggregatDto saksbehandletAktiviteter) {
        this.saksbehandletAktiviteter = saksbehandletAktiviteter;
    }

    void setBeregningsgrunnlagTilstand(BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        this.beregningsgrunnlagTilstand = beregningsgrunnlagTilstand;
    }

    void setOverstyringer(BeregningAktivitetOverstyringerDto overstyringer) {
        this.overstyringer = overstyringer;
    }

    void setFaktaAggregat(FaktaAggregatDto faktaAggregat) {
        this.faktaAggregat = faktaAggregat;
    }

    public Optional<BeregningRefusjonOverstyringerDto> getRefusjonOverstyringer() {
        return Optional.ofNullable(refusjonOverstyringer);
    }

    void setRefusjonOverstyringer(BeregningRefusjonOverstyringerDto refusjonOverstyringer) {
        this.refusjonOverstyringer = refusjonOverstyringer;
    }

    private Optional<BeregningAktivitetAggregatDto> getOverstyrteAktiviteter() {
        if (overstyringer != null) {
            List<BeregningAktivitetDto> overstyrteAktiviteter = registerAktiviteter.getBeregningAktiviteter().stream()
                    .filter(beregningAktivitet -> beregningAktivitet.skalBrukes(overstyringer))
                    .collect(Collectors.toList());
            BeregningAktivitetAggregatDto.Builder overstyrtBuilder = BeregningAktivitetAggregatDto.builder()
                    .medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
            overstyrteAktiviteter.forEach(aktivitet -> {
                Optional<BeregningAktivitetOverstyringDto> overstyrtAktivitet
                        = hentOverstyrtAktivitet(overstyringer, aktivitet);
                BeregningAktivitetDto kopiert = BeregningAktivitetDto
                        .kopier(aktivitet)
                        .medPeriode(getIntervall(aktivitet, overstyrtAktivitet))
                        .build();
                overstyrtBuilder.leggTilAktivitet(kopiert);
            });
            return Optional.of(overstyrtBuilder.build());
        }
        return Optional.empty();
    }

    /**
     * Her hentes overstyrt aktivitet fra 'overstyringsAktiviteter' hvis 'aktivitet' finnes i listen av overstyringer
     * i 'overstyringsAktiviteter' (hvis nøklene deres er like). Hvis denne finnes så skal det altså bety at aktiveten
     * har blitt overstyrt, og hvis ikke, så har ikke aktiviteten overstyrt.
     *
     * @param overstyringsAktiviteter
     * @param aktivitet
     * @return En 'BeregningAktivitetOverstyringDto' hvis 'BeregningAktivitetDto' er overstyrt.
     */
    private Optional<BeregningAktivitetOverstyringDto> hentOverstyrtAktivitet(
            final BeregningAktivitetOverstyringerDto overstyringsAktiviteter, final BeregningAktivitetDto aktivitet) {
        return overstyringsAktiviteter
                .getOverstyringer()
                .stream()
                .filter(overstyrtAktivitet -> overstyrtAktivitet.getNøkkel().equals(aktivitet.getNøkkel()))
                .findFirst();

    }

    /**
     * Henter periode fra overstyrt aktivitet hvis aktivitet er overstyrt. Hvis ikke så hentes periode fra aktivitet.
     * @param aktivitet
     * @param overstyring
     * @return opprinnelig eller overstyrt intervall
     */
    private Intervall getIntervall(BeregningAktivitetDto aktivitet, Optional<BeregningAktivitetOverstyringDto> overstyring) {
        if (overstyring.isPresent()) {
            return overstyring.get().getPeriode();
        }
        return aktivitet.getPeriode();
    }
}
