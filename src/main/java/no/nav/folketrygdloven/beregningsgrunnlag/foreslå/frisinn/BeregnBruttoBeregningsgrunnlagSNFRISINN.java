package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        Optional<Periodeinntekt> sistePeriodeinntektMedTypeSøknad = grunnlag.getInntektsgrunnlag().getSistePeriodeinntektMedTypeSøknad();
        BigDecimal bruttoSN = sistePeriodeinntektMedTypeSøknad.map(Periodeinntekt::getInntekt)
            .orElseThrow(() -> new IllegalStateException("Finner ikke oppgitt inntekt for søker av FRISINN ytelse som er selvstendig næringsdrivende"));
        BeregningsgrunnlagPrStatus.builder(bgps).medBeregnetPrÅr(bruttoSN).build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("oppgittInntekt", bruttoSN);

        return beregnet(resultater);
    }

}
