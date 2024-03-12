package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBeregnetPrÅrFRISINN.ID)
class FastsettBeregnetPrÅrFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.5";
    static final String BESKRIVELSE = "Beregnet årsinntekt er sum av alle rapporterte inntekter";

    FastsettBeregnetPrÅrFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        BeregningsgrunnlagHjemmel hjemmel = settHjemmelForATFL(grunnlag.getBeregningsgrunnlag());

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("beregnetPrÅr", bgps.getBeregnetPrÅr());
        resultater.put("samletNaturalytelseBortfaltMinusTilkommetPrÅr", bgps.samletNaturalytelseBortfaltMinusTilkommetPrÅr());
        resultater.put("hjemmel", hjemmel);
        return beregnet(resultater);
    }

    private BeregningsgrunnlagHjemmel settHjemmelForATFL(Beregningsgrunnlag grunnlag) {
        AktivitetStatusMedHjemmel status = grunnlag.getAktivitetStatus(AktivitetStatus.ATFL);
        BeregningsgrunnlagHjemmel hjemmel = BeregningsgrunnlagHjemmel.KORONALOVEN_3;
        status.setHjemmel(hjemmel);
        return hjemmel;
    }
}
