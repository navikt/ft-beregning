package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN.ID)
class RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.9";
    static final String BESKRIVELSE = "Vurderer om beregnet inntekt skal overstyres med inntekt fra søknad";

    RegelVurderRegisterinntektMotOppgittInntektATFLFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        var oppgittArbeidsinntekt = finnOppgittArbeidsinntekt(grunnlag);
        var samletATInntekt = finnSamletATInntekt(bgps);

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
        var arbeidsforhold = andel.getArbeidsforholdIkkeFrilans();
        if (totalATInntekt.compareTo(BigDecimal.ZERO) <= 0) {
            fordelLiktMellomAlleArbfor(arbeidsforhold, oppgittArbeidsinntekt);
        } else {
            for (var arbfor : arbeidsforhold) {
                var andelAvTotalInntekt = finnAndelAvTotaltGrunnlag(arbfor, totalATInntekt);
                var nyArbeidsinntekt = andelAvTotalInntekt.multiply(oppgittArbeidsinntekt);
                endreEksisterendeAndel(arbfor, nyArbeidsinntekt);
            }
        }
    }

    private void fordelLiktMellomAlleArbfor(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold, BigDecimal oppgittArbeidsinntekt) {
        var antallArbfor = arbeidsforhold.size();
        arbeidsforhold.forEach(arbfor -> {
            var nyArbeidsinntekt = oppgittArbeidsinntekt.divide(BigDecimal.valueOf(antallArbfor), 10, RoundingMode.HALF_EVEN);
            endreEksisterendeAndel(arbfor, nyArbeidsinntekt);
        });
    }

    private void endreEksisterendeAndel(BeregningsgrunnlagPrArbeidsforhold arbfor, BigDecimal nyArbeidsinntekt) {
        BeregningsgrunnlagPrArbeidsforhold.builder(arbfor)
            .medOverstyrtPrÅr(nyArbeidsinntekt)
            .build();
    }

    private BigDecimal finnAndelAvTotaltGrunnlag(BeregningsgrunnlagPrArbeidsforhold arbfor, BigDecimal totalATInntekt) {
        return arbfor.getBruttoPrÅr().orElse(BigDecimal.ZERO).divide(totalATInntekt, 10, RoundingMode.HALF_EVEN);
    }

    private BigDecimal finnSamletATInntekt(BeregningsgrunnlagPrStatus atflAndel) {
        return atflAndel.getArbeidsforholdIkkeFrilans().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBruttoPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal finnOppgittArbeidsinntekt(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getInntektsgrunnlag().getOppgittInntektForStatusIPeriode(AktivitetStatus.AT, grunnlag.getBeregningsgrunnlagPeriode()).orElse(BigDecimal.ZERO);
    }

}
