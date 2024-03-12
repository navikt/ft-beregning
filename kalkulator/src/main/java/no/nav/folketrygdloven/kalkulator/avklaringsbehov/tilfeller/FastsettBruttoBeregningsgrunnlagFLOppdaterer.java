package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettMånedsinntektFLDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;


class FastsettBruttoBeregningsgrunnlagFLOppdaterer {

    private FastsettBruttoBeregningsgrunnlagFLOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        FastsettMånedsinntektFLDto fastsettMånedsinntektFLDto = dto.getFastsettMaanedsinntektFL();
        Integer frilansinntekt = fastsettMånedsinntektFLDto.getMaanedsinntekt();
        var årsinntektFL = Beløp.fra(frilansinntekt).multipliser(KonfigTjeneste.getMånederIÅr());
        List<BeregningsgrunnlagPeriodeDto> bgPerioder = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriodeDto bgPeriode : bgPerioder) {
            BeregningsgrunnlagPrStatusOgAndelDto bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bpsa -> AktivitetStatus.FRILANSER.equals(bpsa.getAktivitetStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Mangler BeregningsgrunnlagPrStatusOgAndel[FRILANS] for behandling " + input.getKoblingReferanse().getId()));
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                .medBeregnetPrÅr(årsinntektFL)
                .medFastsattAvSaksbehandler(true);
        }
    }

}
