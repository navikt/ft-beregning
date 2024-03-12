package no.nav.folketrygdloven.kalkulator.adapter.util;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;

import java.time.LocalDate;
import java.util.Optional;

public class Dekningsgradtjeneste {
    private Dekningsgradtjeneste() {
        // Skjuler default konstruktør
    }

    public static int finnDekningsgradProsentverdi(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, Optional<LocalDate> datoForDekningsgrad) {
        var dekningsgrad = finnDekningsgrad(ytelsespesifiktGrunnlag, datoForDekningsgrad);
        return switch(dekningsgrad) {
            case DEKNINGSGRAD_60 -> 60;
            case DEKNINGSGRAD_65 -> 65;
            case DEKNINGSGRAD_70 -> 70;
            case DEKNINGSGRAD_80 -> 80;
            case DEKNINGSGRAD_100 -> 100;
        };
    }

    public static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad mapTilDekningsgradRegel(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, Optional<LocalDate> datoForDekningsgrad) {
        var dekningsgrad = finnDekningsgrad(ytelsespesifiktGrunnlag, datoForDekningsgrad);
        return switch (dekningsgrad) {
            case DEKNINGSGRAD_60 -> no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad.DEKNINGSGRAD_60;
            case DEKNINGSGRAD_65 -> no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad.DEKNINGSGRAD_65;
            case DEKNINGSGRAD_70 -> no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad.DEKNINGSGRAD_70;
            case DEKNINGSGRAD_80 -> no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad.DEKNINGSGRAD_80;
            case DEKNINGSGRAD_100 -> no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad.DEKNINGSGRAD_100;
        };
    }

    private static Dekningsgrad finnDekningsgrad(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, Optional<LocalDate> datoForDekningsgrad) {
        if (ytelsespesifiktGrunnlag instanceof ForeldrepengerGrunnlag fg) {
            return fg.getDekningsgrad();
        } else if (ytelsespesifiktGrunnlag instanceof FrisinnGrunnlag fg && datoForDekningsgrad.isPresent()) {
            return fg.getDekningsgradForDato(datoForDekningsgrad.get());
        }
        // Returnerer 100 som default, men ideelt sett burde ytelser uten dekningsgrad få egen modell der det ikke er påkrevd
        return Dekningsgrad.DEKNINGSGRAD_100;
    }
}
