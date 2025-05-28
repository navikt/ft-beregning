package no.nav.folketrygdloven.beregningsgrunnlag.fastsette;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenarioFastsett.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenarioFastsett.leggTilArbeidsforholdMedInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenarioFastsett.opprettBeregningsgrunnlagFraInntektsmelding;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;

class RegelFullføreBeregningsgrunnlagTest {

	private static Long generatedId = 1L;
	private final Offset<Double> offset = Offset.offset(0.01);

	private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

	private static final String ORGNR1 = "123";
	private static final String ORGNR2 = "456";
	private static final String ORGNR3 = "789";

	private static final String[] ORGNRS = {ORGNR1, ORGNR2, ORGNR3};

	@Test
	// Bruker er arbeidstaker
	// Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse < 6G
	// Flere arbeidsgiver har refusjonskrav
	// Ingen naturalytelse bortfaller
	void totaltBruttoBGUnder6GMedRefusjonUtenNaturalYtelseBortfallerForToArbeidsgivere() {
		//Arrange
        var bruttoBG1 = 200000d;
        var refusjonskrav1 = 200000d;
        var bortfaltYtelse1 = 0d;

        var bruttoBG2 = 250000d;
        var refusjonskrav2 = 150000d;
        var bortfaltYtelse2 = 0d;

        var totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2;
        var forventetRedusertBrukersAndel1 = bruttoBG1 + bortfaltYtelse1 - refusjonskrav1;
        var forventetRedusertBrukersAndel2 = bruttoBG2 + bortfaltYtelse2 - refusjonskrav2;
        var forventetRedusertArbeidsgiver1 = refusjonskrav1;
        var forventetRedusertArbeidsgiver2 = refusjonskrav2;

        var grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, List.of(bruttoBG1, bruttoBG2), List.of(refusjonskrav1 / 12, refusjonskrav2 / 12),
				List.of(bortfaltYtelse1, bortfaltYtelse2))
				.getBeregningsgrunnlagPerioder().get(0);

		kjørRegel(grunnlag);
        var arbeidsForhold = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();

		//Assert
		assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlag, offset);
		assertThat(arbeidsForhold.get(0).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel1, offset);
		assertThat(arbeidsForhold.get(1).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel2, offset);
		assertThat(arbeidsForhold.get(0).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver1, offset);
		assertThat(arbeidsForhold.get(1).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver2, offset);
	}

	@Test
	// Bruker er arbeidstaker
	// Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse < 6G
	// Flere arbeidsgiver har refusjonskrav
	// Naturalytelse for ein av arbeidsgiverane bortfaller
	void totaltBruttoBGUnder6GMedRefusjonMedNaturalYtelseBortfallerForEnAvToArbeidsgivere() {
		//Arrange
        var bruttoBG1 = 200000d;
        var refusjonskrav1 = 200000d;
        var bortfaltYtelse1 = 12000d;

        var bruttoBG2 = 250000d;
        var refusjonskrav2 = 150000d;
        var bortfaltYtelse2 = 0d;

        var totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2;
        var forventetRedusertBrukersAndel1 = bruttoBG1 + bortfaltYtelse1 - refusjonskrav1;
        var forventetRedusertBrukersAndel2 = bruttoBG2 + bortfaltYtelse2 - refusjonskrav2;
        var forventetRedusertArbeidsgiver1 = refusjonskrav1;
        var forventetRedusertArbeidsgiver2 = refusjonskrav2;

        var grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, List.of(bruttoBG1, bruttoBG2), List.of(refusjonskrav1 / 12, refusjonskrav2 / 12),
				List.of(bortfaltYtelse1, bortfaltYtelse2))
				.getBeregningsgrunnlagPerioder().get(0);

		kjørRegel(grunnlag);
        var arbeidsForhold = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();

		//Assert
		assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlag, offset);
		assertThat(arbeidsForhold.get(0).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel1, offset);
		assertThat(arbeidsForhold.get(1).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel2, offset);
		assertThat(arbeidsForhold.get(0).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver1, offset);
		assertThat(arbeidsForhold.get(1).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver2, offset);
	}

	@Test
	// Bruker er arbeidstaker
	// Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse < 6G
	// Flere arbeidsgiver har refusjonskrav
	// Naturalytelse for begge arbeidsgiverane bortfaller
	void totaltBruttoBGUnder6GMedRefusjonMedNaturalYtelseBortfallerForBeggeArbeidsgivere() {
		//Arrange
        var bruttoBG1 = 200000d;
        var refusjonskrav1 = 200000d;
        var bortfaltYtelse1 = 12000d;

        var bruttoBG2 = 250000d;
        var refusjonskrav2 = 150000d;
        var bortfaltYtelse2 = 25000d;

        var totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2;
        var forventetRedusertBrukersAndel1 = bruttoBG1 + bortfaltYtelse1 - refusjonskrav1;
        var forventetRedusertBrukersAndel2 = bruttoBG2 + bortfaltYtelse2 - refusjonskrav2;
        var forventetRedusertArbeidsgiver1 = refusjonskrav1;
        var forventetRedusertArbeidsgiver2 = refusjonskrav2;

        var grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, List.of(bruttoBG1, bruttoBG2), List.of(refusjonskrav1 / 12, refusjonskrav2 / 12),
				List.of(bortfaltYtelse1, bortfaltYtelse2))
				.getBeregningsgrunnlagPerioder().get(0);
		kjørRegel(grunnlag);
        var arbeidsForhold = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();

		//Assert
		assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlag, offset);
		assertThat(arbeidsForhold.get(0).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel1, offset);
		assertThat(arbeidsForhold.get(1).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel2, offset);
		assertThat(arbeidsForhold.get(0).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver1, offset);
		assertThat(arbeidsForhold.get(1).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver2, offset);
	}


	@Test
	// Bruker er arbeidstaker
	// Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse < 6G
	// Flere arbeidsgiver har refusjonskrav
	// Naturalytelse for begge arbeidsgiverane bortfaller medfører BG over 6G
	void totaltBruttoBGUnder6GMedRefusjonMedNaturalYtelseBortfallerForBeggeArbeidsgivereOgMedførerBGOver6G() {
		//Arrange
		double bruttoBG1 = GRUNNBELØP_2017 * 3; //Totalt under 6G
		double refusjonskrav1 = GRUNNBELØP_2017 * 3;
        var bortfaltYtelse1 = GRUNNBELØP_2017 * 0.3;

        var bruttoBG2 = GRUNNBELØP_2017 * 2.5; //Totalt under 6G
        var refusjonskrav2 = GRUNNBELØP_2017 * 1.5;
        var bortfaltYtelse2 = GRUNNBELØP_2017 * 0.3;

        var totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2; // Overstiger 6G
        var fraksjonBrukersAndel1 = (bruttoBG1 + bortfaltYtelse1) / totaltBeregningsgrunnlag;
        var fraksjonBrukersAndel2 = (bruttoBG2 + bortfaltYtelse2) / totaltBeregningsgrunnlag;
        var fordelingArbeidsforhold1 = GRUNNBELØP_2017 * 6 * fraksjonBrukersAndel1;
        var fordelingArbeidsforhold2 = GRUNNBELØP_2017 * 6 * fraksjonBrukersAndel2;
        var forventetRedusertArbeidsgiver1 = refusjonskrav1;
        var forventetRedusertArbeidsgiver2 = refusjonskrav2;
        var forventetRedusertBrukersAndel1 = fordelingArbeidsforhold1 - forventetRedusertArbeidsgiver1;
        var forventetRedusertBrukersAndel2 = fordelingArbeidsforhold2 - forventetRedusertArbeidsgiver2;

        var grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, List.of(bruttoBG1, bruttoBG2), List.of(refusjonskrav1 / 12, refusjonskrav2 / 12),
				List.of(bortfaltYtelse1, bortfaltYtelse2))
				.getBeregningsgrunnlagPerioder().get(0);

		kjørRegel(grunnlag);
        var arbeidsForhold = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();

		//Assert
		assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlag, offset);
		assertThat(arbeidsForhold.get(0).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel1, offset);
		assertThat(arbeidsForhold.get(1).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel2, offset);
		assertThat(arbeidsForhold.get(0).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver1, offset);
		assertThat(arbeidsForhold.get(1).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver2, offset);
	}

	@Test
	// Bruker er arbeidstaker
	// Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse > 6G
	// brutto beregningsgrunnlag i de forskjellige arbeidsforholdene er < 6G
	// Flere arbeidsgiver har refusjonskrav < brutto beregningsgrunnlag for arbeidsgiveren
	// Totalt refusjonskrav < 6G
	// Naturalytelse bortfaller i alle arbeidsforholdene:
	//          - Naturalytelse for arbeidsgiver1 i 2. periode
	//          - Naturalytelse for arbeidsgiver2 i 3. periode
	//          - Naturalytelse for arbeidsgiver3 i 4. periode
	void totaltBruttoBGOver6GMedRefusjonMedNaturalYtelseBortfallerForTreArbeidsgivere() {
		//Arrange
		double bruttoBG1 = GRUNNBELØP_2017 * 4; //Totalt over 6G
		double refusjonskrav1 = GRUNNBELØP_2017 * 3;
        var bortfaltYtelse1 = GRUNNBELØP_2017 * 0.24;

		double bruttoBG2 = GRUNNBELØP_2017 * 5; //Totalt over 6G
        var refusjonskrav2 = GRUNNBELØP_2017 * 1.5;
        var bortfaltYtelse2 = GRUNNBELØP_2017 * 0.5;

        var bruttoBG3 = GRUNNBELØP_2017 * 2.5; //Totalt over 6G
		double refusjonskrav3 = 0L;
        var bortfaltYtelse3 = GRUNNBELØP_2017 * 0.24;

        var refusjonsKrav = List.of(refusjonskrav1, refusjonskrav2, refusjonskrav3);
        var bruttoBG = List.of(bruttoBG1, bruttoBG2, bruttoBG3);

        var totaltBeregningsgrunnlagPeriode1 = bruttoBG1 + bruttoBG2 + bruttoBG3;
		// Andel for arbeidsforhold1 er mindre enn refusjon. Settes til 0.
        var foreventetBrukersAndelerPeriode1 = getForventetBrukersAndeler(bruttoBG,
				List.of(0d, 0d, 0d), refusjonsKrav, GRUNNBELØP_2017 * 6.0);

        var totaltBeregningsgrunnlagPeriode2 = bruttoBG1 + bortfaltYtelse1 + bruttoBG2 + bruttoBG3;
		// Andel for arbeidsforhold1 er mindre enn refusjon. Settes til 0.
        var foreventetBrukersAndelerPeriode2 = getForventetBrukersAndeler(bruttoBG,
				List.of(bortfaltYtelse1, 0d, 0d), refusjonsKrav, GRUNNBELØP_2017 * 6.0);

        var totaltBeregningsgrunnlagPeriode3 = bruttoBG1 + bortfaltYtelse1 + bruttoBG2 + bortfaltYtelse2 + bruttoBG3;
		// Andel for arbeidsforhold1 er mindre enn refusjon. Settes til 0.
        var foreventetBrukersAndelerPeriode3 = getForventetBrukersAndeler(bruttoBG,
				List.of(bortfaltYtelse1, bortfaltYtelse2, 0d), refusjonsKrav, GRUNNBELØP_2017 * 6.0);

        var totaltBeregningsgrunnlagPeriode4 = bruttoBG1 + bortfaltYtelse1 + bruttoBG2 + bortfaltYtelse2 + bruttoBG3 + bortfaltYtelse3;
		// Andel for arbeidsforhold1 er mindre enn refusjon. Settes til 0.
        var foreventetBrukersAndelerPeriode4 = getForventetBrukersAndeler(bruttoBG,
				List.of(bortfaltYtelse1, bortfaltYtelse2, bortfaltYtelse3), refusjonsKrav, GRUNNBELØP_2017 * 6.0);


		// 1. periode: Ingen bortfalt ytelse for nokon av arbeidsgiverane
        var grunnlag1 = lagBeregningsgrunnlagMedBortfaltNaturalytelse(3, bruttoBG,
				List.of(refusjonskrav1 / 12, refusjonskrav2 / 12, refusjonskrav3 / 12), List.of(0.0, 0.0, 0.0))
				.getBeregningsgrunnlagPerioder().get(0);

		// 2. periode: Bortfalt ytelse for arbeidsgiver1
        var grunnlag2 = lagBeregningsgrunnlagMedBortfaltNaturalytelse(3, bruttoBG,
				List.of(refusjonskrav1 / 12, refusjonskrav2 / 12, refusjonskrav3 / 12), List.of(bortfaltYtelse1, 0.0, 0.0))
				.getBeregningsgrunnlagPerioder().get(0);

		// 3. periode: Bortfalt ytelse for arbeidsgiver1 og arbeidsgiver2
        var grunnlag3 = lagBeregningsgrunnlagMedBortfaltNaturalytelse(3, bruttoBG,
				List.of(refusjonskrav1 / 12, refusjonskrav2 / 12, refusjonskrav3 / 12), List.of(bortfaltYtelse1, bortfaltYtelse2, 0.0))
				.getBeregningsgrunnlagPerioder().get(0);

		// 4. periode: Bortfalt ytelse alle arbeidsgivere
        var grunnlag4 = lagBeregningsgrunnlagMedBortfaltNaturalytelse(3, bruttoBG,
				List.of(refusjonskrav1 / 12, refusjonskrav2 / 12, refusjonskrav3 / 12), List.of(bortfaltYtelse1, bortfaltYtelse2, bortfaltYtelse3))
				.getBeregningsgrunnlagPerioder().get(0);

		kjørRegel(grunnlag1);
		kjørRegel(grunnlag2);
		kjørRegel(grunnlag3);
		kjørRegel(grunnlag4);

		//Assert
		// Periode 1
		assertPeriode(refusjonsKrav, totaltBeregningsgrunnlagPeriode1, foreventetBrukersAndelerPeriode1, grunnlag1);

		// Periode 2
		assertPeriode(refusjonsKrav, totaltBeregningsgrunnlagPeriode2, foreventetBrukersAndelerPeriode2, grunnlag2);

		// Periode 3
		assertPeriode(refusjonsKrav, totaltBeregningsgrunnlagPeriode3, foreventetBrukersAndelerPeriode3, grunnlag3);

		// Periode 4
		assertPeriode(refusjonsKrav, totaltBeregningsgrunnlagPeriode4, foreventetBrukersAndelerPeriode4, grunnlag4);

	}

	private void assertPeriode(List<Double> refusjonskrav, double totaltBeregningsgrunnlagPeriode, List<Double> foreventetBrukersAndelerPeriode, BeregningsgrunnlagPeriode grunnlag) {
        var arbeidsForholdEtterPeriode = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();
		assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlagPeriode, offset);
		Double belopTilBetalingPeriode = 0d;
		for (var i = 0; i < arbeidsForholdEtterPeriode.size(); i++) {
			assertThat(arbeidsForholdEtterPeriode.get(i).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(foreventetBrukersAndelerPeriode.get(i), offset);
			assertThat(arbeidsForholdEtterPeriode.get(i).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(refusjonskrav.get(i), offset);
			belopTilBetalingPeriode += foreventetBrukersAndelerPeriode.get(i) + refusjonskrav.get(i);
		}
		assertThat(belopTilBetalingPeriode).isEqualTo(GRUNNBELØP_2017 * 6, offset);
	}


	private List<Double> getForventetBrukersAndeler(List<Double> brutto, List<Double> bortfalteYtelser, List<Double> refusjonsBelop, Double belopTilFordeling) {
        var forventetBG = brutto.stream().reduce(0d, (a, b) -> a + b) + bortfalteYtelser.stream().reduce(0d, (v1, v2) -> v1 + v2);
        var byIterator = bortfalteYtelser.iterator();
        var fraksjonBrukersAndeler = brutto.stream().map(v -> (v + byIterator.next()) / forventetBG).collect(Collectors.toList());
		assertThat(fraksjonBrukersAndeler.stream().reduce(0d, (v1, v2) -> v1 + v2)).isEqualTo(1, offset);
        var fordelingArbeidsforhold = fraksjonBrukersAndeler.stream().map(v -> belopTilFordeling * v).collect(Collectors.toList());
        var refusjonIterator = refusjonsBelop.iterator();
        var forventetBrukersAndeler = fordelingArbeidsforhold.stream().map(v -> (v - refusjonIterator.next())).collect(Collectors.toList());
		if (forventetBrukersAndeler.get(0) < 0) {
			if (brutto.size() > 1) {
                var restListe = getForventetBrukersAndeler(brutto.subList(1, brutto.size()), bortfalteYtelser.subList(1, brutto.size()),
						refusjonsBelop.subList(1, brutto.size()), belopTilFordeling - refusjonsBelop.get(0));
				restListe.add(0, 0d);
				return restListe;
			}
			return List.of(0d);
		}
		return forventetBrukersAndeler;
	}

	@Test
	void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesØvreGrenseScenario() {
		//Arrange
        var beregnetPrÅr = GRUNNBELØP_2017 * 5.99;
		totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesScenario(beregnetPrÅr);
	}

	@Test
	void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesNedreGrenseScenario() {
		//Arrange
        var beregnetPrÅr = GRUNNBELØP_2017 * 0.50;
		totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesScenario(beregnetPrÅr);
	}

	private void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesScenario(double beregnetPrÅr) {
        var beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		//Act
        var resultat = kjørRegel(grunnlag);

		//Assert
		verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetPrÅr, beregnetPrÅr, beregnetPrÅr); //beregnetPrår = brutto = avkortet = redusert

        var arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold, beregnetPrÅr);
	}

	@Test
	void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesNedreGrenseMedFlereArbeidsforholdScenario() {
		//Arrange
        var beregnetPrÅr = GRUNNBELØP_2017 * 0.25;
        var beregnetPrÅr2 = GRUNNBELØP_2017 * 0.15;
        var beregnetPrÅr3 = GRUNNBELØP_2017 * 0.11;
        var beregnetSum = beregnetPrÅr + beregnetPrÅr2 + beregnetPrÅr3; //Totalt rett over 0,5G
        var beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
		leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
		leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr3, 0);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		//Act
        var resultat = kjørRegel(grunnlag);

		//Assert
		verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetSum, beregnetSum, beregnetSum); //beregnetPrår = brutto = avkortet = redusert

        var arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(0), beregnetPrÅr);
		verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(1), beregnetPrÅr2);
		verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(2), beregnetPrÅr3);
	}

	@Test
	void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesØvreGrenseMedFlereArbeidsforholdScenario() {
		//Arrange
        var beregnetPrÅr = GRUNNBELØP_2017 * 3.0;
        var beregnetPrÅr2 = GRUNNBELØP_2017 * 2.0;
        var beregnetPrÅr3 = GRUNNBELØP_2017 * 0.99;
        var beregnetSum = beregnetPrÅr + beregnetPrÅr2 + beregnetPrÅr3; //Totalt rett under 6G
        var beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
		leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
		leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr3, 0);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		//Act
        var resultat = kjørRegel(grunnlag);

		//Assert
		verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetSum, beregnetSum, beregnetSum); //beregnetPrår = brutto = avkortet = redusert

        var arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
		verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(0), beregnetPrÅr);
		verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(1), beregnetPrÅr2);
		verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(2), beregnetPrÅr3);
	}

	@Test
	void totaltBruttoBGUnder6GMedRefusjonØvreGrenseScenario() {
		//Arrange
        var beregnetPrÅr = GRUNNBELØP_2017 * 5.99;
        var refusjonskrav = GRUNNBELØP_2017 * 3.0;
        var beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, refusjonskrav);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		//Act
        var resultat = kjørRegel(grunnlag);

		//Assert
		verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetPrÅr, beregnetPrÅr, beregnetPrÅr);

        var arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsgrunnlagPrArbeidsforhold(arbeidsforhold, refusjonskrav, beregnetPrÅr, beregnetPrÅr,
				beregnetPrÅr, refusjonskrav,
				refusjonskrav, beregnetPrÅr - refusjonskrav, beregnetPrÅr - refusjonskrav);

	}

	@Test
	void totaltBruttoBGUnder6GMedRefusjonStørreEnnBruttoBGScenario() {
		//Arrange
        var beregnetPrÅr = GRUNNBELØP_2017 * 4.0;
        var refusjonskrav = GRUNNBELØP_2017 * 5.0;
        var beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, refusjonskrav);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		//Act
        var resultat = kjørRegel(grunnlag);

		//Assert
		verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetPrÅr, beregnetPrÅr, beregnetPrÅr);

        var arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsgrunnlagPrArbeidsforhold(arbeidsforhold, beregnetPrÅr, beregnetPrÅr, beregnetPrÅr,
				beregnetPrÅr, beregnetPrÅr, beregnetPrÅr, 0.0, 0.0);
	}

	@Test
	void totaltBruttoBGUnder6GUtenRefusjonMedReduksjonScenario() {
		//Arrange
        var beregnetPrÅr = GRUNNBELØP_2017 * 4.0;
        var redusertPrÅr = 0.80 * beregnetPrÅr;
        var beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0.0, Dekningsgrad.DEKNINGSGRAD_80);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		//Act
        var resultat = kjørRegel(grunnlag);

		//Assert
		verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetPrÅr, beregnetPrÅr, redusertPrÅr);

        var arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsgrunnlagPrArbeidsforhold(arbeidsforhold, 0.0, beregnetPrÅr, beregnetPrÅr,
				redusertPrÅr, 0.0, 0.0, beregnetPrÅr, redusertPrÅr);
	}

	@Test
	void totalBruttoOver6GRefusjonKravUnder6GTotalBGForArbeidsforholdUnder6G() {
        var bruttoATFL = 300000d;
        var refusjonsKrav = 20000d;
        var bruttoDP = 130000d;
        var bruttoAAP = 110000d;
		double bruttoSN = 100000;

        var bgBuilder = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(skjæringstidspunkt, null));
        var bgpsATFL = lagBeregningsgrunnlagPrStatus(ORGNR1, bruttoATFL, 1, AktivitetStatus.ATFL, refusjonsKrav * 12);
        var bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2, bruttoDP, 2, AktivitetStatus.DP, refusjonsKrav * 12);
        var bgpsAAP = lagBeregningsgrunnlagPrStatus(ORGNR3, bruttoAAP, 3, AktivitetStatus.AAP, refusjonsKrav * 12);
        var bgpsSN = lagBeregningsgrunnlagPrStatus("112", bruttoSN, 4, AktivitetStatus.SN, refusjonsKrav * 12);
        var periode = bgBuilder
				.medBeregningsgrunnlagPrStatus(bgpsATFL)
				.medBeregningsgrunnlagPrStatus(bgpsDP)
				.medBeregningsgrunnlagPrStatus(bgpsAAP)
				.medBeregningsgrunnlagPrStatus(bgpsSN)
				.build();
        var beregningsgrunnlag = opprettGrunnlag(periode);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        var forventetAvkortet = 130000d;
        var forventetAvkortet2 = 110000d;
        var forventetAvkortet3 = GRUNNBELØP_2017 * 6 - bruttoATFL - bruttoDP - bruttoAAP;

		kjørRegel(grunnlag);

		assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP).getAvkortetPrÅr().doubleValue()).isEqualTo(forventetAvkortet);
		assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP).getAvkortetPrÅr().doubleValue()).isEqualTo(forventetAvkortet2);
		assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getAvkortetPrÅr().doubleValue()).isEqualTo(forventetAvkortet3);
	}

	@Test
	void maksimalRefusjonSkalIkkeOverskrives() {
		//Arrange
        var beregnetPrÅr = GRUNNBELØP_2017 * 4.0;
		double maksimalRefusjonPrÅr = 238000;
        var beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0.0, Dekningsgrad.DEKNINGSGRAD_80);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var bgPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(bgPrArbeidsforhold).medMaksimalRefusjonPrÅr(BigDecimal.valueOf(maksimalRefusjonPrÅr));

		//Act
		@SuppressWarnings("unused") var resultat = kjørRegel(grunnlag);

		//Assert
        var arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		assertThat(arbeidsforhold.getMaksimalRefusjonPrÅr()).isEqualTo(BigDecimal.valueOf(maksimalRefusjonPrÅr));
	}

	private RegelResultat kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
		return new RegelFullføreBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
	}

	private void verifiserBeregningsgrunnlag(RegelResultat resultat, BeregningsgrunnlagPeriode grunnlag, double bruttoPrÅr,
	                                         double avkortetPrÅr, double redusertPrÅr) {
		assertThat(resultat.merknader()).isEmpty();
		assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(bruttoPrÅr, offset);
		assertThat(grunnlag.getAvkortetPrÅr().doubleValue()).isEqualTo(avkortetPrÅr, offset);
		assertThat(grunnlag.getRedusertPrÅr().doubleValue()).isEqualTo(redusertPrÅr, offset);
	}

	private void verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, double beregnetPrÅr) {
		verifiserBeregningsgrunnlagPrArbeidsforhold(arbeidsforhold, 0.0, beregnetPrÅr,
				beregnetPrÅr, beregnetPrÅr, 0.0, 0.0, beregnetPrÅr, beregnetPrÅr);
	}

	private void verifiserBeregningsgrunnlagPrArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, double maxRefusjon, double bruttoPrÅr,
	                                                         double avkortetPrÅr, double redusertPrÅr,
	                                                         double avkortetRefusjonPrÅr, double redusertRefusjonPrÅr, double avkortetBrukersAndelPrÅr, double redusertBrukersAndelPrÅr) {
		assertThat(arbeidsforhold.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(maxRefusjon, offset);
		assertThat(arbeidsforhold.getBruttoPrÅr().get().doubleValue()).isEqualTo(bruttoPrÅr, offset);
		assertThat(arbeidsforhold.getAvkortetPrÅr().doubleValue()).isEqualTo(avkortetPrÅr, offset);
		assertThat(arbeidsforhold.getRedusertPrÅr().doubleValue()).isEqualTo(redusertPrÅr, offset);
		assertThat(arbeidsforhold.getAvkortetRefusjonPrÅr().doubleValue()).isEqualTo(avkortetRefusjonPrÅr, offset);
		assertThat(arbeidsforhold.getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(redusertRefusjonPrÅr, offset);
		assertThat(arbeidsforhold.getAvkortetBrukersAndelPrÅr().doubleValue()).isEqualTo(avkortetBrukersAndelPrÅr, offset);
		assertThat(arbeidsforhold.getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(redusertBrukersAndelPrÅr, offset);
        var dagsatsBruker = Math.round(redusertBrukersAndelPrÅr / 260);
        var dagsatsArbeidsgiver = Math.round(redusertRefusjonPrÅr / 260);
		assertThat(arbeidsforhold.getDagsatsBruker()).isEqualTo(dagsatsBruker);
		assertThat(arbeidsforhold.getDagsatsArbeidsgiver()).isEqualTo(dagsatsArbeidsgiver);
		assertThat(arbeidsforhold.getDagsats()).isEqualTo(dagsatsBruker + dagsatsArbeidsgiver);
	}


	private Beregningsgrunnlag opprettBeregningsgrunnlag(LocalDate skjæringstidspunkt, double beregnetPrÅr, double refusjonskravPrÅr) {
		return opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, refusjonskravPrÅr, Dekningsgrad.DEKNINGSGRAD_100);
	}

	private Beregningsgrunnlag opprettBeregningsgrunnlag(LocalDate skjæringstidspunkt, double beregnetPrÅr, double refusjonskravPrÅr, Dekningsgrad dekningsgrad) {
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.valueOf(refusjonskravPrÅr / 12));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPeriode.oppdater(grunnlag).medDekningsgrad(dekningsgrad);

		BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
				.medBruttoPrÅr(BigDecimal.valueOf(beregnetPrÅr)).build();
		return beregningsgrunnlag;
	}

	private void leggTilArbeidsforhold(Beregningsgrunnlag grunnlag, double beregnetPrÅr, double refusjonskrav) {
        var bgPeriode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        var nyttOrgnr = generateId().toString();
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(nyttOrgnr);
		leggTilArbeidsforholdMedInntektsmelding(bgPeriode, BigDecimal.valueOf(refusjonskrav / 12), arbeidsforhold);
        var atfl = bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        var bgpaf = atfl.getArbeidsforhold().stream()
				.filter(af -> af.getArbeidsforhold().getOrgnr().equals(nyttOrgnr)).findFirst().get();
		BeregningsgrunnlagPrArbeidsforhold.builder(bgpaf)
				.medBruttoPrÅr(BigDecimal.valueOf(beregnetPrÅr))
				.build();
	}


	private Beregningsgrunnlag opprettGrunnlag(BeregningsgrunnlagPeriode periode) {
        var grunnlagsBuilder = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, null)))
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017));
        var arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        var inntektsgrunnlag = new Inntektsgrunnlag();
		arbeidsforhold.forEach(af -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
				.medArbeidsgiver(af.getArbeidsforhold())
				.medMåned(skjæringstidspunkt)
				.medInntekt(af.getBruttoPrÅr().get())
				.build()));
		grunnlagsBuilder.medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.oppdater(periode).medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100).build());
		return grunnlagsBuilder.build();
	}

	private Beregningsgrunnlag lagBeregningsgrunnlagMedBortfaltNaturalytelse(int antallArbeidsforhold, List<Double> bruttoBG, List<Double> refusjonsKrav, List<Double> bortfalteYtelserPerArbeidsforhold) {
		assertThat(bruttoBG).hasSize(antallArbeidsforhold);
		assertThat(refusjonsKrav).hasSize(antallArbeidsforhold);
		assertThat(bortfalteYtelserPerArbeidsforhold).hasSize(antallArbeidsforhold);
        var bgBuilder = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(skjæringstidspunkt, null));

        var prStatusBuilder = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.ATFL);
		for (var i = 0; i < antallArbeidsforhold; i++) {
            var afBuilder = BeregningsgrunnlagPrArbeidsforhold.builder()
					.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNRS[i]))
					.medAndelNr(i + 1)
					.medBruttoPrÅr(BigDecimal.valueOf(bruttoBG.get(i)))
					.medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(bortfalteYtelserPerArbeidsforhold.get(i)))
					.medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav.get(i) * 12))
					.build();
			prStatusBuilder.medArbeidsforhold(afBuilder);
		}

        var bgpsATFL1 = prStatusBuilder.build();
        var periode = bgBuilder
				.medBeregningsgrunnlagPrStatus(bgpsATFL1)
				.build();

		return opprettGrunnlag(periode);
	}

	private BeregningsgrunnlagPrStatus lagBeregningsgrunnlagPrStatus(String orgNr, double brutto, int andelNr,
	                                                                 AktivitetStatus aktivitetStatus, double refusjonsKrav) {
        var afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
				.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgNr))
				.medBruttoPrÅr(BigDecimal.valueOf(brutto))
				.medRefusjonPrÅr(BigDecimal.valueOf(refusjonsKrav))
				.medAndelNr(andelNr)
				.build();
		return BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(aktivitetStatus)
				.medAndelNr(aktivitetStatus.equals(AktivitetStatus.ATFL) ? null : Integer.toUnsignedLong(andelNr))
				.medArbeidsforhold(afBuilder1)
				.build();
	}

	private static Long generateId() {
		return generatedId++;
	}
}
