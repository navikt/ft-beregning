package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;

public class Inntektsgrunnlag {
    public static final int VIRKEDAGER_I_ET_ÅR = 260;
    private int inntektRapporteringFristDag;
    private final List<Periodeinntekt> periodeinntekter = new ArrayList<>();

    public Inntektsgrunnlag() {
        // default constructor
    }

    public int getInntektRapporteringFristDag() {
        return inntektRapporteringFristDag;
    }

    public void setInntektRapporteringFristDag(int inntektRapporteringFristDag) {
        this.inntektRapporteringFristDag = inntektRapporteringFristDag;
    }

    public void leggTilPeriodeinntekt(Periodeinntekt periodeinntekt) {
        this.periodeinntekter.add(periodeinntekt);
    }

    public List<Periodeinntekt> getPeriodeinntekter() {
        return periodeinntekter;
    }

    public List<Periodeinntekt> getPeriodeinntekterForSNFraSøknad(Periode periode){
        return periodeinntekter.stream()
            .filter(i -> i.getInntektskilde().equals(Inntektskilde.SØKNAD)
                && i.erSelvstendingNæringsdrivende()
                && periode.overlapper(Periode.of(i.getFom(), i.getTom())))
            .collect(Collectors.toUnmodifiableList());
    }

    public Optional<Periodeinntekt> getPeriodeinntekt(Inntektskilde inntektskilde, LocalDate dato) {
        return getPeriodeinntektMedKilde(inntektskilde)
            .filter(pi -> pi.inneholder(dato))
            .findFirst();
    }

    public List<Periodeinntekt> getInntektForArbeidsforholdIPeriode(Inntektskilde inntektskilde, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, Periode periode) {
        if (arbeidsforhold.erFrilanser()) {
            return getPeriodeinntektMedKilde(inntektskilde)
                .filter(pi -> pi.getArbeidsgiver().isPresent())
                .filter(pi -> pi.getArbeidsgiver().get().erFrilanser()) //NOSONAR
                .filter(pi -> pi.getArbeidsgiver().get().equals(arbeidsforhold.getArbeidsforhold())) //NOSONAR
                .filter(pi -> pi.erInnenforPeriode(periode))
                .collect(Collectors.toList());
        } else {
            return getPeriodeinntektMedKilde(inntektskilde)
                .filter(pi -> pi.erInnenforPeriode(periode))
                .filter(pi -> pi.getArbeidsgiver().isPresent())
                .filter(pi -> pi.getArbeidsgiver().get().equals(arbeidsforhold.getArbeidsforhold()))
                .collect(Collectors.toList());
        }
    }


    public Optional<LocalDate> sistePeriodeMedInntektFørDato(Inntektskilde inntektskilde, LocalDate dato) {
        Optional<Periodeinntekt> perioder = getPeriodeinntektMedKilde(inntektskilde)
            .filter(pi -> dato.isAfter(pi.getTom()))
            .max(Comparator.comparing(Periodeinntekt::getFom));
        return perioder.map(Periodeinntekt::getFom);
    }

    public Optional<Periodeinntekt> getSistePeriodeinntektMedTypeSøknad() {
        return getPeriodeinntektMedKilde(Inntektskilde.SØKNAD)
            .max(Comparator.comparing(Periodeinntekt::getFom));
    }

    public Optional<BigDecimal> getOppgittInntektForStatusIPeriode(AktivitetStatus status, Periode periode) {
        List<Periodeinntekt> periodeinntekter = getInntektspostFraSøknadForStatusIPeriode(status, periode);
        if (periodeinntekter.isEmpty()) {
            return Optional.empty();
        }
        return periodeinntekter.stream()
            .map(this::finnÅrsinntektForPeriode)
            .reduce(BigDecimal::add);
    }

    public List<Periodeinntekt> getInntektspostFraSøknadForStatusIPeriode(AktivitetStatus status, Periode periode) {
        return getPeriodeinntektMedKilde(Inntektskilde.SØKNAD)
                .filter(pi -> Objects.equals(pi.getAktivitetStatus(), status))
                .filter(i -> periode.overlapper(Periode.of(i.getFom(), i.getTom())))
                .collect(Collectors.toList());
    }

    private BigDecimal finnÅrsinntektForPeriode(Periodeinntekt oppgittInntekt) {
        BigDecimal dagsats = finnEffektivDagsatsIPeriode(oppgittInntekt);
        return dagsats.multiply(BigDecimal.valueOf(VIRKEDAGER_I_ET_ÅR));
    }

    /**
     * Finner opptjent inntekt pr dag i periode
     *
     * @param oppgittInntekt Informasjon om oppgitt inntekt
     * @return dagsats i periode
     */
    private BigDecimal finnEffektivDagsatsIPeriode(Periodeinntekt oppgittInntekt) {
        long dagerIRapportertPeriode = Virkedager.beregnAntallVirkedagerEllerKunHelg(oppgittInntekt.getFom(), oppgittInntekt.getTom());
        if (dagerIRapportertPeriode == 0) {
            return BigDecimal.ZERO;
        }
        return oppgittInntekt.getInntekt().divide(BigDecimal.valueOf(dagerIRapportertPeriode), 10, RoundingMode.HALF_UP);
    }

    public BigDecimal getInntektFraInntektsmelding(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return getPeriodeinntektInntektsmeldingForArbeidsgiver(arbeidsforhold.getArbeidsforhold())
            .findFirst()
            .map(Periodeinntekt::getInntekt)
            .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getÅrsinntektSigrun(int år) {
        return getPeriodeinntektMedKilde(Inntektskilde.SIGRUN)
            .filter(pi -> pi.erFraår(år))
            .map(Periodeinntekt::getInntekt)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean finnesInntektsdata(Inntektskilde inntektskilde, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return getPeriodeinntektMedKilde(inntektskilde)
            .map(Periodeinntekt::getArbeidsgiver)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .anyMatch(arbeidsgiver -> arbeidsforhold.getArbeidsforhold().equals(arbeidsgiver));
    }

    public BigDecimal getSamletInntektISammenligningsperiode(Periode periode) {
        return getPeriodeinntektMedKilde(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
            .filter(pi-> periode.inneholder(pi.getFom()))
            .map(Periodeinntekt::getInntekt)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSamletInntektISammenligningsperiodeFL(Periode periode) {
        return getPeriodeinntektMedKilde(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
            .filter(Periodeinntekt::erFrilans)
            .filter(pi-> periode.inneholder(pi.getFom()))
            .map(Periodeinntekt::getInntekt)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSamletInntektISammenligningsperiodeAT(Periode periode) {
        return getPeriodeinntektMedKilde(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
            .filter(Periodeinntekt::erArbeidstaker)
            .filter(pi-> periode.inneholder(pi.getFom()))
            .map(Periodeinntekt::getInntekt)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<BigDecimal> getPeriodeinntekter(Inntektskilde inntektskilde, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, LocalDate førDato, int måneder) {
        if (arbeidsforhold.erFrilanser()) {
            return getAlleFrilansinntekterForArbeidsforhold(inntektskilde, førDato, måneder, arbeidsforhold.getArbeidsforhold());
        }
        List<BigDecimal> inntekter = new ArrayList<>();
        for (int måned = 0; måned < måneder; måned++) {
            final int siden = måned;
            Optional<Periodeinntekt> beløp = getPeriodeinntektMedKilde(inntektskilde)
                .filter(pi -> pi.getArbeidsgiver().isPresent())
                .filter(pi -> arbeidsforhold.getArbeidsforhold().equals(pi.getArbeidsgiver().get()))//NOSONAR
                .filter(pi -> pi.inneholder(førDato.minusMonths(siden)))
                .findFirst();
            beløp.ifPresent(månedsinntekt -> inntekter.add(månedsinntekt.getInntekt()));
        }
        return inntekter;
    }

    public List<BigDecimal> getFrilansPeriodeinntekter(Inntektskilde inntektskilde, LocalDate førDato, int måneder) {
        return getAlleFrilansinntekter(inntektskilde, førDato, måneder);

    }

    private List<BigDecimal> getAlleFrilansinntekterForArbeidsforhold(Inntektskilde inntektskilde, LocalDate førDato, int måneder, Arbeidsforhold arbeidsforhold) {
        Periode periode = Periode.of(førDato.minusMonths(måneder), førDato);
        return getPeriodeinntektMedKilde(inntektskilde)
            .filter(pi -> pi.getArbeidsgiver().isPresent())
            .filter(pi -> pi.getArbeidsgiver().get().erFrilanser()) //NOSONAR
            .filter(pi -> pi.getArbeidsgiver().get().equals(arbeidsforhold)) //NOSONAR
            .filter(pi -> pi.erInnenforPeriode(periode))
            .map(Periodeinntekt::getInntekt)
            .collect(Collectors.toList());
    }

    private List<BigDecimal> getAlleFrilansinntekter(Inntektskilde inntektskilde, LocalDate førDato, int måneder) {
        Periode periode = Periode.of(førDato.minusMonths(måneder), førDato);
        return finnAlleFrilansInntektPerioder(inntektskilde, periode)
            .stream()
            .map(Periodeinntekt::getInntekt)
            .collect(Collectors.toList());
    }

    public List<Periodeinntekt> finnAlleFrilansInntektPerioder(Inntektskilde inntektskilde, Periode periode) {
        return getPeriodeinntektMedKilde(inntektskilde)
            .filter(pi -> pi.getArbeidsgiver().isPresent())
            .filter(pi -> pi.getArbeidsgiver().get().erFrilanser()) //NOSONAR
            .filter(pi -> pi.erInnenforPeriode(periode))
            .collect(Collectors.toList());
    }

    private Stream<Periodeinntekt> getPeriodeinntektMedKilde(Inntektskilde inntektskilde) {
        return periodeinntekter.stream()
            .filter(pi -> inntektskilde.equals(pi.getInntektskilde()));
    }

    public Optional<BigDecimal> finnTotaltNaturalytelseBeløpMedOpphørsdatoIPeriodeForArbeidsforhold(Arbeidsforhold arbeidsforhold, LocalDate fom, LocalDate tom) {
        Periode periode = Periode.of(fom, tom);
        return getNaturalYtelserForArbeidsgiver(arbeidsforhold)
            .filter(ny -> ny.getTom() != null)
            .filter(ny -> periode.inneholder(ny.getTom().plusDays(1)))//perioden innholder opphørsdatoen
            .map(NaturalYtelse::getBeløp)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add);
    }

    public Optional<BigDecimal> finnTotaltNaturalytelseBeløpTilkommetIPeriodeForArbeidsforhold(Arbeidsforhold arbeidsforhold, LocalDate fom, LocalDate tom) {
        Periode periode = Periode.of(fom, tom);
        return getNaturalYtelserForArbeidsgiver(arbeidsforhold)
            .filter(ny -> ny.getFom() != null)
            .filter(ny -> periode.inneholder(ny.getFom()))
            .map(NaturalYtelse::getBeløp)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add);
    }

    private Stream<NaturalYtelse> getNaturalYtelserForArbeidsgiver(Arbeidsforhold arbeidsforhold) {
        return getPeriodeinntektInntektsmeldingForArbeidsgiver(arbeidsforhold)
            .flatMap(i -> i.getNaturalYtelser().stream());
    }

    private Stream<Periodeinntekt> getPeriodeinntektInntektsmeldingForArbeidsgiver(Arbeidsforhold arbeidsforhold) {
        return getPeriodeinntektMedKilde(Inntektskilde.INNTEKTSMELDING)
            .filter(i -> i.getArbeidsgiver().isPresent())
            .filter(i -> arbeidsforhold.equals(i.getArbeidsgiver().get())); //NOSONAR
    }

}
