package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.EksternArbeidsforholdRef;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public class ArbeidsforholdInformasjonDtoBuilder {

    private final ArbeidsforholdInformasjonDto kladd;

    private ArbeidsforholdInformasjonDtoBuilder(ArbeidsforholdInformasjonDto kladd) {
        this.kladd = kladd;
    }

    public static ArbeidsforholdInformasjonDtoBuilder oppdatere(ArbeidsforholdInformasjonDto oppdatere) {
        return new ArbeidsforholdInformasjonDtoBuilder(new ArbeidsforholdInformasjonDto(oppdatere));
    }

    public ArbeidsforholdOverstyringDtoBuilder getOverstyringBuilderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto ref) {
        return kladd.getOverstyringBuilderFor(arbeidsgiver, ref);
    }

    public ArbeidsforholdInformasjonDtoBuilder leggTil(ArbeidsforholdOverstyringDtoBuilder overstyringBuilder) {
        if (!overstyringBuilder.isOppdatering()) {
            kladd.leggTilOverstyring(overstyringBuilder.build());
        }
        return this;
    }

    public ArbeidsforholdInformasjonDto build() {
        return kladd;
    }

    public void leggTilNyReferanse(ArbeidsforholdReferanseDto arbeidsforholdReferanse) {
        kladd.leggTilNyReferanse(arbeidsforholdReferanse);
    }

    public static ArbeidsforholdInformasjonDtoBuilder oppdatere(Optional<InntektArbeidYtelseGrunnlagDto> grunnlag) {
        return oppdatere(InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(grunnlag).getInformasjon());
    }

    public static ArbeidsforholdInformasjonDtoBuilder builder(Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon) {
        var arbeidInfo = arbeidsforholdInformasjon.map(ai -> new ArbeidsforholdInformasjonDto(ai)).orElseGet(() -> new ArbeidsforholdInformasjonDto());
        return new ArbeidsforholdInformasjonDtoBuilder(arbeidInfo);
    }
}
