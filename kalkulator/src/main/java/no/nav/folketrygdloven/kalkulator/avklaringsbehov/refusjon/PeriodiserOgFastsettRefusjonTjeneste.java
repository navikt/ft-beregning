package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

/**
 * Når beregningsgrunnlagene kommer hit skal de allerede ha satt refusjon fra den dagen refusjon kreves (som regel skjæringstidspunkt for beregning).
 * Denne klassen må derfor nullstille refusjonen i perioder som ligger før
 * fastsatt startdato for refusjon og trenger ikke endre perioder der det skal være refusjon
 */
public final class PeriodiserOgFastsettRefusjonTjeneste {

    /**
     * @param beregningsgrunnlagDto - beregningsgrunnlaget som skal splittes.
     * @param andeler               - overstyringer avklart av saksbehandler som skal brukes til å bestemme splitten.
     * @return beregningsgrunnlag splittet i henhold til refusjonsdatoer med refusjon kun fra refusjonsstart.
     */
    public static BeregningsgrunnlagDto periodiserOgFastsett(BeregningsgrunnlagDto beregningsgrunnlagDto,
                                                             List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler) {
        // Lag refusjonsplittandeler som holder på data relatert til fastsetting av refusjon
        List<RefusjonSplittAndel> splittAndeler = lagSplittAndeler(andeler).stream()
                .sorted(Comparator.comparing(RefusjonSplittAndel::getStartdatoRefusjon))
                .collect(Collectors.toList());

        if (splittAndeler.isEmpty()) {
            return beregningsgrunnlagDto;
        }

        // Splitt beregningsgrunnlaget basert på refusjonsplittandeler
        BeregningsgrunnlagDto periodisertBeregningsgrunnlag = periodiserBeregningsgrunnlag(beregningsgrunnlagDto, splittAndeler);

        // Fjern refusjon for gitt arbeidsforhold i perioder før fastsatt dato
        BeregningsgrunnlagDto periodisertBeregningsgrunnlagMedFastsattRefusjon = oppdaterRefusjonIRelevantePerioder(periodisertBeregningsgrunnlag, splittAndeler);

        // Valider resultatet
        validerGrunnlag(beregningsgrunnlagDto, periodisertBeregningsgrunnlagMedFastsattRefusjon, andeler);

        return periodisertBeregningsgrunnlagMedFastsattRefusjon;
    }

    private static BeregningsgrunnlagDto oppdaterRefusjonIRelevantePerioder(BeregningsgrunnlagDto periodisertBeregningsgrunnlag, List<RefusjonSplittAndel> splittAndeler) {
        BeregningsgrunnlagDto nyttGrunnlag = BeregningsgrunnlagDto.builder(periodisertBeregningsgrunnlag).build();
        nyttGrunnlag.getBeregningsgrunnlagPerioder()
                .forEach(eksisterendePeriode -> eksisterendePeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                        .stream()
                        .filter(PeriodiserOgFastsettRefusjonTjeneste::harInnvilgetRefusjonskrav)
                        .forEach(eksisterendeAndel -> {
                            Optional<RefusjonSplittAndel> matchetSplittAndel = finnFastsattAndelForBGAndel(eksisterendeAndel, splittAndeler);
                            // Hvis saksbehandlet andel er tom er ikke andelens refusjon vurdert og den skal ha refusjon fra start
                            if (matchetSplittAndel.isPresent() && refusjonSkalEndres(eksisterendePeriode, matchetSplittAndel.get().getStartdatoRefusjon())) {
                                BGAndelArbeidsforholdDto.Builder bgAndelArbforBuilder = BGAndelArbeidsforholdDto.Builder.oppdater(eksisterendeAndel.getBgAndelArbeidsforhold());
                                bgAndelArbforBuilder.medSaksbehandletRefusjonPrÅr(matchetSplittAndel.get().getDelvisRefusjonBeløpPrÅr());
                            }
                        }));
        return nyttGrunnlag;
    }

    private static boolean harInnvilgetRefusjonskrav(BeregningsgrunnlagPrStatusOgAndelDto a) {
        return a.getBgAndelArbeidsforhold().isPresent() &&
                a.getBgAndelArbeidsforhold().get().getRefusjon().isPresent() &&
                a.getBgAndelArbeidsforhold().get().getRefusjon().get().getInnvilgetRefusjonskravPrÅr() != null &&
                a.getBgAndelArbeidsforhold().get().getRefusjon().get().getInnvilgetRefusjonskravPrÅr().compareTo(Beløp.ZERO) > 0;
    }

    private static boolean refusjonSkalEndres(BeregningsgrunnlagPeriodeDto eksisterendePeriode, LocalDate startdatoRefusjon) {
        return eksisterendePeriode.getBeregningsgrunnlagPeriodeFom().isBefore(startdatoRefusjon);
    }

    private static Optional<RefusjonSplittAndel> finnFastsattAndelForBGAndel(BeregningsgrunnlagPrStatusOgAndelDto bgAndel, List<RefusjonSplittAndel> splittAndeler) {
        return splittAndeler.stream().filter(splittAndel -> splittAndel.gjelderFor(bgAndel)).findFirst();
    }

    private static BeregningsgrunnlagDto periodiserBeregningsgrunnlag(BeregningsgrunnlagDto eksisterendeGrunnlag, List<RefusjonSplittAndel> splittAndeler) {
        List<BeregningsgrunnlagPeriodeDto> eksisterendePerioder = eksisterendeGrunnlag.getBeregningsgrunnlagPerioder();
        LocalDate stp = eksisterendeGrunnlag.getSkjæringstidspunkt();
        List<LocalDate> refusjonstartDatoer = splittAndeler.stream().map(RefusjonSplittAndel::getStartdatoRefusjon).collect(Collectors.toList());
        List<LocalDate> eksisterendeStartdatoer = eksisterendePerioder.stream().map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom).collect(Collectors.toList());

        List<Intervall> perioderINyttGrunnlag = lagNyeIntervaller(eksisterendeStartdatoer, refusjonstartDatoer);
        BeregningsgrunnlagDto.Builder nyttGrunlagBuilder = BeregningsgrunnlagDto.builder(eksisterendeGrunnlag);
        nyttGrunlagBuilder.fjernAllePerioder();
        perioderINyttGrunnlag.forEach(nyttIntervall -> {
            LocalDate nyFom = nyttIntervall.getFomDato();
            BeregningsgrunnlagPeriodeDto eksisterendeBGPeriode = finnGammelPeriodePåStartdato(nyFom, eksisterendePerioder);
            BeregningsgrunnlagPeriodeDto.Builder nyBGPeriode = BeregningsgrunnlagPeriodeDto.kopier(eksisterendeBGPeriode)
                    .medBeregningsgrunnlagPeriode(nyFom, nyttIntervall.getTomDato());

            boolean erNyStartdato = !eksisterendeStartdatoer.contains(nyFom);
            if (erNyStartdato) {
                nyBGPeriode.fjernPeriodeårsaker();
            }

            boolean skalHaPeriodeÅrsakForRefusjon = refusjonstartDatoer.contains(nyFom) && !nyFom.equals(stp);
            if (skalHaPeriodeÅrsakForRefusjon) {
                nyBGPeriode.leggTilPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
            }

            nyttGrunlagBuilder.leggTilBeregningsgrunnlagPeriode(nyBGPeriode);
        });
        return nyttGrunlagBuilder.build();
    }

    private static BeregningsgrunnlagPeriodeDto finnGammelPeriodePåStartdato(LocalDate fomDato, List<BeregningsgrunnlagPeriodeDto> eksisterendePerioder) {
        return eksisterendePerioder.stream()
                .filter(bgp -> bgp.getPeriode().inkluderer(fomDato))
                .findFirst().
                orElseThrow(() -> new IllegalStateException("Forventer å ha eksisterende periode å basere ny" +
                        " periode på. Dato var " + fomDato + " liste med perioder var :" + eksisterendePerioder));
    }

    private static List<Intervall> lagNyeIntervaller(List<LocalDate> eksisterendeStartdatoer, List<LocalDate> splittDatoer) {
        Set<LocalDate> alleStartdatoer = new HashSet<>(eksisterendeStartdatoer);

        alleStartdatoer.addAll(splittDatoer);

        ArrayList<LocalDate> datoliste = new ArrayList<>(alleStartdatoer);
        Collections.sort(datoliste);

        ListIterator<LocalDate> iterator = datoliste.listIterator();

        List<Intervall> intervaller = new ArrayList<>();

        while (iterator.hasNext()) {
            LocalDate fom = iterator.next();
            LocalDate tom = iterator.hasNext() ? datoliste.get(iterator.nextIndex()).minusDays(1) : TIDENES_ENDE;
            intervaller.add(Intervall.fraOgMedTilOgMed(fom, tom));
        }
        return intervaller;
    }

    private static List<RefusjonSplittAndel> lagSplittAndeler(List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler) {
        return andeler.stream()
                .map(andel -> new RefusjonSplittAndel(mapArbeidsgiver(andel), mapReferanse(andel), andel.getFastsattRefusjonFom(), mapÅrsbeløpRefusjon(andel)))
                .collect(Collectors.toList());
    }

    private static Beløp mapÅrsbeløpRefusjon(VurderRefusjonAndelBeregningsgrunnlagDto andel) {
        return andel.getDelvisRefusjonBeløpPrMnd() == null ? Beløp.ZERO : Beløp.fra(andel.getDelvisRefusjonBeløpPrMnd()).multipliser(KonfigTjeneste.getMånederIÅr());
    }

    private static InternArbeidsforholdRefDto mapReferanse(VurderRefusjonAndelBeregningsgrunnlagDto andel) {
        return andel.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : InternArbeidsforholdRefDto.ref(andel.getInternArbeidsforholdRef());
    }

    private static Arbeidsgiver mapArbeidsgiver(VurderRefusjonAndelBeregningsgrunnlagDto andel) {
        if (andel.getArbeidsgiverOrgnr() != null) {
            return Arbeidsgiver.virksomhet(andel.getArbeidsgiverOrgnr());
        } else {
            return Arbeidsgiver.person(new AktørId(andel.getArbeidsgiverAktørId()));
        }
    }

    private static void validerGrunnlag(BeregningsgrunnlagDto gammeltGrunnlag, BeregningsgrunnlagDto nyttGrunnlag, List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler) {
        Set<LocalDate> startdatoer = new HashSet<>();
        gammeltGrunnlag.getBeregningsgrunnlagPerioder().forEach(bgp -> startdatoer.add(bgp.getPeriode().getFomDato()));
        nyttGrunnlag.getBeregningsgrunnlagPerioder().forEach(bgp -> startdatoer.add(bgp.getPeriode().getFomDato()));

        startdatoer.forEach(dato -> {
            List<BeregningsgrunnlagPrStatusOgAndelDto> gamleAndeler = finnAndelerPåDatoIGrunnlag(dato, gammeltGrunnlag);
            List<BeregningsgrunnlagPrStatusOgAndelDto> nyeAndeler = finnAndelerPåDatoIGrunnlag(dato, nyttGrunnlag);
            if (gamleAndeler.size() != nyeAndeler.size()) {
                throw new IllegalStateException("Forskjellig mengde andeler før og etter splitting av beregningsgrunnlag, gammelt grunnlag :" + gammeltGrunnlag + " nytt grunnlag: " + nyttGrunnlag);
            }
            gamleAndeler.forEach(andel -> {
                boolean finnesAndelIListe = nyeAndeler.stream().anyMatch(a -> a.equals(andel));
                if (!finnesAndelIListe) {
                    throw new IllegalStateException("Andel finnes ikke i begge grunnlag etter splitt. Andel: " + andel + " finnes ikke i liste: " + nyeAndeler);
                }
            });
        });

        validerIngenØkteKrav(nyttGrunnlag, andeler);

    }

    private static void validerIngenØkteKrav(BeregningsgrunnlagDto nyttGrunnlag, List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler) {
        nyttGrunnlag.getBeregningsgrunnlagPerioder().forEach(p -> {
            p.getBeregningsgrunnlagPrStatusOgAndelList().forEach(a -> {
                var refusjon = a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getRefusjon);
                if (refusjon.isPresent() && refusjon.get().getSaksbehandletRefusjonPrÅr() != null && refusjon.get().getRefusjonskravPrÅr() != null) {
                    if (refusjon.get().getSaksbehandletRefusjonPrÅr().compareTo(refusjon.get().getRefusjonskravPrÅr()) > 0) {
                        throw new IllegalStateException("Kan ikke øke refusjonskrav for andel " + a + " Alle endringer som skulle utføres var: " + andeler);
                    }
                }
            });
        });
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> finnAndelerPåDatoIGrunnlag(LocalDate dato, BeregningsgrunnlagDto grunnlag) {
        if (dato.isBefore(grunnlag.getSkjæringstidspunkt())) {
            return grunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        }
        return grunnlag.getBeregningsgrunnlagPerioder().stream()
                .filter(bgp -> bgp.getPeriode().inkluderer(dato))
                .findFirst()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
                .orElse(Collections.emptyList());
    }

}
