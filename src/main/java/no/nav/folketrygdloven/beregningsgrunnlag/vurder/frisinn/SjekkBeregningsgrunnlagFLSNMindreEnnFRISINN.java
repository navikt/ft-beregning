package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.util.Optional;

@RuleDocumentation(SjekkBeregningsgrunnlagFLSNMindreEnnFRISINN.ID)
class SjekkBeregningsgrunnlagFLSNMindreEnnFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 3.2";
    static final String BESKRIVELSE = "Er beregningsgrunnlag fra SN/FL mindre enn en 0,75G?";

    SjekkBeregningsgrunnlagFLSNMindreEnnFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal minstekrav = grunnlag.getGrunnbeløpForVilkårsvurdering().multiply(grunnlag.getAntallGMinstekravVilkår());
        BigDecimal bruttoForSøkteAndeler = BigDecimal.ZERO;

        var frilansandel = finnFrilansAndel(grunnlag);
        BeregningsgrunnlagPeriode førstePeriode = finnFørstePeriode(grunnlag);
        if (frilansandel.isPresent() && frilansandel.get().getErSøktYtelseFor()) {
            var frilansAndelFørstePeriode = finnFrilansAndel(førstePeriode);
            bruttoForSøkteAndeler = bruttoForSøkteAndeler.add(frilansAndelFørstePeriode.flatMap(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr).orElse(BigDecimal.ZERO));
        }
        var snStatus = finnSNStatus(grunnlag);
        if (snStatus.isPresent() && snStatus.get().erSøktYtelseFor()) {
            var snFørstePeriode = finnSNStatus(førstePeriode);
            bruttoForSøkteAndeler = bruttoForSøkteAndeler.add(snFørstePeriode.map(BeregningsgrunnlagPrStatus::getBruttoInkludertNaturalytelsePrÅr).orElse(BigDecimal.ZERO));
        }

        boolean erSøktIPeriode = (snStatus.isPresent() && snStatus.get().erSøktYtelseFor()) || (frilansandel.isPresent() && frilansandel.get().getErSøktYtelseFor());
        SingleEvaluation resultat = erSøktIPeriode && bruttoForSøkteAndeler.compareTo(minstekrav) < 0 ? ja() : nei();
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløpForVilkårsvurdering());
        resultat.setEvaluationProperty("treKvartGrunnbeløp", minstekrav);
        resultat.setEvaluationProperty("faktiskGrunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("bruttoPrÅrSNFL", bruttoForSøkteAndeler);
        return resultat;
    }

    private Optional<BeregningsgrunnlagPrStatus> finnSNStatus(BeregningsgrunnlagPeriode grunnlag) {
        return Optional.ofNullable(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN));
    }

    private Optional<BeregningsgrunnlagPrArbeidsforhold> finnFrilansAndel(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus atflStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        return atflStatus == null ? Optional.empty() : atflStatus.getFrilansArbeidsforhold();
    }

    private BeregningsgrunnlagPeriode finnFørstePeriode(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
            .filter(p -> p.getSkjæringstidspunkt().equals(p.getPeriodeFom())).findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke første periode"));
    }
}
