package no.nav.folketrygdloven.beregningsgrunnlag.dok;

import no.finn.unleash.Unleash;
import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;

/**
 * Det mangler dokumentasjon
 */

@SuppressWarnings("unchecked")
@RuleDocumentation(value = RegelForeslåBeregningsgrunnlag.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class DokumentasjonRegelForeslåBeregningsgrunnlag extends RegelForeslåBeregningsgrunnlag implements BeregningsregelDokumentasjon {

    public DokumentasjonRegelForeslåBeregningsgrunnlag(Unleash unleash) {
        super(RegelmodellForDokumentasjon.regelmodellMedEttArbeidsforhold, unleash);
    }

}
