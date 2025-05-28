package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class MapArbeidsforholdFraVLTilRegel {
	private static final String FEILMELDING = "Arbeidsgiver må være enten aktør eller virksomhet, men var: ";
    private MapArbeidsforholdFraVLTilRegel() {
        // skjul public constructor
    }

    public static Arbeidsforhold arbeidsforholdForMedStartdato(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate stp) {
        var arbeidsgiver = vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver);
        var arbeidsforholdType = vlBGPStatus.getArbeidsforholdType();
        var arbeidsforholdRef = arbeidsforholdRefFor(vlBGPStatus);
        return arbeidsforholdForMedStartdato(vlBGPStatus.getAktivitetStatus(), arbeidsgiver, arbeidsforholdType, arbeidsforholdRef, iayGrunnlag, stp);
    }

    public static Arbeidsforhold arbeidsforholdFor(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        var arbeidsgiver = vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver);
        var arbeidsforholdType = vlBGPStatus.getArbeidsforholdType();
        var arbeidsforholdRef = arbeidsforholdRefFor(vlBGPStatus);
        return arbeidsforholdFor(vlBGPStatus.getAktivitetStatus(), arbeidsgiver, arbeidsforholdType, arbeidsforholdRef);
    }

    public static Arbeidsforhold arbeidsforholdForMedStartdato(AktivitetStatus aktivitetStatus,
                                                               Optional<Arbeidsgiver> arbeidsgiver,
                                                               OpptjeningAktivitetType arbeidsforholdType,
                                                               String arbeidsforholdRef, InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                               LocalDate stp) {
        if (erFrilanser(aktivitetStatus)) {
            return Arbeidsforhold.frilansArbeidsforhold();
        }
        if (arbeidsgiver.isPresent()) {
            return lagArbeidsforholdHosArbeidsgiverMedStartdato(arbeidsgiver.get(), arbeidsforholdRef, iayGrunnlag, stp);
        } else {
            return Arbeidsforhold.anonymtArbeidsforhold(MapOpptjeningAktivitetTypeFraVLTilRegel.map(arbeidsforholdType));
        }
    }

    public static Arbeidsforhold arbeidsforholdFor(AktivitetStatus aktivitetStatus,
                                                   Optional<Arbeidsgiver> arbeidsgiver,
                                                   OpptjeningAktivitetType arbeidsforholdType,
                                                   String arbeidsforholdRef) {
        if (erFrilanser(aktivitetStatus)) {
            return Arbeidsforhold.frilansArbeidsforhold();
        }
        if (arbeidsgiver.isPresent()) {
            return lagArbeidsforholdHosArbeidsgiver(arbeidsgiver.get(), arbeidsforholdRef);
        } else {
            return Arbeidsforhold.anonymtArbeidsforhold(MapOpptjeningAktivitetTypeFraVLTilRegel.map(arbeidsforholdType));
        }
    }


    private static boolean erFrilanser(AktivitetStatus aktivitetStatus) {
        return AktivitetStatus.FRILANSER.equals(aktivitetStatus);
    }

    private static Arbeidsforhold lagArbeidsforholdHosArbeidsgiverMedStartdato(Arbeidsgiver arbeidsgiver, String arbeidsforholdRef, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate stp) {
        var startdato = UtledStartdato.utledStartdato(arbeidsgiver, arbeidsforholdRef, iayGrunnlag, stp);
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.builder()
                    .medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
                    .medOrgnr(arbeidsgiver.getOrgnr())
                    .medArbeidsforholdId(arbeidsforholdRef)
                    .medStartdato(startdato)
                    .build();
        }
        if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.builder()
                    .medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
                    .medAktørId(arbeidsgiver.getAktørId().getId())
                    .medArbeidsforholdId(arbeidsforholdRef)
                    .medStartdato(startdato)
                    .build();
        }
        throw new IllegalStateException(FEILMELDING + arbeidsgiver);
    }

    private static Arbeidsforhold lagArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver, String arbeidsforholdRef) {
        var arbRef = arbeidsforholdRef;
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getOrgnr(), arbRef);
        }
        if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getAktørId().getId(), arbRef);
        }
        throw new IllegalStateException(FEILMELDING + arbeidsgiver);
    }

    private static String arbeidsforholdRefFor(BeregningsgrunnlagPrStatusOgAndelDto vlBGPStatus) {
        return vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).map(InternArbeidsforholdRefDto::getReferanse).orElse(null);
    }

    public static Arbeidsforhold mapArbeidsforhold(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getOrgnr(), arbeidsforholdRef.getReferanse());
        }
        if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getAktørId().getId(), arbeidsforholdRef.getReferanse());
        }
        throw new IllegalStateException(FEILMELDING + arbeidsgiver);
    }

    static Arbeidsforhold mapForInntektsmelding(InntektsmeldingDto im) {
        return mapArbeidsforhold(im.getArbeidsgiver(), im.getArbeidsforholdRef());
    }
}
