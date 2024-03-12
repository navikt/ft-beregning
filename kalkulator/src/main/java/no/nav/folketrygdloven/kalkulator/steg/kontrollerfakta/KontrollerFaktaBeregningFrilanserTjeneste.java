package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class KontrollerFaktaBeregningFrilanserTjeneste {

    private KontrollerFaktaBeregningFrilanserTjeneste() {
        // Skjul
    }

    public static boolean erNyoppstartetFrilanser(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        boolean erFrilanser = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser());

        return harOppgittNyoppstartetISøknad(iayGrunnlag, erFrilanser) || harOppgittPeriodeSomNyoppstartet(beregningsgrunnlagGrunnlag, erFrilanser);
    }

    private static boolean harOppgittPeriodeSomNyoppstartet(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                            boolean erFrilanser) {

        if (erFrilanser) {
            var startAvFrilans = beregningsgrunnlagGrunnlag.getRegisterAktiviteter().getBeregningAktiviteter()
                    .stream().filter(ba -> ba.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.FRILANS))
                    .map(BeregningAktivitetDto::getPeriode)
                    .map(Intervall::getFomDato)
                    .findFirst()
                    .orElse(TIDENES_BEGYNNELSE);

            BeregningsgrunnlagDto bg = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes()
                    .orElseThrow();
            var startAvBeregningsperiode = bg
                    .getBeregningsgrunnlagPerioder().stream()
                    .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                    .filter(andel -> andel.getAktivitetStatus().erFrilanser())
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregningsperiode)
                    .findFirst()
                    .map(Intervall::getFomDato)
                    .orElseThrow(() -> new IllegalStateException("Skal ha beregningsperiode for frilans"));


            return startAvFrilans.isAfter(startAvBeregningsperiode) && startAvFrilans.isBefore(bg.getSkjæringstidspunkt());

        }
        return false;
    }

    private static boolean harOppgittNyoppstartetISøknad(InntektArbeidYtelseGrunnlagDto iayGrunnlag, boolean erFrilanser) {
        return erFrilanser
            && iayGrunnlag.getOppgittOpptjening()
            .flatMap(OppgittOpptjeningDto::getFrilans)
            .map(OppgittFrilansDto::getErNyoppstartet)
            .orElse(false);
    }

    public static boolean erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return !brukerErArbeidstakerOgFrilanserISammeOrganisasjon(beregningsgrunnlag, iayGrunnlag).isEmpty();
    }

    public static Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return arbeidsgivereSomHarFrilansforholdOgArbeidsforholdMedBruker(iayGrunnlag, beregningsgrunnlag);
    }

    private static Set<Arbeidsgiver> arbeidsgivereSomHarFrilansforholdOgArbeidsforholdMedBruker(InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagDto beregningsgrunnlag) {

        // Sjekk om statusliste inneholder AT og FL.

        if (beregningsgrunnlag.getBeregningsgrunnlagPerioder().isEmpty() ||
            !harFrilanserOgArbeidstakerAndeler(beregningsgrunnlag)) {
            return Collections.emptySet();
        }

        // Sjekk om samme orgnr finnes både som arbeidsgiver og frilansoppdragsgiver

        final Set<Arbeidsgiver> arbeidsforholdArbeidsgivere = finnArbeidsgivere(beregningsgrunnlag);
        if (arbeidsforholdArbeidsgivere.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<Arbeidsgiver> frilansOppdragsgivere = finnFrilansOppdragsgivere(beregningsgrunnlag, iayGrunnlag);
        if (frilansOppdragsgivere.isEmpty()) {
            return Collections.emptySet();
        }
        return finnMatchendeArbeidsgiver(arbeidsforholdArbeidsgivere, frilansOppdragsgivere);
    }

    private static boolean harFrilanserOgArbeidstakerAndeler(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
        .anyMatch(andel -> andel.getAktivitetStatus().erFrilanser()) &&
            beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .anyMatch(andel -> andel.getAktivitetStatus().erArbeidstaker());
    }

    private static Set<Arbeidsgiver> finnMatchendeArbeidsgiver(final Set<Arbeidsgiver> virksomheterForArbeidsforhold, final Set<Arbeidsgiver> frilansOppdragsgivere) {
        Set<Arbeidsgiver> intersection = new HashSet<>(virksomheterForArbeidsforhold);
        intersection.retainAll(frilansOppdragsgivere);
        return intersection;
    }

    private static Set<Arbeidsgiver> finnFrilansOppdragsgivere(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        boolean erFrilanser = beregningsgrunnlag.getAktivitetStatuser().stream()
            .map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus)
            .anyMatch(AktivitetStatus::erFrilanser);
        if (!erFrilanser) {
            return Collections.emptySet();
        }
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister()).før(beregningsgrunnlag.getSkjæringstidspunkt());

        return filter.getFrilansOppdrag()
            .stream()
            .map(YrkesaktivitetDto::getArbeidsgiver)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private static Set<Arbeidsgiver> finnArbeidsgivere(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bpsa -> no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER.equals(bpsa.getAktivitetStatus()))
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(BGAndelArbeidsforholdDto::getArbeidsgiver)
            .collect(Collectors.toSet());
    }
}
