package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BeregningsaktivitetLagreDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetNøkkel;

class SaksbehandletBeregningsaktivitetTjeneste {
    private SaksbehandletBeregningsaktivitetTjeneste() {
        // skjul public constructor
    }

    static BeregningAktivitetAggregatDto lagSaksbehandletVersjon(BeregningAktivitetAggregatDto registerAktiviteter, List<BeregningsaktivitetLagreDto> handlingListe) {
        BeregningAktivitetAggregatDto.Builder saksbehandletBuilder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(registerAktiviteter.getSkjæringstidspunktOpptjening());
        registerAktiviteter.getBeregningAktiviteter().stream()
            .filter(ba -> !skalFjernes(handlingListe, ba))
            .forEach(ba -> saksbehandletBuilder.leggTilAktivitet(new BeregningAktivitetDto(ba)));
        return saksbehandletBuilder.build();
    }

    private static boolean skalFjernes(List<BeregningsaktivitetLagreDto> handlingListe, BeregningAktivitetDto beregningAktivitet) {
        BeregningAktivitetNøkkel nøkkel = beregningAktivitet.getNøkkel();
        return handlingListe.stream()
            .filter(baDto -> Objects.equals(baDto.getNøkkel(), nøkkel))
            .anyMatch(baDto -> !baDto.getSkalBrukes());
    }
}
