package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AktivitetTomDatoMappingDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.AvklarAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

public class AvklarAktiviteterDtoTjeneste {


    private AvklarAktiviteterDtoTjeneste() {
    }

    /**
     * Modifiserer dto for fakta om beregning og setter dto for avklaring av aktiviteter på denne.
     *
     * @param registerAktivitetAggregat      aggregat for registeraktiviteter
     * @param saksbehandletAktivitetAggregat aggregat for saksbehandlede aktiviteter
     * @param faktaOmBeregningDto            Dto for fakta om beregning som modifiseres
     */
    static void lagAvklarAktiviteterDto(BeregningAktivitetAggregatDto registerAktivitetAggregat,
                                        Optional<BeregningAktivitetAggregatDto> saksbehandletAktivitetAggregat,
                                        Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon,
                                        FaktaOmBeregningDto faktaOmBeregningDto) {
        AvklarAktiviteterDto avklarAktiviteterDto = new AvklarAktiviteterDto();
        List<BeregningAktivitetDto> beregningAktiviteter = registerAktivitetAggregat.getBeregningAktiviteter();
        List<BeregningAktivitetDto> saksbehandletAktiviteter = saksbehandletAktivitetAggregat
                .map(BeregningAktivitetAggregatDto::getBeregningAktiviteter)
                .orElse(Collections.emptyList());

        avklarAktiviteterDto.setAktiviteterTomDatoMapping(map(beregningAktiviteter, saksbehandletAktiviteter,
                registerAktivitetAggregat.getSkjæringstidspunktOpptjening(), arbeidsforholdInformasjon));
        avklarAktiviteterDto.setSkjæringstidspunkt(registerAktivitetAggregat.getSkjæringstidspunktOpptjening());
        faktaOmBeregningDto.setAvklarAktiviteter(avklarAktiviteterDto);
    }

    /**
     * Lager map for fastsettelse av skjæringstidspunkt for beregning.
     * <p>
     * Mapper fra mulige skjæringstidspunkt til aktiviteter som er aktive på dette tidspunktet.
     * Disse aktivitetene vil bli med videre i beregning.
     *
     * @param beregningAktiviteter     registeraktiviteter
     * @param saksbehandletAktiviteter saksbehandlede aktiviteter
     * @param skjæringstidspunkt       Skjæringstidspunkt for beregning
     * @return Liste med mappingobjekter som knytter eit mulig skjæringstidspunkt for beregning til eit sett med aktiviteter
     */
    private static List<AktivitetTomDatoMappingDto> map(List<BeregningAktivitetDto> beregningAktiviteter, List<BeregningAktivitetDto> saksbehandletAktiviteter,
                                                        LocalDate skjæringstidspunkt, Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        Map<LocalDate, List<no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto>> collect = beregningAktiviteter.stream()
                .map(aktivitet -> MapBeregningAktivitetDto.mapBeregningAktivitet(aktivitet, saksbehandletAktiviteter, arbeidsforholdInformasjon))
                .collect(Collectors.groupingBy(beregningAktivitetDto -> finnTidligste(beregningAktivitetDto.getTom().plusDays(1), skjæringstidspunkt), Collectors.toList()));
        return collect.entrySet().stream()
                .map(entry -> {
                    AktivitetTomDatoMappingDto dto = new AktivitetTomDatoMappingDto();
                    dto.setTom(entry.getKey());
                    dto.setAktiviteter(entry.getValue());
                    return dto;
                })
                .sorted(Comparator.comparing(AktivitetTomDatoMappingDto::getTom).reversed())
                .collect(Collectors.toList());
    }

    private static LocalDate finnTidligste(LocalDate tom, LocalDate skjæringstidspunkt) {
        if (tom.isAfter(skjæringstidspunkt)) {
            return skjæringstidspunkt;
        }
        return tom;
    }

}
