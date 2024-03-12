package no.nav.folketrygdloven.kalkulator.steg.fordeling.avklaringsbehov;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public final class FordelingGraderingTjeneste {
    private static final List<AktivitetStatus> STATUSER_PRIORITERT_OVER_SN = Arrays.asList(AktivitetStatus.ARBEIDSTAKER,
            AktivitetStatus.FRILANSER,
            AktivitetStatus.DAGPENGER,
            AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
    private FordelingGraderingTjeneste() {
        // SKjuler default
    }

    public static List<AndelGradering.Gradering> hentGraderingerForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering) {
        Optional<AndelGradering> graderingOpt = finnGraderingForAndel(andel, aktivitetGradering);
        if (graderingOpt.isPresent()) {
            AndelGradering gradering = graderingOpt.get();
            return gradering.getGraderinger();
        }
        return Collections.emptyList();
    }

    public static boolean harGraderingForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering, Intervall periode) {
        return !hentGraderingerForAndelIPeriode(andel, aktivitetGradering, periode).isEmpty();
    }

    public static List<AndelGradering.Gradering> hentGraderingerForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering, Intervall periode) {
        Optional<AndelGradering> graderingOpt = finnGraderingForAndel(andel, aktivitetGradering);
        if (graderingOpt.isPresent()) {
            AndelGradering andelGradering = graderingOpt.get();
            return andelGradering.getGraderinger().stream()
                    .filter(gradering -> gradering.getPeriode().overlapper(periode))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static Optional<AndelGradering> finnGraderingForAndel(BeregningsgrunnlagPrStatusOgAndelDto andel, AktivitetGradering aktivitetGradering) {
        if (aktivitetGradering == null) {
            return Optional.empty();
        }
        return aktivitetGradering.getAndelGradering().stream()
                .filter(andelGradering -> andelGradering.matcher(andel))
                .findFirst();
    }

    public static boolean skalGraderePåAndelUtenBeregningsgrunnlag(BeregningsgrunnlagPrStatusOgAndelDto andel, boolean harGraderingIBGPeriode) {
        boolean harIkkjeBeregningsgrunnlag = andel.getBruttoUtenManueltFordelt() == null || andel.getBruttoUtenManueltFordelt().compareTo(Beløp.ZERO) == 0;
        return harGraderingIBGPeriode && harIkkjeBeregningsgrunnlag;
    }

    public static boolean gradertAndelVilleBlittAvkortet(BeregningsgrunnlagPrStatusOgAndelDto andel, Beløp grunnbeløp, BeregningsgrunnlagPeriodeDto periode) {
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            var totaltBgFraStatuserPrioritertOverSN = inntektFraAndelerMedStatus(periode, STATUSER_PRIORITERT_OVER_SN);
            var seksG = grunnbeløp.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
            return totaltBgFraStatuserPrioritertOverSN.compareTo(seksG) >= 0;
        }
        if (andel.getAktivitetStatus().erFrilanser()) {
            var totaltBgFraArbeidstaker = inntektFraAndelerMedStatus(periode, Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));
            var seksG = grunnbeløp.multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
            return totaltBgFraArbeidstaker.compareTo(seksG) >= 0;
        }
        return false;
    }

    private static Beløp inntektFraAndelerMedStatus(BeregningsgrunnlagPeriodeDto periode, List<AktivitetStatus> statuserSomSkalTelles) {
        return periode.getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(a -> statuserSomSkalTelles.contains(a.getAktivitetStatus()))
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoUtenManueltFordelt)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

}
