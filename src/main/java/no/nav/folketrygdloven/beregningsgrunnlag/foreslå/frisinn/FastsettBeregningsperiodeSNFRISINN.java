package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RuleDocumentation(FastsettBeregningsperiodeSNFRISINN.ID)
public class FastsettBeregningsperiodeSNFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final BeregningsgrunnlagHjemmel HJEMMEL = BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG;
    private static final LocalDate BG_PERIODE_FOM = LocalDate.of(2017,1,1);
    private static final LocalDate BG_PERIODE_TOM = LocalDate.of(2019,12,31);
    static final String ID = "FRISINN 2.4";
    private static final String BESKRIVELSE = "Fastsett beregningsperiode";

    public FastsettBeregningsperiodeSNFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        Map<String, Object> resultater = new HashMap<>();
        if (grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL) == null) {
            grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.SN).setHjemmel(HJEMMEL);
            resultater.put("hjemmel", HJEMMEL);
        }
        BeregningsgrunnlagPrStatus.builder(bgps).medBeregningsperiode(Periode.of(BG_PERIODE_FOM, BG_PERIODE_TOM)).build();
        return beregnet(resultater);
    }
}
