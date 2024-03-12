package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovutledertjenesteVurderRefusjonK9 {

    private static final Logger log = LoggerFactory.getLogger(AvklaringsbehovutledertjenesteVurderRefusjonK9.class);

    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                       BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {

        VurderRefusjonBeregningsgrunnlagInput vurderInput = (VurderRefusjonBeregningsgrunnlagInput) input;
        List<BeregningsgrunnlagGrunnlagDto> orginaltBGGrunnlag = vurderInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        if (orginaltBGGrunnlag.isEmpty() || orginaltBGGrunnlag.stream().noneMatch(gr -> gr.getBeregningsgrunnlagHvisFinnes().isPresent())) {
            log.info("Fant ingen beregningsgrunnlag fra forrige behandling");
        } else {
            log.info("Fant beregningsgrunnlag fra forrige behandling");
        }

        List<BeregningAvklaringsbehovResultat> avklaringsbehov = new ArrayList<>();

        if (AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            avklaringsbehov.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV));
        }
        return avklaringsbehov;
    }
}
