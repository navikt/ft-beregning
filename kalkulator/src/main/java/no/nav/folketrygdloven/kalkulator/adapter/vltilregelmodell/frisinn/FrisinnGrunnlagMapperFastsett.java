package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.FrisinnPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class FrisinnGrunnlagMapperFastsett {

    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagInput input) {
        if (!(input.getYtelsespesifiktGrunnlag() instanceof no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag)) {
            throw new IllegalStateException("Mangler frisinngrunnlag for frisinnberegning");
        }
        List<FrisinnPeriode> regelPerioder = mapFrisinnPerioder(input);
        return new FrisinnGrunnlag(regelPerioder);
    }

    public static List<FrisinnPeriode> mapFrisinnPerioder(BeregningsgrunnlagInput input) {
        no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag frisinnGrunnlag = input.getYtelsespesifiktGrunnlag();
        return frisinnGrunnlag.getFrisinnPerioder().stream()
                .map(fg -> new FrisinnPeriode(mapPeriode(fg.getPeriode()), fg.getSøkerFrilans(), fg.getSøkerNæring()))
                .collect(Collectors.toList());
    }

    private static Periode mapPeriode(Intervall periode) {
        return new Periode(periode.getFomDato(), periode.getTomDato());
    }
}
