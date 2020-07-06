package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

class IdentifiserPerioderForRefusjon {
    private IdentifiserPerioderForRefusjon() {
        // skjul public constructor
    }

    static Set<PeriodeSplittData> identifiserPerioderForRefusjon(ArbeidsforholdOgInntektsmelding inntektsmelding) {
        if (inntektsmelding.getInnsendingsdatoFørsteInntektsmeldingMedRefusjon() == null) {
            return Collections.emptySet();
        }

        // Setter hjemmel for vurdering av frist for refusjonskrav (folketrygdloven §§ 22-13 sjette ledd)
        ArbeidsforholdOgInntektsmelding.builder(inntektsmelding).medHarVurdertRefusjonskravfrist(true);

        Optional<LocalDate> utvidetRefusjonsdato = inntektsmelding.getOverstyrtRefusjonsFrist();
        LocalDate førsteLovligDato = utvidetRefusjonsdato.orElse(inntektsmelding
            .getInnsendingsdatoFørsteInntektsmeldingMedRefusjon().withDayOfMonth(1).minusMonths(inntektsmelding.getAntallMånederRefusjonskravFrist()));

        ListIterator<Refusjonskrav> li = inntektsmelding.getRefusjoner().listIterator();

        Set<PeriodeSplittData> set = new HashSet<>();
        while (li.hasNext()) {
            Refusjonskrav refusjon = li.next();
            LocalDate fom = refusjon.getFom().isBefore(førsteLovligDato) ? førsteLovligDato : refusjon.getFom();
            boolean finnesIkkeNesteEllerNesteStarterEtterLovlig = !li.hasNext() || inntektsmelding.getRefusjoner().get(li.nextIndex()).getFom().isAfter(førsteLovligDato);
            boolean kanBrukes = finnesIkkeNesteEllerNesteStarterEtterLovlig;
            if (kanBrukes) {
                Optional<PeriodeÅrsak> periodeÅrsakOpt = utledPeriodeÅrsak(inntektsmelding, refusjon, fom);
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
