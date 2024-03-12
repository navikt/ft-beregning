package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovUtlederFordelBeregning {

    private AvklaringsbehovUtlederFordelBeregning() {
        // Skjul
    }

    public static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovFor(KoblingReferanse ref,
                                                                                 BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                 List<Intervall> forlengelseperioder) {
        var inntektsmeldinger = iayGrunnlag.getInntektsmeldinger().map(InntektsmeldingAggregatDto::getAlleInntektsmeldinger).orElse(Collections.emptyList());
        var perioderTilManuellVurdering = finnPerioderMedTilfellerForFordeling(ref,
                beregningsgrunnlagGrunnlag,
                ytelsespesifiktGrunnlag,
                inntektsmeldinger,
                forlengelseperioder);
        var utledetbehovForAvklaring = new ArrayList<BeregningAvklaringsbehovResultat>();
        if (AvklaringsbehovUtlederRepresentererStortinget.skalVurderePeriodeForStortingsrepresentasjon(beregningsgrunnlagGrunnlag, iayGrunnlag, forlengelseperioder)) {
            utledetbehovForAvklaring.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_REPRESENTERER_STORTINGET));
        }
        if (!perioderTilManuellVurdering.isEmpty()) {
            utledetbehovForAvklaring.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.FORDEL_BG));
        }
        return utledetbehovForAvklaring;
    }

    private static List<Intervall> finnPerioderMedTilfellerForFordeling(@SuppressWarnings("unused") KoblingReferanse ref,
                                                                        BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                        Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                        List<Intervall> forlengelseperioder) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes()
                .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler beregningsgrunnlagGrunnlag"));
        FordelBeregningsgrunnlagTilfelleInput fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(beregningsgrunnlag, finnGraderinger(ytelsespesifiktGrunnlag), inntektsmeldinger, forlengelseperioder);
        return FordelBeregningsgrunnlagTilfelleTjeneste.finnPerioderMedBehovForManuellVurdering(fordelingInput);
    }

    private static AktivitetGradering finnGraderinger(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return ytelsespesifiktGrunnlag instanceof ForeldrepengerGrunnlag ? ((ForeldrepengerGrunnlag) ytelsespesifiktGrunnlag).getAktivitetGradering() : AktivitetGradering.INGEN_GRADERING;
    }
}
