package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettAndelLikBruttoBG.ID)
public class FastsettAndelLikBruttoBG extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 29.6.2";
    static final String BESKRIVELSE = "Fastsett andel lik brutto bg";

    public FastsettAndelLikBruttoBG() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        for (BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (AktivitetStatus.erArbeidstaker(beregningsgrunnlagPrStatus.getAktivitetStatus())) {
                for (BeregningsgrunnlagPrArbeidsforhold af : beregningsgrunnlagPrStatus.getArbeidsforhold()) {
                    BigDecimal bruttoInkludertNaturalytelsePrÅr = af.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO);
                    BeregningsgrunnlagPrArbeidsforhold.builder(af)
                        .medAndelsmessigFørGraderingPrAar(bruttoInkludertNaturalytelsePrÅr)
                        .build();
                }
            } else {
                BigDecimal avkortetPrStatus = beregningsgrunnlagPrStatus.getBruttoPrÅr();
                BeregningsgrunnlagPrStatus.builder(beregningsgrunnlagPrStatus)
                    .medAndelsmessigFørGraderingPrAar(avkortetPrStatus)
                    .build();
            }
        }
        return ja();

    }

}
