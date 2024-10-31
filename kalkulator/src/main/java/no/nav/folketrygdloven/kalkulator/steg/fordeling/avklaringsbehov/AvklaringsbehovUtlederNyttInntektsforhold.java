package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Ved nye inntektsforhold skal beregningsgrunnlaget graderes mot inntekt.
 * <p>
 * Utleder her om det er potensielle nye inntektsforhold.
 * <p>
 * Se https://navno.sharepoint.com/sites/fag-og-ytelser-arbeid-sykefravarsoppfolging-og-sykepenger/SitePages/%C2%A7-8-13-Graderte-sykepenger.aspx
 */
public class AvklaringsbehovUtlederNyttInntektsforhold {


    public static boolean skalVurdereNyttInntektsforhold(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                         List<Intervall> forlengelseperioder) {

        var bg = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));

        var tilVurderingTjeneste = new PerioderTilVurderingTjeneste(forlengelseperioder, bg);
        return bg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> tilVurderingTjeneste.erTilVurdering(p.getPeriode()))
                .anyMatch(p -> !p.getTilkomneInntekter().isEmpty());
    }

}
