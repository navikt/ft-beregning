package no.nav.folketrygdloven.kalkulator.guitjenester;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingGraderingTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class FinnArbeidsprosenterFP implements FinnArbeidsprosenter {

    @Override
    public List<BigDecimal> finnArbeidsprosenterIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                         YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, Intervall periode) {
        var aktivitetGradering = finnGradering(ytelsespesifiktGrunnlag);
        List<AndelGradering.Gradering> graderingForAndelIPeriode = FordelingGraderingTjeneste.hentGraderingerForAndelIPeriode(andel, aktivitetGradering, periode).stream()
                .sorted().collect(Collectors.toList());

        List<BigDecimal> prosentAndelerIPeriode = new ArrayList<>();
        if (graderingForAndelIPeriode.isEmpty()) {
            leggTilNullProsent(prosentAndelerIPeriode);
            return prosentAndelerIPeriode;
        }
        AndelGradering.Gradering gradering = graderingForAndelIPeriode.get(0);
        prosentAndelerIPeriode.add(gradering.getArbeidstidProsent().verdi());
        if (graderingForAndelIPeriode.size() == 1) {
            if (graderingDekkerHeilePerioden(gradering, periode)) {
                return prosentAndelerIPeriode;
            }
            prosentAndelerIPeriode.add(BigDecimal.ZERO);
            return prosentAndelerIPeriode;
        }

        for (int i = 1; i < graderingForAndelIPeriode.size(); i++) {
            AndelGradering.Gradering nesteGradering = graderingForAndelIPeriode.get(i);
            prosentAndelerIPeriode.add(nesteGradering.getArbeidstidProsent().verdi());
            if (!gradering.getPeriode().getFomDato().isBefore(periode.getTomDato())) {
                break;
            }
            if (gradering.getPeriode().getTomDato().isBefore(nesteGradering.getPeriode().getFomDato()) &&
                    !gradering.getPeriode().getTomDato().equals(nesteGradering.getPeriode().getFomDato().minusDays(1))) {
                leggTilNullProsent(prosentAndelerIPeriode);
            }
            gradering = nesteGradering;
        }

        if (periode.getTomDato() == null || gradering.getPeriode().getTomDato().isBefore(periode.getTomDato())) {
            leggTilNullProsent(prosentAndelerIPeriode);
        }

        return prosentAndelerIPeriode;
    }

    private static void leggTilNullProsent(List<BigDecimal> prosentAndelerIPeriode) {
        if (!prosentAndelerIPeriode.contains(BigDecimal.ZERO)) {
            prosentAndelerIPeriode.add(BigDecimal.ZERO);
        }
    }

    private static boolean graderingDekkerHeilePerioden(AndelGradering.Gradering gradering, Intervall periode) {
        return !gradering.getPeriode().getFomDato().isAfter(periode.getFomDato()) &&
                (gradering.getPeriode().getTomDato().equals(TIDENES_ENDE) ||
                        (periode.getTomDato() != null &&
                                !gradering.getPeriode().getTomDato().isBefore(periode.getTomDato())));
    }

    private static AktivitetGradering finnGradering(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return ytelsespesifiktGrunnlag instanceof ForeldrepengerGrunnlag ? ((ForeldrepengerGrunnlag) ytelsespesifiktGrunnlag).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
    }


}
