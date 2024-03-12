package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class AvklaringsbehovUtlederRepresentererStortinget {

    public static final String STORTINGET = "874707112";

    public static boolean skalVurderePeriodeForStortingsrepresentasjon(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                       List<Intervall> forlengelseperioder) {

        if (!KonfigurasjonVerdi.instance().get("AKSJONSPUNKT_VURDER_STORTINGET", false)) {
            return false;
        }

        var bg = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));

        var tilVurderingTjeneste = new PerioderTilVurderingTjeneste(forlengelseperioder, bg);
        var perioderSomSkalVurderes = bg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> tilVurderingTjeneste.erTilVurdering(p.getPeriode()))
                .collect(Collectors.toList());

        return perioderSomSkalVurderes.stream().anyMatch(p -> representererStortingetIPeriode(p.getPeriode(), iayGrunnlag));
    }

    private static boolean representererStortingetIPeriode(Intervall periode, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var storingetFrilansArbeid = iayGrunnlag.getAktørArbeidFraRegister().stream()
                .flatMap(a -> a.hentAlleYrkesaktiviteter().stream())
                .filter(ya -> ya.getArbeidType().equals(ArbeidType.FRILANSER_OPPDRAGSTAKER))
                .filter(ya -> ya.getArbeidsgiver().getIdentifikator().equals(STORTINGET))
                .collect(Collectors.toList());

        return storingetFrilansArbeid.stream()
                .anyMatch(ya -> ya.getAlleAnsettelsesperioder()
                        .stream().anyMatch(ap -> ap.getPeriode().overlapper(periode)));

    }


}
