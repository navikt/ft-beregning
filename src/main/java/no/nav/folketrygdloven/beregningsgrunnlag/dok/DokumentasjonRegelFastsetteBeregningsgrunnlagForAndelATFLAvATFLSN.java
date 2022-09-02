package no.nav.folketrygdloven.beregningsgrunnlag.dok;

import no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon.RegelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN;
import no.nav.fpsak.nare.doc.RuleDocumentation;

/**
 * Det mangler dokumentasjon
 */

@SuppressWarnings("unchecked")
@RuleDocumentation(value = RegelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=216009135")
public class DokumentasjonRegelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN
        extends RegelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN implements BeregningsregelDokumentasjon {

    public DokumentasjonRegelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN() {
        super(RegelmodellForDokumentasjon.regelmodellMedEttArbeidsforhold);
    }

}
