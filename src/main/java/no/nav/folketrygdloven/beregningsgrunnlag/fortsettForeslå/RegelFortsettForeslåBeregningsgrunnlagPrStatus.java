package no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlagTilNull;
import no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon.RegelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.RegelBeregningsgrunnlagSN;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFortsettForeslåBeregningsgrunnlagPrStatus.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class RegelFortsettForeslåBeregningsgrunnlagPrStatus extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "BG-PR-STATUS";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
	    ServiceArgument arg = getServiceArgument();
	    if (arg == null || !(arg.getVerdi() instanceof AktivitetStatusMedHjemmel)) {
		    throw new IllegalStateException("Utviklerfeil: AktivitetStatus må angis som parameter");
	    }
	    if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
		    return new IkkeBeregnet(new BeregningUtfallMerknad(BeregningUtfallÅrsak.UDEFINERT));
	    }
	    AktivitetStatus aktivitetStatus = ((AktivitetStatusMedHjemmel) arg.getVerdi()).getAktivitetStatus();
	    if (AktivitetStatus.SN.equals(aktivitetStatus)) {
		    return new RegelBeregningsgrunnlagSN().getSpecification().medScope(arg);
	    } else if (AktivitetStatus.ATFL_SN.equals(aktivitetStatus)) {
		    RegelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN regelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN = new RegelFastsetteBeregningsgrunnlagForAndelATFLAvATFLSN(regelmodell);
		    return new RegelBeregningsgrunnlagSN().getSpecification().medScope(arg);
	    }
		return new RegelForeslåBeregningsgrunnlagTilNull().medServiceArgument(arg).getSpecification();
    }
}
