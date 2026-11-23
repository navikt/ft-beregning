package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrRefusjonDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel;
import no.nav.folketrygdloven.kalkulator.input.HåndterBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;

import java.util.ArrayList;
import java.util.List;

public class RefusjonOverstyringHåndterer {

    private RefusjonOverstyringHåndterer() {
        // Skjuler default konstruktør
    }

    public static BeregningsgrunnlagGrunnlagDto håndter(HåndterBeregningsgrunnlagInput input, OverstyrRefusjonDto refusjonDto) {
        var grunnlagBuilder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(input.getBeregningsgrunnlagGrunnlag());
        var forrigeFordelingsGrunnlag = input.getFordelingGrunnlag().orElseThrow();
        var nyttFordelingGrunnlag = lagNyttFordelingGrunnlag(forrigeFordelingsGrunnlag, refusjonDto);

        //TODO lagre den overstyrte refusjonen som et nytt fordelingsgrunnlag og oppdatere beregningsgrunnlaget med nye refusjonsperioder ?
        //FaktaOmBeregningTilfellerOppdaterer.oppdater(refusjonDto, forrigeBg, input, grunnlagBuilder);
        return grunnlagBuilder.build(input.getHåndteringTilstand());
    }

    static FordelingGrunnlag lagNyttFordelingGrunnlag(FordelingGrunnlag forrigeFordelingsGrunnlag, OverstyrRefusjonDto refusjonDto) {
        var andeler = new ArrayList<>(forrigeFordelingsGrunnlag.refusjonsandeler());

        refusjonDto.getOverstyrteAndeler().stream()
            .filter(refusjonOverstyringAndel -> refusjonOverstyringAndel.kilde().equals(RefusjonKilde.SAKSBEHANDLER))
            .map(RefusjonOverstyringHåndterer::mapTilRefusjonsandel)
            .forEach(nyAndel -> {
                andeler.removeIf(andel -> erSammeAndel(andel, nyAndel));
                andeler.add(nyAndel);
            });

        return new FordelingGrunnlag(andeler);
    }

    private static Refusjonsandel mapTilRefusjonsandel(RefusjonOverstyringAndel overstyrtAndel) {
        var perioder = overstyrtAndel.refusjonsperioder().stream()
            .map(p -> new Refusjonsperiode(Intervall.fraOgMedTilOgMed(p.fom(), p.tom()), Beløp.fra(p.refusjonPrMnd()), p.utfall(), p.utfallÅrsak()))
            .toList();
        return new Refusjonsandel(overstyrtAndel.arbeidsgiverIdent(), overstyrtAndel.arbeidsforholdId(), overstyrtAndel.kilde(), null, perioder);
    }

    private static boolean erSammeAndel(Refusjonsandel andel, Refusjonsandel annen) {
        return andel.arbeidsgiver().equals(annen.arbeidsgiver())
            && andel.arbeidsforholdRef().equals(annen.arbeidsforholdRef())
            && andel.kilde().equals(annen.kilde());
    }
}
