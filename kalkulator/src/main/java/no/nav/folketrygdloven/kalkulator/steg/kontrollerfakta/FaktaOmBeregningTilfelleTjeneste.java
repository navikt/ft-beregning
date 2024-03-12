package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.ArbeidstakerOgFrilanserISammeOrganisasjonTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.EtterlønnSluttpakkeTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.KortvarigArbeidsforholdTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.KunYtelseTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.NyIArbeidslivetTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.NyoppstartetFLTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.VurderLønnsendringTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.VurderMilitærTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.VurderMottarYtelseTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere.VurderRefusjonskravTilfelleUtleder;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.ytelse.fp.VurderBesteberegningTilfelleUtleder;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


/**
 * Utleder hvilke tilfeller (FaktaOmBeregningTilfelle) som inntreffer i fakta om beregning.
 */
public class FaktaOmBeregningTilfelleTjeneste {

    private static final Map<FaktaOmBeregningTilfelle, Set<FagsakYtelseType>> RELEVANT_UTLEDERE = Map.ofEntries(
            Map.entry(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON, Set.of()),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_ETTERLØNN_SLUTTPAKKE, Set.of()),
            Map.entry(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING,
                    Set.of(FagsakYtelseType.FORELDREPENGER, FagsakYtelseType.SVANGERSKAPSPENGER)),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD,
                    Set.of(FagsakYtelseType.FORELDREPENGER, FagsakYtelseType.SVANGERSKAPSPENGER)),
            Map.entry(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE, Set.of()),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET, Set.of()),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL, Set.of()),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING, Set.of()),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_MILITÆR_SIVILTJENESTE, Set.of()),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE, Set.of()),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT,
                    Set.of(FagsakYtelseType.FORELDREPENGER, FagsakYtelseType.SVANGERSKAPSPENGER, FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
                            FagsakYtelseType.OPPLÆRINGSPENGER, FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE)),
            Map.entry(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING, Set.of(FagsakYtelseType.FORELDREPENGER))
    );

    private FaktaOmBeregningTilfelleTjeneste() {
        // For CDI
    }

    /**
     * Finner tilfeller i fakta om beregning som gir grunnlag for manuell behandling.
     *
     * @param input                      Beregningsgrunnlaginput
     * @param beregningsgrunnlagGrunnlag Beregningsgrunnlag
     * @return Liste med tilfeller
     */
    public static List<FaktaOmBeregningTilfelle> finnTilfellerForFellesAvklaringsbehov(FaktaOmBeregningInput input,
                                                                                BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        List<FaktaOmBeregningTilfelle> tilfeller = new ArrayList<>();
        Arrays.stream(FaktaOmBeregningTilfelle.values())
                .filter(fbt -> RELEVANT_UTLEDERE.get(fbt) != null)
                .filter(fbt -> RELEVANT_UTLEDERE.get(fbt).isEmpty() || RELEVANT_UTLEDERE.get(fbt).contains(input.getFagsakYtelseType()))
                .forEach(fbt -> kjørUtleder(fbt, input, beregningsgrunnlagGrunnlag).ifPresent(tilfeller::add));
        return tilfeller;
    }

    private static Optional<FaktaOmBeregningTilfelle> kjørUtleder(FaktaOmBeregningTilfelle tilfelle, FaktaOmBeregningInput input,
                                                                  BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        var utleder = switch (tilfelle) {
            case FASTSETT_BG_KUN_YTELSE -> new KunYtelseTilfelleUtleder();
            case FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING -> new FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder();
            case VURDER_AT_OG_FL_I_SAMME_ORGANISASJON -> new ArbeidstakerOgFrilanserISammeOrganisasjonTilfelleUtleder();
            case VURDER_BESTEBEREGNING -> new VurderBesteberegningTilfelleUtleder();
            case VURDER_ETTERLØNN_SLUTTPAKKE -> new EtterlønnSluttpakkeTilfelleUtleder();
            case VURDER_LØNNSENDRING -> new VurderLønnsendringTilfelleUtleder();
            case VURDER_MILITÆR_SIVILTJENESTE -> new VurderMilitærTilfelleUtleder();
            case VURDER_MOTTAR_YTELSE -> new VurderMottarYtelseTilfelleUtleder();
            case VURDER_NYOPPSTARTET_FL -> new NyoppstartetFLTilfelleUtleder();
            case VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT -> new VurderRefusjonskravTilfelleUtleder();
            case VURDER_SN_NY_I_ARBEIDSLIVET -> new NyIArbeidslivetTilfelleUtleder();
            case VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD -> new KortvarigArbeidsforholdTilfelleUtleder();
            case UDEFINERT, FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE, FASTSETT_BG_ARBEIDSTAKER_UTEN_INNTEKTSMELDING,
                    FASTSETT_ENDRET_BEREGNINGSGRUNNLAG, FASTSETT_ETTERLØNN_SLUTTPAKKE, FASTSETT_MAANEDSINNTEKT_FL,
                    TILSTØTENDE_YTELSE -> throw new IllegalStateException("Utviklerfeil: Kaller kjørutleder med tilfelle " + tilfelle);
        };
        return utleder.utled(input, beregningsgrunnlagGrunnlag);
    }

}
