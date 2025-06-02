package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;

public class FinnPerioderUtenYtelse {

    public static final Periode ÅRET_2017 = Periode.of(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 12, 31));

    private FinnPerioderUtenYtelse() {
        // Vedskjul
    }

    public static List<Periode> finnPerioder(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunktForOpptjening) {
        var ytelseperioder = finnPerioderMedYtelseFørDato(inntektsgrunnlag, skjæringstidspunktForOpptjening);
        return finnPerioderUtenYtelse(skjæringstidspunktForOpptjening, ytelseperioder);
    }

    public static List<Periode> finnPerioder(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunktForOpptjening, Map<String, Object> resultater) {
        var ytelseperioder = finnPerioderMedYtelseFørDato(inntektsgrunnlag, skjæringstidspunktForOpptjening);
        ytelseperioder.forEach(p -> resultater.put("Periode: " + p.periode().getFom() + " - " + p.periode().getTom(), "Ytelseperiode"));
        return finnPerioderUtenYtelse(skjæringstidspunktForOpptjening, ytelseperioder);
    }

    private static List<Periode> finnPerioderUtenYtelse(LocalDate skjæringstidspunktForOpptjening, List<YtelsePeriode> ytelseperioder) {
        if (ytelseperioder.isEmpty()) {
            List<Periode> beregningsperioder = new ArrayList<>();
            leggTilMånederMellom(beregningsperioder, skjæringstidspunktForOpptjening.minusMonths(13), skjæringstidspunktForOpptjening);
            verifiserPerioder(beregningsperioder);
            return beregningsperioder;
        }

        var beregningsperioder = finnPerioderUtenYtelseFra36MndFørStp(skjæringstidspunktForOpptjening, ytelseperioder);
        verifiserPerioder(beregningsperioder);
        if (harKunInntektFra2017OgDPEllerAAPPåStp(skjæringstidspunktForOpptjening, ytelseperioder, beregningsperioder)) {
            return Collections.emptyList();
        }
        var perioderEtter12MndFørStp = finnPerioderUtenYtelse12MndFørStp(skjæringstidspunktForOpptjening, beregningsperioder);
        return finnMinst6MndUtenYtelse(beregningsperioder, perioderEtter12MndFørStp);
    }

    private static boolean harKunInntektFra2017OgDPEllerAAPPåStp(LocalDate skjæringstidspunktForOpptjening, List<YtelsePeriode> ytelseperioder, List<Periode> beregningsperioder) {
        var harKunInntektFra2017 = beregningsperioder.stream().allMatch(p -> p.overlapper(ÅRET_2017));
        var harAAPEllerDPVedSTPOpptjening = ytelseperioder.stream()
            .filter(p -> p.periode().overlapper(Periode.of(skjæringstidspunktForOpptjening.minusMonths(1), skjæringstidspunktForOpptjening.minusDays(1))))
            .anyMatch(p -> p.inntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP));
        return harKunInntektFra2017 && harAAPEllerDPVedSTPOpptjening;
    }

    private static List<Periode> finnMinst6MndUtenYtelse(List<Periode> beregningsperioder, List<Periode> perioderEtter12MndFørStp) {
        if (perioderEtter12MndFørStp.size() >= 6) {
            return perioderEtter12MndFørStp;
        } else {
            return finn6MndUtenYtelse36MndFørStp(beregningsperioder);
        }
    }

    private static List<Periode> finn6MndUtenYtelse36MndFørStp(List<Periode> beregningsperioder) {
        var perioderSortertOmvendtKronologisk = beregningsperioder.stream()
            .sorted(Comparator.comparing(Periode::getFom).reversed())
            .toList();
        if (perioderSortertOmvendtKronologisk.size() <= 6) {
            return perioderSortertOmvendtKronologisk.stream().sorted(Comparator.comparing(Periode::getFom)).toList();
        }
        return perioderSortertOmvendtKronologisk.subList(0, 6)
            .stream().sorted(Comparator.comparing(Periode::getFom)).toList();
    }

    private static List<Periode> finnPerioderUtenYtelse12MndFørStp(LocalDate skjæringstidspunktForOpptjening, List<Periode> beregningsperioder) {
        return beregningsperioder.stream()
            .filter(p -> !p.getFom().isBefore(skjæringstidspunktForOpptjening.minusMonths(12).withDayOfMonth(1)))
            .toList();
    }

    private static List<Periode> finnPerioderUtenYtelseFra36MndFørStp(LocalDate skjæringstidspunktForBeregning, List<YtelsePeriode> ytelseperioder) {
        List<Periode> beregningsperioder = new ArrayList<>();
        // Må starte på måneden før opplysningsperioden startet for 3 år siden
        var gjeldendeTom = skjæringstidspunktForBeregning.minusMonths(37);
        var i = 0;
        while (gjeldendeTom.isBefore(skjæringstidspunktForBeregning)) {
            var periode = ytelseperioder.get(i).periode();
            if (erMinstEnMånedMellom(gjeldendeTom, periode.getFom())) {
                leggTilMånederMellom(beregningsperioder, gjeldendeTom, periode.getFom());
            }
            gjeldendeTom = periode.getTom();
            i = i + 1;
            if (i == ytelseperioder.size()) {
                if (erMinstEnMånedMellom(gjeldendeTom, skjæringstidspunktForBeregning)) {
                    leggTilMånederMellom(beregningsperioder, gjeldendeTom, skjæringstidspunktForBeregning);
                }
                gjeldendeTom = skjæringstidspunktForBeregning;
            }
        }
        return beregningsperioder;
    }

    private static List<YtelsePeriode> finnPerioderMedYtelseFørDato(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunktForBeregning) {
        return inntektsgrunnlag.getPeriodeinntekter()
            .stream()
            .filter(i -> i.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP) || i.getInntektskilde().equals(Inntektskilde.ANNEN_YTELSE))
            .filter(i -> i.getFom().isBefore(skjæringstidspunktForBeregning))
            .map(i -> new YtelsePeriode(i.getInntektskilde(), Periode.of(i.getFom(), i.getTom())))
            .sorted(Comparator.comparing(p -> p.periode().getFom()))
            .toList();
    }

    private static void leggTilMånederMellom(List<Periode> beregningsperioder, LocalDate førsteDato, LocalDate sisteDato) {
        var månederMellom = finnHeleMånederMellom(førsteDato, sisteDato);
        for (long k = 1; k <= månederMellom; k++) {
            var måned = førsteDato.plusMonths(k);
            beregningsperioder.add(Periode.of(måned.withDayOfMonth(1), måned.withDayOfMonth(måned.lengthOfMonth())));
        }
    }

    private static boolean erMinstEnMånedMellom(LocalDate dato1, LocalDate dato2) {
        return dato1.isBefore(dato2) && finnHeleMånederMellom(dato1, dato2) >= 1;
    }

    private static long finnHeleMånederMellom(LocalDate dato1, LocalDate dato2) {
        var årMellom = dato2.getYear() - dato1.getYear();
        var månederMellom = dato2.getMonthValue() - dato1.getMonthValue();

        // Hvis start og slutt er i samme måned
        if (månederMellom == 0 && årMellom == 0) {
            return 0;
        }

        // Antall måneder i perioden ikke medregnet start og slutt
        return (årMellom * 12L) + (månederMellom - 1L);
    }

    private static void verifiserPerioder(List<Periode> perioder) {
        // Alle perioder skal vare nøyaktig en hel måned, fra første dag i måneden til siste dag i måneden.
        perioder.forEach(periode -> {
            var fomÅrMåned = YearMonth.of(periode.getFom().getYear(), periode.getFom().getMonth());
            var tomÅrMåned = YearMonth.of(periode.getTom().getYear(), periode.getTom().getMonth());
            if (!fomÅrMåned.equals(tomÅrMåned)) {
                throw new IllegalStateException("Periode har ikke start og slutt i samme måned / år. Periode var: " + periode.toString());
            }
            if (!periode.getFom().equals(periode.getFom().with(TemporalAdjusters.firstDayOfMonth()))) {
                throw new IllegalStateException("Periode starter ikke på første dag i måneden. Periode var: " + periode.toString());
            }
            if (!periode.getTom().equals(periode.getTom().with(TemporalAdjusters.lastDayOfMonth()))) {
                throw new IllegalStateException("Periode slutter ikke på siste dag i måneden. Periode var: " + periode.toString());
            }
        });
    }

    private static record YtelsePeriode(Inntektskilde inntektskilde, Periode periode) {
    }

}
