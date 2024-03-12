package no.nav.folketrygdloven.kalkulator;

import static no.nav.folketrygdloven.kalkulator.OpprettKravPerioderFraInntektsmeldinger.opprett;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.SvangerskapspengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.utils.Tuple;


public class BeregningsgrunnlagInputTestUtil {

    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";


    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(KoblingReferanse koblingReferanse,
                                                                        Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand> aktivt) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, null, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(aktivt.getElement1(), aktivt.getElement2());
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }

    public static HåndterBeregningsgrunnlagInput lagHåndteringInputMedBeregningsgrunnlag(KoblingReferanse koblingReferanse,
                                                                                         Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand> aktivt,
                                                                                         Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand> forrige) {
        var input = new BeregningsgrunnlagInput(koblingReferanse, null, null, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(aktivt.getElement1(), aktivt.getElement2());
        var inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        var håndterBeregningsgrunnlagInput = new HåndterBeregningsgrunnlagInput(inputMedBeregningsgrunnlag, forrige.getElement2());
        var foorrigeGr = forrige.getElement1() == null ? null : lagGrunnlag(forrige.getElement1(), forrige.getElement2());
        håndterBeregningsgrunnlagInput = håndterBeregningsgrunnlagInput.medForrigeGrunnlagFraHåndtering(foorrigeGr);
        return håndterBeregningsgrunnlagInput;
    }

    public static HåndterBeregningsgrunnlagInput lagHåndteringInputMedBeregningsgrunnlag(KoblingReferanse koblingReferanse,
                                                                                         Tuple<BeregningsgrunnlagDto, BeregningsgrunnlagTilstand> aktivt,
                                                                                         BeregningsgrunnlagTilstand forrigeTilstand) {
        var input = new BeregningsgrunnlagInput(koblingReferanse, null, null, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(aktivt.getElement1(), aktivt.getElement2());
        var inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return new HåndterBeregningsgrunnlagInput(inputMedBeregningsgrunnlag, forrigeTilstand);
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(KoblingReferanse koblingReferanse, BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, null, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(beregningsgrunnlag, beregningsgrunnlagTilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }

    private static BeregningsgrunnlagGrunnlagDto lagGrunnlag(BeregningsgrunnlagDto beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlag).build(beregningsgrunnlagTilstand);
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlag(KoblingReferanse koblingReferanse, BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder, BeregningsgrunnlagTilstand tilstand) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, null, List.of(), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }

    public static BeregningsgrunnlagInput lagInputMedIAYOgOpptjeningsaktiviteter(KoblingReferanse koblingReferanse,
                                                                                 OpptjeningAktiviteterDto opptjeningAktiviteterDto,
                                                                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag, Dekningsgrad dekningsgrad) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(dekningsgrad, false);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, opptjeningAktiviteterDto,
                opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning()), foreldrepengerGrunnlag);
        input.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return input;
    }

    public static ForeslåBeregningsgrunnlagInput lagForeslåttBeregningsgrunnlagInput(KoblingReferanse koblingReferanse,
                                                                                     BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                                     BeregningsgrunnlagTilstand tilstand,
                                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
                opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning()), foreldrepengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return new ForeslåBeregningsgrunnlagInput(new StegProsesseringInput(inputMedBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT))
                .medGrunnbeløpInput(GrunnbeløpMock.GRUNNBELØPINPUT);
    }

    public static FortsettForeslåBeregningsgrunnlagInput lagFortsettForeslåttBeregningsgrunnlagInput(KoblingReferanse koblingReferanse,
                                                                                     BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                                     BeregningsgrunnlagTilstand tilstand,
                                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
                opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning()), foreldrepengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return new FortsettForeslåBeregningsgrunnlagInput(new StegProsesseringInput(inputMedBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT))
                .medGrunnbeløpInput(GrunnbeløpMock.GRUNNBELØPINPUT);
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(KoblingReferanse koblingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
                opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning()), foreldrepengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagGrunnlag(KoblingReferanse koblingReferanse,
                                                                                BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                                BeregningsgrunnlagTilstand tilstand) {
        var foreldrepengerGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, null, null,
                Collections.emptyList(), foreldrepengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }


    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(KoblingReferanse koblingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                             SvangerskapspengerGrunnlag svangerskapspengerGrunnlag) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
                opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning()), svangerskapspengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }

    public static ForeslåBeregningsgrunnlagInput lagForeslåttBeregningsgrunnlagInput(KoblingReferanse koblingReferanse,
                                                                                     BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                                     BeregningsgrunnlagTilstand tilstand,
                                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                     OmsorgspengerGrunnlag omsorgspengerGrunnlag) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
                opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning()), omsorgspengerGrunnlag);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return new ForeslåBeregningsgrunnlagInput(new StegProsesseringInput(inputMedBeregningsgrunnlag, BeregningsgrunnlagTilstand.FORESLÅTT))
                .medGrunnbeløpInput(GrunnbeløpMock.GRUNNBELØPINPUT);
    }

    public static BeregningsgrunnlagInput lagInputMedBeregningsgrunnlagOgIAY(KoblingReferanse koblingReferanse,
                                                                             BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                             BeregningsgrunnlagTilstand tilstand,
                                                                             InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                             Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        BeregningsgrunnlagInput input = new BeregningsgrunnlagInput(koblingReferanse, iayGrunnlag, null,
                opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning(), førsteInnsendingAvRefusjonMap), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        BeregningsgrunnlagInput inputMedBeregningsgrunnlag = input.medBeregningsgrunnlagGrunnlag(grunnlag);
        inputMedBeregningsgrunnlag.leggTilKonfigverdi(INNTEKT_RAPPORTERING_FRIST_DATO, 5);
        return inputMedBeregningsgrunnlag;
    }

}
