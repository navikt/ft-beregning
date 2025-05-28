package no.nav.folketrygdloven.skjæringstidspunkt.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettKombinasjoner.ID)
public class FastsettKombinasjoner extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR_19_4";
    static final String BESKRIVELSE = "Sett kombinasjoner";

    public FastsettKombinasjoner() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        var aktivitetStatuser = regelmodell.getAktivitetStatuser();
        var kombinasjonStatus = Arrays.asList(AktivitetStatus.ATFL, AktivitetStatus.SN);
        Map<String, Object> resultater = new HashMap<>();
        if(aktivitetStatuser.containsAll(kombinasjonStatus)){
            regelmodell.leggTilAktivitetStatus(AktivitetStatus.ATFL_SN);
            regelmodell.fjernAktivitetStatus(kombinasjonStatus);
            resultater.put("aktivitetStatus", AktivitetStatus.ATFL_SN.getBeskrivelse());
        }
        return beregnet(resultater);
    }
}
