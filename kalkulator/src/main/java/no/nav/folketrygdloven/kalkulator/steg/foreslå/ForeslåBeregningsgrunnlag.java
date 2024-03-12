package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.BeregningsgrunnlagVerifiserer;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class ForeslåBeregningsgrunnlag {

    private final MapBeregningsgrunnlagFraVLTilRegel mapBeregningsgrunnlagFraVLTilRegel = new MapBeregningsgrunnlagFraVLTilRegel();
    private final MapBeregningsgrunnlagFraRegelTilVL mapBeregningsgrunnlagFraRegelTilVL = new MapBeregningsgrunnlagFraRegelTilVL();

    public BeregningsgrunnlagRegelResultat foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input) {
        BeregningsgrunnlagGrunnlagDto grunnlag = input.getBeregningsgrunnlagGrunnlag();

        // Oversetter initielt Beregningsgrunnlag -> regelmodell
        Beregningsgrunnlag regelmodellBeregningsgrunnlag = mapBeregningsgrunnlagFraVLTilRegel.map(input, grunnlag);
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlag.getBeregningsgrunnlagHvisFinnes()
                .orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her"));
        splittPerioder(input, regelmodellBeregningsgrunnlag, beregningsgrunnlag, input.getBeregningsgrunnlagGrunnlag().getFaktaAggregat());
        List<RegelResultat> regelResultater = kjørRegelForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag);

        // Oversett endelig resultat av regelmodell til foreslått Beregningsgrunnlag  (+ spore input -> evaluation)
        BeregningsgrunnlagDto foreslåttBeregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL.mapForeslåBeregningsgrunnlag(regelmodellBeregningsgrunnlag, beregningsgrunnlag);
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = utledAvklaringsbehov(input, regelResultater);

        verifiserBeregningsgrunnlag(foreslåttBeregningsgrunnlag, input);

        List<RegelSporingPeriode> regelsporinger = MapRegelSporingFraRegelTilVL.mapRegelsporingPerioder(
                regelResultater,
                foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toList()), BeregningsgrunnlagPeriodeRegelType.FORESLÅ);
        return new BeregningsgrunnlagRegelResultat(foreslåttBeregningsgrunnlag, avklaringsbehov,
                new RegelSporingAggregat(regelsporinger));
    }

    protected void verifiserBeregningsgrunnlag(BeregningsgrunnlagDto foreslåttBeregningsgrunnlag, ForeslåBeregningsgrunnlagInput input) {
        BeregningsgrunnlagVerifiserer.verifiserForeslåttBeregningsgrunnlag(foreslåttBeregningsgrunnlag);
    }

    protected void splittPerioder(BeregningsgrunnlagInput input,
                                  Beregningsgrunnlag regelmodellBeregningsgrunnlag,
                                  BeregningsgrunnlagDto beregningsgrunnlag,
                                  Optional<FaktaAggregatDto> faktaAggregat) {
        opprettPerioderForKortvarigeArbeidsforhold(regelmodellBeregningsgrunnlag,
                beregningsgrunnlag, input.getIayGrunnlag(), faktaAggregat);
    }

    protected List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input, List<RegelResultat> regelResultater) {
        return AvklaringsbehovUtlederForeslåBeregning.utledAvklaringsbehov(input, regelResultater);
    }

    protected List<RegelResultat> kjørRegelForeslåBeregningsgrunnlag(Beregningsgrunnlag regelmodellBeregningsgrunnlag) {
        // Evaluerer hver BeregningsgrunnlagPeriode fra initielt Beregningsgrunnlag
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : regelmodellBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            regelResultater.add(KalkulusRegler.foreslåBeregningsgrunnlag(periode));
        }
        return regelResultater;
    }

    private void opprettPerioderForKortvarigeArbeidsforhold(Beregningsgrunnlag regelBeregningsgrunnlag,
                                                            BeregningsgrunnlagDto vlBeregningsgrunnlag,
                                                            InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                            Optional<FaktaAggregatDto> faktaAggregat) {
        var filter = getYrkesaktivitetFilter(iayGrunnlag);
        Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> kortvarigeAktiviteter = KortvarigArbeidsforholdTjeneste.hentAndelerForKortvarigeArbeidsforhold(vlBeregningsgrunnlag, iayGrunnlag);
        kortvarigeAktiviteter.entrySet().stream()
                .filter(entry -> entry.getKey().getBgAndelArbeidsforhold()
                        .filter(a -> faktaAggregat.flatMap(fa -> fa.getFaktaArbeidsforhold(a))
                                .map(FaktaArbeidsforholdDto::getErTidsbegrensetVurdering)
                                .orElse(false)).isPresent())
                .map(Map.Entry::getValue)
                .forEach(ya -> SplittBGPerioder.splitt(regelBeregningsgrunnlag, finnAnsettelsesperioder(filter, ya), PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET));
    }

    private List<Intervall> finnAnsettelsesperioder(YrkesaktivitetFilterDto filter, YrkesaktivitetDto ya) {
        return filter.getAnsettelsesPerioder(ya).stream().map(AktivitetsAvtaleDto::getPeriode).collect(Collectors.toList());
    }

    private YrkesaktivitetFilterDto getYrkesaktivitetFilter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
    }

}
