package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelBeregningsgrunnlagTilfelleInput;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelBeregningsgrunnlagTilfelleTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov.FordelingTilfelle;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class ManuellBehandlingRefusjonGraderingDtoTjeneste {

    private ManuellBehandlingRefusjonGraderingDtoTjeneste() {
        // Skjul
    }

    public static boolean skalSaksbehandlerRedigereInntekt(BeregningsgrunnlagGrunnlagDto grunnlag,
                                                           AktivitetGradering aktivitetGradering,
                                                           BeregningsgrunnlagPeriodeDto periode,
                                                           List<BeregningsgrunnlagPeriodeDto> perioder,
                                                           Collection<InntektsmeldingDto> inntektsmeldinger,
                                                           List<Intervall> forlengelseperioder) {
        boolean grunnetTidligerePerioder = skalRedigereGrunnetTidligerePerioder(grunnlag, aktivitetGradering, periode, perioder, inntektsmeldinger, forlengelseperioder);
        if (grunnetTidligerePerioder) {
            return true;
        }
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = utledTilfellerForAndelerIPeriode(
                grunnlag,
                aktivitetGradering,
                periode, inntektsmeldinger, forlengelseperioder);
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> andelLiggerITilfelleMap(andelFraSteg, periodeTilfelleMap));
    }

    public static boolean skalRedigereGrunnetTidligerePerioder(BeregningsgrunnlagGrunnlagDto grunnlag, AktivitetGradering aktivitetGradering,
                                                               BeregningsgrunnlagPeriodeDto periode, List<BeregningsgrunnlagPeriodeDto> perioder,
                                                               Collection<InntektsmeldingDto> inntektsmeldinger, List<Intervall> forlengelseperioder) {
        return perioder.stream()
                .filter(p -> p.getBeregningsgrunnlagPeriodeFom().isBefore(periode.getBeregningsgrunnlagPeriodeFom()))
                .flatMap(p -> utledTilfellerForAndelerIPeriode(grunnlag, aktivitetGradering, p, inntektsmeldinger, forlengelseperioder).values().stream())
                .anyMatch(tilfelle -> tilfelle.equals(FordelingTilfelle.GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0)
                        || tilfelle.equals(FordelingTilfelle.FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0));
    }

    public static boolean skalSaksbehandlerRedigereRefusjon(BeregningsgrunnlagGrunnlagDto grunnlag,
                                                            AktivitetGradering aktivitetGradering,
                                                            BeregningsgrunnlagPeriodeDto periode,
                                                            Collection<InntektsmeldingDto> inntektsmeldinger,
                                                            Beløp grunnbeløp, List<Intervall> forlengelseperioder) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap = utledTilfellerForAndelerIPeriode(grunnlag, aktivitetGradering,
                periode, inntektsmeldinger, forlengelseperioder);
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().anyMatch(andelFraSteg -> andelLiggerITilfelleMap(andelFraSteg, periodeTilfelleMap)
                && RefusjonDtoTjeneste.skalKunneEndreRefusjon(andelFraSteg, periode, aktivitetGradering, grunnbeløp));
    }

    private static Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> utledTilfellerForAndelerIPeriode(BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                                                                 AktivitetGradering aktivitetGradering,
                                                                                                                 BeregningsgrunnlagPeriodeDto periode,
                                                                                                                 Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                                                                 List<Intervall> forlengelseperioder) {
        var fordelingInput = new FordelBeregningsgrunnlagTilfelleInput(grunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null), aktivitetGradering, inntektsmeldinger, forlengelseperioder);
        return FordelBeregningsgrunnlagTilfelleTjeneste.vurderManuellBehandlingForPeriode(periode, fordelingInput);
    }

    private static boolean andelLiggerITilfelleMap(BeregningsgrunnlagPrStatusOgAndelDto andelFraSteg,
                                                   Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> periodeTilfelleMap) {
        return periodeTilfelleMap.keySet().stream().anyMatch(key -> Objects.equals(key.getAndelsnr(), andelFraSteg.getAndelsnr()));
    }

}
