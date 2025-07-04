package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettAvkortetLikBruttoBG.ID)
class FastsettAvkortetLikBruttoBG extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 29.6.2";
    static final String BESKRIVELSE = "Fastsett Avkortet brukers andel og ev avkortet arbeidsgivers andel";

    FastsettAvkortetLikBruttoBG() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        for (var beregningsgrunnlagPrStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (AktivitetStatus.erArbeidstaker(beregningsgrunnlagPrStatus.getAktivitetStatus())) {
                for (var af : beregningsgrunnlagPrStatus.getArbeidsforhold()) {
                    var bruttoInkludertNaturalytelsePrÅr = af.getAktivitetsgradertBruttoInkludertNaturalytelsePrÅr()
                        .orElseThrow(() -> new IllegalStateException("Brutto er ikke satt for arbeidsforhold " + af.toString()));
                    BeregningsgrunnlagPrArbeidsforhold.builder(af)
                        .medAvkortetPrÅr(bruttoInkludertNaturalytelsePrÅr)
                        .medAvkortetRefusjonPrÅr(af.getMaksimalRefusjonPrÅr())
                        .medAvkortetBrukersAndelPrÅr(bruttoInkludertNaturalytelsePrÅr.subtract(af.getMaksimalRefusjonPrÅr()))
                        .build();
                }
            } else {
                var avkortetPrStatus = beregningsgrunnlagPrStatus.getAktivitetsgradertBruttoPrÅr();
                BeregningsgrunnlagPrStatus.builder(beregningsgrunnlagPrStatus).medAvkortetPrÅr(avkortetPrStatus).build();
            }
        }
        //TODO(OMR-61): Regelsporing

        return ja();

    }

}
