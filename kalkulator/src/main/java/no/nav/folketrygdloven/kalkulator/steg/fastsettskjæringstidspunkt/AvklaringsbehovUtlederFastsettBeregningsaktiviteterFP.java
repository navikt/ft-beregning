package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import static java.util.Collections.emptyList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class AvklaringsbehovUtlederFastsettBeregningsaktiviteterFP implements AvklaringsbehovUtlederFastsettBeregningsaktiviteter {

    private static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovFP(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                 BeregningsgrunnlagInput input,
                                                                                 boolean erOverstyrt,
                                                                                 LocalDate skjæringstidspunktForBeregning) {
        Optional<AktørYtelseDto> aktørYtelse = input.getIayGrunnlag().getAktørYtelseFraRegister();
        Optional<LocalDate> ventPåRapporteringAvInntektFrist = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektFL(input, LocalDate.now(), beregningAktivitetAggregat, skjæringstidspunktForBeregning);
        if (ventPåRapporteringAvInntektFrist.isPresent()) {
            return List.of(autopunkt(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_INNTKT_RAP_FRST, BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST, ventPåRapporteringAvInntektFrist.get()));
        }
        Optional<LocalDate> ventPåMeldekortFrist = AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste.skalVenteTilDatoPåMeldekortAAPellerDP(aktørYtelse, LocalDate.now(), beregningAktivitetAggregat.getSkjæringstidspunktOpptjening(), Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER, YtelseType.DAGPENGER));
        if (ventPåMeldekortFrist.isPresent()) {
            return List.of(autopunkt(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_SISTE_AAP_DP_MELDKRT, BeregningVenteårsak.VENT_PÅ_SISTE_AAP_MELDEKORT, ventPåMeldekortFrist.get()));
        }
        if (erOverstyrt) {
            return emptyList();
        }

        if (skalAvklareAktiviteter(beregningAktivitetAggregat, aktørYtelse)) {
            return List.of(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.AVKLAR_AKTIVITETER));
        }
        return emptyList();
    }

    protected static BeregningAvklaringsbehovResultat autopunkt(AvklaringsbehovDefinisjon apDef, BeregningVenteårsak venteårsak, LocalDate settPåVentTom) {
        return BeregningAvklaringsbehovResultat.opprettMedFristFor(apDef, venteårsak, LocalDateTime.of(settPåVentTom, LocalTime.MIDNIGHT));
    }

    private static boolean skalAvklareAktiviteter(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                  Optional<AktørYtelseDto> aktørYtelse) {
        return harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(beregningAktivitetAggregat)
                || harFullAAPITilleggTilAnnenAktivitet(beregningAktivitetAggregat, aktørYtelse);
    }

    public static boolean harVentelønnEllerVartpengerSomSisteAktivitetIOpptjeningsperioden(BeregningAktivitetAggregatDto beregningAktivitetAggregat) {
        List<BeregningAktivitetDto> relevanteAktiviteter = beregningAktivitetAggregat.getBeregningAktiviteter();
        LocalDate skjæringstidspunkt = beregningAktivitetAggregat.getSkjæringstidspunktOpptjening();
        List<BeregningAktivitetDto> aktiviteterPåStp = relevanteAktiviteter.stream()
                .filter(opptjeningsperiode -> opptjeningsperiode.getPeriode().getFomDato().isBefore(skjæringstidspunkt))
                .filter(opptjeningsperiode -> !opptjeningsperiode.getPeriode().getTomDato().isBefore(skjæringstidspunkt))
                .toList();
        return aktiviteterPåStp.stream()
                .anyMatch(aktivitet -> aktivitet.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.VENTELØNN_VARTPENGER));
    }

    public static boolean harFullAAPITilleggTilAnnenAktivitet(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                              Optional<AktørYtelseDto> aktørYtelse) {
        LocalDate skjæringstidspunkt = beregningAktivitetAggregat.getSkjæringstidspunktOpptjening();
        List<OpptjeningAktivitetType> opptjeningsaktivitetTyper = beregningAktivitetAggregat.getAktiviteterPåDato(skjæringstidspunkt).stream()
                .map(BeregningAktivitetDto::getOpptjeningAktivitetType).collect(Collectors.toList());
        if (opptjeningsaktivitetTyper.stream().noneMatch(type -> type.equals(OpptjeningAktivitetType.AAP))) {
            return false;
        }
        if (beregningAktivitetAggregat.getAktiviteterPåDato(skjæringstidspunkt).size() <= 1) {
            return false;
        }
        return hentUtbetalingsprosent(aktørYtelse, skjæringstidspunkt, YtelseType.ARBEIDSAVKLARINGSPENGER)
                .filter(verdi -> verdi.compareTo(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG) == 0)
                .isPresent();
    }

    private static Optional<BigDecimal> hentUtbetalingsprosent(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt, YtelseType ytelseTypeForMeldekort) {
        var ytelseFilter = new YtelseFilterDto(aktørYtelse).før(skjæringstidspunkt);

        Optional<YtelseDto> nyligsteVedtak = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(ytelseTypeForMeldekort));
        if (nyligsteVedtak.isEmpty()) {
            return Optional.empty();
        }
        Optional<YtelseAnvistDto> nyligsteMeldekort = MeldekortUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyligsteVedtak.get(), skjæringstidspunkt, Set.of(ytelseTypeForMeldekort));
        return Optional.of(nyligsteMeldekort.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent).map(Stillingsprosent::verdi).orElse(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG));
    }

    @Override
    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagRegelResultat regelResultat, BeregningsgrunnlagInput input, boolean erOverstyrt) {
        BeregningAktivitetAggregatDto registerAktiviteter = regelResultat.getRegisterAktiviteter();
        LocalDate skjæringstidspunkt = regelResultat.getBeregningsgrunnlag().getSkjæringstidspunkt();
        return utledAvklaringsbehovFP(registerAktiviteter, input, erOverstyrt, skjæringstidspunkt);
    }
}
