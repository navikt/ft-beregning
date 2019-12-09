package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

public class IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger {
    private IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger() {
        // skjul public constructor
    }

    public static Set<PeriodeSplittData> identifiser(AndelGradering endringISøktYtelse) {
        Set<PeriodeSplittData> set = new HashSet<>();
        List<Gradering> graderinger = endringISøktYtelse.getGraderinger();
        for (int i = 0; i < graderinger.size(); i++) {
            Gradering curr = graderinger.get(i);
            if (i > 0) {
                Gradering prev = graderinger.get(i - 1);
                if (curr.getUtbetalingsprosent().compareTo(prev.getUtbetalingsprosent()) != 0) {
                    PeriodeSplittData periodeSplitt = lagPeriodeSplitt(curr.getFom());
                    set.add(periodeSplitt);
                }
            } else {
                if (curr.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) != 0) {
                    PeriodeSplittData periodeSplitt = lagPeriodeSplitt(curr.getFom());
                    set.add(periodeSplitt);
                }
            }
        }
        return set;
    }

    private static PeriodeSplittData lagPeriodeSplitt(LocalDate fom) {
        return PeriodeSplittData.builder()
            .medPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR)
            .medFom(fom)
            .build();
    }
}
