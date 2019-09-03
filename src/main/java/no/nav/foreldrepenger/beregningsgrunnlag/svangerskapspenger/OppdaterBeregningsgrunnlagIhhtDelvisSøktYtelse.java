package no.nav.foreldrepenger.beregningsgrunnlag.svangerskapspenger;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OppdaterBeregningsgrunnlagIhhtDelvisSøktYtelse.ID)
class OppdaterBeregningsgrunnlagIhhtDelvisSøktYtelse extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "SVP_BR_X";
    public static final String BESKRIVELSE = "Oppdater beregningsgrunnlag i henhold til delvis søkt ytelse.";

    OppdaterBeregningsgrunnlagIhhtDelvisSøktYtelse() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new LinkedHashMap<>();
        grunnlag.getBeregningsgrunnlagPrStatus().forEach(bgps -> {
            if (bgps.getAktivitetStatus().equals(AktivitetStatus.ATFL)) {
                bgps.getArbeidsforhold().forEach(arbeidsforhold -> {
                    if (arbeidsforhold.getUtbetalingsprosentSVP() != null) {
                        BigDecimal utbetalingsgrad = arbeidsforhold.getUtbetalingsprosentSVP().scaleByPowerOfTen(-2);
                        BigDecimal gammeltBeløp = arbeidsforhold.getBruttoPrÅr();
                        BigDecimal nyttBeløp = gammeltBeløp.multiply(utbetalingsgrad);
                        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
                            .medOverstyrtPrÅr(nyttBeløp)
                            .build();
                        resultater.put("utbetalingsgrad." + arbeidsforhold.getArbeidsgiverId(), utbetalingsgrad);
                        resultater.put("gammeltBeregningsgrunnlag." + arbeidsforhold.getArbeidsgiverId(), gammeltBeløp);
                        resultater.put("nyttBeregningsgrunnlag." + arbeidsforhold.getArbeidsgiverId(), nyttBeløp);
                    }
                });
            } else {
                if (bgps.getUtbetalingsprosentSVP() != null) {
                    BigDecimal utbetalingsgrad = bgps.getUtbetalingsprosentSVP().scaleByPowerOfTen(-2);
                    BigDecimal gammeltBeløp = bgps.getBruttoPrÅr();
                    BigDecimal nyttBeløp = gammeltBeløp.multiply(utbetalingsgrad);
                    BeregningsgrunnlagPrStatus.builder(bgps)
                        .medOverstyrtPrÅr(nyttBeløp)
                        .build();
                    resultater.put("utbetalingsgrad." + bgps.getAktivitetStatus().name(), utbetalingsgrad);
                    resultater.put("gammeltBeregningsgrunnlag." + bgps.getAktivitetStatus().name(), gammeltBeløp);
                    resultater.put("nyttBeregningsgrunnlag." + bgps.getAktivitetStatus().name(), nyttBeløp);
                }
            }
        });
        return beregnet(resultater);
    }
}
