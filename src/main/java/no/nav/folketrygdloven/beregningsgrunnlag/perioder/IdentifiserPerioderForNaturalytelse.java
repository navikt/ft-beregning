package no.nav.folketrygdloven.beregningsgrunnlag.perioder;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

class IdentifiserPerioderForNaturalytelse {
    private IdentifiserPerioderForNaturalytelse() {
        // skjul public constructor
    }

    static Set<PeriodeSplittData> identifiserPerioderForNaturalytelse(ArbeidsforholdOgInntektsmelding inntektsmelding,
                                                                      LocalDate skjæringstidspunkt) {
        Set<PeriodeSplittData> set = new HashSet<>();

        inntektsmelding.getNaturalYtelser().forEach(naturalYtelse -> {
            LocalDate naturalYtelseFom = naturalYtelse.getFom();
            PeriodeSplittData.Builder builder = PeriodeSplittData.builder().medInntektsmelding(inntektsmelding);
            boolean starterEtterSkjæringstidspunkt = naturalYtelseFom.isAfter(skjæringstidspunkt);
            if (starterEtterSkjæringstidspunkt) {
                PeriodeSplittData splittData = builder.medPeriodeÅrsak(PeriodeÅrsak.NATURALYTELSE_TILKOMMER)
                    .medFom(naturalYtelseFom)
                    .build();
                set.add(splittData);
            }
            LocalDate naturalYtelseTom = naturalYtelse.getTom();
            LocalDate opphørsdato = naturalYtelseTom.plusDays(1);
            boolean oppHørerEtterSkjæringstidspunkt = opphørsdato.isAfter(skjæringstidspunkt);
            if (!naturalYtelseTom.equals(DateUtil.TIDENES_ENDE) && oppHørerEtterSkjæringstidspunkt) {
                PeriodeSplittData splittData = builder.medPeriodeÅrsak(PeriodeÅrsak.NATURALYTELSE_BORTFALT)
                    .medFom(opphørsdato)
                    .build();
                set.add(splittData);
            }
        });
        return set;
    }
}
