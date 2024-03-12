package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


/**
 * TilfelleUtleder
 * Utleder et FaktaOmBeregningTilfelle for behandlingen.
 * Instanser av dette interfacet vil bli injeksert inn i FaktaOmBeregningTilfelleTjeneste (må ha @FagsakYtelseTypeRef).
 * Alle implementasjoner av denne klassen kan enten vere en defaultimplementasjon eller en implementasjon for en spesifikk ytelse.
 * Tilfeller som har spesifikke implementasjoner av klassen må også spesifisere @FaktaOmBeregningTilfelleRef, ellers blir både defaultimplementasjon
 * og spesifikk implementasjon evaluert.
 * Implementasjoner av denne klassen må derfor være injectable for å skulle kunne brukes i utledelsen av avklaringsbehov for
 * kontroller fakta om beregning.
 */
public interface TilfelleUtleder {
    Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                             BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag);
}
