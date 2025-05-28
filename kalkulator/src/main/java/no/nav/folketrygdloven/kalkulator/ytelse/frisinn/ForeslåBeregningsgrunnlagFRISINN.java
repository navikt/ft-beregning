package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import static no.nav.folketrygdloven.kalkulator.steg.foreslå.SplittBGPerioder.splitBeregningsgrunnlagPeriode;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.MapRegelSporingFraRegelTilVL;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class ForeslåBeregningsgrunnlagFRISINN {

    private final MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel();
    private final MapBeregningsgrunnlagFraRegelTilVL mapBeregningsgrunnlagFraRegelTilVL = new MapBeregningsgrunnlagFraRegelTilVL();

    public BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input) {
        var grunnlag = input.getBeregningsgrunnlagGrunnlag();

        // Oversetter initielt Beregningsgrunnlag -> regelmodell
        var regelmodellBeregningsgrunnlag = mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
        var beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        splittPerioder(input, regelmodellBeregningsgrunnlag, beregningsgrunnlag, input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat());
        var regelResultater = kjørRegelForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag);

        // Oversett endelig resultat av regelmodell til foreslått Beregningsgrunnlag  (+ spore input -> evaluation)
        var foreslåttBeregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL.mapForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, beregningsgrunnlag);
        var avklaringsbehov = utledAvklaringsbehov(input, regelResultater);

        verifiserBeregningsgrunnlag(foreslåttBeregningsgrunnlag, input);

        var regelsporinger = MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder(
                regelResultater,
                foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList()), BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        return new BeregningsgrunnlagRegelResultat(foreslåttBeregningsgrunnlag, avklaringsbehov,
                new RegelSporingAggregat(regelsporinger));
    }

    protected List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input, List<RegelResultat> regelResultater) {
        return Collections.emptyList();
    }

    protected List<RegelResultat> kjørRegelForeslåBeregningsgrunnlag(Beregningsgrunnlag regelmodellBeregningsgrunnlag) {
        // Evaluerer hver BeregningsgrunnlagPeriode fra initielt Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (var periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            regelResultater.add(KalkulusRegler.foreslåBeregningsgrunnlagFRISINN(periode));
        }
        return regelResultater;
    }

    protected void splittPerioder(BeregningsgrunnlagInput input, Beregningsgrunnlag regelmodellBeregningsgrunnlag, BeregningsgrunnlagDto beregningsgrunnlag, Optional<FaktaAggregatDto> faktaAggregat) {
        List<OppgittPeriodeInntekt> inntektListe = input.getIayGrunnlag().getOppgittOpptjening().stream().flatMap(oo -> oo.getEgenNæring().stream()).collect(Collectors.toList());
        List<OppgittPeriodeInntekt> oppgittFLInntekt = input.getIayGrunnlag().getOppgittOpptjening().stream().flatMap(ofl -> ofl.getFrilans().stream())
                .flatMap(fl -> fl.getOppgittFrilansInntekt().stream()).collect(Collectors.toList());
        inntektListe.addAll(oppgittFLInntekt);
        var inntekterSomSkalFøreTilSPlitt = inntektListe.stream()
                .filter(inntekt -> !inntekt.getPeriode().getFomDato().isBefore(input.getSkjæringstidspunktOpptjening()))
                .collect(Collectors.toList());
        inntekterSomSkalFøreTilSPlitt.forEach(oppgittPeriodeInntekt -> splittForOppgittPeriode(oppgittPeriodeInntekt, regelmodellBeregningsgrunnlag));
    }


    protected void verifiserBeregningsgrunnlag(BeregningsgrunnlagDto foreslåttBeregningsgrunnlag, ForeslåBeregningsgrunnlagInput input) {
        BeregningsgrunnlagVerifisererFRISINN.verifiserForeslåttBeregningsgrunnlag(foreslåttBeregningsgrunnlag);
    }

    private void splittForOppgittPeriode(OppgittPeriodeInntekt oppgittPeriodeInntekt, Beregningsgrunnlag regelmodellBeregningsgrunnlag) {
        var fom = oppgittPeriodeInntekt.getPeriode().getFomDato();
        var tom = oppgittPeriodeInntekt.getPeriode().getTomDato();
        splittVedFom(regelmodellBeregningsgrunnlag, fom);
        splittVedTom(regelmodellBeregningsgrunnlag, tom);
    }


    private void splittVedTom(Beregningsgrunnlag regelmodellBeregningsgrunnlag, LocalDate tom) {
        var periodeSomInneholderTomOpt = regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPeriode().inneholder(tom))
                .findFirst();
        if (periodeSomInneholderTomOpt.isPresent()) {
            var periode = periodeSomInneholderTomOpt.get();
            if (periode.getBeregningsgrunnlagPeriode().getTom().isEqual(tom) && !tom.equals(TIDENES_ENDE)) {
                var nestePeriode = regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                        .filter(p -> p.getBeregningsgrunnlagPeriode().getFom().equals(tom.plusDays(1)))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Forventer å finne neste periode"));
                if (!nestePeriode.getPeriodeÅrsaker().contains(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR)) {
                    BeregningsgrunnlagPeriode.builder(nestePeriode).leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
                }
            } else {
                splitBeregningsgrunnlagPeriode(periode, tom, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
            }
        }
    }

    private void splittVedFom(Beregningsgrunnlag regelmodellBeregningsgrunnlag, LocalDate fom) {
        var periodeSomInneholderFomOpt = regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getBeregningsgrunnlagPeriode().inneholder(fom))
                .findFirst();

        if (periodeSomInneholderFomOpt.isPresent()) {
            var periode = periodeSomInneholderFomOpt.get();
            if (periode.getBeregningsgrunnlagPeriode().getFom().isEqual(fom)) {
                if (!periode.getPeriodeÅrsaker().contains(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR)) {
                    BeregningsgrunnlagPeriode.builder(periode).leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
                }
            } else {
                var nyPeriodeTom = fom.minusDays(1);
                splitBeregningsgrunnlagPeriode(periode, nyPeriodeTom, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
            }
        }
    }

}
