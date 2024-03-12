package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller.FaktaOmBeregningTilfellerOppdaterer;
import no.nav.folketrygdloven.kalkulator.felles.MatchBeregningsgrunnlagTjeneste;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class BeregningFaktaOgOverstyringHåndterer {

    public BeregningFaktaOgOverstyringHåndterer() {
        // For CDI
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(HåndterBeregningsgrunnlagInput input, FaktaBeregningLagreDto faktaDto) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());

        Optional<BeregningsgrunnlagDto> forrigeBg = input.getForrigeGrunnlagFraHåndteringTilstand().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes);

        FaktaOmBeregningTilfellerOppdaterer.oppdater(faktaDto, forrigeBg, input, grunnlagBuilder);
        return grunnlagBuilder.build(input.getHåndteringTilstand());
    }


    public static BeregningsgrunnlagGrunnlagDto håndterMedOverstyring(HåndterBeregningsgrunnlagInput input, OverstyrBeregningsgrunnlagDto dto) {
        // Overstyring kan kun gjøres på grunnlaget fra 98-steget
        BeregningsgrunnlagGrunnlagDto aktivtGrunnlag = input.getBeregningsgrunnlagGrunnlag();
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(aktivtGrunnlag);

        Optional<BeregningsgrunnlagDto> forrigeBg = input.getForrigeGrunnlagFraHåndteringTilstand().flatMap(BeregningsgrunnlagGrunnlagDto::getBeregningsgrunnlagHvisFinnes);

        FaktaBeregningLagreDto fakta = dto.getFakta();
        if (fakta != null) {
            FaktaOmBeregningTilfellerOppdaterer.oppdater(fakta, forrigeBg, input, grunnlagBuilder);
        }
        BeregningsgrunnlagDto.Builder beregningsgrunnlagBuilder = grunnlagBuilder.getBeregningsgrunnlagBuilder().medOverstyring(true);
        overstyrInntekterPrPeriode(beregningsgrunnlagBuilder.getBeregningsgrunnlag(), forrigeBg, dto.getOverstyrteAndeler());
        finnManglendeAktivitetstatuser(beregningsgrunnlagBuilder.getBeregningsgrunnlag(), dto.getOverstyrteAndeler())
                .forEach(as -> BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(as).build(beregningsgrunnlagBuilder.getBeregningsgrunnlag()));
        return grunnlagBuilder.build(input.getHåndteringTilstand());
    }

    private static List<AktivitetStatus> finnManglendeAktivitetstatuser(BeregningsgrunnlagDto bg, List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler) {
        List<AktivitetStatus> manglendeStatuser = new ArrayList<>();
        overstyrteAndeler.stream().flatMap(a -> a.getAktivitetStatus().stream()).forEach(as -> {
            if (bgManglerStatus(bg.getAktivitetStatuser(), as)) {
                manglendeStatuser.add(as);
            }
        });
        return manglendeStatuser;
    }

    private static boolean bgManglerStatus(List<BeregningsgrunnlagAktivitetStatusDto> statuser, AktivitetStatus statusSomSjekkes) {
        if (statusSomSjekkes.erSelvstendigNæringsdrivende()) {
            return statuser.stream().noneMatch(aks -> aks.getAktivitetStatus().erSelvstendigNæringsdrivende());
        }
        else if (statusSomSjekkes.erFrilanser()) {
            return statuser.stream().noneMatch(aks -> aks.getAktivitetStatus().erFrilanser());
        }
        else if (statusSomSjekkes.erArbeidstaker()) {
            return statuser.stream().noneMatch(aks -> aks.getAktivitetStatus().erArbeidstaker());
        }
        else return statuser.stream().noneMatch(aks -> aks.getAktivitetStatus().equals(statusSomSjekkes));
    }

    private static void overstyrInntekterPrPeriode(BeregningsgrunnlagDto nyttGrunnlag,
                                            Optional<BeregningsgrunnlagDto> forrigeBg,
                                            List<FastsettBeregningsgrunnlagAndelDto> overstyrteAndeler) {
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriodeDto bgPeriode : bgPerioder) {
            Optional<BeregningsgrunnlagPeriodeDto> forrigeBgPeriode = MatchBeregningsgrunnlagTjeneste
                    .finnOverlappendePeriodeOmKunEnFinnes(bgPeriode, forrigeBg);
            overstyrteAndeler
                    .forEach(andelDto ->
                            FastsettFaktaOmBeregningVerdierTjeneste.fastsettVerdierForAndel(andelDto, andelDto.getFastsatteVerdier(), bgPeriode, forrigeBgPeriode));
        }
    }
}
