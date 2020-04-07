package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

import java.time.LocalDate;

@RuleDocumentationGrunnlag
public class AktivitetStatusModellFRISINN extends AktivitetStatusModell {

    @Override
    public LocalDate sisteAktivitetsdato() {
        return finnSisteAktivitetsdatoFraSistePeriode();
    }

}
