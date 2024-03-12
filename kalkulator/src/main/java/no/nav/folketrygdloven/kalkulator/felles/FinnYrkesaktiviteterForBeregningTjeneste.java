package no.nav.folketrygdloven.kalkulator.felles;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.ErFjernetIOverstyrt;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;

public class FinnYrkesaktiviteterForBeregningTjeneste {


    private FinnYrkesaktiviteterForBeregningTjeneste() {
        // Skjul
    }

    public static Collection<YrkesaktivitetDto> finnYrkesaktiviteter(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                     BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                     LocalDate skjæringstidspunktBeregning) {
        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning = filter.getYrkesaktiviteterForBeregning();
        var aktivitetOverstyringer = grunnlag.getOverstyring();
        return yrkesaktiviteterForBeregning.stream()
            .filter(yrkesaktivitet ->
                !ErFjernetIOverstyrt.erFjernetIOverstyrt(filter,
                        yrkesaktivitet,
                        aktivitetOverstyringer,
                        skjæringstidspunktBeregning
                ))
            .filter(ya -> FinnAnsettelsesPeriode.finnMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning).isPresent())
            .collect(Collectors.toList());
    }

    /**
     * Henter ut yrkesaktiviteter for beregning inkludert korresponderende yrkesaktivitet for fjernede aktiviteter i beregning
     *
     * Skal kun brukes i tilfeller der man også skal hente ut fjernede aktiviteter eller der disse ikke er relevante (f.eks om man kun ser på aktiviteter etter stp)
     *
     * @param filter Yrkesaktivitetfilter
     * @param skjæringstidspunktBeregning
     * @return Yrkesaktiviteter inkludert fjernede i overstyring av beregningaktiviteter
     */
    public static Collection<YrkesaktivitetDto> finnAlleYrkesaktiviteterInkludertFjernetIOverstyring(YrkesaktivitetFilterDto filter,
                                                                                                     LocalDate skjæringstidspunktBeregning) {
        Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning = filter.getYrkesaktiviteterForBeregning();
        return yrkesaktiviteterForBeregning.stream()
            .filter(ya -> FinnAnsettelsesPeriode.finnMinMaksPeriode(filter.getAnsettelsesPerioder(ya), skjæringstidspunktBeregning).isPresent())
            .collect(Collectors.toList());
    }

}
