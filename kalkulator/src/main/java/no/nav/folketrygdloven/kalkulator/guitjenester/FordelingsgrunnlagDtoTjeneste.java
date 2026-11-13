package no.nav.folketrygdloven.kalkulator.guitjenester;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.RefusjonsandelDto;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.RefusjonsperiodeDto;

public class FordelingsgrunnlagDtoTjeneste {

    public no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.FordelingGrunnlagDto lagFordelingsgrunnlagDto(
            BeregningsgrunnlagGUIInput input) {
        var grunnlag = input.getFordelingGrunnlag()
                .orElseThrow(() -> new IllegalStateException("Fordelingsgrunnlag mangler i GUI-input."));
        var refusjonsandeler = grunnlag.refusjonsandeler().stream()
                .map(FordelingsgrunnlagDtoTjeneste::mapRefusjonsandel)
                .toList();
        return new no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.FordelingGrunnlagDto(refusjonsandeler);
    }

    private static RefusjonsandelDto mapRefusjonsandel(
            no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsandelDto andel) {
        var refusjonsperioder = andel.refusjonsperioder().stream()
                .map(FordelingsgrunnlagDtoTjeneste::mapRefusjonsperiode)
                .toList();
        return new RefusjonsandelDto(
                andel.arbeidsgiver().getIdentifikator(),
                mapArbeidsforholdRef(andel.arbeidsforholdRef()),
                andel.kilde(),
                andel.journalpostId(),
                refusjonsperioder);
    }

    private static RefusjonsperiodeDto mapRefusjonsperiode(
            no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsperiodeDto periode) {
        return new RefusjonsperiodeDto(
                periode.periode().getFomDato(),
                periode.periode().getTomDato(),
                periode.refusjonPrMnd() == null ? null : ModellTyperMapper.beløpTilDto(periode.refusjonPrMnd()),
                periode.utfall(),
                periode.utfallÅrsak(),
                periode.utfallBegrunnelse());
    }

    private static String mapArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
        return arbeidsforholdRef == null ? null : arbeidsforholdRef.getReferanse();
    }

}
