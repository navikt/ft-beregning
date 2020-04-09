package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

public class IdentifiserPeriodeÅrsaker {

    private IdentifiserPeriodeÅrsaker() {
        // skjul public constructor
    }

    static IdentifisertePeriodeÅrsaker identifiser(PeriodeModell input) {
        LocalDate skjæringstidspunkt = input.getSkjæringstidspunkt();
        IdentifisertePeriodeÅrsaker map = new IdentifisertePeriodeÅrsaker();
        leggTilPeriodesplitterForEksisterendePerioder(input, map);
        input.getArbeidsforholdOgInntektsmeldinger().forEach(inntektsmelding -> {
            Set<PeriodeSplittData> refusjonPerioder = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(inntektsmelding);
            refusjonPerioder.forEach(map::leggTilPeriodeÅrsak);
        });

        Map<ArbeidsforholdOgInntektsmelding, List<Refusjonskrav>> refusjonskravPrArbeidsgiver = GrupperPeriodeÅrsakerPerArbeidsgiver.grupper(map.getPeriodeMap());
        input.getArbeidsforholdOgInntektsmeldinger().forEach(inntektsmelding -> {
            List<Refusjonskrav> gyldigeRefusjonskrav = refusjonskravPrArbeidsgiver.getOrDefault(inntektsmelding, List.of());
            inntektsmelding.setGyldigeRefusjonskrav(gyldigeRefusjonskrav);
        });

        input.getArbeidsforholdOgInntektsmeldinger().forEach(inntektsmelding -> {
            Set<PeriodeSplittData> naturalYtelsePerioder = IdentifiserPerioderForNaturalytelse.identifiserPerioderForNaturalytelse(inntektsmelding, skjæringstidspunkt);
            naturalYtelsePerioder.forEach(map::leggTilPeriodeÅrsak);
            Set<PeriodeSplittData> graderingPerioder = IdentifiserPerioderForGradering.identifiser(input, inntektsmelding);
            graderingPerioder.forEach(map::leggTilPeriodeÅrsak);
        });
        input.getAndelGraderinger().forEach(andelGradering -> {
            Set<PeriodeSplittData> graderingPerioder = IdentifiserPerioderForGradering.identifiser(input, andelGradering);
            graderingPerioder.forEach(map::leggTilPeriodeÅrsak);
        });

        input.getEndringerISøktYtelse().forEach(endringISøktYtelse -> {
            Set<PeriodeSplittData> endringerISøktYtelse = IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger.identifiser(endringISøktYtelse);
            endringerISøktYtelse.forEach(map::leggTilPeriodeÅrsak);
        });


        Set<PeriodeSplittData> meldekortSplitt = IdentifiserPerioderForMeldekort.identifiserPerioderForEndringerIMeldekort(input);
        meldekortSplitt.forEach(map::leggTilPeriodeÅrsak);

        // må alltid ha en første periode, også når ingen gradering/refusjon/naturalytelse fra start
        if (!map.getPeriodeMap().containsKey(input.getSkjæringstidspunkt())) {
            PeriodeSplittData førstePeriode = PeriodeSplittData.builder()
                .medFom(input.getSkjæringstidspunkt())
                .medPeriodeÅrsak(PeriodeÅrsak.UDEFINERT)
                .build();
            map.leggTilPeriodeÅrsak(førstePeriode);
        }
        return map;
    }

    private static void leggTilPeriodesplitterForEksisterendePerioder(PeriodeModell input, IdentifisertePeriodeÅrsaker map) {
        input.getEksisterendePerioder().forEach(eksisterendePeriode -> {
            if (!eksisterendePeriode.getPeriodeÅrsaker().isEmpty()) {
                eksisterendePeriode.getPeriodeÅrsaker().forEach(periodeÅrsak -> {
                    PeriodeSplittData periodeSplittData = PeriodeSplittData.builder()
                        .medFom(eksisterendePeriode.getPeriode().getFom())
                        .medPeriodeÅrsak(periodeÅrsak).build();
                    map.leggTilPeriodeÅrsak(periodeSplittData);
                });
            }
        });
    }
}
