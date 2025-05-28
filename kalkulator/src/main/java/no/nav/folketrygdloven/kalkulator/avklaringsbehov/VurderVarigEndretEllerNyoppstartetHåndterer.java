package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;


public class VurderVarigEndretEllerNyoppstartetHåndterer {


    private VurderVarigEndretEllerNyoppstartetHåndterer() {
        // Skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(BeregningsgrunnlagInput input, Integer bruttoBeregningsgrunnlag, AktivitetStatus aktivitetstatus) {
        if (bruttoBeregningsgrunnlag != null) {
            var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
            var bgPerioder = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
            for (var bgPeriode : bgPerioder) {
                var bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> aktivitetstatus.equals(bpsa.getAktivitetStatus()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Mangler BeregningsgrunnlagPrStatusOgAndel + " + aktivitetstatus + " for kobling " + input.getKoblingReferanse().getKoblingId()));

                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(bgAndel)
                    .medOverstyrtPrÅr(Beløp.fra(bruttoBeregningsgrunnlag));
            }
            return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FORESLÅTT_DEL_2_UT);
        } else {
            // Ingen endring
            var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder
                    .oppdatere(input.getBeregningsgrunnlagGrunnlag());
            return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FORESLÅTT_DEL_2_UT);
        }
    }

}
