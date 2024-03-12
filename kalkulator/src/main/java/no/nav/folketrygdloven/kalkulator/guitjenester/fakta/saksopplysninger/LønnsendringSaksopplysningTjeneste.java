package no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt.BeregningsperiodeTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.LønnsendringScenario;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta.ArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta.LønnsendringSaksopplysningDto;

/**
 * Tjeneste for å mappe dto for lønnsendring
 */
public class LønnsendringSaksopplysningTjeneste {

    /**
     * Lager liste med dtoer for lønnsendring pr arbeidsforhold
     *
     * @param input Beregningsgrunnlaginput
     * @return Liste med dtoer
     */
    public static List<LønnsendringSaksopplysningDto> lagDto(BeregningsgrunnlagGUIInput input) {
        var lønnsendringArbeidsforhold = finnArbeidsforholdMedLønnsendring(input);
        return lønnsendringArbeidsforhold.stream().map(mapTilDto(input)).toList();


    }

    private static Set<FaktaArbeidsforholdDto> finnArbeidsforholdMedLønnsendring(BeregningsgrunnlagGUIInput input) {
        return input.getFaktaAggregat().stream()
                .flatMap(f -> f.getFaktaArbeidsforhold().stream())
                .filter(faktaArbeidsforholdDto -> faktaArbeidsforholdDto.getHarLønnsendringIBeregningsperiodenVurdering() != null && faktaArbeidsforholdDto.getHarLønnsendringIBeregningsperiodenVurdering())
                .collect(Collectors.toSet());
    }

    private static Function<FaktaArbeidsforholdDto, LønnsendringSaksopplysningDto> mapTilDto(BeregningsgrunnlagGUIInput input) {
        return arbeidsforhold -> getLønnsendringSaksopplysningDto(input, arbeidsforhold);
    }

    private static LønnsendringSaksopplysningDto getLønnsendringSaksopplysningDto(BeregningsgrunnlagGUIInput input, FaktaArbeidsforholdDto arbeidsforhold) {
        var sisteLønnsendringsdatoFørStp = finnSisteLønnsendring(input, arbeidsforhold);
        var andel = finnMatchendeAndel(input, arbeidsforhold).orElseThrow();
        return new LønnsendringSaksopplysningDto(sisteLønnsendringsdatoFørStp,
                utledScenario(sisteLønnsendringsdatoFørStp, input.getSkjæringstidspunktForBeregning(),
                        andel),
                mapArbeidsforhold(input, arbeidsforhold, andel)
        );
    }

    private static LocalDate finnSisteLønnsendring(BeregningsgrunnlagGUIInput input, FaktaArbeidsforholdDto arbeidsforhold) {
        return input.getIayGrunnlag().getAktørArbeidFraRegister().stream()
                .flatMap(a -> a.hentAlleYrkesaktiviteter().stream())
                .filter(ya -> ya.gjelderFor(arbeidsforhold.getArbeidsgiver(), arbeidsforhold.getArbeidsforholdRef()))
                .flatMap(ya -> ya.getAlleAktivitetsAvtaler().stream().map(AktivitetsAvtaleDto::getSisteLønnsendringsdato))
                .filter(Objects::nonNull)
                .filter(d -> d.isBefore(input.getSkjæringstidspunktForBeregning()))
                .max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalStateException("Fant ikke lønnsendring for " + arbeidsforhold + " før skjæringstidspunktet " + input.getSkjæringstidspunktForBeregning()));
    }

    private static ArbeidsforholdDto mapArbeidsforhold(BeregningsgrunnlagGUIInput input, FaktaArbeidsforholdDto arbeidsforhold, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return new ArbeidsforholdDto(andel.getAndelsnr(),
                arbeidsforhold.getArbeidsgiver().getIdentifikator(),
                finnEksternArbeidsforholdId(arbeidsforhold.getArbeidsgiver(), arbeidsforhold.getArbeidsforholdRef(), input.getIayGrunnlag())
                        .map(EksternArbeidsforholdRef::getReferanse).orElse(null)
        );
    }

    private static LønnsendringScenario utledScenario(LocalDate sisteLønnsendringsdatoFørStp,
                                                      LocalDate skjæringstidspunktForBeregning,
                                                      BeregningsgrunnlagPrStatusOgAndelDto andel) {

        var standardBeregningsperiode = new BeregningsperiodeTjeneste().fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunktForBeregning);
        if (andel.getFastsattAvSaksbehandler() || andel.getBeregningsperiode().equals(standardBeregningsperiode)) {
            return LønnsendringScenario.MANUELT_BEHANDLET;
        }
        var førsteDagIMånedFørStp = skjæringstidspunktForBeregning.minusMonths(1).withDayOfMonth(1);
        var førsteDagISammeMåneSomStp = skjæringstidspunktForBeregning.withDayOfMonth(1);
        if (sisteLønnsendringsdatoFørStp.isAfter(førsteDagIMånedFørStp) && sisteLønnsendringsdatoFørStp.isBefore(førsteDagISammeMåneSomStp)) {
            return LønnsendringScenario.DELVIS_MÅNEDSINNTEKT_SISTE_MND;
        }
        var førsteDagToMånederFørStp = skjæringstidspunktForBeregning.minusMonths(2).withDayOfMonth(1);
        if (sisteLønnsendringsdatoFørStp.isAfter(førsteDagToMånederFørStp)) {
            return LønnsendringScenario.FULL_MÅNEDSINNTEKT_EN_MND;
        }
        return LønnsendringScenario.FULL_MÅNEDSINNTEKT_TO_MND;
    }

    private static Optional<EksternArbeidsforholdRef> finnEksternArbeidsforholdId(Arbeidsgiver arbeidsgiver,
                                                                                  InternArbeidsforholdRefDto internArbeidsforholdRefDto,
                                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getArbeidsforholdInformasjon()
                .map(d -> d.finnEkstern(arbeidsgiver, internArbeidsforholdRefDto));
    }

    private static Optional<BeregningsgrunnlagPrStatusOgAndelDto> finnMatchendeAndel(BeregningsgrunnlagGUIInput input, FaktaArbeidsforholdDto a) {
        return input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(andel -> andel.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(andel -> andel.getBgAndelArbeidsforhold().isPresent() && andel.getBgAndelArbeidsforhold().get().getArbeidsgiver().equals(a.getArbeidsgiver()) &&
                        andel.getBgAndelArbeidsforhold().get().getArbeidsforholdRef().gjelderFor(a.getArbeidsforholdRef()))
                .findFirst();
    }

}
