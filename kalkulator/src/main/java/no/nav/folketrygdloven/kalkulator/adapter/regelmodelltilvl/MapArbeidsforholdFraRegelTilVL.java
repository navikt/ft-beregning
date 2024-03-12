package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.ReferanseType;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class MapArbeidsforholdFraRegelTilVL {
    private MapArbeidsforholdFraRegelTilVL() {
        // skjul private constructor
    }

    static Arbeidsgiver map(ReferanseType referanseType, String orgnr, String aktørId) {
        if (ReferanseType.AKTØR_ID.equals(referanseType)) {
            return Arbeidsgiver.person(new AktørId(aktørId));
        } else if (ReferanseType.ORG_NR.equals(referanseType)) {
            return Arbeidsgiver.virksomhet(orgnr);
        }
        return null;
    }
}
