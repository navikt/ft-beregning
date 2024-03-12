package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

/**
 * Tjeneste som utleder FordelingTilfelle for å manuelt fordele beregningsgrunnlaget
 */
public final class FordelBeregningsgrunnlagTilfelleTjeneste {

    private FordelBeregningsgrunnlagTilfelleTjeneste() {
        // Skjuler default konstruktør
    }

    public static List<Intervall> finnPerioderMedBehovForManuellVurdering(FordelBeregningsgrunnlagTilfelleInput input) {
        var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(input.getForlengelseperioder(), input.getBeregningsgrunnlag());
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .filter(p -> perioderTilVurderingTjeneste.erTilVurdering(p.getPeriode()))
                .filter(p -> !vurderManuellBehandlingForPeriode(p, input).isEmpty())
                .map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .collect(Collectors.toList());
    }

    public static Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> vurderManuellBehandlingForPeriode(BeregningsgrunnlagPeriodeDto periode,
                                                                                                                 FordelBeregningsgrunnlagTilfelleInput input) {
        Map<BeregningsgrunnlagPrStatusOgAndelDto, FordelingTilfelle> andelTilfelleMap = new HashMap<>();
        for (BeregningsgrunnlagPrStatusOgAndelDto andel : periode.getBeregningsgrunnlagPrStatusOgAndelList()) {
            Optional<FordelingTilfelle> tilfelle = utledTilfelleForAndel(periode, input, andel);
            tilfelle.ifPresent(fordelingTilfelle -> andelTilfelleMap.put(andel, fordelingTilfelle));
        }
        return andelTilfelleMap;
    }

    private static Optional<FordelingTilfelle> utledTilfelleForAndel(BeregningsgrunnlagPeriodeDto periode, FordelBeregningsgrunnlagTilfelleInput input, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (FordelTilkommetArbeidsforholdTjeneste.erAktivitetLagtTilIPeriodisering(andel) && !erAutomatiskFordelt(andel)) {
            return Optional.of(FordelingTilfelle.NY_AKTIVITET);
        }

        boolean andelHarRefusjonIPerioden = harInnvilgetRefusjon(andel);
        boolean harGraderingIBGPeriode = FordelingGraderingTjeneste.harGraderingForAndelIPeriode(andel, input.getAktivitetGradering(), periode.getPeriode());
        if (!harGraderingIBGPeriode && !andelHarRefusjonIPerioden) {
            return Optional.empty();
        }

        if (FordelingGraderingTjeneste.skalGraderePåAndelUtenBeregningsgrunnlag(andel, harGraderingIBGPeriode)) {
            return Optional.of(FordelingTilfelle.FORESLÅTT_BG_PÅ_GRADERT_ANDEL_ER_0);
        }

        if (harGraderingIBGPeriode && !andelHarRefusjonIPerioden) {
            Beløp grunnbeløp = input.getBeregningsgrunnlag().getGrunnbeløp();
            if (FordelingGraderingTjeneste.gradertAndelVilleBlittAvkortet(andel, grunnbeløp, periode)) {
                return Optional.of(FordelingTilfelle.GRADERT_ANDEL_SOM_VILLE_HA_BLITT_AVKORTET_TIL_0);
            }
            boolean refusjonForPeriodeOverstiger6G = grunnbeløp.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi()).compareTo(finnTotalRefusjonPrÅr(periode)) <= 0;
            if (refusjonForPeriodeOverstiger6G) {
                return Optional.of(FordelingTilfelle.TOTALT_REFUSJONSKRAV_STØRRE_ENN_6G);
            }
        }
        return Optional.empty();
    }

    private static Boolean harInnvilgetRefusjon(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getInnvilgetRefusjonskravPrÅr)
                .map(r -> r.compareTo(Beløp.ZERO) > 0).orElse(false);
    }

    private static Beløp finnTotalRefusjonPrÅr(BeregningsgrunnlagPeriodeDto periode) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().flatMap(a -> a.getBgAndelArbeidsforhold().stream())
                .map(BGAndelArbeidsforholdDto::getInnvilgetRefusjonskravPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private static boolean erAutomatiskFordelt(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return erInntektskategoriSatt(andel) && andel.getFordeltPrÅr() != null;
    }

    private static boolean erInntektskategoriSatt(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getGjeldendeInntektskategori() != null && !andel.getGjeldendeInntektskategori().equals(Inntektskategori.UDEFINERT);
    }
}
