package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import static no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FinnGjennomsnittligPGI.finnGjennomsnittligPGI;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnGjennomsnittligPGIForAktivitetstatus.ID)
public class BeregnGjennomsnittligPGIForAktivitetstatus extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.2";
    static final String BESKRIVELSE = "Beregn gjennomsnittlig PGI oppjustert til G";
	private final AktivitetStatus aktivitetStatus;

	public BeregnGjennomsnittligPGIForAktivitetstatus(AktivitetStatus aktivitetStatus) {
        super(ID, BESKRIVELSE);
		if (!AktivitetStatus.SN.equals(aktivitetStatus) && !AktivitetStatus.BA.equals(aktivitetStatus)) {
			throw new IllegalArgumentException("Kan ikke beregning gjennomsnittlig PGI for aktivitetstatus " + aktivitetStatus);
		}
		this.aktivitetStatus = aktivitetStatus;

	}

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
	    var bgps = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
	    Map<String, Object> resultater = new HashMap<>();
	    var gjennomsnittligPGI = finnGjennomsnittligPGI(bgps.getBeregningsperiode().getTom(), grunnlag.getBeregningsgrunnlag().getGrunnbeløpsatser(), grunnlag.getInntektsgrunnlag(), grunnlag.getGrunnbeløp(), resultater);
	    resultater.put("GjennomsnittligPGI", gjennomsnittligPGI);
        BeregningsgrunnlagPrStatus.builder(bgps)
            .medGjennomsnittligPGI(gjennomsnittligPGI)
            .build();

        return beregnet(resultater);
    }


}
