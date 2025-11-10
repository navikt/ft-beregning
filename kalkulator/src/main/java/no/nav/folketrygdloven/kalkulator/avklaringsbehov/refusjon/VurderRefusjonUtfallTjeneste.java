package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;


public final class VurderRefusjonUtfallTjeneste {

    private VurderRefusjonUtfallTjeneste() {
        // Statisk klasse, skal aldri kalles
    }

    public static BeregningsgrunnlagDto justerBeregningsgrunnlagForVurdertRefusjonsfrist(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                         List<VurderRefusjonAndelBeregningsgrunnlagDto> refusjonAndeler) {
        var justertBeregningsgrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        var refusjonAndelerMedUtvidetFrist = getRefusjonAndelerForErFristUtvidet(refusjonAndeler, Boolean.TRUE);
        oppdaterSaksbehandletRefusjonPerArbeidsforhold(justertBeregningsgrunnlag, refusjonAndelerMedUtvidetFrist);

        var refusjonAndelerUtenUtvidetFrist = getRefusjonAndelerForErFristUtvidet(refusjonAndeler, Boolean.FALSE);
        verifiserUnderkjentRefusjon(justertBeregningsgrunnlag, refusjonAndelerUtenUtvidetFrist);

        return justertBeregningsgrunnlag;
    }

    private static void oppdaterSaksbehandletRefusjonPerArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                       List<VurderRefusjonAndelBeregningsgrunnlagDto> refusjonAndeler) {
        refusjonAndeler.forEach(refusjonAndel -> getMatchendeArbeidsforhold(beregningsgrunnlag, refusjonAndel).stream()
            .filter(VurderRefusjonUtfallTjeneste::harUnderkjentRefusjon)
            .forEach(VurderRefusjonUtfallTjeneste::godkjennRefusjon));
    }

    private static void verifiserUnderkjentRefusjon(BeregningsgrunnlagDto beregningsgrunnlag,
                                                    List<VurderRefusjonAndelBeregningsgrunnlagDto> refusjonAndeler) {
        // Skal ikke trenge å oppdatere utfallet da det skal være riktig fra steget, validerer derfor tilstanden
        refusjonAndeler.forEach(refusjonAndel -> getMatchendeArbeidsforhold(beregningsgrunnlag, refusjonAndel).stream()
            .filter(VurderRefusjonUtfallTjeneste::harUnderkjentRefusjon)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "VurderRefusjonUtfallTjeneste: Forventet å finne arbeidsforhold med underkjent refusjon for arbeidsgiver: "
                    + refusjonAndel.getArbeidsgiver())));
    }

    private static List<VurderRefusjonAndelBeregningsgrunnlagDto> getRefusjonAndelerForErFristUtvidet(List<VurderRefusjonAndelBeregningsgrunnlagDto> refusjonAndeler,
                                                                                                      Boolean erFristUtvidet) {
        return refusjonAndeler.stream().filter(andel -> andel.getErFristUtvidet().filter(a -> a.equals(erFristUtvidet)).isPresent()).toList();
    }

    private static List<BGAndelArbeidsforholdDto> getMatchendeArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                             VurderRefusjonAndelBeregningsgrunnlagDto refusjonAndel) {
        var matchendeArbeidsforhold = beregningsgrunnlag.getBeregningsgrunnlagPerioder()
            .stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .flatMap(Optional::stream)
            .filter(arbeidsforhold -> Objects.equals(arbeidsforhold.getArbeidsgiver(), refusjonAndel.getArbeidsgiver()))
            .toList();
        if (matchendeArbeidsforhold.isEmpty()) {
            throw new IllegalStateException("VurderRefusjonUtfallTjeneste: Fant ingen andel med arbeidsgiver: " + refusjonAndel.getArbeidsgiver());
        }
        return matchendeArbeidsforhold;
    }

    private static boolean harUnderkjentRefusjon(BGAndelArbeidsforholdDto arbeidsforhold) {
        return arbeidsforhold.getRefusjon().map(Refusjon::getRefusjonskravFristUtfall).filter(Utfall.UNDERKJENT::equals).isPresent();
    }

    private static void godkjennRefusjon(BGAndelArbeidsforholdDto arbeidsforhold) {
        var refusjon = arbeidsforhold.getRefusjon().orElseThrow();
        var godkjentRefusjon = new Refusjon(refusjon.getRefusjonskravPrÅr(), refusjon.getSaksbehandletRefusjonPrÅr(),
            refusjon.getFordeltRefusjonPrÅr(), refusjon.getManueltFordeltRefusjonPrÅr(), refusjon.getHjemmelForRefusjonskravfrist(), Utfall.GODKJENT);
        BGAndelArbeidsforholdDto.Builder.oppdater(Optional.of(arbeidsforhold)).medRefusjon(godkjentRefusjon);
    }
}
