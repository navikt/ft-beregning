package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.PermisjonDto;

class UtledPermisjonTilDto {

    private UtledPermisjonTilDto() {
        // skjul default
    }

    static Optional<PermisjonDto> utled(InntektArbeidYtelseGrunnlagDto grunnlag, LocalDate stp, BGAndelArbeidsforholdDto bgAndelArbeidsforhold) {
        Arbeidsgiver arbeidsgiver = bgAndelArbeidsforhold.getArbeidsgiver();
        InternArbeidsforholdRefDto arbeidsforholdRef = bgAndelArbeidsforhold.getArbeidsforholdRef();
        var yrkesaktivitetFilter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());
        var permisjonSomOVerlapperStp = yrkesaktivitetFilter.getYrkesaktiviteterForBeregning().stream().filter(ya -> ya.gjelderFor(arbeidsgiver, arbeidsforholdRef))
                .flatMap(ya -> ya.getFullPermisjon().stream())
                .filter(p -> p.getPeriode().inkluderer(stp))
                .findFirst();
        if (permisjonSomOVerlapperStp.isPresent()) {
            PermisjonDto dto = lagPermisjonDto(permisjonSomOVerlapperStp.get());
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    private static PermisjonDto lagPermisjonDto(no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDto permisjon) {
        LocalDate fomDato = permisjon.getPeriode().getFomDato();
        LocalDate tomDato = permisjon.getPeriode().getTomDato();
        return new PermisjonDto(fomDato, tomDato);
    }

}
