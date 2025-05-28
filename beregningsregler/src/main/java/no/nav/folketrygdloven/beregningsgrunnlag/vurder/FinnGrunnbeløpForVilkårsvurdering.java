package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;

public class FinnGrunnbeløpForVilkårsvurdering {

	private FinnGrunnbeløpForVilkårsvurdering() {
		// Skjuler default konstruktør
	}

	/**
     * Finner det riktige grunnbeløpet som skal brukes i vilkårsvurdering
     *
     * @param grunnlag Beregningsgrunnlagperiode
     * @return Grunnbeløp som skal brukes i vilkårsvurdering
     */
    public static BigDecimal finnGrunnbeløpForVilkårsvurdering(BeregningsgrunnlagPeriode grunnlag) {
        var erAktivtGrunnbeløpLavereEnnUregulert = grunnlag.getGrunnbeløp().compareTo(grunnlag.getUregulertGrunnbeløp()) < 0;
        if (erAktivtGrunnbeløpLavereEnnUregulert) {
            // Dette betyr at det aktive beregningsgrunnlaget har et skjæringstidspunkt som gir et nedsatt G-beløp
            return grunnlag.getGrunnbeløp();
        } else {
            // Det uregulerte grunnbeløpet er mindre enn eller lik det aktive. Her bruker vi det uregulerte
            // for å unngå endring i vilkårsvurdering ved G-regulering
            return grunnlag.getUregulertGrunnbeløp();
        }
    }

}
