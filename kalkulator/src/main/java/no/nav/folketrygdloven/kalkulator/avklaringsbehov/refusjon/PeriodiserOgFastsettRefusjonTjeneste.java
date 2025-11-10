package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

/**
 * Når beregningsgrunnlagene kommer hit skal de allerede ha satt refusjon fra den dagen refusjon kreves (som regel skjæringstidspunkt for beregning).
 * Denne klassen må derfor nullstille refusjonen i perioder som ligger før
 * fastsatt startdato for refusjon og trenger ikke endre perioder der det skal være refusjon
 */
public final class PeriodiserOgFastsettRefusjonTjeneste {
	private static final int MÅNEDER_I_ÅR = 12;

    /**
     * @param beregningsgrunnlagDto - beregningsgrunnlaget som skal splittes.
     * @param andeler               - overstyringer avklart av saksbehandler som skal brukes til å bestemme splitten.
     * @return beregningsgrunnlag splittet i henhold til refusjonsdatoer med refusjon kun fra refusjonsstart.
     */
    public static BeregningsgrunnlagDto periodiserOgFastsett(BeregningsgrunnlagDto beregningsgrunnlagDto,
                                                             List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler) {
        // Lag refusjonsplittandeler som holder på data relatert til fastsetting av refusjon
        var splittAndeler = lagSplittAndeler(andeler).stream()
                .sorted(Comparator.comparing(RefusjonSplittAndel::startdatoRefusjon))
                .collect(Collectors.toList());

        if (splittAndeler.isEmpty()) {
            return beregningsgrunnlagDto;
        }

        // Splitt beregningsgrunnlaget basert på refusjonsplittandeler
        var periodisertBeregningsgrunnlag = periodiserBeregningsgrunnlag(beregningsgrunnlagDto, splittAndeler);

        // Fjern refusjon for gitt arbeidsforhold i perioder før fastsatt dato
        var periodisertBeregningsgrunnlagMedFastsattRefusjon = oppdaterRefusjonIRelevantePerioder(periodisertBeregningsgrunnlag, splittAndeler);

        // Valider resultatet
        validerGrunnlag(beregningsgrunnlagDto, periodisertBeregningsgrunnlagMedFastsattRefusjon, andeler);

        return periodisertBeregningsgrunnlagMedFastsattRefusjon;
    }

    private static BeregningsgrunnlagDto oppdaterRefusjonIRelevantePerioder(BeregningsgrunnlagDto periodisertBeregningsgrunnlag, List<RefusjonSplittAndel> splittAndeler) {
        var nyttGrunnlag = BeregningsgrunnlagDto.builder(periodisertBeregningsgrunnlag).build();
        nyttGrunnlag.getBeregningsgrunnlagPerioder()
                .forEach(eksisterendePeriode -> eksisterendePeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                        .stream()
                        .filter(PeriodiserOgFastsettRefusjonTjeneste::harInnvilgetRefusjonEllerSattRefusjonFraSaksbehandler)
                        .forEach(eksisterendeAndel -> {
                            var matchetSplittAndel = finnFastsattAndelForBGAndel(eksisterendeAndel, splittAndeler);
                            // Hvis saksbehandlet andel er tom er ikke andelens refusjon vurdert og den skal ha refusjon fra start
                            if (matchetSplittAndel.isPresent() && refusjonSkalEndres(eksisterendePeriode, matchetSplittAndel.get().startdatoRefusjon())) {
                                var bgAndelArbforBuilder = BGAndelArbeidsforholdDto.Builder.oppdater(eksisterendeAndel.getBgAndelArbeidsforhold());
                                bgAndelArbforBuilder.medSaksbehandletRefusjonPrÅr(matchetSplittAndel.get().delvisRefusjonBeløpPrÅr());
                            } else if (matchetSplittAndel.isPresent()) {
								// Dersom refusjonen ikkje skal settes manuelt må vi sørge for at det ikkje ligger verdi i saksbehandletRefusjonPrÅr
	                            // Dette kan skje ved kopiering/spoling av grunnlag til steg-ut grunnlaget i kopieringslogikken som kjører før lagring i kalkulus
                                var bgAndelArbforBuilder = BGAndelArbeidsforholdDto.Builder.oppdater(eksisterendeAndel.getBgAndelArbeidsforhold());
	                            bgAndelArbforBuilder.medSaksbehandletRefusjonPrÅr(null);
                            }
                        }));
        return nyttGrunnlag;
    }

    private static boolean harInnvilgetRefusjonEllerSattRefusjonFraSaksbehandler(BeregningsgrunnlagPrStatusOgAndelDto a) {
	    var refusjon = a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforholdDto::getRefusjon);
		var saksbehandletRefusjonErSatt = refusjon.map(r -> r.getSaksbehandletRefusjonPrÅr() != null && r.getSaksbehandletRefusjonPrÅr().compareTo(Beløp.ZERO) > 0).orElse(false);
		var innvilgetRefusjonErSatt = refusjon.map(r -> r.getInnvilgetRefusjonskravPrÅr() != null && r.getInnvilgetRefusjonskravPrÅr().compareTo(Beløp.ZERO) > 0).orElse(false);
	    return innvilgetRefusjonErSatt || saksbehandletRefusjonErSatt;
    }

    private static boolean refusjonSkalEndres(BeregningsgrunnlagPeriodeDto eksisterendePeriode, LocalDate startdatoRefusjon) {
        return eksisterendePeriode.getBeregningsgrunnlagPeriodeFom().isBefore(startdatoRefusjon);
    }

	private static Optional<RefusjonSplittAndel> finnFastsattAndelForBGAndel(BeregningsgrunnlagPrStatusOgAndelDto bgAndel, List<RefusjonSplittAndel> splittAndeler) {
        return splittAndeler.stream().filter(splittAndel -> splittAndel.gjelderFor(bgAndel)).findFirst();
    }

    private static BeregningsgrunnlagDto periodiserBeregningsgrunnlag(BeregningsgrunnlagDto eksisterendeGrunnlag, List<RefusjonSplittAndel> splittAndeler) {
        var eksisterendePerioder = eksisterendeGrunnlag.getBeregningsgrunnlagPerioder();
        var stp = eksisterendeGrunnlag.getSkjæringstidspunkt();
        var refusjonstartDatoer = splittAndeler.stream().map(RefusjonSplittAndel::startdatoRefusjon).collect(Collectors.toList());
        var eksisterendeStartdatoer = eksisterendePerioder.stream().map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPeriodeFom).collect(Collectors.toList());

        var perioderINyttGrunnlag = lagNyeIntervaller(eksisterendeStartdatoer, refusjonstartDatoer);
        var nyttGrunlagBuilder = BeregningsgrunnlagDto.builder(eksisterendeGrunnlag);
        nyttGrunlagBuilder.fjernAllePerioder();
        perioderINyttGrunnlag.forEach(nyttIntervall -> {
            var nyFom = nyttIntervall.getFomDato();
            var eksisterendeBGPeriode = finnGammelPeriodePåStartdato(nyFom, eksisterendePerioder);
            var nyBGPeriode = BeregningsgrunnlagPeriodeDto.kopier(eksisterendeBGPeriode)
                    .medBeregningsgrunnlagPeriode(nyFom, nyttIntervall.getTomDato());

            var erNyStartdato = !eksisterendeStartdatoer.contains(nyFom);
            if (erNyStartdato) {
                nyBGPeriode.fjernPeriodeårsaker();
            }

            var skalHaPeriodeÅrsakForRefusjon = refusjonstartDatoer.contains(nyFom) && !nyFom.equals(stp);
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
        var segments = Stream.concat(eksisterendeStartdatoer.stream(), splittDatoer.stream())
            .distinct()
            .map(dato -> LocalDateSegment.emptySegment(dato, TIDENES_ENDE))
            .toList();

        var beregningsgrunnlagTidslinje = new LocalDateTimeline<>(segments, StandardCombinators::leftOnly);

        return beregningsgrunnlagTidslinje.getLocalDateIntervals().stream()
            .map(i -> Intervall.fraOgMedTilOgMed(i.getFomDato(), i.getTomDato()))
            .toList();
    }

    private static List<RefusjonSplittAndel> lagSplittAndeler(List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler) {
        return andeler.stream()
                .map(andel -> new RefusjonSplittAndel(andel.getArbeidsgiver(), mapReferanse(andel), andel.getFastsattRefusjonFom(), mapÅrsbeløpRefusjon(andel)))
                .collect(Collectors.toList());
    }

    private static Beløp mapÅrsbeløpRefusjon(VurderRefusjonAndelBeregningsgrunnlagDto andel) {
        return andel.getDelvisRefusjonBeløpPrMnd() == null ? Beløp.ZERO : Beløp.fra(andel.getDelvisRefusjonBeløpPrMnd()).multipliser(KonfigTjeneste.getMånederIÅr());
    }

    private static InternArbeidsforholdRefDto mapReferanse(VurderRefusjonAndelBeregningsgrunnlagDto andel) {
        return andel.getInternArbeidsforholdRef() == null ? InternArbeidsforholdRefDto.nullRef() : InternArbeidsforholdRefDto.ref(andel.getInternArbeidsforholdRef());
    }

    private static void validerGrunnlag(BeregningsgrunnlagDto gammeltGrunnlag, BeregningsgrunnlagDto nyttGrunnlag, List<VurderRefusjonAndelBeregningsgrunnlagDto> andeler) {
        Set<LocalDate> startdatoer = new HashSet<>();
        gammeltGrunnlag.getBeregningsgrunnlagPerioder().forEach(bgp -> startdatoer.add(bgp.getPeriode().getFomDato()));
        nyttGrunnlag.getBeregningsgrunnlagPerioder().forEach(bgp -> startdatoer.add(bgp.getPeriode().getFomDato()));

        startdatoer.forEach(dato -> {
            var gamleAndeler = finnAndelerPåDatoIGrunnlag(dato, gammeltGrunnlag);
            var nyeAndeler = finnAndelerPåDatoIGrunnlag(dato, nyttGrunnlag);
            if (gamleAndeler.size() != nyeAndeler.size()) {
                throw new IllegalStateException("Forskjellig mengde andeler før og etter splitting av beregningsgrunnlag, gammelt grunnlag :" + gammeltGrunnlag + " nytt grunnlag: " + nyttGrunnlag);
            }
            gamleAndeler.forEach(andel -> {
                var finnesAndelIListe = nyeAndeler.stream().anyMatch(a -> a.equals(andel));
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
					if (tilMånedsbeløp(refusjon.get().getSaksbehandletRefusjonPrÅr()).compareTo(tilMånedsbeløp(refusjon.get().getRefusjonskravPrÅr())) > 0) {
						throw new IllegalStateException("Kan ikke øke refusjonskrav for andel " + a + " Alle endringer som skulle utføres var: " + andeler + " Perioden som endres var " + p.getPeriode());
					}
                }
            });
        });
    }

	private static Beløp tilMånedsbeløp(Beløp årsbeløp) {
		// Bruker ikke desimaler da månedsbeløp fastsatt av saksbehandler er uten desimaler
		return årsbeløp.divider(MÅNEDER_I_ÅR, 0, RoundingMode.HALF_EVEN);
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
