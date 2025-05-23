package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;


class FastsettBruttoBeregningsgrunnlagFLOppdaterer {

    private FastsettBruttoBeregningsgrunnlagFLOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        var fastsettMånedsinntektFLDto = dto.getFastsettMaanedsinntektFL();
        var frilansinntekt = fastsettMånedsinntektFLDto.getMaanedsinntekt();
        var årsinntektFL = Beløp.fra(frilansinntekt).multipliser(KonfigTjeneste.getMånederIÅr());
        var bgPerioder = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        for (var bgPeriode : bgPerioder) {
            var bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bpsa -> AktivitetStatus.FRILANSER.equals(bpsa.getAktivitetStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Mangler BeregningsgrunnlagPrStatusOgAndel[FRILANS] for behandling " + input.getKoblingReferanse().getId()));
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                .medBeregnetPrÅr(årsinntektFL)
                .medFastsattAvSaksbehandler(true);
        }
    }

}
