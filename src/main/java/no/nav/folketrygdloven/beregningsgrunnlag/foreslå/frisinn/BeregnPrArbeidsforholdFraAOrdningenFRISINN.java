package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
<<<<<<< Updated upstream
import java.util.Optional;
=======
import java.util.stream.Collectors;
>>>>>>> Stashed changes

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
                perioderSomSkalBrukesForInntekter = lagMånederUtenYtelseEtterFørsteInntektsdag(grunnlag, perioderSomSkalBrukesForInntekter);
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

    private List<Periode> lagMånederUtenYtelseEtterFørsteInntektsdag(BeregningsgrunnlagPeriode grunnlag, List<Periode> perioderUtenYtelse) {
        LocalDate nyoppstartetGrense = LocalDate.of(2019, 3, 1);
        List<Periode> perioderEtterGrenseUtenYtelse = perioderUtenYtelse.stream()
            .filter(p -> !p.getFom().isBefore(nyoppstartetGrense))
            .collect(Collectors.toList());
        if (perioderEtterGrenseUtenYtelse.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate førsteDatoMedInntekt = finnFørsteDatoMedFrilansInntektEtterDato(grunnlag, nyoppstartetGrense);
        return perioderEtterGrenseUtenYtelse.stream()
            .filter(p -> !p.getFom().isBefore(førsteDatoMedInntekt))
            .collect(Collectors.toList());
    }

    private LocalDate finnFørsteDatoMedFrilansInntektEtterDato(BeregningsgrunnlagPeriode grunnlag, LocalDate nyoppstartetGrense) {
        Periode periode = Periode.of(nyoppstartetGrense, grunnlag.getSkjæringstidspunkt());
        List<Periodeinntekt> frilansInntekter = grunnlag.getBeregningsgrunnlag().getInntektsgrunnlag()
            .finnAlleFrilansInntektPerioder(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, periode);
        return frilansInntekter.stream()
            .map(Periodeinntekt::getFom)
            .min(Comparator.naturalOrder())
            .orElse(grunnlag.getSkjæringstidspunkt());
    }

    private BigDecimal beregnÅrsinntektFrilans(List<Periode> inntektsperioder, Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagPeriode grunnlag, Map<String, Object> resultater) {
        BigDecimal samletInntekt = BigDecimal.ZERO;
        for (Periode periode : inntektsperioder) {
            samletInntekt = samletInntekt.add(finnInntektForPeriode(periode, inntektsgrunnlag, resultater).orElse(BigDecimal.ZERO));
        }
        BigDecimal antallPerioder = BigDecimal.valueOf(inntektsperioder.size());
        BigDecimal snittMånedslønnFraRegister = inntektsperioder.size() == 0 ? BigDecimal.ZERO : samletInntekt.divide(antallPerioder, 10, RoundingMode.HALF_EVEN);
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
            Optional<BigDecimal> inntektForPeriode = finnInntektForPeriode(periode, inntektsgrunnlag, resultater);
            if (inntektForPeriode.isPresent()) {
                antallPerioderMedInntekt++;
                samletInntekt = samletInntekt.add(inntektForPeriode.get());
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

    private Optional<BigDecimal> finnInntektForPeriode(Periode periode, Inntektsgrunnlag inntektsgrunnlag, Map<String, Object> resultater) {
        List<Periodeinntekt> inntekterHosAgForPeriode = inntektsgrunnlag.getInntektForArbeidsforholdIPeriode(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, periode);
        if (inntekterHosAgForPeriode.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal sumForPeriode = inntekterHosAgForPeriode.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        resultater.put("sumForPeriode" + periode.toString(), sumForPeriode);
        return Optional.of(sumForPeriode);

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
