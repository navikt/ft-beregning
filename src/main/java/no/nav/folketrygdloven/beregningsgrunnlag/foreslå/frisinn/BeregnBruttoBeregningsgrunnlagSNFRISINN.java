package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        var rapportertÅrsinntekt = finnRapportertÅrsinntekt(grunnlag);
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

    private BigDecimal finnRapportertÅrsinntekt(BeregningsgrunnlagPeriode grunnlag) {
        Optional<BigDecimal> inntekt2019 = finnInntekter2019(grunnlag);
        return inntekt2019.orElseGet(() -> finnInntekter2020(grunnlag).orElse(BigDecimal.ZERO));
    }

    private Optional<BigDecimal> finnInntekter2019(BeregningsgrunnlagPeriode grunnlag) {
        List<Periodeinntekt> inntekter2019 = grunnlag.getInntektsgrunnlag().getPeriodeinntekterForSNFraSøknad(Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31)));
        if (inntekter2019.isEmpty()) {
            return Optional.empty();
        }
        LocalDate førsteDatoMedInntekt = inntekter2019.stream().map(Periodeinntekt::getFom).min(Comparator.naturalOrder()).orElse(LocalDate.of(2020, 1, 1));
        Optional<BigDecimal> sumIPeriode = inntekter2019.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add);
        if (førsteDatoMedInntekt.isAfter(LocalDate.of(2019,1, 1))) {
            int virkedager = Virkedager.beregnAntallVirkedager(førsteDatoMedInntekt, LocalDate.of(2020, 1, 1));
            if (virkedager > 0) {
                Optional<BigDecimal> dagsats = sumIPeriode.map(b -> b.divide(BigDecimal.valueOf(virkedager), 10, RoundingMode.HALF_EVEN));
                return dagsats.map(b -> b.multiply(BigDecimal.valueOf(260)));
            }
        }
        return sumIPeriode;
    }

    private Optional<BigDecimal> finnInntekter2020(BeregningsgrunnlagPeriode grunnlag) {
        LocalDate førsteDatoMedInntekt;
        List<Periodeinntekt> inntekter2020 = grunnlag.getInntektsgrunnlag().getPeriodeinntekterForSNFraSøknad(Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 3, 1)));
        førsteDatoMedInntekt = inntekter2020.stream().map(Periodeinntekt::getFom).min(Comparator.naturalOrder()).orElse(LocalDate.of(2020, 1, 1));
        Optional<BigDecimal> sumIPeriode = inntekter2020.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add);
        int virkedager = Virkedager.beregnAntallVirkedager(førsteDatoMedInntekt, LocalDate.of(2020, 3, 1));
        if (virkedager > 0) {
            Optional<BigDecimal> dagsats = sumIPeriode.map(b -> b.divide(BigDecimal.valueOf(virkedager), 10, RoundingMode.HALF_EVEN));
            return dagsats.map(b -> b.multiply(BigDecimal.valueOf(260)));
        }
        return Optional.empty();
    }

}
