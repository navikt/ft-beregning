package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FinnGrenseverdiForTotalOver6G.ID)
public class FinnGrenseverdiForTotalOver6G extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 6.2";
    public static final String BESKRIVELSE = "Finn grenseverdi for total bg over 6G";

    public FinnGrenseverdiForTotalOver6G() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();

        BigDecimal totalATGrunnlag = finnTotalGrunnlagAT(grunnlag);
        BigDecimal totalDPGrunnlag = finnInntektForStatus(grunnlag, AktivitetStatus.DP);
        BigDecimal totalAAPGrunnlag = finnInntektForStatus(grunnlag, AktivitetStatus.AAP);
        BigDecimal løpendeFL = finnLøpendeBgFL(grunnlag);
        BigDecimal løpendeSN = finnLøpendeBgSN(grunnlag);

        BigDecimal grenseverdiFratrektAT = grunnlag.getGrenseverdi()
            .subtract(totalAAPGrunnlag)
            .subtract(totalATGrunnlag)
            .subtract(totalDPGrunnlag);
        BigDecimal grenseverdiFratrektFL = grenseverdiFratrektAT
            .subtract(løpendeFL);

        BigDecimal skalTrekkesFraGrenseverdiINestePeriode = BigDecimal.ZERO;

        if (grenseverdiFratrektFL.compareTo(BigDecimal.ZERO) < 0) {
            BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
            if (atflAndel != null) {
                Optional<BeregningsgrunnlagPrArbeidsforhold> frilansArbeidsforhold = atflAndel.getFrilansArbeidsforhold();
                Boolean erSøktForFrilans = frilansArbeidsforhold.map(BeregningsgrunnlagPrArbeidsforhold::getErSøktYtelseFor).orElse(false);
                if (erSøktForFrilans) {
                    int virkedager = Virkedager.beregnAntallVirkedager(grunnlag.getBeregningsgrunnlagPeriode());
                    if (grenseverdiFratrektAT.compareTo(BigDecimal.ZERO) <= 0) {
                        // overfør hele løpende FL til neste grenseverdi
                        BigDecimal dagsatsLøpendeFL = finnDagsatsFraÅrsbeløp(løpendeFL);
                        BigDecimal løpendeIDennePeriode = dagsatsLøpendeFL.multiply(BigDecimal.valueOf(virkedager));
                        BigDecimal rest = finnEffektivRestForNestePeriode(grunnlag, løpendeIDennePeriode);
                        skalTrekkesFraGrenseverdiINestePeriode = skalTrekkesFraGrenseverdiINestePeriode.add(rest);
                    } else {
                        // Overfør - grenseverdiFratrektFL til neste grenseverdi (tatt hensyn til virkedager i perioder)
                        BigDecimal rest = finnEffektivRestForNestePeriode(grunnlag, grenseverdiFratrektFL.abs());
                        skalTrekkesFraGrenseverdiINestePeriode = skalTrekkesFraGrenseverdiINestePeriode.add(rest);
                    }
                }
            }
        }

        BigDecimal grenseverdi = grenseverdiFratrektFL
            .subtract(løpendeSN)
            .max(BigDecimal.ZERO);
        
        // Kan ikke kalles for siste periode!
        BeregningsgrunnlagPeriode nestePeriode = finnNestePeriode(grunnlag);
        nestePeriode.setGrenseverdi(nestePeriode.getGrenseverdi().subtract(skalTrekkesFraGrenseverdiINestePeriode));


        resultater.put("grenseverdi", grenseverdi);
        grunnlag.setGrenseverdi(grenseverdi);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }

    private BigDecimal finnEffektivRestForNestePeriode(BeregningsgrunnlagPeriode grunnlag, BigDecimal løpendeIDennePeriode) {
        BeregningsgrunnlagPeriode nestePeriode = finnNestePeriode(grunnlag);
        // Her kan neste periode vere ueeeeendelig
        int virkedagerNestePeriode = Virkedager.beregnAntallVirkedager(nestePeriode.getBeregningsgrunnlagPeriode());
        BigDecimal dagsatsNestePeriode = løpendeIDennePeriode.divide(BigDecimal.valueOf(virkedagerNestePeriode), 10, RoundingMode.HALF_EVEN);
        // Lag årsbeløp
        // Trekk fra grenseverdi
        return BigDecimal.ZERO;
    }

    private BeregningsgrunnlagPeriode finnNestePeriode(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
            .filter(p -> p.getBeregningsgrunnlagPeriode().getFom().minusDays(1).equals(grunnlag.getBeregningsgrunnlagPeriode().getTom()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Kan ikke finne neste periode."));
    }

    private BigDecimal finnDagsatsFraÅrsbeløp(BigDecimal løpendeFL) {
        return løpendeFL.divide(BigDecimal.valueOf(260), 10, RoundingMode.HALF_EVEN);
    }

    private BigDecimal finnInntektForStatus(BeregningsgrunnlagPeriode grunnlag, AktivitetStatus status) {
        BeregningsgrunnlagPrStatus andel = grunnlag.getBeregningsgrunnlagPrStatus(status);
        if (andel == null) {
            return BigDecimal.ZERO;
        }
        return andel.getBruttoInkludertNaturalytelsePrÅr();
    }

    private BigDecimal finnLøpendeBgSN(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus snStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        if (snStatus == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal bruttoSN = snStatus.getBruttoInkludertNaturalytelsePrÅr();
        BigDecimal bortfaltSN = snStatus.getGradertBruttoInkludertNaturalytelsePrÅr();
        return bruttoSN.subtract(bortfaltSN).max(BigDecimal.ZERO);
    }

    private BigDecimal finnTotalGrunnlagAT(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atflAndel == null) {
            return BigDecimal.ZERO;
        }
        return atflAndel
            .getArbeidsforholdIkkeFrilans()
            .stream()
            .flatMap(arbeid -> arbeid.getBruttoInkludertNaturalytelsePrÅr().stream())
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    }

    private BigDecimal finnLøpendeBgFL(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atflAndel == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalBgFL = atflAndel
            .getFrilansArbeidsforhold()
            .flatMap(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr)
            .orElse(BigDecimal.ZERO);
        BigDecimal bortfaltBgFL = atflAndel
            .getFrilansArbeidsforhold()
            .flatMap(BeregningsgrunnlagPrArbeidsforhold::getGradertBruttoInkludertNaturalytelsePrÅr)
            .orElse(BigDecimal.ZERO);
        return totalBgFL.subtract(bortfaltBgFL).max(BigDecimal.ZERO);
    }

}
