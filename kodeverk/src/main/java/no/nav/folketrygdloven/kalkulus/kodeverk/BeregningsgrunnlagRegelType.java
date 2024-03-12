package no.nav.folketrygdloven.kalkulus.kodeverk;

import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.BESTEBEREGNET;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING;
import static no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand.VURDERT_REFUSJON;


public enum BeregningsgrunnlagRegelType implements Kodeverdi, DatabaseKode {

    SKJÆRINGSTIDSPUNKT(OPPDATERT_MED_ANDELER), // Fastsette skjæringstidspunkt"
    BRUKERS_STATUS(OPPDATERT_MED_ANDELER), // Fastsette brukers status/aktivitetstatus"
    PERIODISERING_NATURALYTELSE(OPPDATERT_MED_ANDELER), // Periodiser beregningsgrunnlag pga naturalytelse
    PERIODISERING_REFUSJON(VURDERT_REFUSJON), // Periodiser beregningsgrunnlag pga refusjon
    // TODO: Vurder om perioder i databasen skal migreres over til PERIODISERING_UTBETALINGSGRAD før fp-sak flytter til kalkulus
    PERIODISERING_GRADERING(VURDERT_REFUSJON), // Periodiser beregningsgrunnlag pga gradering
    PERIODISERING_UTBETALINGSGRAD(VURDERT_REFUSJON), // Periodiser beregningsgrunnlag pga endring i utbetalingsgrad
    BESTEBEREGNING(BESTEBEREGNET), // Sammenligner beregning etter kap 8 med beregning ved besteberegning

    UDEFINERT(BeregningsgrunnlagTilstand.UDEFINERT),

    // Skal ikke lagres til men eksisterer fordi det finnes entries med denne i databasen (før ble det kun lagret 1 sporing for periodisering)
    @Deprecated
    PERIODISERING(OPPDATERT_MED_REFUSJON_OG_GRADERING), // Periodiser beregningsgrunnlag
    ;

    private final BeregningsgrunnlagTilstand lagretTilstand;

    BeregningsgrunnlagRegelType(BeregningsgrunnlagTilstand lagretTilstand) {
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
