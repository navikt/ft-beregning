package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.inaktiv.RegelBeregningsgrunnlagInaktiv;
import no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon.RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy;
import no.nav.folketrygdloven.beregningsgrunnlag.militær.RegelForeslåBeregningsgrunnlagMilitær;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.RegelForeslåBeregningsgrunnlagTY;
import no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap.RegelFastsettBeregningsgrunnlagDPellerAAP;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelForeslåBeregningsgrunnlagPrStatusNy.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class RegelForeslåBeregningsgrunnlagPrStatusNy extends DynamicRuleService<BeregningsgrunnlagPeriode> {

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

		// Disse beregnes i fortsettForeslå steget
        if (aktivitetStatus.equals(AktivitetStatus.MS) || aktivitetStatus.equals(AktivitetStatus.SN)) {
			return new Beregnet();
        }
		if (AktivitetStatus.ATFL.equals(aktivitetStatus)) {
            return new RegelBeregningsgrunnlagATFL(regelmodell).getSpecification().medScope(arg);
        } else if (AktivitetStatus.ATFL_SN.equals(aktivitetStatus)) {
            RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy regelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy(regelmodell);
            return regelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy.getSpecification().medScope(arg);
        } else if (aktivitetStatus.erAAPellerDP()) {
            RegelFastsettBeregningsgrunnlagDPellerAAP regelFastsettBeregningsgrunnlagDPellerAAP = new RegelFastsettBeregningsgrunnlagDPellerAAP();
            return regelFastsettBeregningsgrunnlagDPellerAAP.getSpecification().medScope(arg);
        } else if (AktivitetStatus.KUN_YTELSE.equals(aktivitetStatus)) {
            RegelForeslåBeregningsgrunnlagTY regelForeslåBeregningsgrunnlagTY = new RegelForeslåBeregningsgrunnlagTY(regelmodell);
            return regelForeslåBeregningsgrunnlagTY.getSpecification().medScope(arg);
        } else if (AktivitetStatus.MIDL_INAKTIV.equals(aktivitetStatus)) {
	        return new RegelBeregningsgrunnlagInaktiv(regelmodell).getSpecification().medScope(arg);
        }

        return new RegelForeslåBeregningsgrunnlagTilNull().medServiceArgument(arg).getSpecification();
    }
}
