package no.nav.folketrygdloven.kalkulator.felles.ytelseovergang;

import static no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste.finnBeregningstidspunkt;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektPeriodeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class InfotrygdvedtakMedDagpengerTjeneste {

    public static Boolean harYtelsePåGrunnlagAvDagpenger(Collection<YtelseDto> ytelser, LocalDate skjæringstidspunkt, YtelseType ytelse) {
        LocalDate beregningstidspunkt = finnBeregningstidspunkt(skjæringstidspunkt);
        if (KonfigurasjonVerdi.instance().get("MAP_YTELSE_DAGPENGER_FRA_ANDELER", false)) {
            return finnYtelseBasertPåDagpengerFraAnvisteAndeler(ytelser, beregningstidspunkt, ytelse).compareTo(Beløp.ZERO) > 0;
        }
        return finnYtelseBasertPåDagpenger(ytelser, beregningstidspunkt, ytelse).isPresent();
    }

    public static Beløp finnDagsatsFraYtelsevedtak(Collection<YtelseDto> ytelser, LocalDate skjæringstidspunkt, YtelseType ytelse) {
        LocalDate beregningstidspunkt = finnBeregningstidspunkt(skjæringstidspunkt);

        if (KonfigurasjonVerdi.instance().get("MAP_YTELSE_DAGPENGER_FRA_ANDELER", false)) {
            // Returnerer graderte dagpenger
            return finnYtelseBasertPåDagpengerFraAnvisteAndeler(ytelser, beregningstidspunkt, ytelse);
        } else {
            // Returnerer ugraderte dagpenger
            return finnYtelseBasertPåDagpengerFraYtelseGrunnlag(ytelser, ytelse, beregningstidspunkt);
        }
    }

    private static Beløp finnYtelseBasertPåDagpengerFraYtelseGrunnlag(Collection<YtelseDto> ytelser, YtelseType ytelse, LocalDate beregningstidspunkt) {
        var ytelseGrunnlag = finnYtelseBasertPåDagpenger(ytelser, beregningstidspunkt, ytelse);

        var spAvDP = ytelseGrunnlag.stream()
                .flatMap(yg -> yg.getFordeling().stream())
                .filter(f -> f.getArbeidsgiver() == null)
                .findFirst();

        return spAvDP.map(f -> {
            var hyppighet = f.getHyppighet();
            // Antar dagsats ved ingen periodetype
            if (hyppighet == null || InntektPeriodeType.DAGLIG.equals(hyppighet)) {
                return f.getBeløp();
            }
            throw new IllegalArgumentException("Hånterer foreløpig kun dagsats som periodetype for ytelse av dagpenger.");
        }).orElse(Beløp.ZERO);
    }

    private static Beløp finnYtelseBasertPåDagpengerFraAnvisteAndeler(Collection<YtelseDto> ytelser, LocalDate beregningstidspunkt, YtelseType ytelse) {
        var anvisning = finnAnvisning(ytelser, beregningstidspunkt, ytelse);
        return anvisning.stream().flatMap(y -> y.getAnvisteAndeler().stream())
                .filter(a  -> a.getInntektskategori().equals(Inntektskategori.DAGPENGER))
                .findFirst()
                .map(AnvistAndel::getDagsats)
                .orElse(Beløp.ZERO);
    }

    private static Optional<YtelseAnvistDto> finnAnvisning(Collection<YtelseDto> ytelser, LocalDate beregningstidspunkt, YtelseType ytelse) {
        var anvisning = ytelser.stream()
                .filter(y -> y.getPeriode().inkluderer(beregningstidspunkt))
                .filter(y -> y.getYtelseType().equals(ytelse))
                .flatMap(y -> y.getYtelseAnvist().stream())
                .filter(y -> y.getAnvistPeriode().inkluderer(beregningstidspunkt))
                .findFirst();
        if (anvisning.isEmpty() && beregningstidspunkt.getDayOfWeek().equals(DayOfWeek.SUNDAY) || beregningstidspunkt.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            var fredagFørHelg = beregningstidspunkt.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
            return ytelser.stream()
                    .filter(y -> y.getPeriode().inkluderer(fredagFørHelg))
                    .filter(y -> y.getYtelseType().equals(ytelse))
                    .flatMap(y -> y.getYtelseAnvist().stream())
                    .filter(y -> y.getAnvistPeriode().inkluderer(fredagFørHelg))
                    .findFirst();
        }
        return anvisning;
    }


    private static Optional<YtelseGrunnlagDto> finnYtelseBasertPåDagpenger(Collection<YtelseDto> ytelser, LocalDate beregningstidspunkt, YtelseType ytelse) {
        return ytelser.stream()
                .filter(y -> y.getPeriode().inkluderer(beregningstidspunkt))
                .filter(y -> y.getYtelseType().equals(ytelse))
                .flatMap(y -> y.getYtelseGrunnlag().stream())
                .filter(gr -> Arbeidskategori.DAGPENGER.equals(gr.getArbeidskategori()) ||
                        Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER.equals(gr.getArbeidskategori()))
                .findFirst();
    }


}
