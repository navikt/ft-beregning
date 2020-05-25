package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FinnPerioderUtenYtelse {

    private FinnPerioderUtenYtelse() {
        // Vedskjul
    }

    public static List<Periode> finnPerioder(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunktForOpptjening) {
        List<Periode> ytelseperioder = finnPerioderMedYtelseFørDato(inntektsgrunnlag, skjæringstidspunktForOpptjening);
        return finnPerioderUtenYtelse(skjæringstidspunktForOpptjening, ytelseperioder);
    }

    public static List<Periode> finnPerioder(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunktForOpptjening, Map<String, Object> resultater) {
        List<Periode> ytelseperioder = finnPerioderMedYtelseFørDato(inntektsgrunnlag, skjæringstidspunktForOpptjening);
        ytelseperioder.forEach(p -> resultater.put("Periode: " + p.getFom() + " - " + p.getTom(), "Ytelseperiode"));
        return finnPerioderUtenYtelse(skjæringstidspunktForOpptjening, ytelseperioder);
    }

    private static List<Periode> finnPerioderUtenYtelse(LocalDate skjæringstidspunktForOpptjening, List<Periode> ytelseperioder) {
        if (ytelseperioder.isEmpty()) {
            List<Periode> beregningsperioder = new ArrayList<>();
            leggTilMånederMellom(beregningsperioder, skjæringstidspunktForOpptjening.minusMonths(13), skjæringstidspunktForOpptjening);
            verifiserPerioder(beregningsperioder);
            return beregningsperioder;
        }

        List<Periode> beregningsperioder = finnPerioderUtenYtelseFra36MndFørStp(skjæringstidspunktForOpptjening, ytelseperioder);
        verifiserPerioder(beregningsperioder);
        List<Periode> perioderEtter12MndFørStp = finnPerioderUtenYtelse12MndFørStp(skjæringstidspunktForOpptjening, beregningsperioder);
        return finnMinst6MndUtenYtelse(beregningsperioder, perioderEtter12MndFørStp);
    }

    private static List<Periode> finnMinst6MndUtenYtelse(List<Periode> beregningsperioder, List<Periode> perioderEtter12MndFørStp) {
        if (perioderEtter12MndFørStp.size() >= 6) {
            return perioderEtter12MndFørStp;
        } else {
            return finn6MndUtenYtelse36MndFørStp(beregningsperioder);
        }
    }

    private static List<Periode> finn6MndUtenYtelse36MndFørStp(List<Periode> beregningsperioder) {
        List<Periode> perioderSortertOmvendtKronologisk = beregningsperioder.stream()
            .sorted(Comparator.comparing(Periode::getFom).reversed())
            .collect(Collectors.toList());
        if (perioderSortertOmvendtKronologisk.size() <= 6) {
            return perioderSortertOmvendtKronologisk.stream().sorted(Comparator.comparing(Periode::getFom)).collect(Collectors.toList());
        }
        return perioderSortertOmvendtKronologisk.subList(0, 6)
            .stream().sorted(Comparator.comparing(Periode::getFom)).collect(Collectors.toList());
    }

    private static List<Periode> finnPerioderUtenYtelse12MndFørStp(LocalDate skjæringstidspunktForOpptjening, List<Periode> beregningsperioder) {
        return beregningsperioder.stream()
            .filter(p -> !p.getFom().isBefore(skjæringstidspunktForOpptjening.minusMonths(12).withDayOfMonth(1)))
            .collect(Collectors.toList());
    }

    private static List<Periode> finnPerioderUtenYtelseFra36MndFørStp(LocalDate skjæringstidspunktForBeregning, List<Periode> ytelseperioder) {
        List<Periode> beregningsperioder = new ArrayList<>();
        // Må starte på måneden før opplysningsperioden startet for 3 år siden
        LocalDate gjeldendeTom = skjæringstidspunktForBeregning.minusMonths(37);
        int i = 0;
        while (gjeldendeTom.isBefore(skjæringstidspunktForBeregning)) {
            Periode periode = ytelseperioder.get(i);
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

    private static List<Periode> finnPerioderMedYtelseFørDato(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunktForBeregning) {
        return inntektsgrunnlag.getPeriodeinntekter()
            .stream()
            .filter(i -> i.getInntektskilde().equals(Inntektskilde.YTELSER))
            .filter(i -> i.getFom().isBefore(skjæringstidspunktForBeregning))
            .map(i -> Periode.of(i.getFom(), i.getTom()))
            .sorted(Comparator.comparing(Periode::getFom))
            .collect(Collectors.toList());
    }

    private static void leggTilMånederMellom(List<Periode> beregningsperioder, LocalDate førsteDato, LocalDate sisteDato) {
        long månederMellom = finnHeleMånederMellom(førsteDato, sisteDato);
        for (int k = 1; k <= månederMellom; k++) {
            LocalDate måned = førsteDato.plusMonths(k);
            beregningsperioder.add(Periode.of(måned.withDayOfMonth(1), måned.withDayOfMonth(måned.lengthOfMonth())));
        }
    }

    private static boolean erMinstEnMånedMellom(LocalDate dato1, LocalDate dato2) {
        return dato1.isBefore(dato2) && finnHeleMånederMellom(dato1, dato2) >= 1;
    }

    private static long finnHeleMånederMellom(LocalDate dato1, LocalDate dato2) {
        int årMellom = dato2.getYear() - dato1.getYear();
        int månederMellom = dato2.getMonthValue() - dato1.getMonthValue();

        // Hvis start og slutt er i samme måned
        if (månederMellom == 0 && årMellom == 0) {
            return 0;
        }

        // Antall måneder i perioden ikke medregnet start og slutt
        return (årMellom * 12) + (månederMellom -1);
    }

    private static void verifiserPerioder(List<Periode> perioder) {
        // Alle perioder skal vare nøyaktig en hel måned, fra første dag i måneden til siste dag i måneden.
        perioder.forEach(periode -> {
            YearMonth fomÅrMåned = YearMonth.of(periode.getFom().getYear(), periode.getFom().getMonth());
            YearMonth tomÅrMåned = YearMonth.of(periode.getTom().getYear(), periode.getTom().getMonth());
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

}
