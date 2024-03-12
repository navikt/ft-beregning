package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public interface FinnArbeidsprosenter {

    List<BigDecimal> finnArbeidsprosenterIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, Intervall periode);

}
