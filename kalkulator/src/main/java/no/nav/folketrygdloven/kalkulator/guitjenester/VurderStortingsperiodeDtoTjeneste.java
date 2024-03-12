package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderRepresentererStortingetDto;

class VurderStortingsperiodeDtoTjeneste {

    private VurderStortingsperiodeDtoTjeneste() {
        // Skjul
    }

    public static Optional<VurderRepresentererStortingetDto> lagDto(BeregningsgrunnlagGUIInput input) {
        boolean harUtførtSteg = !input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagTilstand().erFør(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);
        if (!harUtførtSteg) {
            return Optional.empty();
        }
        if (input.getAvklaringsbehov().stream().noneMatch(a -> a.getDefinisjon().equals(AvklaringsbehovDefinisjon.VURDER_REPRESENTERER_STORTINGET))) {
            return Optional.empty();
        }
        var stortingsperioder = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()
                .stream()
                .filter(p -> p.getPeriodeÅrsaker().stream().anyMatch(på -> på.equals(PeriodeÅrsak.REPRESENTERER_STORTINGET)))
                .map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .collect(Collectors.toSet());

        var harLøstAksjonspunkt = input.getAvklaringsbehov().stream()
                .anyMatch(a -> a.getDefinisjon().equals(AvklaringsbehovDefinisjon.VURDER_REPRESENTERER_STORTINGET) && a.getStatus().equals(AvklaringsbehovStatus.UTFØRT));

        var vurderDto = new VurderRepresentererStortingetDto(
                stortingsperioder.stream().map(Intervall::getFomDato).min(Comparator.naturalOrder()).orElse(null),
                stortingsperioder.stream().map(Intervall::getTomDato).max(Comparator.naturalOrder()).orElse(null),
                harLøstAksjonspunkt ? !stortingsperioder.isEmpty() : null
        );
        return Optional.of(vurderDto);
    }

}
