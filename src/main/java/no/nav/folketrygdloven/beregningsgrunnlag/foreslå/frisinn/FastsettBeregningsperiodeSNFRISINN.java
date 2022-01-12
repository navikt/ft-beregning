package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBeregningsperiodeSNFRISINN.ID)
public class FastsettBeregningsperiodeSNFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final BeregningsgrunnlagHjemmel HJEMMEL = BeregningsgrunnlagHjemmel.KORONALOVEN_3;
    static final String ID = "FRISINN 2.4";
    private static final String BESKRIVELSE = "Fastsett beregningsperiode";

    public FastsettBeregningsperiodeSNFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        Map<String, Object> resultater = new HashMap<>();
        if (grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL) == null) {
            grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.SN).setHjemmel(HJEMMEL);
            resultater.put("hjemmel", HJEMMEL);
        }
	    var skjæringstidspunkt = grunnlag.getBeregningsgrunnlag().getSkjæringstidspunkt();
	    BeregningsgrunnlagPrStatus.builder(bgps).medBeregningsperiode(Periode.of(skjæringstidspunkt.minusYears(3).withDayOfYear(1), skjæringstidspunkt.withDayOfYear(1).minusDays(1))).build();
        return beregnet(resultater);
    }
}
