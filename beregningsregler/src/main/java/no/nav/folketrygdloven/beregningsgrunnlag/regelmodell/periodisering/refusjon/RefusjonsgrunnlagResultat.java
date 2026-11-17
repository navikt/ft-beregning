package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;

public record RefusjonsgrunnlagResultat(List<Refusjonsandel> refusjonsandeler, RegelResultat regelResultat) {

    public RefusjonsgrunnlagResultat {
        refusjonsandeler = List.copyOf(refusjonsandeler);
    }

    public record Refusjonsandel(Arbeidsgiver arbeidsgiver, String internArbeidsforholdId, String journalpostId,  RefusjonKilde refusjonKilde,
                                List<Refusjonsperiode> refusjonskrav) {

        public record Refusjonsperiode(Periode periode, BigDecimal refusjonPrMnd, Utfall utfall, String journalpostId) {}
    }
}
