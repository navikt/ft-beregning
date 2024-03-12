package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsatteAndelerTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsattePerioderTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FastsettBGTidsbegrensetArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;


public class FastsettBGTidsbegrensetArbeidsforholdHåndterer {

    private FastsettBGTidsbegrensetArbeidsforholdHåndterer() {
        // Skjul
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(BeregningsgrunnlagInput input, FastsettBGTidsbegrensetArbeidsforholdDto dto) {
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        List<BeregningsgrunnlagPeriodeDto> perioder = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        List<FastsattePerioderTidsbegrensetDto> fastsattePerioder = dto.getFastsatteTidsbegrensedePerioder()
                .stream()
                .sorted(Comparator.comparing(FastsattePerioderTidsbegrensetDto::getPeriodeFom))
                .toList();
        if (dto.getFrilansInntekt() != null) {
            for (BeregningsgrunnlagPeriodeDto periode : perioder) {
                BeregningsgrunnlagPrStatusOgAndelDto frilansAndel = finnFrilansAndel(periode)
                    .orElseThrow(() -> new IllegalStateException("Mangler frilansandel for behandling " + input.getKoblingReferanse().getKoblingId()));
                BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(frilansAndel).medOverstyrtPrÅr(Beløp.fra(dto.getFrilansInntekt()));
            }
        }
        for (FastsattePerioderTidsbegrensetDto periode: fastsattePerioder) {
            List<BeregningsgrunnlagPeriodeDto> bgPerioderSomSkalFastsettesAvDennePerioden = perioder
                .stream()
                .filter(p -> !p.getBeregningsgrunnlagPeriodeFom().isBefore(periode.getPeriodeFom()))
                .collect(Collectors.toList());
            List<FastsatteAndelerTidsbegrensetDto> fastatteAndeler = periode.getFastsatteTidsbegrensedeAndeler();
            fastatteAndeler.forEach(andel ->
                fastsettAndelerIPeriode(bgPerioderSomSkalFastsettesAvDennePerioden, andel));
        }
        return grunnlagBuilder.build(BeregningsgrunnlagTilstand.FORESLÅTT_UT);
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnFrilansAndel(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> a.getAktivitetStatus().equals(AktivitetStatus.FRILANSER))
            .findFirst();
    }

    private static void fastsettAndelerIPeriode(List<BeregningsgrunnlagPeriodeDto> bgPerioderSomSkalFastsettesAvDennePerioden, FastsatteAndelerTidsbegrensetDto andel) {
        bgPerioderSomSkalFastsettesAvDennePerioden.forEach(p -> {
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> korrektAndel = p.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andel.getAndelsnr())).findFirst();
            korrektAndel.ifPresent(beregningsgrunnlagPrStatusOgAndel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(beregningsgrunnlagPrStatusOgAndel)
                .medOverstyrtPrÅr(Beløp.fra(andel.getBruttoFastsattInntekt())));
        });
    }

}
