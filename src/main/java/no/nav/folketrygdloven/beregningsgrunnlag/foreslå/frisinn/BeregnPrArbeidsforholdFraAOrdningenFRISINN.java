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

        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        BigDecimal årsinntekt;
        if (arbeidsforhold.erFrilanser()) {
            årsinntekt = beregnÅrsinntektFrilans(perioderSomSkalBrukesForInntekter, inntektsgrunnlag, resultater);
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

    private BigDecimal beregnÅrsinntektFrilans(List<Periode> inntektsperioder, Inntektsgrunnlag inntektsgrunnlag, Map<String, Object> resultater) {
        BigDecimal samletInntekt = BigDecimal.ZERO;
        for (Periode periode : inntektsperioder) {
            samletInntekt = samletInntekt.add(finnInntektForPeriode(periode, inntektsgrunnlag, resultater));
        }
        BigDecimal antallPerioder = BigDecimal.valueOf(inntektsperioder.size());
        BigDecimal snittMånedslønnFraRegister = samletInntekt.divide(antallPerioder, 10, RoundingMode.HALF_EVEN);
        BigDecimal årslønnFraRegister = snittMånedslønnFraRegister.multiply(ANTALL_MÅNEDER_I_ÅR);
        BigDecimal årsinntektFraSøknad = finnOppgittÅrsinntektFL(inntektsgrunnlag);
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
