package no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.NaturalytelserPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeSplittDataNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

class IdentifiserPerioderForNaturalytelse {
    private IdentifiserPerioderForNaturalytelse() {
        // skjul public constructor
    }

    static Set<PeriodeSplittDataNaturalytelse> identifiserPerioderForNaturalytelse(NaturalytelserPrArbeidsforhold inntektsmelding,
                                                                      LocalDate skjæringstidspunkt) {
        Set<PeriodeSplittDataNaturalytelse> set = new HashSet<>();

        inntektsmelding.getNaturalYtelser().forEach(naturalYtelse -> {
            var naturalYtelseFom = naturalYtelse.getFom();
	        var builder = PeriodeSplittDataNaturalytelse.builder().medNaturalytelserPrArbeidsforhold(inntektsmelding);
            var starterEtterSkjæringstidspunkt = naturalYtelseFom.isAfter(skjæringstidspunkt);
            if (starterEtterSkjæringstidspunkt) {
                var splittData = builder.medPeriodeÅrsak(PeriodeÅrsak.NATURALYTELSE_TILKOMMER)
                    .medFom(naturalYtelseFom)
                    .build();
                set.add(splittData);
            }
            var naturalYtelseTom = naturalYtelse.getTom();
            var opphørsdato = naturalYtelseTom.plusDays(1);
            var oppHørerEtterSkjæringstidspunkt = opphørsdato.isAfter(skjæringstidspunkt);
            if (!naturalYtelseTom.equals(DateUtil.TIDENES_ENDE) && oppHørerEtterSkjæringstidspunkt) {
                var splittData = builder.medPeriodeÅrsak(PeriodeÅrsak.NATURALYTELSE_BORTFALT)
                    .medFom(opphørsdato)
                    .build();
                set.add(splittData);
            }
        });
        return set;
    }
}
