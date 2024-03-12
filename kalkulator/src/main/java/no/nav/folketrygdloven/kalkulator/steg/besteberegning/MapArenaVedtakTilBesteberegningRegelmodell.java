package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

/**
 * For arenaytelser mapper vi alle meldekort om til inntektsperioder og bryr oss ikke om vedtakene
 */
public class MapArenaVedtakTilBesteberegningRegelmodell {

    private MapArenaVedtakTilBesteberegningRegelmodell() {
        // Skjuler default
    }

    public static List<Periodeinntekt> lagInntektFraArenaYtelser(YtelseFilterDto ytelseFilter) {
        Collection<YtelseDto> arenaytelser = ytelseFilter.filter(ytelse -> ytelse.getYtelseType().erArenaytelse())
                .getFiltrertYtelser();
        List<Periodeinntekt> inntekter = new ArrayList<>();
        arenaytelser.forEach(vedtak -> inntekter.addAll(vedtak.getYtelseAnvist().stream()
                .map(mk -> mapMeldekortTilInntekt(mk, vedtak.getPeriode().getFomDato()))
                .collect(Collectors.toList())));
        return inntekter;
    }

    private static Periodeinntekt mapMeldekortTilInntekt(YtelseAnvistDto mk, LocalDate vedtakFom) {
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP) // OBS: Utbetaling er eit eingangsbeløp og skjer ikkje daglig
                .medInntekt(mk.getBeløp().map(Beløp::verdi).orElse(BigDecimal.ZERO))
                .medPeriode(Periode.of(finnStartdato(mk.getAnvistFOM(), vedtakFom), mk.getAnvistTOM()))
                .build();
    }

    private static LocalDate finnStartdato(LocalDate meldekortFom,
                                           LocalDate vedtakFom) {
        return meldekortFom.isAfter(vedtakFom) ? meldekortFom : vedtakFom;
    }
}
