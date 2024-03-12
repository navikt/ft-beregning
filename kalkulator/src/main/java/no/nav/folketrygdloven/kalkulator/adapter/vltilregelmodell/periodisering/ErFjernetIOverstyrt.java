package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;

public class ErFjernetIOverstyrt {
    private ErFjernetIOverstyrt() {
        // skjul public constructor
    }

    public static boolean erFjernetIOverstyrt(YrkesaktivitetFilterDto filter,
                                              YrkesaktivitetDto yrkesaktivitet,
                                              Optional<BeregningAktivitetOverstyringerDto> aktivitetOverstyringer, LocalDate skjæringstidspunktBeregning) {

        List<Periode> ansettelsesPerioder = filter.getAnsettelsesPerioder(yrkesaktivitet).stream()
                .map(aa -> new Periode(aa.getPeriode().getFomDato(), aa.getPeriode().getTomDato()))
                .filter(periode -> !periode.getTom().isBefore(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning)))
                .collect(Collectors.toList());
        if (erAktivDagenFørSkjæringstidspunktet(skjæringstidspunktBeregning, ansettelsesPerioder)) {
            return erFjernetIOverstyring(yrkesaktivitet, aktivitetOverstyringer) &&
                    varIkkeIPermisjonPåSkjæringstidspunkt(yrkesaktivitet.getFullPermisjon(), skjæringstidspunktBeregning);
        }
        return false;
    }

    private static Boolean erFjernetIOverstyring(YrkesaktivitetDto yrkesaktivitet, Optional<BeregningAktivitetOverstyringerDto> aktivitetOverstyringer) {
        return aktivitetOverstyringer.map(a -> a.getOverstyringer().stream().anyMatch(
                o -> o.getHandling().equals(BeregningAktivitetHandlingType.IKKE_BENYTT) &&
                        Objects.equals(o.getArbeidsgiver().orElse(null), yrkesaktivitet.getArbeidsgiver()) &&
                        o.getArbeidsforholdRef().gjelderFor(yrkesaktivitet.getArbeidsforholdRef()))).orElse(false);
    }

    private static boolean varIkkeIPermisjonPåSkjæringstidspunkt(Collection<PermisjonDto> permisjoner, LocalDate skjæringstidspunktBeregning) {
        return permisjoner.stream()
                .noneMatch(p -> p.getPeriode().inkluderer(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning)));
    }

    private static boolean erAktivDagenFørSkjæringstidspunktet(LocalDate skjæringstidspunktBeregning,
                                                               List<Periode> ansettelsesPerioder) {
        return ansettelsesPerioder.stream()
                .anyMatch(periode -> periode.inneholder(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning)));
    }
}
