package no.nav.folketrygdloven.skjæringstidspunkt.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellK9;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettKombinasjoner.ID)
public class FastsettStatusOgAndelPrPeriode extends LeafSpecification<AktivitetStatusModell> {

    public static final String ID = "FP_BR_19_2";
    public static final String BESKRIVELSE = "Fastsett status per andel og periode";
	private static final int MIDLERTIDIG_INAKTIV_MAX_VARIGHET_DAGER = 28;

	protected FastsettStatusOgAndelPrPeriode() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        opprettAktivitetStatuser(regelmodell);

        Map<String, Object> resultater = new HashMap<>();
        regelmodell.getAktivitetStatuser()
            .forEach(as -> resultater.put("Aktivitetstatus." + as.name(), as.getBeskrivelse()));
        regelmodell.getBeregningsgrunnlagPrStatusListe()
            .forEach(bgps -> resultater.put("BeregningsgrunnlagPrStatus." + bgps.getAktivitetStatus().name(), bgps.getAktivitetStatus().getBeskrivelse()));
        return beregnet(resultater);
    }

    private void opprettAktivitetStatuser(AktivitetStatusModell regelmodell) {
        List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
        List<AktivPeriode> aktivePerioderVedStp = hentAktivePerioderForBeregning(regelmodell, aktivePerioder);

	    if (harKunYtelsePåSkjæringstidspunkt(aktivePerioderVedStp)) {
            regelmodell.leggTilAktivitetStatus(AktivitetStatus.KUN_YTELSE);
	        leggTilBrukersAndel(regelmodell);
        }
	    else {
		    MidlertidigInaktivType midlertidigInaktivType = finnMidlertidigInaktivType(regelmodell);

		    if (midlertidigInaktivType != null && midlertidigInaktivType.equals(MidlertidigInaktivType.A)) {
			    regelmodell.leggTilAktivitetStatus(AktivitetStatus.MIDL_INAKTIV);
			    leggTilBrukersAndel(regelmodell);
		    } else if (midlertidigInaktivType != null && midlertidigInaktivType.equals(MidlertidigInaktivType.B)) {
			    regelmodell.leggTilAktivitetStatus(AktivitetStatus.MIDL_INAKTIV);
			    if (harAlleInntektsmelding(aktivePerioderVedStp)) {
				    opprettStatusForAktiviteter(regelmodell, aktivePerioderVedStp);
			    } else {
				    leggTilBrukersAndel(regelmodell);
			    }
		    } else {
			    opprettStatusForAktiviteter(regelmodell, aktivePerioderVedStp);
		    }
	    }
    }

	private boolean harAlleInntektsmelding(List<AktivPeriode> aktivePerioderVedStp) {
		return aktivePerioderVedStp.stream().allMatch(a -> a.getArbeidsforhold() != null && a.getArbeidsforhold().harInntektsmelding());
	}

	private MidlertidigInaktivType finnMidlertidigInaktivType(AktivitetStatusModell regelmodell) {
		if (regelmodell instanceof AktivitetStatusModellK9) {
			return ((AktivitetStatusModellK9) regelmodell).getMidlertidigInaktivType();
		}
		return null;
	}

	private void leggTilBrukersAndel(AktivitetStatusModell regelmodell) {
		BeregningsgrunnlagPrStatus bgPrStatus = new BeregningsgrunnlagPrStatus(AktivitetStatus.BA);
		regelmodell.leggTilBeregningsgrunnlagPrStatus(bgPrStatus);
	}


	private void opprettStatusForAktiviteter(AktivitetStatusModell regelmodell, List<AktivPeriode> aktivePerioderVedStp) {
        for (AktivPeriode ap : aktivePerioderVedStp) {
            AktivitetStatus aktivitetStatus = mapAktivitetTilStatus(ap.getAktivitet());
            if (!AktivitetStatus.KUN_YTELSE.equals(aktivitetStatus)
		            && ikkeMilitærMedAndreAktiviteterPåStp(aktivePerioderVedStp, aktivitetStatus)) {
                regelmodell.leggTilAktivitetStatus(aktivitetStatus);
                var arbeidsforhold = AktivitetStatus.ATFL.equals(aktivitetStatus) ? ap.getArbeidsforhold() : null;
                regelmodell.leggTilBeregningsgrunnlagPrStatus(new BeregningsgrunnlagPrStatus(aktivitetStatus, arbeidsforhold));
            }
        }
    }

    private boolean ikkeMilitærMedAndreAktiviteterPåStp(List<AktivPeriode> aktivPerioderVedSkjæringtidspunkt, AktivitetStatus aktivitetStatus) {
        return !(AktivitetStatus.MS.equals(aktivitetStatus) && aktivPerioderVedSkjæringtidspunkt.size() > 1);
    }

    private boolean harKunYtelsePåSkjæringstidspunkt(List<AktivPeriode> aktivPerioderVedSkjæringtidspunkt) {
        return !aktivPerioderVedSkjæringtidspunkt.isEmpty() && aktivPerioderVedSkjæringtidspunkt.stream()
            .allMatch(ap -> mapAktivitetTilStatus(ap.getAktivitet()).equals(AktivitetStatus.KUN_YTELSE));
    }

    private AktivitetStatus mapAktivitetTilStatus(Aktivitet aktivitet) {
        List<Aktivitet> arbeistaker = Arrays.asList(Aktivitet.ARBEIDSTAKERINNTEKT, Aktivitet.FRILANSINNTEKT,
            Aktivitet.VENTELØNN_VARTPENGER, Aktivitet.ETTERLØNN_SLUTTPAKKE, Aktivitet.VIDERE_ETTERUTDANNING,
            Aktivitet.UTDANNINGSPERMISJON);
        List<Aktivitet> tilstøtendeYtelse = Arrays.asList(Aktivitet.SYKEPENGER_MOTTAKER, Aktivitet.FORELDREPENGER_MOTTAKER,
            Aktivitet.PLEIEPENGER_MOTTAKER, Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Aktivitet.OPPLÆRINGSPENGER, Aktivitet.FRISINN_MOTTAKER,
            Aktivitet.OMSORGSPENGER);
        AktivitetStatus aktivitetStatus;

        if (Aktivitet.NÆRINGSINNTEKT.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.SN;
        } else if (Aktivitet.DAGPENGEMOTTAKER.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.DP;
        } else if (Aktivitet.AAP_MOTTAKER.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.AAP;
        } else if (Aktivitet.MILITÆR_ELLER_SIVILTJENESTE.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.MS;
        } else if (tilstøtendeYtelse.contains(aktivitet)) {
            aktivitetStatus = AktivitetStatus.KUN_YTELSE;
        } else if (arbeistaker.contains(aktivitet)) {
            aktivitetStatus = AktivitetStatus.ATFL;
        } else {
            aktivitetStatus = AktivitetStatus.UDEFINERT;
        }
        return aktivitetStatus;
    }

	private List<AktivPeriode> hentAktivePerioderForBeregning(AktivitetStatusModell regelmodell, List<AktivPeriode> aktivePerioder) {
		if (regelmodell instanceof AktivitetStatusModellK9) {
			return hentAktivePerioderJustertForK9(regelmodell.getBeregningstidspunkt(), aktivePerioder);
		}
		return hentAktivePerioder(regelmodell.getBeregningstidspunkt(), aktivePerioder);
	}

	private List<AktivPeriode> hentAktivePerioder(LocalDate beregningstidspunkt, List<AktivPeriode> aktivePerioder) {
		return aktivePerioder.stream()
				.filter(ap -> ap.inneholder(beregningstidspunkt)).collect(Collectors.toList());
	}

	private List<AktivPeriode> hentAktivePerioderJustertForK9(LocalDate beregningstidspunkt, List<AktivPeriode> aktivePerioder) {
		return aktivePerioder.stream()
				.filter(ap -> ap.inneholder(beregningstidspunkt) ? true : ap.inneholder(justerBeregningstidspunktForHelg(beregningstidspunkt)))
				.collect(Collectors.toList());
	}

	private LocalDate justerBeregningstidspunktForHelg(LocalDate beregningstidspunkt) {
		if (beregningstidspunkt.getDayOfWeek() == DayOfWeek.SATURDAY || beregningstidspunkt.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return beregningstidspunkt.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY));
		}
		return beregningstidspunkt;
	}
}
