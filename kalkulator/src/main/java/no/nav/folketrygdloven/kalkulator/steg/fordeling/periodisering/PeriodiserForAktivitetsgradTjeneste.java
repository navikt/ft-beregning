package no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering;

import static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.StandardPeriodeSplittCombinators;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class PeriodiserForAktivitetsgradTjeneste {

    public static BeregningsgrunnlagDto splittVedEndringIAktivitetsgrad(BeregningsgrunnlagDto beregningsgrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {

        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            List<LocalDateSegment<Map<AktivitetDto, BigDecimal>>> aktivitetsgradSegmenter = new ArrayList<>();
            for (UtbetalingsgradPrAktivitetDto utbetalingsgradPrAktivitet : utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet()) {
                AktivitetDto aktivitet = utbetalingsgradPrAktivitet.getUtbetalingsgradArbeidsforhold();
                for (PeriodeMedUtbetalingsgradDto periode : utbetalingsgradPrAktivitet.getPeriodeMedUtbetalingsgrad()) {
                    if (periode.getAktivitetsgrad().isPresent()) {
                        aktivitetsgradSegmenter.add(new LocalDateSegment<>(periode.getPeriode().getFomDato(), periode.getPeriode().getTomDato(), Map.of(aktivitet, periode.getAktivitetsgrad().map(Aktivitetsgrad::verdi).orElse(BigDecimal.ZERO))));
                    }
                }
            }
            LocalDateTimeline<Map<AktivitetDto, BigDecimal>> aktivitetsgradTidslinje = new LocalDateTimeline<>(aktivitetsgradSegmenter, PeriodiserForAktivitetsgradTjeneste::merge);
            return lagPeriodeSplitter(beregningsgrunnlag.getSkjæringstidspunkt()).splittPerioder(beregningsgrunnlag, aktivitetsgradTidslinje);
        }
        return beregningsgrunnlag;
    }

    static LocalDateSegment<Map<AktivitetDto, BigDecimal>> merge(LocalDateInterval intervall, LocalDateSegment<Map<AktivitetDto, BigDecimal>> lhs, LocalDateSegment<Map<AktivitetDto, BigDecimal>> rhs) {
        Map<AktivitetDto, BigDecimal> resultat = new LinkedHashMap<>();
        resultat.putAll(lhs.getValue());
        resultat.putAll(rhs.getValue());
        return new LocalDateSegment<>(intervall, resultat);
    }

    private static PeriodeSplitter<Map<AktivitetDto, BigDecimal>> lagPeriodeSplitter(LocalDate stp) {
        SplittPeriodeConfig<Map<AktivitetDto, BigDecimal>> splittPeriodeConfig = new SplittPeriodeConfig<>(
                StandardPeriodeSplittCombinators.splittPerioderOgSettÅrsakCombinator(ENDRING_I_AKTIVITETER_SØKT_FOR, (di1, lhs1, rhs1) -> di1.getFomDato().isAfter(stp) && lhs1 != null));
        splittPeriodeConfig.setLikhetsPredikatForCompress(PeriodiserForAktivitetsgradTjeneste::like);
        return new PeriodeSplitter<>(splittPeriodeConfig);
    }

    private static boolean like(Map<AktivitetDto, BigDecimal> a, Map<AktivitetDto, BigDecimal> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (Map.Entry<AktivitetDto, BigDecimal> bEntry : b.entrySet()) {
            BigDecimal aVerdi = a.get(bEntry.getKey());
            BigDecimal bVerdi = bEntry.getValue();
            if (aVerdi == null && bVerdi == null) {
                continue;
            }
            if (aVerdi == null || bVerdi == null) {
                return false;
            }
            if (aVerdi.compareTo(bVerdi) != 0) {
                return false;
            }
        }
        return true;
    }

}
