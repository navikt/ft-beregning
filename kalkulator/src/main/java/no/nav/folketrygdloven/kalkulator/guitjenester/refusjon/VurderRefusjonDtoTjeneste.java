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
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AndelerMedØktRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravSomKommerForSentDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;

public final class VurderRefusjonDtoTjeneste {

    private VurderRefusjonDtoTjeneste() {
        // Skjuler default
    }

    public static Optional<RefusjonTilVurderingDto> lagRefusjonTilVurderingDto(BeregningsgrunnlagGUIInput input) {
        /* Dto-en skal lages hvis
            1a. Det finnes andeler med økt refusjon sammenlignet med tidligere beregningsgrunnlag
            1b. Det finnes tidligere avklaringer på refusjon (refusjon overstyringer)
            2. Det finnes refusjonskrav som kommer for sent
         */

        // TODO: Se om alt dette kan forenkles, spesielt mht. tidligere avklaringer
        // Kan 1b og 2 eksistere samtidig?

        var beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes();
        var originaleGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().stream()
                .flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream()).toList();

        if (originaleGrunnlag.isEmpty() || beregningsgrunnlag.isEmpty() || beregningsgrunnlag.get().getGrunnbeløp() == null) {
            return Optional.empty();
        }

        List<RefusjonskravSomKommerForSentDto> refusjonskravSomKomForSentListe = input.isEnabled("refusjonsfrist.flytting",
            false) ? getRefusjonskravSomKomForSent(input) : Collections.emptyList();

        var grenseverdi = beregningsgrunnlag.get().getGrunnbeløp().multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());

        var andelerMedØktRefusjon = originaleGrunnlag.stream()
                .flatMap(originaltBg -> AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(beregningsgrunnlag.get(), originaltBg, grenseverdi, input.getYtelsespesifiktGrunnlag()).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, unikeElementer()));
        if (!andelerMedØktRefusjon.isEmpty()) {
            return LagVurderRefusjonDto.lagDto(input, andelerMedØktRefusjon, refusjonskravSomKomForSentListe);
        }

        var tidligereAndelerMedØktRefusjon = getAndelerMedØktRefusjonFraTidligereAvklaringer(input);
        if (!tidligereAndelerMedØktRefusjon.isEmpty()) {
            return LagVurderRefusjonDto.lagDto(input, tidligereAndelerMedØktRefusjon, refusjonskravSomKomForSentListe);
        }

        if (!refusjonskravSomKomForSentListe.isEmpty()) {
            return LagVurderRefusjonDto.lagDto(input, Collections.emptyMap(), refusjonskravSomKomForSentListe);
        }
        return Optional.empty();
    }

    static List<RefusjonskravSomKommerForSentDto> getRefusjonskravSomKomForSent(BeregningsgrunnlagGUIInput input) {
        var refusjonOverstyringer = getRefusjonOverstyringer(input);
        var arbeidsgivere = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgivereSomHarSøktRefusjonForSent(input.getIayGrunnlag(),
            input.getBeregningsgrunnlagGrunnlag(), input.getKravperioderPrArbeidsgiver(), input.getFagsakYtelseType());
        return arbeidsgivere.stream().map(arbeidsgiver -> {
            var dto = new RefusjonskravSomKommerForSentDto();
            dto.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
            sjekkOmEventueltRefusjonskravErGyldig(arbeidsgiver.getIdentifikator(), refusjonOverstyringer,
                input.getSkjæringstidspunktForBeregning()).ifPresent(dto::setErRefusjonskravGyldig);
            return dto;
        }).toList();
    }

    // Metode for å støtte visning av saker som tidligere er løst men som av ulike grunner ikke lenger gir samme resultat i avklaringsbehovutledning
    private static Map<Intervall, List<RefusjonAndel>> getAndelerMedØktRefusjonFraTidligereAvklaringer(BeregningsgrunnlagGUIInput input) {
        var hardkodetIntervall = Intervall.fraOgMed(input.getSkjæringstidspunktForBeregning()); // Bruker hele perioden det kan kreves refusjon for
        var refusjonOverstyringer = getRefusjonOverstyringer(input)
                .stream()
                .filter(refusjonOverstyring -> !refusjonOverstyring.getRefusjonPerioder().isEmpty());

        var andeler = refusjonOverstyringer
            .flatMap(refusjonOverstyring -> refusjonOverstyring.getRefusjonPerioder().stream()
                .map(refusjonPeriode -> new RefusjonAndel(
                    AktivitetStatus.ARBEIDSTAKER,
                    refusjonOverstyring.getArbeidsgiver(),
                    refusjonPeriode.getArbeidsforholdRef(),
                    Beløp.ZERO,
                    Beløp.ZERO)))
            .toList();

        return andeler.isEmpty() ? Collections.emptyMap() : Map.of(hardkodetIntervall, andeler);
    }

    private static List<BeregningRefusjonOverstyringDto> getRefusjonOverstyringer(BeregningsgrunnlagGUIInput input) {
        return input.getBeregningsgrunnlagGrunnlag()
            .getRefusjonOverstyringer()
            .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
            .orElse(Collections.emptyList());
    }

    private static Optional<Boolean> sjekkOmEventueltRefusjonskravErGyldig(String arbeidsgiverIdentifikator,
                                                                           List<BeregningRefusjonOverstyringDto> refusjonOverstyringer,
                                                                           LocalDate skjæringstidspunktForBeregning) {
        return refusjonOverstyringer.stream()
            .filter(refusjonOverstyring -> refusjonOverstyring.getArbeidsgiver().getIdentifikator().equals(arbeidsgiverIdentifikator))
            .findFirst()
            .flatMap(refusjonOverstyring -> getErFristUtvidet(refusjonOverstyring, skjæringstidspunktForBeregning));
    }

    private static Optional<Boolean> getErFristUtvidet(BeregningRefusjonOverstyringDto refusjonOverstyring,
                                                       LocalDate skjæringstidspunktForBeregning) {
        return refusjonOverstyring.getFørsteMuligeRefusjonFom()
                .map(skjæringstidspunktForBeregning::isEqual)
                .or(refusjonOverstyring::getErFristUtvidet);
    }

    private static BinaryOperator<List<RefusjonAndel>> unikeElementer() {
        return (andeler1, andeler2) -> Stream.concat(andeler1.stream(), andeler2.stream()).distinct().toList();
    }
}
