package no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå;

import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalKjøreFortsettForeslå.ID)
public class SkalKjøreFortsettForeslå extends LeafSpecification<BeregningsgrunnlagPeriode> {

    /**
     * https://jira.adeo.no/browse/TFP-2806
     * Når beregningsgrunnlaget er fastsatt etter besteberegning skal det ikke avviksvurderes
     */
    static final String ID = "FP_BR 2.20";
    static final String BESKRIVELSE = "Er beregningsgrunnlaget besteberegnet?";

    public SkalKjøreFortsettForeslå() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
	    var harStatusSomMåBeregnesEtterForeslå = grunnlag.getBeregningsgrunnlag().getAktivitetStatuser().stream()
			    .map(AktivitetStatusMedHjemmel::getAktivitetStatus)
			    .anyMatch(AktivitetStatus::erSelvstendigNæringsdrivende);
	    return harStatusSomMåBeregnesEtterForeslå
            ? ja()
            : nei();
    }
}
