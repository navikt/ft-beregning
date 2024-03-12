package no.nav.folketrygdloven.kalkulator.felles.frist;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class StartRefusjonTjeneste {

    /**
     * Finner første dato det er søkt refusjon for f
     *
     * @param gjeldendeAktiviteter        Alle gjeldende aktiviteter i beregning
     * @param skjæringstidspunktBeregning Skjæringstidspunkt for beregning
     * @param yrkesaktivitet              Arbeidsforholdreferanse
     * @return Første dag med søkt refusjon
     */
    static LocalDate finnFørsteMuligeDagRefusjon(BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                         LocalDate skjæringstidspunktBeregning,
                                                         YrkesaktivitetDto yrkesaktivitet) {
        boolean erNyttArbeidsforhold = erTilkommetEtterBeregningstidspunkt(
                yrkesaktivitet.getArbeidsgiver(),
                yrkesaktivitet.getArbeidsforholdRef(),
                gjeldendeAktiviteter, skjæringstidspunktBeregning);
        if (erNyttArbeidsforhold) {
            LocalDate førsteAnsattdato = yrkesaktivitet.getAlleAnsettelsesperioder().stream()
                    .filter(aa -> aa.getPeriode().inkluderer(skjæringstidspunktBeregning)
                            || aa.getPeriode().getFomDato().isAfter(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning)))
                    .map(AktivitetsAvtaleDto::getPeriode)
                    .map(Intervall::getFomDato)
                    .min(Comparator.naturalOrder())
                    .orElse(skjæringstidspunktBeregning);
            return førsteAnsattdato.isAfter(skjæringstidspunktBeregning) ? førsteAnsattdato : skjæringstidspunktBeregning;
        } else {
            return skjæringstidspunktBeregning;
        }
    }

    /**
     * Finner første gyldige dato med refusjon.
     *
     * @param innsendingsdato
     * @return Første lovlige dato med refusjon på grunnlag av opplysninger tilgjengelig i register
     */
    static LocalDate finnFørsteGyldigeDatoMedRefusjon(LocalDate innsendingsdato) {
        return innsendingsdato.minusMonths(KonfigTjeneste.getFristMånederEtterRefusjon()).withDayOfMonth(1);
    }

    private static boolean erTilkommetEtterBeregningstidspunkt(Arbeidsgiver arbeidsgiver,
                                                               InternArbeidsforholdRefDto arbeidsforholdRef,
                                                               BeregningAktivitetAggregatDto aktivitetAggregat,
                                                               LocalDate skjæringstidspunkt) {
        var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
        return beregningAktiviteter.stream()
                .filter(beregningAktivitet -> erAktivPåBeregningstidspunkt(skjæringstidspunkt, beregningAktivitet))
                .noneMatch(beregningAktivitet -> arbeidsforholdRef.gjelderFor(beregningAktivitet.getArbeidsforholdRef()) && matcherArbeidsgiver(arbeidsgiver, beregningAktivitet));
    }

    private static boolean erAktivPåBeregningstidspunkt(LocalDate skjæringstidspunkt, BeregningAktivitetDto beregningAktivitet) {
        LocalDate beregningstidspunkt = BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt);
        return beregningAktivitet.getPeriode().inkluderer(beregningstidspunkt);
    }

    private static boolean matcherArbeidsgiver(Arbeidsgiver arbeidsgiver, BeregningAktivitetDto beregningAktivitet) {
        return Objects.equals(arbeidsgiver, beregningAktivitet.getArbeidsgiver());
    }


}
