package no.nav.folketrygdloven.beregningsgrunnlag;

import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class BeregningsgrunnlagScenarioFastsett {

	private static final String ORGNR = "987";
	public static final long GRUNNBELØP_2017 = 93634;

	public static Beregningsgrunnlag settoppGrunnlagMedEnPeriode(LocalDate skjæringstidspunkt,
	                                                             List<AktivitetStatus> aktivitetStatuser,
	                                                             List<Arbeidsforhold> arbeidsforhold,
	                                                             List<BigDecimal> refusjonskravPrår) {

        var periodeBuilder = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(skjæringstidspunkt, null));
		long andelNr = arbeidsforhold.size() + 1;
		for (var aktivitetStatus : aktivitetStatuser) {
			if (AktivitetStatus.ATFL_SN.equals(aktivitetStatus)) {
                var bgpsATFL = BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(arbeidsforhold, refusjonskravPrår)
						.build();
                var bgpsSN = BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.SN)
						.medAndelNr(andelNr++)
						.build();
				periodeBuilder.medBeregningsgrunnlagPrStatus(bgpsATFL).medBeregningsgrunnlagPrStatus(bgpsSN);
			} else if (AktivitetStatus.KUN_YTELSE.equals(aktivitetStatus)) {
                var bgpsBA = BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.BA)
						.medAndelNr(andelNr++)
						.build();
				periodeBuilder.medBeregningsgrunnlagPrStatus(bgpsBA);
			} else {
                var bgps = BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(aktivitetStatus);
				if (AktivitetStatus.erArbeidstakerEllerFrilanser(aktivitetStatus)) {
					bgps.medArbeidsforhold(arbeidsforhold, refusjonskravPrår);
				} else {
					bgps.medAndelNr(andelNr++);
				}
				periodeBuilder.medBeregningsgrunnlagPrStatus(bgps.build());
			}
		}
        var bgPeriode = periodeBuilder.build();
		return Beregningsgrunnlag.builder()
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medAktivitetStatuser(aktivitetStatuser.stream().map(as -> new AktivitetStatusMedHjemmel(as, null)).collect(Collectors.toList()))
				.medBeregningsgrunnlagPeriode(bgPeriode)
				.build();
	}

	public static Beregningsgrunnlag opprettBeregningsgrunnlagFraInntektsmelding(LocalDate skjæringstidspunkt, BigDecimal refusjonskrav) {
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
		return settoppGrunnlagMedEnPeriode(skjæringstidspunkt, singletonList(AktivitetStatus.ATFL), singletonList(arbeidsforhold), singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12))));
	}


	public static void leggTilArbeidsforholdMedInntektsmelding(BeregningsgrunnlagPeriode grunnlag,
	                                                           BigDecimal refusjonskrav, Arbeidsforhold arbeidsforhold) {
		BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL))
				.medArbeidsforhold(singletonList(arbeidsforhold), singletonList(refusjonskrav))
				.build();
	}

}
