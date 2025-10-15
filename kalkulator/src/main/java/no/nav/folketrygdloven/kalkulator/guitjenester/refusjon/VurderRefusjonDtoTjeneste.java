package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AndelerMedØktRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravForSentDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonAndelTilVurderingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;

public final class VurderRefusjonDtoTjeneste {

    private VurderRefusjonDtoTjeneste() {
        // Skjuler default
    }

    public static Optional<RefusjonTilVurderingDto> lagRefusjonTilVurderingDto(BeregningsgrunnlagGUIInput input) {
        if (input.getAvklaringsbehov()
            .stream()
            .noneMatch(a -> a.getDefinisjon().equals(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV))) {
            return Optional.empty();
        }

        var refusjonskravForSentListe = hentRefusjonskravForSent(input);
        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlag();
        var forrigeGrunnlagListe = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().stream()
            .flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream()).toList();

        if (!forrigeGrunnlagListe.isEmpty() && beregningsgrunnlag.getGrunnbeløp() != null) {
            var refusjonOverstyringer = hentRefusjonOverstyringer(input);

            var andelerMedØktRefusjonFraTidligereBehandlinger = hentAndelerMedØktRefusjonFraTidligereBehandlinger(input.getYtelsespesifiktGrunnlag(),
                forrigeGrunnlagListe, beregningsgrunnlag);
            if (!andelerMedØktRefusjonFraTidligereBehandlinger.isEmpty()) {
                return Optional.of(new RefusjonTilVurderingDto(
                    lagAndeler(input, andelerMedØktRefusjonFraTidligereBehandlinger, beregningsgrunnlag, forrigeGrunnlagListe,
                        refusjonOverstyringer), refusjonskravForSentListe));
            }

            var andelerMedØktRefusjonFraOverstyringer = hentAndelerMedØktRefusjonFraOverstyringer(refusjonOverstyringer,
                input.getSkjæringstidspunktForBeregning());
            if (!andelerMedØktRefusjonFraOverstyringer.isEmpty()) {
                return Optional.of(new RefusjonTilVurderingDto(
                    lagAndeler(input, andelerMedØktRefusjonFraOverstyringer, beregningsgrunnlag, forrigeGrunnlagListe, refusjonOverstyringer),
                    refusjonskravForSentListe));
            }
        }

        if (!refusjonskravForSentListe.isEmpty()) {
            return Optional.of(new RefusjonTilVurderingDto(Collections.emptyList(), refusjonskravForSentListe));
        }

        return Optional.empty();
    }

    static List<RefusjonskravForSentDto> hentRefusjonskravForSent(BeregningsgrunnlagGUIInput input) {
        if (!input.isEnabled("refusjonsfrist.flytting", false)) {
            return Collections.emptyList();
        }

        var refusjonOverstyringer = hentRefusjonOverstyringer(input);
        var arbeidsgivere = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgivereSomHarSøktRefusjonForSent(input.getIayGrunnlag(),
            input.getBeregningsgrunnlagGrunnlag(), input.getKravperioderPrArbeidsgiver(), input.getFagsakYtelseType());
        return arbeidsgivere.stream().map(arbeidsgiver -> lagRefusjonskravForSentDto(input, arbeidsgiver, refusjonOverstyringer)).toList();
    }

    private static List<RefusjonAndelTilVurderingDto> lagAndeler(BeregningsgrunnlagGUIInput input,
                                                                 Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon,
                                                                 BeregningsgrunnlagDto beregningsgrunnlag,
                                                                 List<BeregningsgrunnlagDto> forrigeGrunnlagListe,
                                                                 List<BeregningRefusjonOverstyringDto> refusjonOverstyringer) {
        var arbeidsforholdInformasjon = input.getIayGrunnlag().getArbeidsforholdInformasjon();
        return RefusjonAndelTilVurderingDtoTjeneste.lagDtoListe(andelerMedØktRefusjon, beregningsgrunnlag, forrigeGrunnlagListe, refusjonOverstyringer,
            arbeidsforholdInformasjon);
    }

    private static Map<Intervall, List<RefusjonAndel>> hentAndelerMedØktRefusjonFraTidligereBehandlinger(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                                                                         List<BeregningsgrunnlagDto> forrigeGrunnlagListe,
                                                                                                         BeregningsgrunnlagDto beregningsgrunnlag) {
        var grenseverdi = beregningsgrunnlag.getGrunnbeløp().multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());
        return forrigeGrunnlagListe.stream()
            .flatMap(forrigeGrunnlag -> AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(beregningsgrunnlag, forrigeGrunnlag, grenseverdi,
                ytelsespesifiktGrunnlag).entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, unikeElementer()));
    }

    // Metode for å støtte visning av saker som tidligere er løst men som av ulike grunner ikke lenger gir samme resultat i avklaringsbehovutledning
    private static Map<Intervall, List<RefusjonAndel>> hentAndelerMedØktRefusjonFraOverstyringer(List<BeregningRefusjonOverstyringDto> refusjonOverstyringer, LocalDate skjæringstidspunktForBeregning) {
        var andeler = refusjonOverstyringer.stream()
            .flatMap(refusjonOverstyring -> refusjonOverstyring.getRefusjonPerioder()
                .stream()
                .map(refusjonPeriode -> opprettRefusjonAndel(refusjonOverstyring, refusjonPeriode)))
            .toList();

        // Intervallet bruker hele perioden det kan kreves refusjon for
        return andeler.isEmpty() ? Collections.emptyMap() : Map.of(Intervall.fraOgMed(skjæringstidspunktForBeregning), andeler);
    }

    private static RefusjonskravForSentDto lagRefusjonskravForSentDto(BeregningsgrunnlagGUIInput input,
                                                                      Arbeidsgiver arbeidsgiver,
                                                                      List<BeregningRefusjonOverstyringDto> refusjonOverstyringer) {
        var dto = new RefusjonskravForSentDto();
        dto.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
        dto.setErRefusjonskravGyldig(
            sjekkOmRefusjonskravErGyldig(arbeidsgiver.getIdentifikator(), refusjonOverstyringer, input.getSkjæringstidspunktForBeregning()));
        return dto;
    }

    private static List<BeregningRefusjonOverstyringDto> hentRefusjonOverstyringer(BeregningsgrunnlagGUIInput input) {
        return input.getBeregningsgrunnlagGrunnlag()
            .getRefusjonOverstyringer()
            .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
            .orElse(Collections.emptyList());
    }

    private static Boolean sjekkOmRefusjonskravErGyldig(String arbeidsgiverIdentifikator,
                                                        List<BeregningRefusjonOverstyringDto> refusjonOverstyringer,
                                                        LocalDate skjæringstidspunktForBeregning) {
        return refusjonOverstyringer.stream()
            .filter(refusjonOverstyring -> refusjonOverstyring.getArbeidsgiver().getIdentifikator().equals(arbeidsgiverIdentifikator))
            .findFirst()
            .flatMap(refusjonOverstyring -> erFristUtvidet(refusjonOverstyring, skjæringstidspunktForBeregning))
            .orElse(null);
    }

    private static Optional<Boolean> erFristUtvidet(BeregningRefusjonOverstyringDto refusjonOverstyring,
                                                    LocalDate skjæringstidspunktForBeregning) {
        return refusjonOverstyring.getFørsteMuligeRefusjonFom()
                .map(skjæringstidspunktForBeregning::isEqual)
                .or(refusjonOverstyring::getErFristUtvidet);
    }

    private static RefusjonAndel opprettRefusjonAndel(BeregningRefusjonOverstyringDto refusjonOverstyring, BeregningRefusjonPeriodeDto refusjonPeriode) {
        return new RefusjonAndel(AktivitetStatus.ARBEIDSTAKER, refusjonOverstyring.getArbeidsgiver(), refusjonPeriode.getArbeidsforholdRef(),
            Beløp.ZERO, Beløp.ZERO);
    }

    private static BinaryOperator<List<RefusjonAndel>> unikeElementer() {
        return (andeler1, andeler2) -> Stream.concat(andeler1.stream(), andeler2.stream()).distinct().toList();
    }
}
