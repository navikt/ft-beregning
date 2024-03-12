package no.nav.folketrygdloven.kalkulus.kodeverk;

import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FASTSATT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.FORESLÅTT_DEL_2;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_VILKÅR;


public enum BeregningsgrunnlagPeriodeRegelType implements Kodeverdi, DatabaseKode {
    FORESLÅ(FORESLÅTT), // Foreslå beregningsgrunnlag
    FORESLÅ_2(FORESLÅTT_DEL_2), // Foreslå beregningsgrunnlag del 2
    VILKÅR_VURDERING(VURDERT_VILKÅR), // Vurder beregningsvilkår
    FORDEL(OPPDATERT_MED_REFUSJON_OG_GRADERING), // Fordel beregningsgrunnlag
    FASTSETT(FASTSATT), // Fastsett/fullføre beregningsgrunnlag
    FINN_GRENSEVERDI(FASTSATT), // Finne grenseverdi til kjøring av fastsett beregningsgrunnlag for SVP
    UDEFINERT(BeregningsgrunnlagTilstand.UDEFINERT),

    @Deprecated
    OPPDATER_GRUNNLAG_SVP(FASTSATT), // Oppdater grunnlag for SVP
    @Deprecated
    FASTSETT2(FASTSATT), // Fastsette/fullføre beregningsgrunnlag for andre gangs kjøring for SVP

    ;

    private final BeregningsgrunnlagTilstand lagretTilstand;

    BeregningsgrunnlagPeriodeRegelType(BeregningsgrunnlagTilstand lagretTilstand) {
        this.lagretTilstand = lagretTilstand;
    }

    @Override
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public BeregningsgrunnlagTilstand getLagretTilstand() {
        return lagretTilstand;
    }

}
