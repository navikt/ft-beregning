package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RuleDocumentation(RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN.ID)
class RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.9";
    static final String BESKRIVELSE = "Vurderer om beregnet inntekt skal overstyres med inntekt fra søknad";

    RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        BigDecimal oppgittArbeidsinntekt = finnOppgittArbeidsinntekt(grunnlag);
        BigDecimal samletATInntekt = finnSamletATInntekt(bgps);

        if (samletATInntekt.compareTo(oppgittArbeidsinntekt) < 0) {
            fordelOppgittArbeidstakerInntekt(bgps, oppgittArbeidsinntekt, samletATInntekt);
        }

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("oppgittArbeidsinntekt", oppgittArbeidsinntekt);
        resultater.put("beregnetArbeidsinntekt", samletATInntekt);

        bgps.getArbeidsforholdIkkeFrilans().forEach(arbfor -> {
            resultater.put("arbeidsforhold", arbfor.getBeskrivelse());
            resultater.put("beregnet", arbfor.getBeregnetPrÅr());
            resultater.put("overstyrt", arbfor.getOverstyrtPrÅr());
        });

        return beregnet(resultater);
    }

    private void fordelOppgittArbeidstakerInntekt(BeregningsgrunnlagPrStatus andel, BigDecimal oppgittArbeidsinntekt, BigDecimal totalATInntekt) {
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = andel.getArbeidsforholdIkkeFrilans();
        if (totalATInntekt.compareTo(BigDecimal.ZERO) <= 0) {
            fordelLiktMellomAlleArbfor(arbeidsforhold, oppgittArbeidsinntekt);
        } else {
            for (BeregningsgrunnlagPrArbeidsforhold arbfor : arbeidsforhold) {
                BigDecimal andelAvTotalInntekt = finnAndelAvTotaltGrunnlag(arbfor, totalATInntekt);
                BigDecimal nyArbeidsinntekt = andelAvTotalInntekt.multiply(oppgittArbeidsinntekt);
                endreEksisterendeAndel(arbfor, nyArbeidsinntekt);
            }
        }
    }

    private void fordelLiktMellomAlleArbfor(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold, BigDecimal oppgittArbeidsinntekt) {
        int antallArbfor = arbeidsforhold.size();
        arbeidsforhold.forEach(arbfor -> {
            BigDecimal nyArbeidsinntekt = oppgittArbeidsinntekt.divide(BigDecimal.valueOf(antallArbfor), 10, RoundingMode.HALF_EVEN);
            endreEksisterendeAndel(arbfor, nyArbeidsinntekt);
        });
    }

    private void endreEksisterendeAndel(BeregningsgrunnlagPrArbeidsforhold arbfor, BigDecimal nyArbeidsinntekt) {
        BeregningsgrunnlagPrArbeidsforhold.builder(arbfor)
            .medOverstyrtPrÅr(nyArbeidsinntekt)
            .build();
    }

    private BigDecimal finnAndelAvTotaltGrunnlag(BeregningsgrunnlagPrArbeidsforhold arbfor, BigDecimal totalATInntekt) {
        return arbfor.getBruttoPrÅr().divide(totalATInntekt, 10, RoundingMode.HALF_EVEN);
    }

    private BigDecimal finnSamletATInntekt(BeregningsgrunnlagPrStatus atflAndel) {
        return atflAndel.getArbeidsforholdIkkeFrilans().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBruttoPrÅr)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal finnOppgittArbeidsinntekt(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getInntektsgrunnlag().getOppgittInntektForStatusIPeriode(AktivitetStatus.AT, grunnlag.getBeregningsgrunnlagPeriode()).orElse(BigDecimal.ZERO);
    }

}
