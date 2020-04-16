package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;

public class FinnPerioderUtenYtelse {

    private FinnPerioderUtenYtelse() {
        // skjul
    }

    public static List<Periode> finnPerioder(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunktForBeregning, Map<String, Object> resultater) {
        List<Periode> ytelseperioder = finnPerioderMedYtelseFørDato(inntektsgrunnlag, skjæringstidspunktForBeregning);
        ytelseperioder.forEach(p -> resultater.put("Periode: " + p.getFom() + " - " + p.getTom(), "Ytelseperiode"));

        if (ytelseperioder.isEmpty()) {
            List<Periode> beregningsperioder = new ArrayList<>();
            leggTilMånederMellom(beregningsperioder, skjæringstidspunktForBeregning.minusMonths(13), skjæringstidspunktForBeregning);
            return beregningsperioder;
        }

        List<Periode> beregningsperioder = finnPerioderUtenYtelseFra36MndFørStp(skjæringstidspunktForBeregning, ytelseperioder);
        List<Periode> perioderEtter12MndFørStp = finnPerioderUtenYtelse12MndFørStp(skjæringstidspunktForBeregning, beregningsperioder);
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

    private static List<Periode> finnPerioderUtenYtelse12MndFørStp(LocalDate skjæringstidspunktForBeregning, List<Periode> beregningsperioder) {
        return beregningsperioder.stream()
            .filter(p -> p.getFom().isAfter(skjæringstidspunktForBeregning.minusMonths(12)))
            .collect(Collectors.toList());
    }

    private static List<Periode> finnPerioderUtenYtelseFra36MndFørStp(LocalDate skjæringstidspunktForBeregning, List<Periode> ytelseperioder) {
        List<Periode> beregningsperioder = new ArrayList<>();
        LocalDate gjeldendeTom = skjæringstidspunktForBeregning.minusMonths(36);
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
        for (int k = 1; k < månederMellom; k++) {
            LocalDate måned = førsteDato.plusMonths(k);
            beregningsperioder.add(Periode.of(måned.withDayOfMonth(1), måned.withDayOfMonth(måned.lengthOfMonth())));
        }
    }

    private static boolean erMinstEnMånedMellom(LocalDate dato1, LocalDate dato2) {
        return dato1.isBefore(dato2) && finnHeleMånederMellom(dato1, dato2) > 1;
    }

    private static long finnHeleMånederMellom(LocalDate dato1, LocalDate dato2) {
        int årMellom = dato2.getYear() - dato1.getYear();
        int månederMellom = dato2.getMonthValue() - dato1.getMonthValue();
        if (månederMellom >= 2 || årMellom >= 1) {
            if (årMellom >= 1) {
                return årMellom * 11 + månederMellom;
            }
            return månederMellom;
        }
        return 0;
    }


}
