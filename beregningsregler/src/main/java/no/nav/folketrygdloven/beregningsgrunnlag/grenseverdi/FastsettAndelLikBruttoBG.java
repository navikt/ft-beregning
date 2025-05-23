package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettAndelLikBruttoBG.ID)
public class FastsettAndelLikBruttoBG extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 29.6.2_med_fordeling";
    static final String BESKRIVELSE = "Fastsett andel lik brutto bg";

    public FastsettAndelLikBruttoBG() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        for (var beregningsgrunnlagPrStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (AktivitetStatus.erArbeidstaker(beregningsgrunnlagPrStatus.getAktivitetStatus())) {
                for (var af : beregningsgrunnlagPrStatus.getArbeidsforhold()) {
                    var bruttoInkludertNaturalytelsePrÅr = af.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO);
                    BeregningsgrunnlagPrArbeidsforhold.builder(af)
                        .medAndelsmessigFørGraderingPrAar(bruttoInkludertNaturalytelsePrÅr)
                        .build();
                }
            } else {
                var avkortetPrStatus = beregningsgrunnlagPrStatus.getBruttoPrÅr();
                BeregningsgrunnlagPrStatus.builder(beregningsgrunnlagPrStatus)
                    .medAndelsmessigFørGraderingPrAar(avkortetPrStatus)
                    .build();
            }
        }
        return ja();

    }

}
