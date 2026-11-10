package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Arbeidsgiver;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell.Arbeidsforholdsinformasjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;

public final class MapRefusjonsgrunnlagFraVLTilRegel {

    private MapRefusjonsgrunnlagFraVLTilRegel() {
    }

    public static Optional<RefusjonsgrunnlagModell> map(BeregningsgrunnlagInput input) {
        return switch (input.getFagsakYtelseType()) {
            case FORELDREPENGER, SVANGERSKAPSPENGER, OMSORGSPENGER, PLEIEPENGER_SYKT_BARN,
                 OPPLÆRINGSPENGER, PLEIEPENGER_NÆRSTÅENDE -> Optional.of(mapGrunnlag(input));
            default -> Optional.empty();
        };
    }

    private static RefusjonsgrunnlagModell mapGrunnlag(BeregningsgrunnlagInput input) {
        var skjæringstidspunkt = input.getSkjæringstidspunktForBeregning();
        var yrkesaktiviteter = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(
            input.getIayGrunnlag(), input.getBeregningsgrunnlagGrunnlag(), skjæringstidspunkt);
        var ytelser = input.getIayGrunnlag().getAktørYtelseFraRegister()
            .map(AktørYtelseDto::getAlleYtelser).orElse(Collections.emptyList());
        var permisjonFilter = new PermisjonFilter(ytelser, yrkesaktiviteter, skjæringstidspunkt);
        var krav = input.getInntektsmeldinger().stream()
            .filter(MapRefusjonsgrunnlagFraVLTilRegel::harRefusjon)
            .filter(im -> yrkesaktiviteter.stream().anyMatch(ya -> ya.gjelderFor(im)))
            .map(im -> {
                var aktiviteterForIm = yrkesaktiviteter.stream().filter(ya -> ya.gjelderFor(im)).collect(Collectors.toSet());
                var ansattperioder = RefusjonsgrunnlagGyldigePerioder.finnAnsattperioder(aktiviteterForIm, skjæringstidspunkt);
                var startdato = RefusjonsgrunnlagGyldigePerioder.utledStartdato(input, im, aktiviteterForIm,
                    ansattperioder, permisjonFilter);
                var gyldigePerioder = RefusjonsgrunnlagGyldigePerioder.finnGyldigePerioder(input, im, startdato, ansattperioder);
                var perioder = NormaliserRefusjonsgrunnlag.periodiser(im, startdato, gyldigePerioder);
                var arbeidsgiver = im.getArbeidsgiver();
                var arbeidsforhold = new Arbeidsforholdsinformasjon(arbeidsgiver.getErVirksomhet()
                    ? Arbeidsgiver.medOrgnr(arbeidsgiver.getIdentifikator()) : Arbeidsgiver.medAktørId(arbeidsgiver.getIdentifikator()),
                    im.getArbeidsforholdRef().getReferanse(), startdato);
                var journalpost = Objects.requireNonNull(im.getJournalpostId(),
                    "Journalpost er påkrevd for refusjonskrav fra inntektsmelding");
                return new Refusjonskrav(arbeidsforhold, journalpost.getId(), perioder);
            })
            .toList();
        return new RefusjonsgrunnlagModell(krav, skjæringstidspunkt);
    }

    private static boolean harRefusjon(InntektsmeldingDto im) {
        return (im.getRefusjonBeløpPerMnd() != null && !im.getRefusjonBeløpPerMnd().erNullEller0())
            || !im.getEndringerRefusjon().isEmpty();
    }
}
