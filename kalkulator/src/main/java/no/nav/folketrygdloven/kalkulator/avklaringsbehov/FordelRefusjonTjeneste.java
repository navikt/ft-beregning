package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagAndelDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.fordeling.FordelBeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class FordelRefusjonTjeneste {

    private FordelRefusjonTjeneste() {
        // Skjul
    }

    /**
     * Lager map for å fordele refusjon mellom andeler i periode
     *
     * @param fordeltPeriode periode fra dto
     * @param korrektPeriode periode fra beregningsgrunnlag
     * @return Map fra andel til refusjonsbeløp
     */
    static Map<FordelBeregningsgrunnlagAndelDto, Beløp> getRefusjonPrÅrMap(FordelBeregningsgrunnlagPeriodeDto fordeltPeriode,
                                                                                BeregningsgrunnlagPeriodeDto korrektPeriode) {
        var beløpMap = getTotalbeløpPrArbeidsforhold(fordeltPeriode, korrektPeriode);
        Map<FordelBeregningsgrunnlagAndelDto, Beløp> refusjonMap = new HashMap<>();
        fordeltPeriode.getAndeler()
                .stream()
                .filter(a -> a.getArbeidsgiverId() != null)
                .forEach(fordeltAndel -> {
                    var arbeidsforhold = getArbeidsforholdNøkkel(fordeltAndel);
                    fordelRefusjonTilAndel(beløpMap, refusjonMap, fordeltAndel, arbeidsforhold);
                });
        return refusjonMap;
    }

    private static void fordelRefusjonTilAndel(Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> beløpMap,
                                               Map<FordelBeregningsgrunnlagAndelDto, Beløp> refusjonMap,
                                               FordelBeregningsgrunnlagAndelDto fordeltAndel,
                                               ArbeidsforholdNøkkel arbeidsforhold) {
        var refusjonOgFastsattBeløp = beløpMap.get(arbeidsforhold);
        if (refusjonOgFastsattBeløp.getTotalFastsattBeløpPrÅr().compareTo(Beløp.ZERO) == 0 ||
                refusjonOgFastsattBeløp.getTotalRefusjonPrÅr().compareTo(Beløp.ZERO) == 0) {
            if (fordeltAndel.getFastsatteVerdier().getRefusjonPrÅr() != null) {
                refusjonMap.put(fordeltAndel, Beløp.fra(fordeltAndel.getFastsatteVerdier().getRefusjonPrÅr()));
            }
            return;
        }
        var refusjonPrÅr = getAndelAvTotalRefusjonPrÅr(fordeltAndel, refusjonOgFastsattBeløp);
        refusjonMap.put(fordeltAndel, refusjonPrÅr);
    }

    private static Beløp getAndelAvTotalRefusjonPrÅr(FordelBeregningsgrunnlagAndelDto fordeltAndel,
                                                          RefusjonOgFastsattBeløp refusjonOgFastsattBeløp) {
        var fastsatt = fordeltAndel.getFastsatteVerdier().finnEllerUtregnFastsattBeløpPrÅr();
        var totalFastsatt = refusjonOgFastsattBeløp.getTotalFastsattBeløpPrÅr();
        var totalRefusjon = refusjonOgFastsattBeløp.getTotalRefusjonPrÅr();
        return totalRefusjon.multipliser(fastsatt)
                .divider(totalFastsatt, 10, RoundingMode.HALF_UP);
    }

    private static Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> getTotalbeløpPrArbeidsforhold(FordelBeregningsgrunnlagPeriodeDto fordeltPeriode,
                                                                                                    BeregningsgrunnlagPeriodeDto korrektPeriode) {
        Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap = new HashMap<>();
        fordeltPeriode.getAndeler()
                .stream()
                .filter(a -> a.getArbeidsgiverId() != null)
                .forEach(fordeltAndel -> {
                    leggTilRefusjon(korrektPeriode, arbeidsforholdRefusjonMap, fordeltAndel);
                    leggTilFastsattFordeling(arbeidsforholdRefusjonMap, fordeltAndel);
                });
        return arbeidsforholdRefusjonMap;
    }

    private static void leggTilFastsattFordeling(Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                 FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        var korrektArbeidsforhold = getArbeidsforholdNøkkel(fordeltAndel);
        var fastsattBeløpPrÅr = fordeltAndel.getFastsatteVerdier().finnEllerUtregnFastsattBeløpPrÅr();
        settEllerOppdaterFastsattBeløp(arbeidsforholdRefusjonMap, korrektArbeidsforhold, fastsattBeløpPrÅr);
    }

    private static void settEllerOppdaterFastsattBeløp(Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                       ArbeidsforholdNøkkel arbeidsforhold, Beløp fastsattBeløpPrÅr) {
        if (arbeidsforholdRefusjonMap.containsKey(arbeidsforhold)) {
            RefusjonOgFastsattBeløp nyttBeløp = arbeidsforholdRefusjonMap.get(arbeidsforhold)
                    .leggTilFastsattBeløp(fastsattBeløpPrÅr);
            arbeidsforholdRefusjonMap.put(arbeidsforhold, nyttBeløp);
        } else {
            arbeidsforholdRefusjonMap.put(arbeidsforhold, new RefusjonOgFastsattBeløp(Beløp.ZERO, fastsattBeløpPrÅr));
        }
    }

    private static void leggTilRefusjon(BeregningsgrunnlagPeriodeDto korrektPeriode,
                                        Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                        FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        if (fordeltAndel.getFastsatteVerdier().getRefusjonPrÅr() == null) {
            leggTilForKunEndretFordeling(korrektPeriode, arbeidsforholdRefusjonMap, fordeltAndel);
        } else {
            leggTilForEndretFordelingOgRefusjon(arbeidsforholdRefusjonMap, fordeltAndel);
        }
    }

    private static void leggTilForEndretFordelingOgRefusjon(Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                            FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        var korrektArbeidsforhold = getArbeidsforholdNøkkel(fordeltAndel);
        var refusjonskravPrÅr = Beløp.fra(fordeltAndel.getFastsatteVerdier().getRefusjonPrÅr());
        settEllerOppdaterTotalRefusjon(arbeidsforholdRefusjonMap, korrektArbeidsforhold, refusjonskravPrÅr);
    }

    private static void leggTilForKunEndretFordeling(BeregningsgrunnlagPeriodeDto korrektPeriode,
                                                     Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                     FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        if (!fordeltAndel.erLagtTilAvSaksbehandler()) {
            Optional<BeregningsgrunnlagPrStatusOgAndelDto> korrektAndelOpt = korrektPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(andel -> andel.getAndelsnr().equals(fordeltAndel.getAndelsnr())).findFirst();
            korrektAndelOpt.ifPresent(korrektAndel ->
                    leggTilRefusjonForAndelIGrunnlag(arbeidsforholdRefusjonMap, korrektAndel)
            );
        }
    }

    private static void leggTilRefusjonForAndelIGrunnlag(Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap, BeregningsgrunnlagPrStatusOgAndelDto korrektAndel) {
        korrektAndel.getBgAndelArbeidsforhold().ifPresent(arbeidsforhold -> {
            var refusjonskravPrÅr = Optional.ofNullable(arbeidsforhold.getGjeldendeRefusjonPrÅr()).orElse(Beløp.ZERO);
            settEllerOppdaterTotalRefusjon(arbeidsforholdRefusjonMap,
                    new ArbeidsforholdNøkkel(arbeidsforhold.getArbeidsgiver(), arbeidsforhold.getArbeidsforholdRef()),
                    refusjonskravPrÅr);
        });
    }

    private static void settEllerOppdaterTotalRefusjon(Map<ArbeidsforholdNøkkel, RefusjonOgFastsattBeløp> arbeidsforholdRefusjonMap,
                                                       ArbeidsforholdNøkkel arbeidsforhold,
                                                       Beløp refusjonskravPrÅr) {
        if (arbeidsforholdRefusjonMap.containsKey(arbeidsforhold)) {
            var nyttBeløp = arbeidsforholdRefusjonMap.get(arbeidsforhold)
                    .leggTilRefusjon(refusjonskravPrÅr);
            arbeidsforholdRefusjonMap.put(arbeidsforhold, nyttBeløp);
        } else {
            arbeidsforholdRefusjonMap.put(arbeidsforhold, new RefusjonOgFastsattBeløp(refusjonskravPrÅr));
        }
    }

    private static ArbeidsforholdNøkkel getArbeidsforholdNøkkel(FordelBeregningsgrunnlagAndelDto fordeltAndel) {
        var arbeidsforholdId = fordeltAndel.getArbeidsforholdId();
        var arbeidsgiverId = fordeltAndel.getArbeidsgiverId();
        var arbeidsgiver = OrgNummer.erGyldigOrgnr(arbeidsgiverId) ? Arbeidsgiver.virksomhet(arbeidsgiverId) : Arbeidsgiver.person(new AktørId(arbeidsgiverId));
        return new ArbeidsforholdNøkkel(arbeidsgiver, arbeidsforholdId);
    }


    private record ArbeidsforholdNøkkel(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
    }

}
