package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.utenfordeling;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettAndelLikBruttoBGUtenFordeling.ID)
class FastsettAndelLikBruttoBGUtenFordeling extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 29.6.2_uten_fordeling";
    static final String BESKRIVELSE = "Fastsett andel lik brutto bg";

    public FastsettAndelLikBruttoBGUtenFordeling() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        for (var beregningsgrunnlagPrStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (AktivitetStatus.erArbeidstaker(beregningsgrunnlagPrStatus.getAktivitetStatus())) {
                for (var af : beregningsgrunnlagPrStatus.getArbeidsforhold()) {
                    var inntekt = af.getInntektsgrunnlagInkludertNaturalytelsePrÅr();
                    BeregningsgrunnlagPrArbeidsforhold.builder(af)
                        .medAndelsmessigFørGraderingPrAar(inntekt)
                        .build();
                }
            } else {
                var avkortetPrStatus = beregningsgrunnlagPrStatus.getInntektsgrunnlagPrÅr();
                BeregningsgrunnlagPrStatus.builder(beregningsgrunnlagPrStatus)
                    .medAndelsmessigFørGraderingPrAar(avkortetPrStatus)
                    .build();
            }
        }
        return ja();

    }

}
