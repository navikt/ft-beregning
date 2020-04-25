package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn.BeregnOppjustertInntektFRISINN.ÅRET_2019;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.InntektPeriodeType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RuleDocumentation(BeregnGjennomsnittligPGIFRISINN.ID)
public class BeregnGjennomsnittligPGIFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.6";
    static final String BESKRIVELSE = "Beregn gjennomsnittlig PGI oppjustert til G";

    private static final String BIDRAG_TIL_BG = "bidragTilBG";

    public BeregnGjennomsnittligPGIFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BigDecimal bidragTilBGSum = BigDecimal.ZERO;
        Map<String, Object> resultater = new HashMap<>();

        BigDecimal oppgittInntekt = FinnRapportertÅrsinntektSN.finnRapportertÅrsinntekt(grunnlag);
        BigDecimal gSnittNytt = BigDecimal.valueOf(grunnlag.getBeregningsgrunnlag().snittverdiAvG(2019));
        bidragTilBGSum = bidragTilBGSum.add(finnSkalertBidragIAntallGSnitt(resultater, 2019, gSnittNytt, oppgittInntekt));

        int antallÅrSomBidrar = 1;
        for (int årSiden = 1; årSiden <= 2; årSiden++) {
            int årstall = bgps.getBeregningsperiode().getTom().getYear() - årSiden;
            BigDecimal gSnitt = BigDecimal.valueOf(grunnlag.getBeregningsgrunnlag().snittverdiAvG(årstall));
            BigDecimal pgiÅr = grunnlag.getInntektsgrunnlag().getÅrsinntektSigrun(årstall);
            if (pgiÅr.divide(gSnitt, 10, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(0.75)) > 0) {
                bidragTilBGSum = bidragTilBGSum.add(finnSkalertBidragIAntallGSnitt(resultater, årstall, gSnitt, pgiÅr));
                antallÅrSomBidrar++;
            }
        }

        BigDecimal gjennomsnittligPGI = bidragTilBGSum.compareTo(BigDecimal.ZERO) != 0 ? bidragTilBGSum.divide(BigDecimal.valueOf(antallÅrSomBidrar), 10, RoundingMode.HALF_EVEN).multiply(gSnittNytt)
            : BigDecimal.ZERO;
        resultater.put("GjennomsnittligPGI", gjennomsnittligPGI);
        BeregningsgrunnlagPrStatus.builder(bgps)
            .medGjennomsnittligPGI(gjennomsnittligPGI)
            .build();

        return beregnet(resultater);
    }

    private BigDecimal finnSkalertBidragIAntallGSnitt(Map<String, Object> resultater, int årstall, BigDecimal gSnitt, BigDecimal pgiÅr) {
        BigDecimal treGsnitt = gSnitt.multiply(BigDecimal.valueOf(3));
        BigDecimal seksGsnitt = gSnitt.multiply(BigDecimal.valueOf(6));
        BigDecimal tolvGsnitt = gSnitt.multiply(BigDecimal.valueOf(12));
        if (pgiÅr.compareTo(seksGsnitt) < 1) {
            BigDecimal bidragTilBG = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? pgiÅr.divide(gSnitt, 10, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
            resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
            return bidragTilBG;
        } else if (pgiÅr.compareTo(seksGsnitt) > 0 && pgiÅr.compareTo(tolvGsnitt) < 0) {
            BigDecimal bidragTilBG = pgiÅr.subtract(seksGsnitt).abs().divide(treGsnitt, 10, RoundingMode.HALF_EVEN).add(BigDecimal.valueOf(6));
            resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
            return bidragTilBG;
        } else {
            BigDecimal bidragTilBG = BigDecimal.valueOf(8);
            resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
            return bidragTilBG;
        }
    }

}
