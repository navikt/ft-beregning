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

    public static BeregningsgrunnlagDto justerBeregningsgrunnlagForVurdertRefusjonsfrist(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                         List<VurderRefusjonAndelBeregningsgrunnlagDto> refusjonAndeler) {
        var refusjonAndelerMedUtvidetFrist = getRefusjonAndelerForErFristUtvidet(refusjonAndeler, Boolean.TRUE);
        oppdaterSaksbehandletRefusjonPerArbeidsforhold(beregningsgrunnlag, refusjonAndelerMedUtvidetFrist, Utfall.UNDERKJENT, Utfall.GODKJENT);

        var refusjonAndelerUtenUtvidetFrist = getRefusjonAndelerForErFristUtvidet(refusjonAndeler, Boolean.FALSE);
        oppdaterSaksbehandletRefusjonPerArbeidsforhold(beregningsgrunnlag, refusjonAndelerUtenUtvidetFrist, Utfall.GODKJENT, Utfall.UNDERKJENT);

        return beregningsgrunnlag;
    }

    private static void oppdaterSaksbehandletRefusjonPerArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                       List<VurderRefusjonAndelBeregningsgrunnlagDto> refusjonAndeler,
                                                                       Utfall forrigeUtfall,
                                                                       Utfall saksbehandletUtfall) {
        refusjonAndeler.forEach(refusjonAndel -> {
            var matchendeArbeidsforhold = getMatchendeArbeidsforhold(beregningsgrunnlag, refusjonAndel);
            if (matchendeArbeidsforhold.isEmpty()) {
                throw new IllegalStateException(
                    "VurderRefusjonUtfallTjeneste: Fant ingen arbeidsforhold med orgnr: " + refusjonAndel.getArbeidsgiverOrgnr());
            }
            matchendeArbeidsforhold.stream()
                .filter(arbeidsforhold -> harRefusjonMedUtfall(arbeidsforhold, forrigeUtfall))
                .forEach(arbeidsforhold -> oppdaterRefusjonTilSaksbehandletUtfall(arbeidsforhold, saksbehandletUtfall));
        });
    }

    private static List<VurderRefusjonAndelBeregningsgrunnlagDto> getRefusjonAndelerForErFristUtvidet(List<VurderRefusjonAndelBeregningsgrunnlagDto> refusjonAndeler,
                                                                                                      Boolean erFristUtvidet) {
        return refusjonAndeler.stream().filter(andel -> andel.getErFristUtvidet().filter(a -> a.equals(erFristUtvidet)).isPresent()).toList();
    }

    private static List<BGAndelArbeidsforholdDto> getMatchendeArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                             VurderRefusjonAndelBeregningsgrunnlagDto refusjonAndel) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder()
            .stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
            .flatMap(Optional::stream)
            .filter(arbeidsforhold -> Objects.equals(arbeidsforhold.getArbeidsforholdOrgnr(), refusjonAndel.getArbeidsgiverOrgnr()))
            .toList();
    }

    private static boolean harRefusjonMedUtfall(BGAndelArbeidsforholdDto arbeidsforhold, Utfall forrigeUtfall) {
        return arbeidsforhold.getRefusjon().map(Refusjon::getRefusjonskravFristUtfall).filter(forrigeUtfall::equals).isPresent();
    }

    private static void oppdaterRefusjonTilSaksbehandletUtfall(BGAndelArbeidsforholdDto arbeidsforhold,
                                                               Utfall saksbehandletUtfall) {
        var refusjon = arbeidsforhold.getRefusjon().orElseThrow();
        var godkjentRefusjon = new Refusjon(refusjon.getRefusjonskravPrÅr(), refusjon.getSaksbehandletRefusjonPrÅr(),
            refusjon.getFordeltRefusjonPrÅr(), refusjon.getManueltFordeltRefusjonPrÅr(), refusjon.getHjemmelForRefusjonskravfrist(),
            saksbehandletUtfall);
        BGAndelArbeidsforholdDto.Builder.oppdater(Optional.of(arbeidsforhold)).medRefusjon(godkjentRefusjon);
    }
}
