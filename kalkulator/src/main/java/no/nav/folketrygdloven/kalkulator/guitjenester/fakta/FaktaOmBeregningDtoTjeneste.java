package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger.SaksopplysningerTjeneste.lagSaksopplysninger;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public class FaktaOmBeregningDtoTjeneste {

    public FaktaOmBeregningDtoTjeneste() {
        // CDI
    }

    // TODO (Denne burde splittes i ein del som krever bg og ein del som ikkje krever det)
    public Optional<FaktaOmBeregningDto> lagDto(BeregningsgrunnlagGUIInput input) {
        FaktaOmBeregningDto faktaOmBeregningDto = new FaktaOmBeregningDto();
        var grunnlagEntitet = input.getBeregningsgrunnlagGrunnlag();
        BeregningAktivitetAggregatDto registerAktivitetAggregat = Optional.ofNullable(grunnlagEntitet.getRegisterAktiviteter())
                .orElse(grunnlagEntitet.getGjeldendeAktiviteter());
        Optional<BeregningAktivitetAggregatDto> saksbehandletAktivitetAggregat = grunnlagEntitet.getOverstyrteEllerSaksbehandletAktiviteter();
        Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon = input.getIayGrunnlag().getArbeidsforholdInformasjon();
        AvklarAktiviteterDtoTjeneste.lagAvklarAktiviteterDto(registerAktivitetAggregat,
                saksbehandletAktivitetAggregat, arbeidsforholdInformasjon, faktaOmBeregningDto);

        // Denne delen krever Beregningsgrunnlag
        var beregningsgrunnlag = grunnlagEntitet.getBeregningsgrunnlagHvisFinnes().orElse(null);
        if (beregningsgrunnlag != null && !beregningsgrunnlag.getBeregningsgrunnlagPerioder().isEmpty()) {
            faktaOmBeregningDto.setAndelerForFaktaOmBeregning(AndelerForFaktaOmBeregningTjeneste.lagAndelerForFaktaOmBeregning(input));
            faktaOmBeregningDto.setSaksopplysninger(lagSaksopplysninger(input));
            if (skalVurdereFaktaForATFL(beregningsgrunnlag)) {
                var tilfeller = new HashSet<>(beregningsgrunnlag.getFaktaOmBeregningTilfeller()).stream()
                        .sorted(Comparator.comparing(FaktaOmBeregningTilfelle::name))
                        .toList();
                faktaOmBeregningDto.setFaktaOmBeregningTilfeller(tilfeller);
                utledDtoerForTilfeller(tilfeller, input, faktaOmBeregningDto);
            }
        }
        return Optional.of(faktaOmBeregningDto);
    }

    private boolean skalVurdereFaktaForATFL(BeregningsgrunnlagDto beregningsgrunnlag) {
        return !beregningsgrunnlag.getFaktaOmBeregningTilfeller().isEmpty();
    }

    private void utledDtoerForTilfeller(List<FaktaOmBeregningTilfelle> tilfeller, BeregningsgrunnlagGUIInput input,
                                        FaktaOmBeregningDto faktaOmBeregningDto) {
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL)) {
            new NyOppstartetFLDtoTjeneste().lagDto(input, faktaOmBeregningDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
            new VurderATFLISammeOrgDtoTjeneste().lagDto(input, faktaOmBeregningDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.FASTSETT_BG_KUN_YTELSE)) {
            new KunYtelseDtoTjeneste().lagDto(input, faktaOmBeregningDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD)) {
            new KortvarigeArbeidsforholdDtoTjeneste().lagDto(input, faktaOmBeregningDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING)) {
            new VurderLønnsendringDtoTjeneste().lagDto(input, faktaOmBeregningDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_MOTTAR_YTELSE)) {
            new VurderMottarYtelseDtoTjeneste().lagDto(input, faktaOmBeregningDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_BESTEBEREGNING) || tilfeller.contains(FaktaOmBeregningTilfelle.FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE)) {
            new VurderBesteberegningTilfelleDtoTjeneste().lagDto(input, faktaOmBeregningDto);
        }
        if (tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT)) {
            new VurderRefusjonTilfelleDtoTjeneste().lagDto(input, faktaOmBeregningDto);
        }
        new VurderMilitærDtoTjeneste().lagDto(input, faktaOmBeregningDto);
    }
}
