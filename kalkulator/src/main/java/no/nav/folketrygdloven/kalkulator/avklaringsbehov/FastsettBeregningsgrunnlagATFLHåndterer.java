package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBeregningsgrunnlagATFLDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.InntektPrAndelDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;


public class FastsettBeregningsgrunnlagATFLHåndterer {

    private FastsettBeregningsgrunnlagATFLHåndterer() {
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(BeregningsgrunnlagInput input, FastsettBeregningsgrunnlagATFLDto dto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes().orElseThrow(() -> new IllegalStateException("Skal ha beregningsgrunnlag her."));

        var nyttBeregningsgrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();

        var beregningsgrunnlagPerioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        var fastsattInntektListe = dto.getInntektPrAndelList();
        var førstePeriode = beregningsgrunnlagPerioder.get(0);
        var arbeidstakerList = førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
            .collect(Collectors.toList());
        if (fastsattInntektListe != null && !arbeidstakerList.isEmpty()) {
            for (var inntekPrAndel : fastsattInntektListe) {
                var korresponderendeAndelIFørstePeriode = arbeidstakerList.stream()
                    .filter(andel -> andel.getAndelsnr().equals(inntekPrAndel.getAndelsnr()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Fant ingen korresponderende andel med andelsnr " + inntekPrAndel.getAndelsnr() + " i første periode for behandling " + input.getKoblingReferanse().getKoblingId()));
                for (var periode : beregningsgrunnlagPerioder) {
                    var korresponderendeAndelOpt = finnRiktigAndel(korresponderendeAndelIFørstePeriode, periode);
                    korresponderendeAndelOpt.ifPresent(andel-> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(andel)
                        .medOverstyrtPrÅr(Beløp.fra(inntekPrAndel.getInntekt())));
                }
            }
        }
        if (dto.getInntektFrilanser() != null) {
            for (var periode : beregningsgrunnlagPerioder) {
                var frilanserList = periode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream()
                    .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
                    .collect(Collectors.toList());
                frilanserList.forEach(prStatusOgAndel ->
                    BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(prStatusOgAndel).medOverstyrtPrÅr(Beløp.fra(dto.getInntektFrilanser())));
            }
        }

        var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        grunnlagBuilder.medBeregningsgrunnlag(nyttBeregningsgrunnlag);
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FORESLÅTT_UT);
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnRiktigAndel(BeregningsgrunnlagPrStatusOgAndelDto andelIFørstePeriode, BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
            .filter(andel -> andel.equals(andelIFørstePeriode)).findFirst();
    }

}
