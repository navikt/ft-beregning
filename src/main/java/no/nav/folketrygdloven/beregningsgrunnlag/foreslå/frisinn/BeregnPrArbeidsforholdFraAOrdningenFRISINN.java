package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.FinnPerioderUtenYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RuleDocumentation(BeregnPrArbeidsforholdFraAOrdningenFRISINN.ID)
class BeregnPrArbeidsforholdFraAOrdningenFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {
    public static final int VIRKEDAGER_I_ET_ÅR = 260;
    private static final BigDecimal ANTALL_MÅNEDER_I_ÅR = BigDecimal.valueOf(12);
    static final String ID = "FRISINN 2.3";
    static final String BESKRIVELSE = "Rapportert inntekt = snitt av mnd-inntekter i beregningsperioden * 12";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    BeregnPrArbeidsforholdFraAOrdningenFRISINN(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        Objects.requireNonNull(arbeidsforhold, "arbeidsforhold");
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        Inntektsgrunnlag inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        LocalDate skjæringstidspunkt = grunnlag.getSkjæringstidspunkt();
        List<Periode> perioderSomSkalBrukesForInntekter = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, skjæringstidspunkt, resultater);

        BigDecimal totalSumForArbeidsforhold = BigDecimal.ZERO;
        for (Periode periode : perioderSomSkalBrukesForInntekter) {
            List<Periodeinntekt> inntekterHosAgForPeriode = inntektsgrunnlag.getInntektForArbeidsforholdIPeriode(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, periode);
            BigDecimal sumForPeriode = inntekterHosAgForPeriode.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            totalSumForArbeidsforhold = totalSumForArbeidsforhold.add(sumForPeriode);
            resultater.put("sumForPeriode" + periode.toString(), sumForPeriode);
        }
        BigDecimal antallPerioder = BigDecimal.valueOf(perioderSomSkalBrukesForInntekter.size());
        BigDecimal snittlønnPrMnd = totalSumForArbeidsforhold.divide(antallPerioder, 10, RoundingMode.HALF_EVEN);
        BigDecimal årslønn = snittlønnPrMnd.multiply(ANTALL_MÅNEDER_I_ÅR);

        if (arbeidsforhold.erFrilanser()) {
            BigDecimal oppgittÅrslønn = finnOppgittÅrsinntektFL(inntektsgrunnlag);
            årslønn = årslønn.max(oppgittÅrslønn); // Hvis det søker har oppgitt gir høyere årslønn enn vi har kommet frem til, er det denne si skal legge til grunn.
        }

        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medBeregnetPrÅr(årslønn)
            .build();

        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        resultater.put("sumForArbeidsforhold", totalSumForArbeidsforhold);
        resultater.put("antallPerioder", antallPerioder);
        resultater.put("beregnetPrÅr", årslønn);
        return beregnet(resultater);
    }

    private BigDecimal finnOppgittÅrsinntektFL(Inntektsgrunnlag inntektsgrunnlag) {
        Optional<Periodeinntekt> oppgittInntektFL = inntektsgrunnlag.getOppgittInntektFL();
        if (oppgittInntektFL.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Periodeinntekt periodeinntekt = oppgittInntektFL.get();
        return finnEffektivÅrsinntektForLøpenedeInntekt(periodeinntekt);
    }

    public static BigDecimal finnEffektivÅrsinntektForLøpenedeInntekt(Periodeinntekt oppgittInntekt) {
        BigDecimal dagsats = finnEffektivDagsatsIPeriode(oppgittInntekt);
        return dagsats.multiply(BigDecimal.valueOf(VIRKEDAGER_I_ET_ÅR));
    }

    /**
     * Finner opptjent inntekt pr dag i periode
     *
     * @param oppgittInntekt Informasjon om oppgitt inntekt
     * @return dagsats i periode
     */
    private static BigDecimal finnEffektivDagsatsIPeriode(Periodeinntekt oppgittInntekt) {
        long dagerIRapportertPeriode = Virkedager.beregnAntallVirkedager(oppgittInntekt.getFom(), oppgittInntekt.getTom());
        if (dagerIRapportertPeriode == 0) {
            return BigDecimal.ZERO;
        }
        return oppgittInntekt.getInntekt().divide(BigDecimal.valueOf(dagerIRapportertPeriode), RoundingMode.HALF_UP);
    }
}
