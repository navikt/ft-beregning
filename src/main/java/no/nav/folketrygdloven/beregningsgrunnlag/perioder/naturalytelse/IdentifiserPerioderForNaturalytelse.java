package no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.NaturalytelserPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse.PeriodeSplittDataNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

class IdentifiserPerioderForNaturalytelse {
    private IdentifiserPerioderForNaturalytelse() {
        // skjul public constructor
    }

    static Set<PeriodeSplittDataNaturalytelse> identifiserPerioderForNaturalytelse(NaturalytelserPrArbeidsforhold inntektsmelding,
                                                                      LocalDate skjæringstidspunkt) {
        Set<PeriodeSplittDataNaturalytelse> set = new HashSet<>();

        inntektsmelding.getNaturalYtelser().forEach(naturalYtelse -> {
            LocalDate naturalYtelseFom = naturalYtelse.getFom();
	        var builder = PeriodeSplittDataNaturalytelse.builder().medNaturalytelserPrArbeidsforhold(inntektsmelding);
            boolean starterEtterSkjæringstidspunkt = naturalYtelseFom.isAfter(skjæringstidspunkt);
            if (starterEtterSkjæringstidspunkt) {
	            PeriodeSplittDataNaturalytelse splittData = builder.medPeriodeÅrsak(PeriodeÅrsak.NATURALYTELSE_TILKOMMER)
                    .medFom(naturalYtelseFom)
                    .build();
                set.add(splittData);
            }
            LocalDate naturalYtelseTom = naturalYtelse.getTom();
            LocalDate opphørsdato = naturalYtelseTom.plusDays(1);
            boolean oppHørerEtterSkjæringstidspunkt = opphørsdato.isAfter(skjæringstidspunkt);
            if (!naturalYtelseTom.equals(DateUtil.TIDENES_ENDE) && oppHørerEtterSkjæringstidspunkt) {
	            PeriodeSplittDataNaturalytelse splittData = builder.medPeriodeÅrsak(PeriodeÅrsak.NATURALYTELSE_BORTFALT)
                    .medFom(opphørsdato)
                    .build();
                set.add(splittData);
            }
        });
        return set;
    }
}
