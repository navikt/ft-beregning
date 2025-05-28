package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering.Gradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public class GraderingUtenBeregningsgrunnlagTjeneste {

    private GraderingUtenBeregningsgrunnlagTjeneste() {
        // Skjuler default konstruktør
    }

    public static List<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelerMedGraderingUtenBG(BeregningsgrunnlagDto beregningsgrunnlag, AktivitetGradering aktivitetGradering) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> graderingsandelerUtenBG = new ArrayList<>();
        aktivitetGradering.getAndelGradering().forEach(andelGradering -> {
            var andeler = finnTilsvarendeAndelITilsvarendePeriode(andelGradering, beregningsgrunnlag);
            graderingsandelerUtenBG.addAll(andeler);
        });
        return graderingsandelerUtenBG;
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> finnTilsvarendeAndelITilsvarendePeriode(AndelGradering andelGradering, BeregningsgrunnlagDto beregningsgrunnlag) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = new ArrayList<>();
        andelGradering.getGraderinger().forEach(gradering ->{
            var korrektBGPeriode = finnTilsvarendeBGPeriode(gradering, beregningsgrunnlag.getBeregningsgrunnlagPerioder());
            var korrektBGAndel = korrektBGPeriode.flatMap(p -> finnTilsvarendeAndelIPeriode(andelGradering, p));
            if (korrektBGAndel.isPresent() && harIkkeTilkjentBGEtterRedusering(korrektBGAndel.get())) {
                andeler.add(korrektBGAndel.get());
            }
        });
        return andeler;
    }

    private static boolean harIkkeTilkjentBGEtterRedusering(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getRedusertPrÅr() != null && andel.getRedusertPrÅr().compareTo(Beløp.ZERO) <= 0;
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnTilsvarendeAndelIPeriode(AndelGradering andelGradering, BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> bgAndelMatcherGraderingAndel(andel, andelGradering)).findFirst();
    }

    private static boolean bgAndelMatcherGraderingAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AndelGradering andelGradering) {
        if (!andel.getAktivitetStatus().equals(andelGradering.getAktivitetStatus())) {
            return false;
        }
        if (!Objects.equals(andelGradering.getArbeidsgiver(), andel.getArbeidsgiver().orElse(null))) {
            return false;
        }
        return andelGradering.getArbeidsforholdRef().gjelderFor(andel.getArbeidsforholdRef().orElse(InternArbeidsforholdRefDto.nullRef()));
    }

    private static Optional<BeregningsgrunnlagPeriodeDto> finnTilsvarendeBGPeriode(Gradering gradering, List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder) {
        return beregningsgrunnlagPerioder.stream().filter(p -> gradering.getPeriode().overlapper(p.getPeriode())).findFirst();
    }
}
