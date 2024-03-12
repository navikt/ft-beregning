package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;

public class PerioderTilVurderingTjeneste {

    private final List<Intervall> forlengelseperioder;
    private final List<Intervall> beregningsgrunnlagsperioder;

    public PerioderTilVurderingTjeneste(List<Intervall> forlengelseperioder, BeregningsgrunnlagDto beregningsgrunnlag) {
        this.forlengelseperioder = forlengelseperioder;
        this.beregningsgrunnlagsperioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).toList();
    }

    public PerioderTilVurderingTjeneste(List<Intervall> forlengelseperioder, List<Intervall> beregningsgrunnlagsperioder) {
        this.forlengelseperioder = forlengelseperioder;
        this.beregningsgrunnlagsperioder = beregningsgrunnlagsperioder;
    }


    public boolean erTilVurdering(Intervall periode) {
        return finnPerioderTilVurdering().stream().anyMatch(periode::overlapper);
    }

    public <V> boolean erTilVurdering(LocalDateSegment<V> segment) {
        return erTilVurdering(Intervall.fraOgMedTilOgMed(segment.getFom(), segment.getTom()));
    }


    private List<Intervall> finnPerioderTilVurdering() {
        return forlengelseperioder == null || forlengelseperioder.isEmpty() ? beregningsgrunnlagsperioder :
                filtrerKunForlengelse(beregningsgrunnlagsperioder, forlengelseperioder);
    }

    private List<Intervall> filtrerKunForlengelse(List<Intervall> beregningsgrunnlagsperioder, List<Intervall> forlengelseperioder) {
        return beregningsgrunnlagsperioder.stream().filter(p -> forlengelseperioder.stream().anyMatch(p::overlapper)).toList();
    }


}
