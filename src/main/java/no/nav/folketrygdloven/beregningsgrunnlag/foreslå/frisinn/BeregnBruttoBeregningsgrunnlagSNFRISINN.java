package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@RuleDocumentation(BeregnBruttoBeregningsgrunnlagSNFRISINN.ID)
public class BeregnBruttoBeregningsgrunnlagSNFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.8";
    static final String BESKRIVELSE = "Beregn brutto beregningsgrunnlag selvstendig næringsdrivende";

    public BeregnBruttoBeregningsgrunnlagSNFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        var rapportertÅrsinntekt = FinnRapportertÅrsinntektSN.finnRapportertÅrsinntekt(grunnlag);
        BigDecimal årsinntektPeriode = finnÅrsinntektPeriode(grunnlag);
        BigDecimal bruttoSN = rapportertÅrsinntekt.max(årsinntektPeriode);
        BeregningsgrunnlagPrStatus.builder(bgps).medBeregnetPrÅr(bruttoSN).build();
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("oppgittInntekt", bruttoSN);
        return beregnet(resultater);
    }

    private BigDecimal finnÅrsinntektPeriode(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal effektivDagsatsIPeriode = finnEffektivDagsatsIPeriode(grunnlag);
        return effektivDagsatsIPeriode.multiply(BigDecimal.valueOf(260));
    }

    private BigDecimal finnEffektivDagsatsIPeriode(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getInntektsgrunnlag().getPeriodeinntekterForSNFraSøknad(grunnlag.getBeregningsgrunnlagPeriode())
            .stream()
            .map(BeregnBruttoBeregningsgrunnlagSNFRISINN::mapTilEffektivDagsatsIPeriode)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal mapTilEffektivDagsatsIPeriode(Periodeinntekt i) {
        int virkedagerIOverlappendePeriode = Virkedager.beregnAntallVirkedager(Periode.of(i.getFom(), i.getTom()));
        if (virkedagerIOverlappendePeriode == 0) {
            return BigDecimal.ZERO;
        }
        return i.getInntekt().divide(BigDecimal.valueOf(virkedagerIOverlappendePeriode), RoundingMode.HALF_EVEN);
    }

}
