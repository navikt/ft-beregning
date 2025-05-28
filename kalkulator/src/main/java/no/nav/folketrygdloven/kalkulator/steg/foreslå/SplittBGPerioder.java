package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.util.KopierBeregningsgrunnlagUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class SplittBGPerioder {
    private SplittBGPerioder() {
        // skjul public constructor
    }

    public static void splitt(Beregningsgrunnlag regelBeregningsgrunnlag, Collection<Intervall> perioder, PeriodeÅrsak periodeårsak) {
        perioder.forEach(periode -> {
            var periodeTomDato = periode.getTomDato();
            var eksisterendePerioder = regelBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
            var periodeIterator = eksisterendePerioder.listIterator();
            while (periodeIterator.hasNext()) {
                var beregningsgrunnlagPeriode = periodeIterator.next();
                var bgPeriode = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode();
                if (bgPeriode.getTom().equals(periodeTomDato)) {
                    oppdaterPeriodeÅrsakForNestePeriode(eksisterendePerioder, periodeIterator, periodeårsak);
                } else if (bgPeriode.inneholder(periodeTomDato)) {
                    splitBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, periodeTomDato, periodeårsak);
                }
            }
        });
    }

    private static void oppdaterPeriodeÅrsakForNestePeriode(List<BeregningsgrunnlagPeriode> eksisterendePerioder, ListIterator<BeregningsgrunnlagPeriode> periodeIterator, PeriodeÅrsak nyPeriodeÅrsak) {
        if (periodeIterator.hasNext()) {
            var nestePeriode = eksisterendePerioder.get(periodeIterator.nextIndex());
            BeregningsgrunnlagPeriode.builder(nestePeriode)
                .leggTilPeriodeÅrsak(nyPeriodeÅrsak)
                .build();
        }
    }

    public static BeregningsgrunnlagPeriode splitBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, LocalDate nyPeriodeTom, PeriodeÅrsak periodeÅrsak) {
        var eksisterendePeriodeTom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode().getTom();
        BeregningsgrunnlagPeriode.builder(beregningsgrunnlagPeriode)
            .medPeriode(Periode.of(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriode().getFom(), nyPeriodeTom))
            .build();
        var nyPeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(nyPeriodeTom.plusDays(1), eksisterendePeriodeTom))
            .leggTilPeriodeÅrsak(periodeÅrsak)
            .build();
        KopierBeregningsgrunnlagUtil.kopierBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode, nyPeriode);
        leggTilPeriodeIBeregningsgrunnlag(beregningsgrunnlagPeriode.getBeregningsgrunnlag(), nyPeriode);
        return nyPeriode;
    }

    private static void leggTilPeriodeIBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagPeriode nyPeriode) {
        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medBeregningsgrunnlagPeriode(nyPeriode)
            .build();
    }
}
