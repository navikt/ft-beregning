package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

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
        BigDecimal årsInntekt2019 = finnÅrsinntekt2019(grunnlag);
        BigDecimal årsinntektPeriode = finnÅrsinntektPeriode(grunnlag);
        BigDecimal bruttoSN = årsInntekt2019.max(årsinntektPeriode);
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
        return grunnlag.getInntektsgrunnlag().getPeriodeinntekter()
            .stream()
            .filter(i -> i.getInntektskilde().equals(Inntektskilde.SØKNAD)
                && !i.erFrilans()
                && grunnlag.getBeregningsgrunnlagPeriode().overlapper(Periode.of(i.getFom(), i.getTom())))
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

    private BigDecimal finnÅrsinntekt2019(BeregningsgrunnlagPeriode grunnlag) {
        List<Periodeinntekt> inntekter2019 = grunnlag.getInntektsgrunnlag().getPeriodeinntekter()
            .stream()
            .filter(i -> i.getInntektskilde().equals(Inntektskilde.SØKNAD)
                && !i.erFrilans()
                && !i.getFom().isBefore(LocalDate.of(2019, 1, 1))
                && i.getTom().isBefore(LocalDate.of(2020, 1, 1)))
            .collect(Collectors.toList());
        return inntekter2019.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

}
