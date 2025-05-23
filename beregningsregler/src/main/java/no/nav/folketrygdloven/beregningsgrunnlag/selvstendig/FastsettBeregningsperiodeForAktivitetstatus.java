package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBeregningsperiodeForAktivitetstatus.ID)
public class FastsettBeregningsperiodeForAktivitetstatus extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.1 BP";
    private static final String BESKRIVELSE = "Fastsett beregningsperiode";
	private final AktivitetStatus aktivitetStatus;

	public FastsettBeregningsperiodeForAktivitetstatus(AktivitetStatus aktivitetStatus) {
        super(ID, BESKRIVELSE);
        if (!AktivitetStatus.SN.equals(aktivitetStatus) && !AktivitetStatus.BA.equals(aktivitetStatus)) {
        	throw new IllegalArgumentException("Kan ikke fastsette beregningsperiode fra ferdiglignede år for aktivitetstatus " + aktivitetStatus);
        }
		this.aktivitetStatus = aktivitetStatus;
	}

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
	    if (bgps == null) {
	    	throw new IllegalStateException("Hadde ingen aktivitetstatus " + aktivitetStatus);
	    }
        Map<String, Object> resultater = new HashMap<>();
        var sisteLigningsdatoOpt = grunnlag.getInntektsgrunnlag().sistePeriodeMedInntektFørDato(Inntektskilde.SIGRUN, grunnlag.getSkjæringstidspunkt());
        var tidligstMuligBeregningsår = grunnlag.getSkjæringstidspunkt().minusYears(4);
        LocalDate tom;
        LocalDate fom;
        if (sisteLigningsdatoOpt.isPresent()) {
            var sisteLigningsdato = sisteLigningsdatoOpt.get();
            if (sisteLigningsdato.getYear() <= tidligstMuligBeregningsår.plusYears(2).getYear()) {
                fom = tidligstMuligBeregningsår.withMonth(1).withDayOfMonth(1);
                tom = fom.plusYears(2).withMonth(12).withDayOfMonth(31);
            } else {
                tom = sisteLigningsdato.withMonth(12).withDayOfMonth(31);
                fom = tom.minusYears(2).withMonth(1).withDayOfMonth(1);
            }
        } else {
            fom = tidligstMuligBeregningsår.withMonth(1).withDayOfMonth(1);
            tom = fom.plusYears(2).withMonth(12).withDayOfMonth(31);
        }
        BeregningsgrunnlagPrStatus.builder(bgps).medBeregningsperiode(Periode.of(fom, tom)).build();
        return beregnet(resultater);
    }

}
