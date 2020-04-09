package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MeldekortPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

class IdentifiserPerioderForMeldekort {

    static Set<PeriodeSplittData> identifiserPerioderForEndringerIMeldekort(PeriodeModell input) {

        List<MeldekortPeriode> meldekortPerioder = input.getMeldekortPerioder();

        LocalDate skjæringstidspunkt = input.getSkjæringstidspunkt();

        List<MeldekortPeriode> perioderEtterStp = meldekortPerioder.stream()
            .filter(p -> !p.getPeriode().getTom().isBefore(skjæringstidspunkt))
            .sorted(Comparator.comparing(p -> p.getPeriode().getTom()))
            .collect(Collectors.toList());

        if (perioderEtterStp.isEmpty()) {
            return Collections.emptySet();
        }

        Set<PeriodeSplittData> periodeSplittData = new HashSet<>();

        MeldekortPeriode forrigePeriode = perioderEtterStp.get(0);

        if (!forrigePeriode.getPeriode().inneholder(skjæringstidspunkt)) {
            PeriodeSplittData splitt = PeriodeSplittData.builder()
                .medFom(forrigePeriode.getPeriode().getFom())
                .medPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_MELDEKORTUTBETALING)
                .medMeldekortPeriode(forrigePeriode)
                .build();
            periodeSplittData.add(splitt);
        }

        for (int i = 1; i < perioderEtterStp.size(); i++) {
            MeldekortPeriode periode = perioderEtterStp.get(i);
            if (periode.finnUtbetaling().compareTo(forrigePeriode.finnUtbetaling()) != 0) {
                PeriodeSplittData splitt = PeriodeSplittData.builder()
                    .medFom(periode.getPeriode().getFom())
                    .medPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_MELDEKORTUTBETALING)
                    .medMeldekortPeriode(periode)
                    .build();
                periodeSplittData.add(splitt);
            }
            forrigePeriode = periode;
        }

        PeriodeSplittData splitt = PeriodeSplittData.builder()
            .medFom(forrigePeriode.getPeriode().getTom().plusDays(1))
            .medPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_MELDEKORTUTBETALING)
            .medMeldekortPeriode(null)
            .build();
        periodeSplittData.add(splitt);

        return periodeSplittData.stream()
            .sorted(Comparator.comparing(PeriodeSplittData::getFom))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
