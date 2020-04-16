package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FinnGrenseverdiForTotalOver6G.ID)
public class FinnGrenseverdiForTotalOver6G extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 6.2";
    public static final String BESKRIVELSE = "Finn grenseverdi for total bg over 6G";

    public FinnGrenseverdiForTotalOver6G() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();

        BigDecimal totalATGrunnlag = finnTotalGrunnlagAT(grunnlag);
        BigDecimal totalDPGrunnlag = finnInntektForStatus(grunnlag, AktivitetStatus.DP);
        BigDecimal totalAAPGrunnlag = finnInntektForStatus(grunnlag, AktivitetStatus.AAP);
        BigDecimal løpendeFL = finnLøpendeBgFL(grunnlag);
        BigDecimal løpendeSN = finnLøpendeBgSN(grunnlag);

        BigDecimal grenseverdi = grunnlag.getGrenseverdi()
            .subtract(totalAAPGrunnlag)
            .subtract(totalATGrunnlag)
            .subtract(totalDPGrunnlag)
            .subtract(løpendeFL)
            .subtract(løpendeSN)
            .max(BigDecimal.ZERO);

        resultater.put("grenseverdi", grenseverdi);
        grunnlag.setGrenseverdi(grenseverdi);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }

    private BigDecimal finnInntektForStatus(BeregningsgrunnlagPeriode grunnlag, AktivitetStatus status) {
        BeregningsgrunnlagPrStatus andel = grunnlag.getBeregningsgrunnlagPrStatus(status);
        if (andel == null) {
            return BigDecimal.ZERO;
        } return andel.getBruttoInkludertNaturalytelsePrÅr();
    }

    private BigDecimal finnLøpendeBgSN(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal bruttoSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN)
            .getBruttoInkludertNaturalytelsePrÅr();
        BigDecimal bortfaltSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN)
            .getGradertBruttoInkludertNaturalytelsePrÅr();
        return bruttoSN.subtract(bortfaltSN).max(BigDecimal.ZERO);
    }

    private BigDecimal finnTotalGrunnlagAT(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atflAndel == null) {
            return BigDecimal.ZERO;
        }
        return atflAndel
                .getArbeidsforholdIkkeFrilans()
                .stream()
                .flatMap(arbeid -> arbeid.getBruttoInkludertNaturalytelsePrÅr().stream())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal finnLøpendeBgFL(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atflAndel == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalBgFL = atflAndel
            .getFrilansArbeidsforhold()
            .flatMap(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr)
            .orElse(BigDecimal.ZERO);
        BigDecimal bortfaltBgFL = atflAndel
            .getFrilansArbeidsforhold()
            .flatMap(BeregningsgrunnlagPrArbeidsforhold::getGradertBruttoInkludertNaturalytelsePrÅr)
            .orElse(BigDecimal.ZERO);
        return totalBgFL.subtract(bortfaltBgFL).max(BigDecimal.ZERO);
    }

}
