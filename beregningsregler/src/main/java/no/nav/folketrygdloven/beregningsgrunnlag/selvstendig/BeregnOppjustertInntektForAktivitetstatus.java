package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnOppjustertInntektForAktivitetstatus.ID)
public class BeregnOppjustertInntektForAktivitetstatus extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.9 BP";
    private static final String BESKRIVELSE = "Beregn oppjustert inntekt for årene i beregningsperioden";
	private final AktivitetStatus aktivitetStatus;

	public BeregnOppjustertInntektForAktivitetstatus(AktivitetStatus aktivitetStatus) {
        super(ID, BESKRIVELSE);
		if (!AktivitetStatus.SN.equals(aktivitetStatus) && !AktivitetStatus.BA.equals(aktivitetStatus)) {
			throw new IllegalArgumentException("Kan ikke beregne oppjustert inntekt for aktivitetstatus " + aktivitetStatus);
		}
		this.aktivitetStatus = aktivitetStatus;

	}

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(aktivitetStatus);
        var beregningsperiodeTom = bgps.getBeregningsperiode().getTom();
        var gjeldendeG = grunnlag.getGrunnbeløp();
        Map<String, Object> resultater = new HashMap<>();
        List<BigDecimal> pgiListe = new ArrayList<>();

        for (var årSiden = 0; årSiden <= 2; årSiden++) {
            var årstall = beregningsperiodeTom.getYear() - årSiden;
            var gSnitt = BigDecimal.valueOf(grunnlag.getBeregningsgrunnlag().snittverdiAvG(årstall));
            var pgiÅr = grunnlag.getInntektsgrunnlag().getÅrsinntektSigrun(årstall);
            var pgiPrG = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? pgiÅr.divide(gSnitt, 10, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
            var pgiJustert = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? gjeldendeG.multiply(pgiPrG) : BigDecimal.ZERO;
            resultater.put("PGI/G." + årstall, pgiPrG);
            resultater.put("PGIjustert." + årstall, pgiJustert);
            pgiListe.add(pgiJustert);
        }
        BeregningsgrunnlagPrStatus.builder(bgps)
            .medPGI(pgiListe)
            .build();
        return beregnet(resultater);
    }
}
