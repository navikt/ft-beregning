package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

public class AvklaringsbehovUtlederFastsettBeregningsaktiviteterSVP implements AvklaringsbehovUtlederFastsettBeregningsaktiviteter {

    private static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovForFelles(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                        BeregningsgrunnlagInput input,
                                                                                        LocalDate skjæringstidspunktForBeregning) {
        Collection<InntektsmeldingDto> inntektsmeldinger = input.getInntektsmeldinger();
        List<Arbeidsgiver> arbeidsgivere = inntektsmeldinger.stream().map(InntektsmeldingDto::getArbeidsgiver).collect(Collectors.toList());
        Optional<LocalDate> ventPåRapporteringAvInntektFrist = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, arbeidsgivere, LocalDate.now(), beregningAktivitetAggregat, skjæringstidspunktForBeregning);
        return ventPåRapporteringAvInntektFrist
                .map(localDate -> List.of(autopunkt(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_INNTKT_RAP_FRST, BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST, localDate)))
                .orElse(emptyList());
    }

    protected static BeregningAvklaringsbehovResultat autopunkt(AvklaringsbehovDefinisjon apDef, BeregningVenteårsak venteårsak, LocalDate settPåVentTom) {
        return BeregningAvklaringsbehovResultat.opprettMedFristFor(apDef, venteårsak, LocalDateTime.of(settPåVentTom, LocalTime.MIDNIGHT));
    }

    @Override
    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagRegelResultat regelResultat, BeregningsgrunnlagInput input, boolean erOverstyrt) {
        BeregningAktivitetAggregatDto registerAktiviteter = regelResultat.getRegisterAktiviteter();
        LocalDate skjæringstidspunkt = regelResultat.getBeregningsgrunnlag().getSkjæringstidspunkt();
        return utledAvklaringsbehovForFelles(registerAktiviteter, input, skjæringstidspunkt);
    }
}
