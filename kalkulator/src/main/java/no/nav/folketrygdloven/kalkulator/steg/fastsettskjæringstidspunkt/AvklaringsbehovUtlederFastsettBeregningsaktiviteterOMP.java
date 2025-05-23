package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

public class AvklaringsbehovUtlederFastsettBeregningsaktiviteterOMP implements AvklaringsbehovUtlederFastsettBeregningsaktiviteter {

    private static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehovForOMP(BeregningAktivitetAggregatDto beregningAktivitetAggregat,
                                                                                     BeregningsgrunnlagInput input,
                                                                                     LocalDate skjæringstidspunktForBeregning) {
	    var inntektsmeldinger = input.getInntektsmeldinger();
	    var arbeidsgivere = inntektsmeldinger.stream().map(InntektsmeldingDto::getArbeidsgiver).collect(Collectors.toList());
	    var ventPåRapporteringAvInntektFrist = AutopunktUtlederFastsettBeregningsaktiviteterInntektrapporteringTjeneste.skalVentePåInnrapporteringAvInntektATFL(input, arbeidsgivere, LocalDate.now(), beregningAktivitetAggregat, skjæringstidspunktForBeregning);
        if (ventPåRapporteringAvInntektFrist.isPresent()) {
            return List.of(autopunkt(AvklaringsbehovDefinisjon.AUTO_VENT_PÅ_INNTKT_RAP_FRST, BeregningVenteårsak.VENT_INNTEKT_RAPPORTERINGSFRIST, ventPåRapporteringAvInntektFrist.get()));
        }
        return emptyList();
    }

    protected static BeregningAvklaringsbehovResultat autopunkt(AvklaringsbehovDefinisjon apDef, BeregningVenteårsak venteårsak, LocalDate settPåVentTom) {
        return BeregningAvklaringsbehovResultat.opprettMedFristFor(apDef, venteårsak, LocalDateTime.of(settPåVentTom, LocalTime.MIDNIGHT));
    }

    @Override
    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagRegelResultat regelResultat, BeregningsgrunnlagInput input, boolean erOverstyrt) {
		var vilkårErGodkjent = regelResultat.getVilkårOppfylt().orElse(false);
        if (!vilkårErGodkjent) {
            return emptyList();
        }
	    var skjæringstidspunkt = regelResultat.getBeregningsgrunnlag().getSkjæringstidspunkt();
	    var registerAktiviteter = regelResultat.getRegisterAktiviteter();
        return utledAvklaringsbehovForOMP(registerAktiviteter, input, skjæringstidspunkt);
    }
}
