package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalGjøreAvviksvurdering.ID)
class SkalGjøreAvviksvurdering extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 26.1";
    static final String BESKRIVELSE = "Skal det gjøres avviksvurdering etter § 8-30?";

    public SkalGjøreAvviksvurdering() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if(skalAlltidSetteAksjonspunktOmAvvik(grunnlag)){
            return ja();
        }
        OmsorgspengerGrunnlag ompGrunnlag = (OmsorgspengerGrunnlag) grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
        return ompGrunnlag.omfattesAvKap9Paragraf9() ? ja() : nei();
    }

    private boolean skalAlltidSetteAksjonspunktOmAvvik(BeregningsgrunnlagPeriode grunnlag){
	    YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag = grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
	    return !(ytelsesSpesifiktGrunnlag instanceof OmsorgspengerGrunnlag);
    }

}

