package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class MapBeregningAktiviteterFraVLTilRegelFelles implements MapBeregningAktiviteterFraVLTilRegel {

    public AktivitetStatusModell mapForSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        var opptjeningSkjæringstidspunkt = input.getSkjæringstidspunktOpptjening();

        var modell = new AktivitetStatusModell();
        modell.setSkjæringstidspunktForOpptjening(opptjeningSkjæringstidspunkt);

        var relevanteAktiviteter = input.getOpptjeningAktiviteterForBeregning();

        if (!relevanteAktiviteter.isEmpty()) {
            var relevantYrkesaktivitet = input.getIayGrunnlag().getAktørArbeidFraRegister()
                    .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                    .orElse(Collections.emptyList());
            var alleYtelser = input.getIayGrunnlag().getAktørYtelseFraRegister().map(AktørYtelseDto::getAlleYtelser).orElse(Collections.emptyList());
            var permisjonFilter = new PermisjonFilter(alleYtelser, relevantYrkesaktivitet, input.getSkjæringstidspunktOpptjening());
            var aktivePerioder = relevanteAktiviteter.stream()
                    .flatMap(opptjeningsperiode ->
                            lagAktivPeriode(
                                    input.getInntektsmeldinger(),
                                    opptjeningsperiode,
                                    relevanteAktiviteter,
                                    input.getSkjæringstidspunktOpptjening(),
                                    permisjonFilter)
                                    .stream()
                    ).collect(Collectors.toList());
            aktivePerioder.forEach(modell::leggTilEllerOppdaterAktivPeriode);
        }

        return modell;
    }

    private Optional<AktivPeriode> lagAktivPeriode(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                   OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                                   Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> relevanteAktiviteter,
                                                   LocalDate skjæringstidspunktOpptjening,
                                                   PermisjonFilter permisjonFilter) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(opptjeningsperiode.getOpptjeningAktivitetType());
        var gjeldendePeriode = opptjeningsperiode.getPeriode();
        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return Optional.of(AktivPeriode.forFrilanser(
                    Periode.of(gjeldendePeriode.getFomDato(), gjeldendePeriode.getTomDato())));
        } else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            var arbeidsgiver = opptjeningsperiode.getArbeidsgiver().orElseThrow(() -> new IllegalStateException("Forventer arbeidsgiver"));
            var tomDato = finnTomdatoTaHensynTilPermisjon(opptjeningsperiode, skjæringstidspunktOpptjening, arbeidsgiver, permisjonFilter);
            if (!tomDato.isBefore(gjeldendePeriode.getFomDato())) {
                return Optional.of(lagAktivPeriodeForArbeidstaker(inntektsmeldinger,
                        Periode.of(gjeldendePeriode.getFomDato(), tomDato),
                        arbeidsgiver,
                        opptjeningsperiode.getArbeidsforholdId(),
                        relevanteAktiviteter));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(AktivPeriode.forAndre(
                    aktivitetType,
                    Periode.of(gjeldendePeriode.getFomDato(), gjeldendePeriode.getTomDato())));
        }
    }

    protected static AktivPeriode lagAktivPeriodeForArbeidstaker(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                 Periode gjeldendePeriode,
                                                                 Arbeidsgiver arbeidsgiver,
                                                                 InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                 Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> alleAktiviteter) {
        if (arbeidsgiver.erAktørId()) {
            return lagAktivePerioderForArbeidstakerHosPrivatperson(arbeidsgiver.getIdentifikator(), gjeldendePeriode);
        } else if (arbeidsgiver.getErVirksomhet()) {
            return lagAktivePerioderForArbeidstakerHosVirksomhet(inntektsmeldinger, gjeldendePeriode, arbeidsgiver.getIdentifikator(), arbeidsforholdRef, alleAktiviteter);
        } else {
            throw new IllegalStateException("Må ha en arbeidsgiver som enten er aktør eller virksomhet når aktivitet er " + Aktivitet.ARBEIDSTAKERINNTEKT);
        }
    }

    private LocalDate finnTomdatoTaHensynTilPermisjon(OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                                      LocalDate skjæringstidspunktOpptjening,
                                                      Arbeidsgiver arbeidsgiver,
                                                      PermisjonFilter permisjonFilter) {
        var permisjonTidslinje = permisjonFilter.tidslinjeForPermisjoner(arbeidsgiver, opptjeningsperiode.getArbeidsforholdId());
        var beregningstidspunkt = BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktOpptjening);
        var sisteDagFørPermisjonStart = permisjonTidslinje.getLocalDateIntervals().stream()
                .filter(p -> p.contains(beregningstidspunkt))
                .map(LocalDateInterval::getFomDato)
                .min(Comparator.naturalOrder())
                .map(d -> d.minusDays(1));
        if (sisteDagFørPermisjonStart.isPresent() && sisteDagFørPermisjonStart.get().isBefore(opptjeningsperiode.getPeriode().getFomDato())) {
            // Kan skje pga opptjening ikke hensyntar permisjon, og ingenting stopper aareg fra å ha permisjon hele arbeidsperioden
            return opptjeningsperiode.getPeriode().getFomDato();
        }
        return sisteDagFørPermisjonStart.orElse(opptjeningsperiode.getPeriode().getTomDato());
    }

    private static AktivPeriode lagAktivePerioderForArbeidstakerHosPrivatperson(String aktørId, Periode gjeldendePeriode) {
        return AktivPeriode.forArbeidstakerHosPrivatperson(gjeldendePeriode, aktørId);
    }

    private static AktivPeriode lagAktivePerioderForArbeidstakerHosVirksomhet(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                              Periode gjeldendePeriode,
                                                                              String opptjeningArbeidsgiverOrgnummer,
                                                                              InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                              Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> alleAktiviteter) {
        if (harSpesifikkInntektsmeldingForArbeidsforhold(inntektsmeldinger, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef)) {
            return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef.getReferanse());
        } else {
            if (harInntektsmeldingForSpesifiktArbeidVedSkjæringstidspunktet(inntektsmeldinger, alleAktiviteter, opptjeningArbeidsgiverOrgnummer)) {
                // Her mangler vi inntektsmelding fra minst ett arbeidsforhold. Disse andelene får Id for at de ikke skal behandles som aggregat-andeler.
                return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, arbeidsforholdRef.getReferanse());
            }
            return AktivPeriode.forArbeidstakerHosVirksomhet(gjeldendePeriode, opptjeningArbeidsgiverOrgnummer, null);
        }
    }

    /**
     * Sjekker om det er mottatt inntektsmelding for et spesifikt arbeidsforhold og et gitt orgnummer
     * og om dette spesifikke arbeidsforholdet er aktivt på skjæringstidspunktet for opptjening.
     * <p>
     * Tilpassningen er gjort for å støtte caset der omsorgspenger kun mottar inntektsmeldinger for arbeidsforhold der man har fravær (https://jira.adeo.no/browse/TSF-1153)
     *
     * @param inntektsmeldinger               Innteksmeldinger
     * @param alleAktiviteter                 Alle aktiviteter
     * @param opptjeningArbeidsgiverOrgnummer Orgnnummer for arbeidsaktivitet
     * @return Boolean som sier om det er motttatt inntektsmelding for et spesifikt arbeidsforhold som er aktivt på skjæringstidspunktet
     */
    private static boolean harInntektsmeldingForSpesifiktArbeidVedSkjæringstidspunktet(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                                       Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> alleAktiviteter,
                                                                                       String opptjeningArbeidsgiverOrgnummer) {
        return alleAktiviteter.stream()
                .filter(a -> a.getArbeidsgiverOrgNummer() != null && a.getArbeidsgiverOrgNummer().equals(opptjeningArbeidsgiverOrgnummer))
                .anyMatch(a -> OpptjeningAktivitetType.ARBEID.equals(a.getType()) && inntektsmeldinger.stream()
                        .anyMatch(im -> im.getArbeidsgiver().getIdentifikator().equals(a.getArbeidsgiverOrgNummer())
                                && im.gjelderForEtSpesifiktArbeidsforhold()
                                && im.getArbeidsforholdRef().gjelderFor(a.getArbeidsforholdId())));
    }

    private static boolean harSpesifikkInntektsmeldingForArbeidsforhold(Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                        String orgnummer,
                                                                        InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (!arbeidsforholdRef.gjelderForSpesifiktArbeidsforhold()) {
            return false;
        } else {
            return inntektsmeldinger.stream()
                    .anyMatch(im -> im.gjelderForEtSpesifiktArbeidsforhold()
                            && Objects.equals(im.getArbeidsgiver().getOrgnr(), orgnummer)
                            && Objects.equals(im.getArbeidsforholdRef().getReferanse(), arbeidsforholdRef.getReferanse()));
        }
    }
}
