package no.nav.folketrygdloven.skjæringstidspunkt.status;

import java.util.Arrays;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkAktuelleKombinasjoner.ID)
public class SjekkAktuelleKombinasjoner extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR_19_3";
    static final String BESKRIVELSE = "Sjekk aktuelle kombinasjoner?";

    public SjekkAktuelleKombinasjoner() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        var aktivitetStatuser = regelmodell.getAktivitetStatuser();
        var kombinasjonStatus = Arrays.asList(AktivitetStatus.ATFL, AktivitetStatus.SN);
        return aktivitetStatuser.containsAll(kombinasjonStatus) ? ja() : nei();
    }
}
