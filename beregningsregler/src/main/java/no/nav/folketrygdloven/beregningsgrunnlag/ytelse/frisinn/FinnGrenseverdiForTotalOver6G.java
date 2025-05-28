package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
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

        var totalATGrunnlag = finnTotalGrunnlagAT(grunnlag);
        var totalDPGrunnlag = grunnlag.getBeregningsgrunnlagFraDagpenger()
				.map(BeregningsgrunnlagPrStatus::getBruttoInkludertNaturalytelsePrÅr)
				.orElse(BigDecimal.ZERO);
        var totalAAPGrunnlag = finnInntektForStatus(grunnlag, AktivitetStatus.AAP);
        var løpendeFL = finnLøpendeBgFL(grunnlag);
        var løpendeSN = finnLøpendeBgSN(grunnlag);

        var grenseverdiFratrektAT = grunnlag.getGrenseverdi()
				.subtract(totalAAPGrunnlag)
				.subtract(totalATGrunnlag)
				.subtract(totalDPGrunnlag);
        var grenseverdiFratrektFL = grenseverdiFratrektAT.subtract(løpendeFL);
        var grenseverdi = grenseverdiFratrektFL.subtract(løpendeSN).max(BigDecimal.ZERO);

		if (!erSistePeriode(grunnlag) && !gårTilSluttenAvMåned(grunnlag)) {
			trekkRestFraNesteGrenseverdi(grunnlag, løpendeFL, løpendeSN, grenseverdiFratrektAT, grenseverdiFratrektFL);
		}

		resultater.put("grenseverdi", grenseverdi);
		grunnlag.setGrenseverdi(grenseverdi);
        var resultat = ja();
		resultat.setEvaluationProperties(resultater);
		return resultat;

	}

	private boolean gårTilSluttenAvMåned(BeregningsgrunnlagPeriode grunnlag) {
		return !grunnlag.getPeriodeTom().isEqual(LocalDate.of(2020, 3, 31)) && grunnlag.getPeriodeTom().isEqual(grunnlag.getPeriodeTom().with(TemporalAdjusters.lastDayOfMonth()));
	}

	private void trekkRestFraNesteGrenseverdi(BeregningsgrunnlagPeriode grunnlag, BigDecimal løpendeFL,
	                                          BigDecimal løpendeSN,
	                                          BigDecimal grenseverdiFratrektAT,
	                                          BigDecimal grenseverdiFratrektFL) {
        var frisinnGrunnlag = (FrisinnGrunnlag) grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
        var skalTrekkesFraGrenseverdiINestePeriode = frisinnGrunnlag.søkerYtelseFrilans(grunnlag.getPeriodeFom()) ? finnFratrekkForFrilans(grunnlag, løpendeFL, grenseverdiFratrektAT) : BigDecimal.ZERO;
        var fratrekkForNæring = frisinnGrunnlag.søkerYtelseNæring(grunnlag.getPeriodeFom()) ? finnFratrekkForNæring(grunnlag, løpendeSN, grenseverdiFratrektFL) : BigDecimal.ZERO;
		skalTrekkesFraGrenseverdiINestePeriode = skalTrekkesFraGrenseverdiINestePeriode.add(fratrekkForNæring);
        var nestePeriode = finnNestePeriode(grunnlag);
		nestePeriode.setGrenseverdi(nestePeriode.getGrenseverdi().subtract(skalTrekkesFraGrenseverdiINestePeriode));
	}

	private BigDecimal finnFratrekkForFrilans(BeregningsgrunnlagPeriode grunnlag,
	                                          BigDecimal løpendeFL,
	                                          BigDecimal grenseverdiFratrektAT) {
        var grenseverdiFratrektFL = grenseverdiFratrektAT
				.subtract(løpendeFL);
		if (grenseverdiFratrektFL.compareTo(BigDecimal.ZERO) >= 0) {
			return BigDecimal.ZERO;
		}
        var atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
		if (atflAndel == null) {
			return BigDecimal.ZERO;
		}
        var frilansArbeidsforhold = atflAndel.getFrilansArbeidsforhold();
        var erSøktForFrilans = frilansArbeidsforhold.map(BeregningsgrunnlagPrArbeidsforhold::getErSøktYtelseFor).orElse(false);
		if (Boolean.FALSE.equals(erSøktForFrilans) || !erSøktYtelseForFrilansNestePeriode(grunnlag)) {
			return BigDecimal.ZERO;
		}
		return finnRestFratrekkForNestePeriode(grunnlag, løpendeFL, grenseverdiFratrektAT);
	}

	private BigDecimal finnFratrekkForNæring(BeregningsgrunnlagPeriode grunnlag,
	                                         BigDecimal løpendeSN,
	                                         BigDecimal grenseverdiFratrektATFL) {
        var grenseverdiFratrektSN = grenseverdiFratrektATFL.subtract(løpendeSN);
		if (grenseverdiFratrektSN.compareTo(BigDecimal.ZERO) >= 0) {
			return BigDecimal.ZERO;
		}
        var sn = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
		if (sn == null) {
			return BigDecimal.ZERO;
		}
		if (!sn.erSøktYtelseFor() || !erSøktYtelseForSNNestePeriode(grunnlag)) {
			return BigDecimal.ZERO;
		}
		return finnRestFratrekkForNestePeriode(grunnlag, løpendeSN, grenseverdiFratrektATFL);
	}

	private boolean erSøktYtelseForFrilansNestePeriode(BeregningsgrunnlagPeriode grunnlag) {
        var nestePeriode = finnNestePeriode(grunnlag);
        var atflNestePeriode = nestePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
		if (atflNestePeriode == null) {
			return false;
		}
		return atflNestePeriode.getFrilansArbeidsforhold().map(BeregningsgrunnlagPrArbeidsforhold::getErSøktYtelseFor).orElse(false);
	}

	private boolean erSøktYtelseForSNNestePeriode(BeregningsgrunnlagPeriode grunnlag) {
        var nestePeriode = finnNestePeriode(grunnlag);
        var snAndelNestePeriode = nestePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
		if (snAndelNestePeriode == null) {
			return false;
		}
		return snAndelNestePeriode.erSøktYtelseFor();
	}

	private BigDecimal finnRestFratrekkForNestePeriode(BeregningsgrunnlagPeriode grunnlag,
	                                                   BigDecimal løpende,
	                                                   BigDecimal grenseverdiFratrektAndreStatuser) {
        var grenseverdiFratrektForStatus = grenseverdiFratrektAndreStatuser.subtract(løpende);
		if (grenseverdiFratrektAndreStatuser.compareTo(BigDecimal.ZERO) <= 0) {
			return finnEffektivRestForNestePeriode(grunnlag, løpende);
		} else if (grenseverdiFratrektForStatus.compareTo(BigDecimal.ZERO) < 0) {
			return finnEffektivRestForNestePeriode(grunnlag, grenseverdiFratrektForStatus.abs());
		}
		return BigDecimal.ZERO;
	}

	private BigDecimal finnEffektivRestForNestePeriode(BeregningsgrunnlagPeriode grunnlag, BigDecimal restPrÅr) {
        var virkedager = Virkedager.beregnAntallVirkedager(grunnlag.getBeregningsgrunnlagPeriode());
        var dagsatsLøpende = finnDagsatsFraÅrsbeløp(restPrÅr);
        var løpendeIDennePeriode = dagsatsLøpende.multiply(BigDecimal.valueOf(virkedager));

        var nestePeriode = finnNestePeriode(grunnlag);
		if (erSistePeriode(nestePeriode)) {
			return BigDecimal.ZERO;
		}
        var virkedagerNestePeriode = Virkedager.beregnAntallVirkedager(nestePeriode.getBeregningsgrunnlagPeriode());
		if (virkedagerNestePeriode == 0) {
			return BigDecimal.ZERO;
		}
        var dagsatsNestePeriode = løpendeIDennePeriode.divide(BigDecimal.valueOf(virkedagerNestePeriode), 10, RoundingMode.HALF_EVEN);
		return finnÅrsbeløpFraDagsats(dagsatsNestePeriode);
	}

	private boolean erSistePeriode(BeregningsgrunnlagPeriode nestePeriode) {
        var tom = nestePeriode.getBeregningsgrunnlagPeriode().getTom();
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
        var andel = grunnlag.getBeregningsgrunnlagPrStatus(status);
		if (andel == null) {
			return BigDecimal.ZERO;
		}
		return andel.getBruttoInkludertNaturalytelsePrÅr();
	}

	private BigDecimal finnLøpendeBgSN(BeregningsgrunnlagPeriode grunnlag) {
        var snStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
		if (snStatus == null) {
			return BigDecimal.ZERO;
		}
        var bruttoSN = snStatus.getBruttoInkludertNaturalytelsePrÅr();
        var bortfaltSN = snStatus.getGradertBruttoInkludertNaturalytelsePrÅr();
		return bruttoSN.subtract(bortfaltSN).max(BigDecimal.ZERO);
	}

	private BigDecimal finnTotalGrunnlagAT(BeregningsgrunnlagPeriode grunnlag) {
        var atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
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
        var atflAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
		if (atflAndel == null) {
			return BigDecimal.ZERO;
		}
        var totalBgFL = atflAndel
				.getFrilansArbeidsforhold()
				.flatMap(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr)
				.orElse(BigDecimal.ZERO);
        var bortfaltBgFL = atflAndel
				.getFrilansArbeidsforhold()
				.flatMap(BeregningsgrunnlagPrArbeidsforhold::getGradertBruttoInkludertNaturalytelsePrÅr)
				.orElse(BigDecimal.ZERO);
		return totalBgFL.subtract(bortfaltBgFL).max(BigDecimal.ZERO);
	}

}
