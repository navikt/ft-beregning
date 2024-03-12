package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;


import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDto;

class MapBeregningAktivitetDto {

    private MapBeregningAktivitetDto() {
        // skjul
    }

    static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto mapBeregningAktivitet(BeregningAktivitetDto beregningAktivitet,
                                                                                                                          List<BeregningAktivitetDto> saksbehandletAktiviteter,
                                                                                                                          Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        var dto = new no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningAktivitetDto();
        if (beregningAktivitet.getArbeidsgiver() != null) {
            if (beregningAktivitet.getArbeidsgiver() != null) {
                dto.setArbeidsgiverIdent(beregningAktivitet.getArbeidsgiver().getIdentifikator());
            }
            dto.setArbeidsforholdId(beregningAktivitet.getArbeidsforholdRef().getReferanse());
            arbeidsforholdInformasjon.ifPresent(info -> {
                var eksternArbeidsforholdId = info.finnEkstern(beregningAktivitet.getArbeidsgiver(), beregningAktivitet.getArbeidsforholdRef());
                if (eksternArbeidsforholdId != null) {
                    dto.setEksternArbeidsforholdId(eksternArbeidsforholdId.getReferanse());
                }
            });
        }
        dto.setArbeidsforholdType(beregningAktivitet.getOpptjeningAktivitetType());
        dto.setFom(beregningAktivitet.getPeriode().getFomDato());
        dto.setTom(beregningAktivitet.getPeriode().getTomDato());
        if (!saksbehandletAktiviteter.isEmpty()) {
            Optional<BeregningAktivitetDto> matchetAktivitet = saksbehandletAktiviteter.stream()
                .filter(a -> a.getNøkkel().equals(beregningAktivitet.getNøkkel())).findFirst();
            matchetAktivitet.ifPresentOrElse(aktivitet -> {
                dto.setSkalBrukes(true);
                dto.setTom(aktivitet.getPeriode().getTomDato());
            }, () -> dto.setSkalBrukes(false));
        }
        return dto;
    }

}
