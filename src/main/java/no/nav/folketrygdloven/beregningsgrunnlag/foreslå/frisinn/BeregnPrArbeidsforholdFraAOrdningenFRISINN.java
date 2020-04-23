package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.FinnPerioderUtenYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

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

        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        BigDecimal årsinntekt;
        if (arbeidsforhold.erFrilanser()) {
            YtelsesSpesifiktGrunnlag ytelsesSpesifiktGrunnlag = grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
            if (ytelsesSpesifiktGrunnlag instanceof FrisinnGrunnlag && ((FrisinnGrunnlag) ytelsesSpesifiktGrunnlag).isErNyoppstartetFrilans()) {
                perioderSomSkalBrukesForInntekter = lag6MånederFørStpOpptjening();
            }
            årsinntekt = beregnÅrsinntektFrilans(perioderSomSkalBrukesForInntekter, inntektsgrunnlag, grunnlag, resultater);
        } else {
            årsinntekt = beregnÅrsinntektArbeidstaker(perioderSomSkalBrukesForInntekter, inntektsgrunnlag, resultater);
        }

        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medBeregnetPrÅr(årsinntekt)
            .build();
        resultater.put("antallPerioder", perioderSomSkalBrukesForInntekter.size());
        resultater.put("beregnetPrÅr", årsinntekt);
        return beregnet(resultater);
    }

    private List<Periode> lag6MånederFørStpOpptjening() {
        LocalDate stpOpptjening = LocalDate.of(2020, 3, 1);
        List<Periode> perioder = new ArrayList<>();
        for (int i = 6; i >= 1; i--) {
            perioder.add(Periode.of(stpOpptjening.minusMonths(i).withDayOfMonth(1), stpOpptjening.minusMonths(i).with(TemporalAdjusters.lastDayOfMonth())));
        }
        return perioder;
    }

    private BigDecimal beregnÅrsinntektFrilans(List<Periode> inntektsperioder, Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagPeriode grunnlag, Map<String, Object> resultater) {
        BigDecimal samletInntekt = BigDecimal.ZERO;
        for (Periode periode : inntektsperioder) {
            samletInntekt = samletInntekt.add(finnInntektForPeriode(periode, inntektsgrunnlag, resultater));
        }
        BigDecimal antallPerioder = BigDecimal.valueOf(inntektsperioder.size());
        BigDecimal snittMånedslønnFraRegister = samletInntekt.divide(antallPerioder, 10, RoundingMode.HALF_EVEN);
        BigDecimal årslønnFraRegister = snittMånedslønnFraRegister.multiply(ANTALL_MÅNEDER_I_ÅR);
        BigDecimal årsinntektFraSøknad = finnOppgittÅrsinntektFL(inntektsgrunnlag, grunnlag);
        resultater.put("årsinntektFraRegister", snittMånedslønnFraRegister);
        resultater.put("årsinntektFraSøknad", årsinntektFraSøknad);
        return årslønnFraRegister.max(årsinntektFraSøknad);
    }

    private BigDecimal beregnÅrsinntektArbeidstaker(List<Periode> inntektsperioder, Inntektsgrunnlag inntektsgrunnlag, Map<String, Object> resultater) {
        BigDecimal samletInntekt = BigDecimal.ZERO;
        int antallPerioderMedInntekt = 0;
        for (Periode periode : inntektsperioder) {
            BigDecimal inntektForPeriode = finnInntektForPeriode(periode, inntektsgrunnlag, resultater);
            if (inntektForPeriode.compareTo(BigDecimal.ZERO) > 0) {
                antallPerioderMedInntekt++;
                samletInntekt = samletInntekt.add(inntektForPeriode);
            }
        }
        resultater.put("perioderMedInntekter ", antallPerioderMedInntekt);
        if (antallPerioderMedInntekt == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal antallPerioder = BigDecimal.valueOf(antallPerioderMedInntekt);
        BigDecimal snittMånedslønn =  samletInntekt.divide(antallPerioder, 10, RoundingMode.HALF_EVEN);
        return snittMånedslønn.multiply(ANTALL_MÅNEDER_I_ÅR);
    }

    private BigDecimal finnInntektForPeriode(Periode periode, Inntektsgrunnlag inntektsgrunnlag, Map<String, Object> resultater) {
        List<Periodeinntekt> inntekterHosAgForPeriode = inntektsgrunnlag.getInntektForArbeidsforholdIPeriode(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, periode);
        BigDecimal sumForPeriode = inntekterHosAgForPeriode.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        resultater.put("sumForPeriode" + periode.toString(), sumForPeriode);
        return sumForPeriode;

    }

    private BigDecimal finnOppgittÅrsinntektFL(Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagPeriode grunnlag) {
        List<Periodeinntekt> oppgittInntektFL = inntektsgrunnlag.getOppgittInntektFLIPeriode(grunnlag.getBeregningsgrunnlagPeriode());
        if (oppgittInntektFL.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return oppgittInntektFL.stream()
            .map(BeregnPrArbeidsforholdFraAOrdningenFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
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
