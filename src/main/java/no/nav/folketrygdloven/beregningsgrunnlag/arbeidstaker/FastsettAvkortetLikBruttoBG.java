package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
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

        for (BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus : grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes()) {
            if (AktivitetStatus.erArbeidstaker(beregningsgrunnlagPrStatus.getAktivitetStatus())) {
                for (BeregningsgrunnlagPrArbeidsforhold af : beregningsgrunnlagPrStatus.getArbeidsforholdSomSkalBrukes()) {
                    BigDecimal bruttoInkludertNaturalytelsePrÅr = af.getGradertBruttoInkludertNaturalytelsePrÅr().orElse(null);
                    BeregningsgrunnlagPrArbeidsforhold.builder(af)
                        .medAvkortetPrÅr(bruttoInkludertNaturalytelsePrÅr)
                        .medAvkortetRefusjonPrÅr(af.getMaksimalRefusjonPrÅr())
                        .medAvkortetBrukersAndelPrÅr(bruttoInkludertNaturalytelsePrÅr.subtract(af.getMaksimalRefusjonPrÅr()))
                        .build();
                }
            } else {
                BigDecimal avkortetPrStatus = beregningsgrunnlagPrStatus.getBruttoPrÅr();
                BeregningsgrunnlagPrStatus.builder(beregningsgrunnlagPrStatus).medAvkortetPrÅr(avkortetPrStatus).build();
            }
        }
        //TODO(OMR-61): Regelsporing

        return ja();

    }

}
