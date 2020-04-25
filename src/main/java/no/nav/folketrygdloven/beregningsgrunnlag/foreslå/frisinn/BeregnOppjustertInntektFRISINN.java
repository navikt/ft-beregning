package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.InntektPeriodeType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RuleDocumentation(BeregnOppjustertInntektFRISINN.ID)
public class BeregnOppjustertInntektFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.5";
    private static final String BESKRIVELSE = "Beregn oppjustert inntekt for årene i beregningsperioden";
    public static final Periode ÅRET_2019 = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31));

    public BeregnOppjustertInntektFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        LocalDate beregningsperiodeTom = bgps.getBeregningsperiode().getTom();
        BigDecimal gjeldendeG = grunnlag.getGrunnbeløp();
        Map<String, Object> resultater = new HashMap<>();
        List<BigDecimal> pgiListe = new ArrayList<>();

        pgiListe.add(FinnRapportertÅrsinntektSN.finnRapportertÅrsinntekt(grunnlag));

        for (int årSiden = 1; årSiden <= 2; årSiden++) {
            int årstall = beregningsperiodeTom.getYear() - årSiden;
            BigDecimal gSnitt = BigDecimal.valueOf(grunnlag.getBeregningsgrunnlag().snittverdiAvG(årstall));
            BigDecimal pgiÅr = grunnlag.getInntektsgrunnlag().getÅrsinntektSigrun(årstall);
            BigDecimal pgiPrG = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? pgiÅr.divide(gSnitt, 10, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
            BigDecimal pgiJustert = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? gjeldendeG.multiply(pgiPrG) : BigDecimal.ZERO;
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
