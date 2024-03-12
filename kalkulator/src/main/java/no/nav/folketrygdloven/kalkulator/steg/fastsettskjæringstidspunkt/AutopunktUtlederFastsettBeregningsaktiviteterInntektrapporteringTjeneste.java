package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.folketrygdloven.beregningsgrunnlag.BevegeligeHelligdagerUtil;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste {

    private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";

    private AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste() {
        // Skjuler default konstruktør
    }


    public static Optional<LocalDate> skalVentePåInnrapporteringAvInntektATFL(BeregningsgrunnlagInput input,
                                                                              List<Arbeidsgiver> arbeidsgivere,
                                                                              LocalDate dagensDato,
                                                                              BeregningAktivitetAggregatDto aktivitetAggregatDto,
                                                                              LocalDate skjæringstidspunktForBeregning) {
        if (!erPåvirketAvInntektsrapporteringsfrist(input, dagensDato, aktivitetAggregatDto, skjæringstidspunktForBeregning)) {
            return Optional.empty();
        }
        boolean harArbeidUtenIM = !alleArbeidsforholdHarInntektsmelding(aktivitetAggregatDto, arbeidsgivere, skjæringstidspunktForBeregning);
        boolean erFrilans = erFrilans(aktivitetAggregatDto, skjæringstidspunktForBeregning);
        return erFrilans || harArbeidUtenIM
                ? Optional.of(utledBehandlingPåVentFrist(input, skjæringstidspunktForBeregning))
                : Optional.empty();
    }

    public static Optional<LocalDate> skalVentePåInnrapporteringAvInntektFL(BeregningsgrunnlagInput input,
                                                                            LocalDate dagensDato,
                                                                            BeregningAktivitetAggregatDto aktivitetAggregatDto,
                                                                            LocalDate skjæringstidspunktForBeregning) {
        if (!erPåvirketAvInntektsrapporteringsfrist(input, dagensDato, aktivitetAggregatDto, skjæringstidspunktForBeregning)) {
            return Optional.empty();
        }
        boolean erFrilans = erFrilans(aktivitetAggregatDto, skjæringstidspunktForBeregning);
        return erFrilans
                ? Optional.of(utledBehandlingPåVentFrist(input, skjæringstidspunktForBeregning))
                : Optional.empty();
    }

    private static boolean erPåvirketAvInntektsrapporteringsfrist(BeregningsgrunnlagInput input,
                                                                  LocalDate dagensDato,
                                                                  BeregningAktivitetAggregatDto aktivitetAggregatDto,
                                                                  LocalDate skjæringstidspunktForBeregning) {
        boolean harRelevantAktivitet = harAktivitetStatuserSomKanSettesPåVent(aktivitetAggregatDto, skjæringstidspunktForBeregning);
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(skjæringstidspunktForBeregning);
        LocalDate originalFrist = beregningsperiodeTom.plusDays((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
        LocalDate fristMedHelligdagerInkl = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(originalFrist);
        boolean erFristPassert = dagensDato.isAfter(fristMedHelligdagerInkl);
        return harRelevantAktivitet && !erFristPassert;
    }

    private static boolean erFrilans(BeregningAktivitetAggregatDto aktivitetAggregatDto, LocalDate skjæringstidspunkt) {
        return aktivitetAggregatDto.getAktiviteterPåDato(skjæringstidspunkt).stream()
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType)
                .anyMatch(t -> t.equals(OpptjeningAktivitetType.FRILANS));

    }

    private static boolean harAktivitetStatuserSomKanSettesPåVent(BeregningAktivitetAggregatDto aktivitetAggregatDto, LocalDate skjæringstidspunkt) {
        return aktivitetAggregatDto.getBeregningAktiviteter().stream()
                .filter(ba -> ba.getPeriode().inkluderer(skjæringstidspunkt))
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType)
                .anyMatch(type -> type.equals(OpptjeningAktivitetType.ARBEID) || type.equals(OpptjeningAktivitetType.FRILANS));
    }

    private static LocalDate utledBehandlingPåVentFrist(BeregningsgrunnlagInput input, LocalDate skjæringstidspunktForBeregning) {
        LocalDate beregningsperiodeTom = hentBeregningsperiodeTomForATFL(skjæringstidspunktForBeregning);
        LocalDate frist = beregningsperiodeTom.plusDays((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
        return BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(frist).plusDays(1);
    }

    private static LocalDate hentBeregningsperiodeTomForATFL(LocalDate skjæringstidspunkt) {
        return skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    }

    private static boolean alleArbeidsforholdHarInntektsmelding(BeregningAktivitetAggregatDto aktivitetAggregatDto,
                                                                List<Arbeidsgiver> arbeidsgivere,
                                                                LocalDate skjæringstidspunktBeregning) {
        return hentAlleArbeidsgiverePåSkjæringstidspunktet(aktivitetAggregatDto, skjæringstidspunktBeregning)
                .filter(arbeidsgiver -> !OrgNummer.erKunstig(arbeidsgiver.getOrgnr())) //Arbeidsforhold er ikke lagt til av saksbehandler
                .allMatch(arbeidsgiver -> arbeidsgivere
                        .stream()
                        .anyMatch(v -> v.equals(arbeidsgiver)));
    }

    private static Stream<Arbeidsgiver> hentAlleArbeidsgiverePåSkjæringstidspunktet(BeregningAktivitetAggregatDto aktivitetAggregatDto, LocalDate skjæringstidspunktBeregning) {
        return aktivitetAggregatDto.getAktiviteterPåDato(skjæringstidspunktBeregning)
                .stream()
                .map(BeregningAktivitetDto::getArbeidsgiver)
                .filter(Objects::nonNull);
    }
}
