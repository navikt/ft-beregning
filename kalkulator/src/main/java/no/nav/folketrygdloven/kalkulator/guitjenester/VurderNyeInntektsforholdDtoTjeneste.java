package no.nav.folketrygdloven.kalkulator.guitjenester;


import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.LønnsinntektBeskrivelse;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.InntektsforholdDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderInntektsforholdPeriodeDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.VurderNyttInntektsforholdDto;

public class VurderNyeInntektsforholdDtoTjeneste {

    public static VurderNyttInntektsforholdDto lagDto(BeregningsgrunnlagGUIInput input) {

        if (input.getAvklaringsbehov().stream().noneMatch(a -> a.getDefinisjon().equals(AvklaringsbehovDefinisjon.VURDER_NYTT_INNTKTSFRHLD))) {
            return null;
        }
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var iayGrunnlag = input.getIayGrunnlag();

        return lagVurderNyttInntektsforholdDto(beregningsgrunnlag, iayGrunnlag
        );
    }

    public static VurderNyttInntektsforholdDto lagVurderNyttInntektsforholdDto(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                               InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var bgPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();

        var periodeListe = bgPerioder.stream()
                .filter(it -> !it.getTilkomneInntekter().isEmpty())
                .map(it -> mapPeriode(iayGrunnlag, it, bgPerioder))
                .collect(Collectors.toList());

        if (!periodeListe.isEmpty()) {
            return new VurderNyttInntektsforholdDto(periodeListe, harMottattOmsorgsstønadEllerFosterhjemsgodtgjørelseEtterStp(iayGrunnlag, beregningsgrunnlag.getSkjæringstidspunkt()));
        }

        return null;
    }

    private static VurderInntektsforholdPeriodeDto mapPeriode(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                              BeregningsgrunnlagPeriodeDto periode,
                                                              List<BeregningsgrunnlagPeriodeDto> allePerioder) {
        var innteksforholdListe = mapInntektforholdDtoListe(iayGrunnlag, periode, allePerioder);
        return new VurderInntektsforholdPeriodeDto(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), innteksforholdListe.stream().toList());
    }


    private static boolean harMottattOmsorgsstønadEllerFosterhjemsgodtgjørelseEtterStp(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate stp) {
        return new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister()).filter(InntektskildeType.INNTEKT_BEREGNING)
                .getFiltrertInntektsposter().stream()
                .filter(p -> p.getPeriode().getTomDato().isAfter(stp))
                .anyMatch(p -> p.getLønnsinnntektBeskrivelse() != null && p.getLønnsinnntektBeskrivelse().equals(LønnsinntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE));
    }

    private static Set<InntektsforholdDto> mapInntektforholdDtoListe(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                     BeregningsgrunnlagPeriodeDto periode,
                                                                     List<BeregningsgrunnlagPeriodeDto> allePerioder) {

        return periode.getTilkomneInntekter()
                .stream()
                .map(a -> mapTilInntektsforhold(a, iayGrunnlag, utledPeriode(a, allePerioder)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Periode utledPeriode(TilkommetInntektDto a, List<BeregningsgrunnlagPeriodeDto> allePerioder) {
        var perioderMedInntekt = allePerioder.stream().filter(it -> it.getTilkomneInntekter().stream().anyMatch(i -> i.matcher(a))).map(BeregningsgrunnlagPeriodeDto::getPeriode).collect(Collectors.toSet());
        var fom = perioderMedInntekt.stream().map(Intervall::getFomDato).min(Comparator.naturalOrder()).orElseThrow();
        var tom = perioderMedInntekt.stream().map(Intervall::getTomDato).max(Comparator.naturalOrder()).orElseThrow();
        return new Periode(fom, tom);
    }

    private static InntektsforholdDto mapTilInntektsforhold(TilkommetInntektDto tilkommetInntektDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                            Periode periode) {
        var inntektsmelding = iayGrunnlag.getInntektsmeldinger().stream()
                .flatMap(it -> it.getAlleInntektsmeldinger().stream())
                .filter(im -> Objects.equals(im.getArbeidsgiver(), tilkommetInntektDto.getArbeidsgiver().orElse(null))
                        && tilkommetInntektDto.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef()))
                .findFirst();

        return new InntektsforholdDto(
                tilkommetInntektDto.getAktivitetStatus(),
                tilkommetInntektDto.getArbeidsgiver().map(Arbeidsgiver::getIdentifikator).orElse(null),
                tilkommetInntektDto.getArbeidsforholdRef().getReferanse(),
                finnEksternArbeidsforholdId(tilkommetInntektDto.getArbeidsgiver(), tilkommetInntektDto.getArbeidsforholdRef(), iayGrunnlag).map(EksternArbeidsforholdRef::getReferanse).orElse(null),
                periode,
                inntektsmelding.map(im -> im.getInntektBeløp().multipliser(KonfigTjeneste.getMånederIÅr())).map(Beløp::intValue).orElse(null),
                Beløp.safeVerdi(tilkommetInntektDto.getBruttoInntektPrÅr()) != null ? tilkommetInntektDto.getBruttoInntektPrÅr().intValue() : null,
                tilkommetInntektDto.skalRedusereUtbetaling()
        );
    }

    private static Optional<EksternArbeidsforholdRef> finnEksternArbeidsforholdId(Optional<Arbeidsgiver> arbeidsgiver,
                                                                                  InternArbeidsforholdRefDto arbeidsforholdRef,
                                                                                  InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (arbeidsgiver.isEmpty() || arbeidsforholdRef.getReferanse() == null) {
            return Optional.empty();
        }
        return iayGrunnlag.getArbeidsforholdInformasjon()
                .map(d -> d.finnEkstern(arbeidsgiver.get(), arbeidsforholdRef));
    }

}
