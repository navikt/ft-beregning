package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.frisinn;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.LagAktivPeriodeForArbeidstakerFelles;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.MapBeregningAktiviteterFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class MapBeregningAktiviteterFraVLTilRegelFRISINN implements MapBeregningAktiviteterFraVLTilRegel {

    public static final String INGEN_AKTIVITET_MELDING = "Må ha aktiviteter for å sette status.";

    public AktivitetStatusModell mapForSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input) {
        LocalDate opptjeningSkjæringstidspunkt = input.getSkjæringstidspunktOpptjening();

        AktivitetStatusModell modell = new AktivitetStatusModell();
        modell.setSkjæringstidspunktForOpptjening(opptjeningSkjæringstidspunkt);

        var relevanteAktiviteter = input.getOpptjeningAktiviteterForBeregning();

        Optional<OppgittOpptjeningDto> oppgittOpptjening = input.getIayGrunnlag().getOppgittOpptjening();
        boolean harFLEtterStp = harOppgittFLEtterStpOpptjening(opptjeningSkjæringstidspunkt, oppgittOpptjening);
        boolean harSNEtterStp = harOppgittSNEtterStpOpptjening(opptjeningSkjæringstidspunkt, oppgittOpptjening);
        boolean harATEtterSTP = harOppgittArbeidsinntektEtterSTP(opptjeningSkjæringstidspunkt, oppgittOpptjening);
        if (relevanteAktiviteter.isEmpty()) { // For enklere feilsøking når det mangler aktiviteter
            throw new IllegalStateException(INGEN_AKTIVITET_MELDING);
        } else {
            relevanteAktiviteter.forEach(opptjeningsperiode -> modell.leggTilEllerOppdaterAktivPeriode(
                    lagAktivPeriode(input.getInntektsmeldinger(), opptjeningsperiode, harFLEtterStp, harSNEtterStp, opptjeningSkjæringstidspunkt)));
        }

        // Legger til 48 mnd med frilans og næring rundt stp om det ikkje finnes, legger også til arbeidsaktivitet om det ikke finnes fra før og er oppgitt
        Periode hardkodetOpptjeningsperiode = Periode.of(opptjeningSkjæringstidspunkt.minusMonths(36), opptjeningSkjæringstidspunkt.plusMonths(12));
        if (relevanteAktiviteter.stream().noneMatch(a -> a.getType().equals(OpptjeningAktivitetType.FRILANS)) && harFLEtterStp) {
            modell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forFrilanser(hardkodetOpptjeningsperiode));
        }
        if (relevanteAktiviteter.stream().noneMatch(a -> a.getType().equals(OpptjeningAktivitetType.NÆRING)) && harSNEtterStp) {
            modell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, hardkodetOpptjeningsperiode));
        }
        if (!erArbeidstakerPåOpptjeningsSTP(relevanteAktiviteter, opptjeningSkjæringstidspunkt) && harATEtterSTP) {
            modell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(hardkodetOpptjeningsperiode, OrgNummer.KUNSTIG_ORG, null));
        }
        return modell;
    }

    private boolean erArbeidstakerPåOpptjeningsSTP(Collection<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> relevanteAktiviteter, LocalDate opptjeningSkjæringstidspunkt) {
        List<OpptjeningAktiviteterDto.OpptjeningPeriodeDto> arbeidstakeraktiviteter = relevanteAktiviteter.stream()
                .filter(a -> a.getType().equals(OpptjeningAktivitetType.ARBEID))
                .collect(Collectors.toList());
        return arbeidstakeraktiviteter.stream().anyMatch(akt -> akt.getPeriode().inkluderer(opptjeningSkjæringstidspunkt));
    }

    private boolean harOppgittArbeidsinntektEtterSTP(LocalDate opptjeningSkjæringstidspunkt, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        List<OppgittArbeidsforholdDto> oppgitteArbfor = oppgittOpptjening.map(OppgittOpptjeningDto::getOppgittArbeidsforhold).orElse(Collections.emptyList());
        return oppgitteArbfor.stream().anyMatch(oa -> !oa.getFom().isBefore(opptjeningSkjæringstidspunkt));
    }

    private boolean harOppgittFLEtterStpOpptjening(LocalDate opptjeningSkjæringstidspunkt, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        return oppgittOpptjening.flatMap(OppgittOpptjeningDto::getFrilans)
                .filter(fl -> fl.getOppgittFrilansInntekt().stream()
                        .anyMatch(ip -> !ip.getPeriode().getTomDato().isBefore(opptjeningSkjæringstidspunkt)))
                .isPresent();
    }

    private boolean harOppgittSNEtterStpOpptjening(LocalDate opptjeningSkjæringstidspunkt, Optional<OppgittOpptjeningDto> oppgittOpptjening) {
        return oppgittOpptjening.map(OppgittOpptjeningDto::getEgenNæring)
                .filter(fl -> fl.stream()
                        .anyMatch(ip -> !ip.getPeriode().getTomDato().isBefore(opptjeningSkjæringstidspunkt)))
                .isPresent();
    }


    private AktivPeriode lagAktivPeriode(Collection<InntektsmeldingDto> inntektsmeldinger,
                                         OpptjeningAktiviteterDto.OpptjeningPeriodeDto opptjeningsperiode,
                                         boolean harFLEtterStp,
                                         boolean harSNEtterStp,
                                         LocalDate opptjeningSkjæringstidspunkt) {
        Aktivitet aktivitetType = MapOpptjeningAktivitetTypeFraVLTilRegel.map(opptjeningsperiode.getOpptjeningAktivitetType());
        var gjeldendePeriode = opptjeningsperiode.getPeriode();
        var regelPeriode = Periode.of(gjeldendePeriode.getFomDato(), gjeldendePeriode.getTomDato());
        Periode utvidetPeriode = Periode.of(opptjeningSkjæringstidspunkt.minusMonths(36), opptjeningSkjæringstidspunkt.plusMonths(12));

        if (Aktivitet.FRILANSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forFrilanser(harFLEtterStp ? utvidetPeriode : regelPeriode);
        } else if (Aktivitet.ARBEIDSTAKERINNTEKT.equals(aktivitetType)) {
            var opptjeningArbeidsgiverAktørId = opptjeningsperiode.getArbeidsgiverAktørId();
            var opptjeningArbeidsgiverOrgnummer = opptjeningsperiode.getArbeidsgiverOrgNummer();
            var opptjeningArbeidsforhold = opptjeningsperiode.getArbeidsforholdId();
            return LagAktivPeriodeForArbeidstakerFelles.lagAktivPeriodeForArbeidstaker(inntektsmeldinger, regelPeriode, opptjeningArbeidsgiverAktørId,
                    opptjeningArbeidsgiverOrgnummer, opptjeningArbeidsforhold);
        }
        if (Aktivitet.NÆRINGSINNTEKT.equals(aktivitetType)) {
            return AktivPeriode.forAndre(aktivitetType, harSNEtterStp ? utvidetPeriode : regelPeriode);
        } else {
            return AktivPeriode.forAndre(aktivitetType, regelPeriode);
        }
    }


}
