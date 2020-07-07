package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

class IdentifiserPerioderForRefusjon {
    private IdentifiserPerioderForRefusjon() {
        // skjul public constructor
    }

    static Set<PeriodeSplittData> identifiserPerioderForRefusjon(ArbeidsforholdOgInntektsmelding inntektsmelding, Map<String, Object> resultater) {
        if (inntektsmelding.getInnsendingsdatoFørsteInntektsmeldingMedRefusjon() == null) {
            return Collections.emptySet();
        }

        int fristAntallMåneder = inntektsmelding.getRefusjonskravFrist() != null ?
            inntektsmelding.getRefusjonskravFrist().getAntallMånederRefusjonskravFrist() : 3;
        resultater.put("antallMånederRefusjonfrist", fristAntallMåneder);
        Optional<LocalDate> utvidetRefusjonsdato = inntektsmelding.getOverstyrtRefusjonsFrist();
        resultater.put("overstyrtFristutvidelse", utvidetRefusjonsdato);
        LocalDate førsteLovligDato = utvidetRefusjonsdato.orElse(inntektsmelding
            .getInnsendingsdatoFørsteInntektsmeldingMedRefusjon().withDayOfMonth(1).minusMonths(fristAntallMåneder));

        resultater.put("førsteLovligeRefusjonsdato", førsteLovligDato);


        ListIterator<Refusjonskrav> li = inntektsmelding.getRefusjoner().listIterator();

        Set<PeriodeSplittData> set = new HashSet<>();
        while (li.hasNext()) {
            Refusjonskrav refusjon = li.next();
            resultater.put("refusjonskrav", refusjon);
            boolean starterFørFørsteLovligeDato = refusjon.getFom().isBefore(førsteLovligDato);
            resultater.put("starterFørFørsteLovligeDato", starterFørFørsteLovligeDato);
            LocalDate tom = refusjon.getPeriode().getTom() == null ? TIDENES_ENDE : refusjon.getPeriode().getTom();
            boolean slutterFørFørsteLovligeDato = tom.isBefore(førsteLovligDato);
            resultater.put("slutterFørFørsteLovligeDato", slutterFørFørsteLovligeDato);
            if (!slutterFørFørsteLovligeDato) {
                LocalDate fom = starterFørFørsteLovligeDato ? førsteLovligDato : refusjon.getFom();
                resultater.put("fom", fom);
                Optional<PeriodeÅrsak> periodeÅrsakOpt = utledPeriodeÅrsak(inntektsmelding, refusjon, fom);
                periodeÅrsakOpt.ifPresent(årsak -> resultater.put("periodeårsak", årsak));
                periodeÅrsakOpt.ifPresent(periodeÅrsak -> {
                    PeriodeSplittData periodeSplittData = PeriodeSplittData.builder()
                        .medPeriodeÅrsak(periodeÅrsak)
                        .medInntektsmelding(inntektsmelding)
                        .medFom(fom)
                        .medRefusjonskravPrMåned(refusjon.getMånedsbeløp())
                        .build();
                    if (periodeÅrsak.equals(PeriodeÅrsak.REFUSJON_OPPHØRER) && set.isEmpty()) {
                        return;
                    }
                    set.add(periodeSplittData);
                });
            }
        }
        return set;
    }

    private static Optional<PeriodeÅrsak> utledPeriodeÅrsak(ArbeidsforholdOgInntektsmelding inntektsmelding,
                                                            Refusjonskrav refusjonskrav, LocalDate fom) {
        BigDecimal årsbeløp = refusjonskrav.getMånedsbeløp();
        if (årsbeløp.compareTo(BigDecimal.ZERO) == 0) {
            if (inntektsmelding.getStartdatoPermisjon().equals(fom)) {
                return Optional.empty();
            }
            return Optional.of(PeriodeÅrsak.REFUSJON_OPPHØRER);
        }
        return Optional.of(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }
}
