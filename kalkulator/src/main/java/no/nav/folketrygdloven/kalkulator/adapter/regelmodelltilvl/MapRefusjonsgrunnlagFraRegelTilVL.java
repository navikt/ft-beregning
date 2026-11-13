package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagResultat;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public final class MapRefusjonsgrunnlagFraRegelTilVL {

    private MapRefusjonsgrunnlagFraRegelTilVL() {
    }

    public static FordelingGrunnlag map(RefusjonsgrunnlagResultat resultat) {
        return new FordelingGrunnlag(resultat.refusjonsandeler().stream()
            .map(MapRefusjonsgrunnlagFraRegelTilVL::mapAndel).toList());
    }

    private static Refusjonsandel mapAndel(RefusjonsgrunnlagResultat.Refusjonsandel andel) {
        var arbeidsgiver = andel.arbeidsgiver();
        var arbeidsgiverDto = arbeidsgiver.erOrganisasjon()
            ? Arbeidsgiver.virksomhet(arbeidsgiver.getIdentifikator())
            : Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdentifikator()));
        var perioder = andel.refusjonskrav().stream()
            .map(krav -> new Refusjonsperiode(
                Intervall.fraOgMedTilOgMed(krav.periode().getFom(), krav.periode().getTom()),
                Beløp.fra(krav.refusjonPrMnd()), switch (krav.utfall()) {
                    case GODKJENT -> RefusjonUtfall.INNVILGET;
                    case UNDERKJENT -> RefusjonUtfall.AVSLÅTT;
                    case IKKE_VURDERT -> throw new IllegalStateException("Refusjonsperioden mangler utfall.");
                }, null))
            .toList();
        return new Refusjonsandel(arbeidsgiverDto, InternArbeidsforholdRefDto.ref(andel.internArbeidsforholdId()),
            switch (andel.refusjonKilde()) {
                case INNTEKTSMELDING -> RefusjonKilde.INNTEKTSMELDING;
                case SAKSBEHANDLER -> RefusjonKilde.SAKSBEHANDLER;
            }, new JournalpostId(andel.journalpostId()), perioder);
    }
}
