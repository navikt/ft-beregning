package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


public class FaktaOmBeregningTilfellerOppdaterer {

    private FaktaOmBeregningTilfellerOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto faktaDto,
                         Optional<BeregningsgrunnlagDto> forrigeBg,
                         BeregningsgrunnlagInput input,
                         BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        kjørOppdateringForTilfeller(faktaDto, forrigeBg, input, grunnlagBuilder);
        List<FaktaOmBeregningTilfelle> tilfeller = faktaDto.getFaktaOmBeregningTilfeller();
        settNyeFaktaOmBeregningTilfeller(grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag(), tilfeller);
    }

    private static void kjørOppdateringForTilfeller(FaktaBeregningLagreDto faktaDto,
                                             Optional<BeregningsgrunnlagDto> forrigeBg,
                                             BeregningsgrunnlagInput input,
                                             BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        faktaDto.getFaktaOmBeregningTilfeller()
            .forEach(tilfelle -> kjørOppdateringForTilfelle(tilfelle, faktaDto, forrigeBg, input, grunnlagBuilder));
    }

    private static void kjørOppdateringForTilfelle(FaktaOmBeregningTilfelle tilfelle,
                                            FaktaBeregningLagreDto faktaDto,
                                            Optional<BeregningsgrunnlagDto> forrigeBg,
                                            BeregningsgrunnlagInput input,
                                            BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        switch (tilfelle) {
            case FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE -> FastsettBesteberegningFødendeKvinneOppdaterer.oppdater(faktaDto, forrigeBg, grunnlagBuilder);
            case FASTSETT_BG_KUN_YTELSE -> FastsettBgKunYtelseOppdaterer.oppdater(faktaDto, forrigeBg, grunnlagBuilder);
            case FASTSETT_MAANEDSINNTEKT_FL -> FastsettBruttoBeregningsgrunnlagFLOppdaterer.oppdater(faktaDto, input, grunnlagBuilder);
            case FASTSETT_ETTERLØNN_SLUTTPAKKE -> FastsettEtterlønnSluttpakkeOppdaterer.oppdater(faktaDto, grunnlagBuilder);
            case VURDER_AT_OG_FL_I_SAMME_ORGANISASJON -> FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer.oppdater(faktaDto, grunnlagBuilder);
            case FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING -> FastsettMånedsinntektUtenInntektsmeldingOppdaterer.oppdater(faktaDto, forrigeBg, grunnlagBuilder);
            case VURDER_MOTTAR_YTELSE -> MottarYtelseOppdaterer.oppdater(faktaDto, grunnlagBuilder);
            case VURDER_ETTERLØNN_SLUTTPAKKE -> VurderEtterlønnSluttpakkeOppdaterer.oppdater(faktaDto, grunnlagBuilder);
            case VURDER_LØNNSENDRING -> VurderLønnsendringOppdaterer.oppdater(faktaDto, forrigeBg, input, grunnlagBuilder);
            case VURDER_MILITÆR_SIVILTJENESTE -> VurderMilitærOppdaterer.oppdater(faktaDto, grunnlagBuilder);
            case VURDER_NYOPPSTARTET_FL -> VurderNyoppstartetFLOppdaterer.oppdater(faktaDto, grunnlagBuilder);
            case VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT -> VurderRefusjonTilfelleOppdaterer.oppdater(faktaDto, input, grunnlagBuilder);
            case VURDER_SN_NY_I_ARBEIDSLIVET -> VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer.oppdater(faktaDto, grunnlagBuilder);
            case VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD -> VurderTidsbegrensetArbeidsforholdOppdaterer.oppdater(faktaDto, grunnlagBuilder);
            case UDEFINERT, FASTSETT_BG_ARBEIDSTAKER_UTEN_INNTEKTSMELDING, FASTSETT_ENDRET_BEREGNINGSGRUNNLAG, VURDER_BESTEBEREGNING, TILSTØTENDE_YTELSE -> {
                // NOOP
            }
        }
    }

    private static void settNyeFaktaOmBeregningTilfeller(BeregningsgrunnlagDto nyttBeregningsgrunnlag, List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        List<FaktaOmBeregningTilfelle> utledetTilfeller = nyttBeregningsgrunnlag.getFaktaOmBeregningTilfeller();
        List<FaktaOmBeregningTilfelle> tilfellerLagtTilManuelt = faktaOmBeregningTilfeller.stream()
            .filter(tilfelle -> !utledetTilfeller.contains(tilfelle)).collect(Collectors.toList());
        if (!tilfellerLagtTilManuelt.isEmpty()) {
            BeregningsgrunnlagDto.Builder.oppdater(Optional.of(nyttBeregningsgrunnlag)).leggTilFaktaOmBeregningTilfeller(tilfellerLagtTilManuelt);
        }
    }
}
