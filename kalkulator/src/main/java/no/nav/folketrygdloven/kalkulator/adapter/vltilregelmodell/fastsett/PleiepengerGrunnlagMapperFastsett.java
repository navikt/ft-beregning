package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.psb.PleiepengerGrunnlagFastsettGrenseverdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerNærståendeGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.PleiepengerSyktBarnGrunnlag;

public class PleiepengerGrunnlagMapperFastsett {

    public YtelsesSpesifiktGrunnlag map(BeregningsgrunnlagInput input) {
        if ((input.getYtelsespesifiktGrunnlag() instanceof PleiepengerSyktBarnGrunnlag pleiepengerGrunnlag)) {
            var pleiepengerGrunnlagFastsettGrenseverdi = PleiepengerGrunnlagFastsettGrenseverdi.forSyktBarn();
            pleiepengerGrunnlagFastsettGrenseverdi.setStartdatoNyeGraderingsregler(pleiepengerGrunnlag.getTilkommetInntektHensyntasFom().orElse(null));
            return pleiepengerGrunnlagFastsettGrenseverdi;
        } else if (input.getYtelsespesifiktGrunnlag() instanceof PleiepengerNærståendeGrunnlag pleiepengerGrunnlag) {
            var pleiepengerGrunnlagFastsettGrenseverdi = PleiepengerGrunnlagFastsettGrenseverdi.forNærstående();
            pleiepengerGrunnlagFastsettGrenseverdi.setStartdatoNyeGraderingsregler(pleiepengerGrunnlag.getTilkommetInntektHensyntasFom().orElse(null));
            return pleiepengerGrunnlagFastsettGrenseverdi;
        }
        throw new IllegalStateException("Hadde ikke sykt barn eller nærstående grunnlag");
    }

}
