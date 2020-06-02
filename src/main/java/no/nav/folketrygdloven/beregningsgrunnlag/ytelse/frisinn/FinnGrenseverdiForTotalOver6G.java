package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FinnGrenseverdiForTotalOver6G.ID)
public class FinnGrenseverdiForTotalOver6G extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 6.2";
    public static final String BESKRIVELSE = "Finn grenseverdi for total bg over 6G";
    public static final BigDecimal DAGER_I_1_ÅR = BigDecimal.valueOf(260);

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
        BigDecimal grenseverdiFratrektFL = grenseverdiFratrektAT.subtract(løpendeFL);
        BigDecimal grenseverdi = grenseverdiFratrektFL.subtract(løpendeSN).max(BigDecimal.ZERO);

        if (!erSistePeriode(grunnlag) && !gårTilSluttenAvMåned(grunnlag)) {
            trekkRestFraNesteGrenseverdi(grunnlag, løpendeFL, løpendeSN, grenseverdiFratrektAT, grenseverdiFratrektFL);
        }

        resultater.put("grenseverdi", grenseverdi);
        grunnlag.setGrenseverdi(grenseverdi);
        SingleEvaluation resultat = ja();
        resultat.setEvaluationProperties(resultater);
        return resultat;

    }

    private boolean gårTilSluttenAvMåned(BeregningsgrunnlagPeriode grunnlag) {
        return !grunnlag.getPeriodeTom().isEqual(LocalDate.of(2020, 3, 31)) && grunnlag.getPeriodeTom().isEqual(grunnlag.getPeriodeTom().with(TemporalAdjusters.lastDayOfMonth()));
    }

    private void trekkRestFraNesteGrenseverdi(BeregningsgrunnlagPeriode grunnlag, BigDecimal løpendeFL, BigDecimal løpendeSN, BigDecimal grenseverdiFratrektAT, BigDecimal grenseverdiFratrektFL) {
        BigDecimal skalTrekkesFraGrenseverdiINestePeriode = finnFratrekkForFrilans(grunnlag, løpendeFL, grenseverdiFratrektAT);
        BigDecimal fratrekkForNæring = finnFratrekkForNæring(grunnlag, løpendeSN, grenseverdiFratrektFL);
        skalTrekkesFraGrenseverdiINestePeriode = skalTrekkesFraGrenseverdiINestePeriode.add(fratrekkForNæring);
        BeregningsgrunnlagPeriode nestePeriode = finnNestePeriode(grunnlag);
        nestePeriode.setGrenseverdi(nestePeriode.getGrenseverdi().subtract(skalTrekkesFraGrenseverdiINestePeriode));
    }

    private BigDecimal finnFratrekkForFrilans(BeregningsgrunnlagPeriode grunnlag,
                                              BigDecimal løpendeFL,
                                              BigDecimal grenseverdiFratrektAT) {
        BigDecimal grenseverdiFratrektFL = grenseverdiFratrektAT
            .subtract(løpendeFL);
        if (grenseverdiFratrektFL.compareTo(BigDecimal.ZERO) >= 0) {
            return BigDecimal.ZERO;
        }
        BeregningsgrunnlagPrStatus atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atflAndel == null) {
            return BigDecimal.ZERO;
        }
        Optional<BeregningsgrunnlagPrArbeidsforhold> frilansArbeidsforhold = atflAndel.getFrilansArbeidsforhold();
        Boolean erSøktForFrilans = frilansArbeidsforhold.map(BeregningsgrunnlagPrArbeidsforhold::getErSøktYtelseFor).orElse(false);
        if (!erSøktForFrilans || !erSøktYtelseForFrilansNestePeriode(grunnlag)) {
            return BigDecimal.ZERO;
        }
        return finnRestFratrekkForNestePeriode(grunnlag, løpendeFL, grenseverdiFratrektAT);
    }

    private BigDecimal finnFratrekkForNæring(BeregningsgrunnlagPeriode grunnlag,
                                              BigDecimal løpendeSN,
                                              BigDecimal grenseverdiFratrektATFL) {
        BigDecimal grenseverdiFratrektSN = grenseverdiFratrektATFL.subtract(løpendeSN);
        if (grenseverdiFratrektSN.compareTo(BigDecimal.ZERO) >= 0) {
            return BigDecimal.ZERO;
        }
        BeregningsgrunnlagPrStatus sn = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        if (sn == null) {
            return BigDecimal.ZERO;
        }
        if (!sn.erSøktYtelseFor() || !erSøktYtelseForSNNestePeriode(grunnlag)) {
            return BigDecimal.ZERO;
        }
        return finnRestFratrekkForNestePeriode(grunnlag, løpendeSN, grenseverdiFratrektATFL);
    }

    private boolean erSøktYtelseForFrilansNestePeriode(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPeriode nestePeriode = finnNestePeriode(grunnlag);
        BeregningsgrunnlagPrStatus atflNestePeriode = nestePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atflNestePeriode == null) {
            return false;
        }
        return atflNestePeriode.getFrilansArbeidsforhold().map(BeregningsgrunnlagPrArbeidsforhold::getErSøktYtelseFor).orElse(false);
    }

    private boolean erSøktYtelseForSNNestePeriode(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPeriode nestePeriode = finnNestePeriode(grunnlag);
        BeregningsgrunnlagPrStatus snAndelNestePeriode = nestePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        if (snAndelNestePeriode == null) {
            return false;
        }
        return snAndelNestePeriode.erSøktYtelseFor();
    }

    private BigDecimal finnRestFratrekkForNestePeriode(BeregningsgrunnlagPeriode grunnlag,
                                                       BigDecimal løpende,
                                                       BigDecimal grenseverdiFratrektAndreStatuser) {
        BigDecimal grenseverdiFratrektForStatus = grenseverdiFratrektAndreStatuser.subtract(løpende);
        if (grenseverdiFratrektAndreStatuser.compareTo(BigDecimal.ZERO) <= 0) {
            return finnEffektivRestForNestePeriode(grunnlag, løpende);
        } else if (grenseverdiFratrektForStatus.compareTo(BigDecimal.ZERO) < 0) {
            return finnEffektivRestForNestePeriode(grunnlag, grenseverdiFratrektForStatus.abs());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal finnEffektivRestForNestePeriode(BeregningsgrunnlagPeriode grunnlag, BigDecimal restPrÅr) {
        int virkedager = Virkedager.beregnAntallVirkedager(grunnlag.getBeregningsgrunnlagPeriode());
        BigDecimal dagsatsLøpende = finnDagsatsFraÅrsbeløp(restPrÅr);
        BigDecimal løpendeIDennePeriode = dagsatsLøpende.multiply(BigDecimal.valueOf(virkedager));

        BeregningsgrunnlagPeriode nestePeriode = finnNestePeriode(grunnlag);
        if (erSistePeriode(nestePeriode)) {
            return BigDecimal.ZERO;
        }
        int virkedagerNestePeriode = Virkedager.beregnAntallVirkedager(nestePeriode.getBeregningsgrunnlagPeriode());
        if (virkedagerNestePeriode == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal dagsatsNestePeriode = løpendeIDennePeriode.divide(BigDecimal.valueOf(virkedagerNestePeriode), 10, RoundingMode.HALF_EVEN);
        return finnÅrsbeløpFraDagsats(dagsatsNestePeriode);
    }

    private boolean erSistePeriode(BeregningsgrunnlagPeriode nestePeriode) {
        LocalDate tom = nestePeriode.getBeregningsgrunnlagPeriode().getTom();
        return tom == null || tom.equals(TIDENES_ENDE);
    }

    private BeregningsgrunnlagPeriode finnNestePeriode(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
            .filter(p -> p.getBeregningsgrunnlagPeriode().getFom().minusDays(1).equals(grunnlag.getBeregningsgrunnlagPeriode().getTom()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Kan ikke finne neste periode."));
    }

    private BigDecimal finnDagsatsFraÅrsbeløp(BigDecimal årsbeløp) {
        return årsbeløp.divide(DAGER_I_1_ÅR, 10, RoundingMode.HALF_EVEN);
    }

    private BigDecimal finnÅrsbeløpFraDagsats(BigDecimal dagsats) {
        return dagsats.multiply(DAGER_I_1_ÅR);
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
