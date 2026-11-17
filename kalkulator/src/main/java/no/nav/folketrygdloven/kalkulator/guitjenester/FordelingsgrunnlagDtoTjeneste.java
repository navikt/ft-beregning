package no.nav.folketrygdloven.kalkulator.guitjenester;

import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.FordelingGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.RefusjonsandelDto;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.RefusjonsperiodeDto;

public class FordelingsgrunnlagDtoTjeneste {

    public FordelingGrunnlagDto lagFordelingsgrunnlagDto(
        FordelingGrunnlag fordelingsgrunnlag) {
        var refusjonsandeler = fordelingsgrunnlag.refusjonsandeler().stream()
                .map(FordelingsgrunnlagDtoTjeneste::mapRefusjonsandel)
                .toList();
        return new FordelingGrunnlagDto(refusjonsandeler);
    }

    private static RefusjonsandelDto mapRefusjonsandel(
            Refusjonsandel andel) {
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
            Refusjonsperiode periode) {
        return new RefusjonsperiodeDto(
                periode.periode().getFomDato(),
                periode.periode().getTomDato(),
                periode.refusjonPrMnd() == null ? null : ModellTyperMapper.beløpTilDto(periode.refusjonPrMnd()),
                periode.utfall(),
                periode.utfallÅrsak());
    }

    private static String mapArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
        return arbeidsforholdRef == null ? null : arbeidsforholdRef.getReferanse();
    }

}
