package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class OmfordelFraAktiviteterUtenArbeidsforhold extends LeafSpecification<FordelModell> {

    private static final String ID = "FP_BR 22.3.6";
    private static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra aktivitet uten arbeidsforhold";
    private final Comparator<FordelAndelModell> AVKORTING_COMPARATOR = Comparator.comparingInt(a -> a.getAktivitetStatus().getAvkortingPrioritet());

    private Arbeidsforhold arbeidsforholdForAndelÅFordeleTil;

    OmfordelFraAktiviteterUtenArbeidsforhold(FordelAndelModell andelMedHøyereRefEnnBG) {
        super(ID, BESKRIVELSE);
        this.arbeidsforholdForAndelÅFordeleTil = andelMedHøyereRefEnnBG.getArbeidsforhold().orElseThrow();
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
        Map<String, Object> resultater = omfordelFraBgPrStatusUtenArbeidsforholdIPrioritertRekkefølge(modell.getInput());
        return beregnet(resultater);
    }

    private Map<String, Object> omfordelFraBgPrStatusUtenArbeidsforholdIPrioritertRekkefølge(FordelPeriodeModell bgPeriode) {
        BigDecimal restÅFlytte = finnRestSomMåOmfordeles(bgPeriode);
        Map<String, Object> resultater = new HashMap<>();
        Optional<FordelAndelModell> bgPrStatusMedBeløpSomKanFlyttes = finnStatusMedDisponibeltBeløpOgHøyestAvkortingPrioritet(bgPeriode);
        while (harMerÅFlytte(restÅFlytte) && bgPrStatusMedBeløpSomKanFlyttes.isPresent()) {
            FordelAndelModell bgPrStatus = bgPrStatusMedBeløpSomKanFlyttes.get();
            FordelAndelModell arbforholdForStatus = finnArbeidsforholdAndelMedRiktigInntektskategori(bgPeriode, bgPrStatus);
            BigDecimal maksFlyttbartGrunnlag = finnFlyttbartGrunnlagForStatus(bgPrStatus);
            if (skalFlytteHeleGrunnlagetFraStatus(restÅFlytte, maksFlyttbartGrunnlag)) {
                restÅFlytte = flyttHeleGrunnlagetForStatus(bgPeriode, restÅFlytte, bgPrStatus, arbforholdForStatus, maksFlyttbartGrunnlag);
            } else {
                restÅFlytte = flyttDelerAvGrunnagetForStatus(bgPeriode, restÅFlytte, bgPrStatus, arbforholdForStatus);
            }
            resultater.put("fordeltPrÅr", bgPrStatus.getFordeltPrÅr());
            resultater.put("aktivitetstatus", bgPrStatus.getAktivitetStatus());
            bgPrStatusMedBeløpSomKanFlyttes = finnStatusMedDisponibeltBeløpOgHøyestAvkortingPrioritet(bgPeriode);
        }
        return resultater;
    }

    private FordelAndelModell finnArbeidsforholdAndelMedRiktigInntektskategori(FordelPeriodeModell bgPeriode, FordelAndelModell bgPrStatus) {
        Optional<FordelAndelModell> arbforholdForStatusOpt = finnAndelForArbforholdMedSammeInntektskategori(bgPeriode, bgPrStatus);
        FordelAndelModell nyAndel;
        if (arbforholdForStatusOpt.isEmpty()) {
            nyAndel = opprettNyAndel(bgPeriode);
        } else {
            nyAndel = arbforholdForStatusOpt.get();
        }
        FordelAndelModell.oppdater(nyAndel).medInntektskategori(bgPrStatus.getInntektskategori());
        return nyAndel;
    }

    private BigDecimal flyttHeleGrunnlagetForStatus(FordelPeriodeModell bgPeriode,
                                                    BigDecimal restSomSkalFlyttesTilArbforhold,
                                                    FordelAndelModell bgPrStatus,
                                                    FordelAndelModell arbforholdForStatus,
                                                    BigDecimal maksimaltBeløpForOmfordelingPrStatus) {
        settFordeltForStatusTilNull(bgPrStatus);
        adderBeløpTilBgForArbeidsforhold(arbforholdForStatus, maksimaltBeløpForOmfordelingPrStatus);
        adderBeløpTilRefusjonForArbeidsforhold(finnEksisterende(bgPeriode), arbforholdForStatus, maksimaltBeløpForOmfordelingPrStatus);
        restSomSkalFlyttesTilArbforhold = reduserRestbeløpSomSkalOmfordeles(restSomSkalFlyttesTilArbforhold, maksimaltBeløpForOmfordelingPrStatus);
        return restSomSkalFlyttesTilArbforhold;
    }

    private BigDecimal flyttDelerAvGrunnagetForStatus(FordelPeriodeModell bgPeriode, BigDecimal restSomSkalFlyttesTilArbforhold, FordelAndelModell bgPrStatus, FordelAndelModell arbforholdForStatus) {
        reduserFordeltForStatus(restSomSkalFlyttesTilArbforhold, bgPrStatus);
        adderBeløpTilBgForArbeidsforhold(arbforholdForStatus, restSomSkalFlyttesTilArbforhold);
        adderBeløpTilRefusjonForArbeidsforhold(finnEksisterende(bgPeriode), arbforholdForStatus, restSomSkalFlyttesTilArbforhold);
        restSomSkalFlyttesTilArbforhold = BigDecimal.ZERO;
        return restSomSkalFlyttesTilArbforhold;
    }

    private Optional<FordelAndelModell> finnAndelForArbforholdMedSammeInntektskategori(FordelPeriodeModell beregningsgrunnlagPeriode, FordelAndelModell bgPrStatus) {
        return getGrunnlagForArbeidsforhold(beregningsgrunnlagPeriode)
            .stream()
	        .filter(a -> a.getInntektskategori() == null || a.getInntektskategori().equals(Inntektskategori.UDEFINERT)
			        || a.getInntektskategori().equals(bgPrStatus.getInntektskategori())).findFirst();
    }

    private FordelAndelModell opprettNyAndel(FordelPeriodeModell beregningsgrunnlagPeriode) {
	    FordelAndelModell nyAndel = FordelAndelModell.builder()
            .medArbeidsforhold(arbeidsforholdForAndelÅFordeleTil)
		    .medAktivitetStatus(AktivitetStatus.AT)
            .erNytt(true)
            .build();
        beregningsgrunnlagPeriode.leggTilAndel(nyAndel);
        return nyAndel;
    }

    private FordelAndelModell finnEksisterende(FordelPeriodeModell beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT).stream()
            .filter(a -> matcherArbeidsforhold(a) && a.getAndelNr() != null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke eksisterende BeregningsgrunnlagPrArbeidsforhold for " + arbeidsforholdForAndelÅFordeleTil));
    }

    private BigDecimal reduserRestbeløpSomSkalOmfordeles(BigDecimal restbeløpSomSkalOmfordelesTilAktivitet, BigDecimal maksimaltBeløpForOmfordelingPrStatus) {
        restbeløpSomSkalOmfordelesTilAktivitet = restbeløpSomSkalOmfordelesTilAktivitet.subtract(maksimaltBeløpForOmfordelingPrStatus);
        return restbeløpSomSkalOmfordelesTilAktivitet;
    }

    private void settFordeltForStatusTilNull(FordelAndelModell bgPrStatus) {
        FordelAndelModell.oppdater(bgPrStatus)
            .medFordeltPrÅr(BigDecimal.ZERO);
    }

    private void reduserFordeltForStatus(BigDecimal restbeløpSomSkalOmfordelesTilAktivitet, FordelAndelModell bgPrStatus) {
        BigDecimal fordelt = bgPrStatus.getBruttoPrÅr().orElse(BigDecimal.ZERO).subtract(restbeløpSomSkalOmfordelesTilAktivitet);
        if (fordelt.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Kan ikke fordele et negativt beløp til " + bgPrStatus.getAktivitetStatus());
        }
        FordelAndelModell.oppdater(bgPrStatus)
            .medFordeltPrÅr(fordelt);
    }

    private BigDecimal finnRestSomMåOmfordeles(FordelPeriodeModell beregningsgrunnlagPeriode) {
        List<FordelAndelModell> grunnlagForArbeidsforhold = getGrunnlagForArbeidsforhold(beregningsgrunnlagPeriode);
        BigDecimal refusjonskravPrÅr = grunnlagForArbeidsforhold.stream()
            .map(FordelAndelModell::getGjeldendeRefusjonPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal bruttoBgForArbeidsforhold = grunnlagForArbeidsforhold.stream().map(FordelAndelModell::getBruttoInkludertNaturalytelsePrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        if (refusjonskravPrÅr.compareTo(bruttoBgForArbeidsforhold) <= 0) {
            throw new IllegalStateException("Skal ikke flytte beregningsgrunnlag til arbeidsforhold der refusjon ikke overstiger beregningsgrunnlag som allerede er satt.");
        }
        return refusjonskravPrÅr.subtract(bruttoBgForArbeidsforhold);
    }

    private List<FordelAndelModell> getGrunnlagForArbeidsforhold(FordelPeriodeModell beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT)
            .stream()
            .filter(this::matcherArbeidsforhold)
            .collect(Collectors.toList());
    }

	private boolean matcherArbeidsforhold(FordelAndelModell a) {
		return a.getArbeidsforhold().orElseThrow(() -> new IllegalStateException("Forventer at " +
				"alle arbeidsandeler har arbeidsforhold")).equals(arbeidsforholdForAndelÅFordeleTil);
	}

	private Optional<FordelAndelModell> finnStatusMedDisponibeltBeløpOgHøyestAvkortingPrioritet(FordelPeriodeModell beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusSomSkalBrukes()
            .stream()
            .filter(this::harBgSomKanFlyttes)
            .filter(bgprStatus -> bgprStatus.getArbeidsforhold().isEmpty())
            .max(AVKORTING_COMPARATOR);
    }

    private BigDecimal finnFlyttbartGrunnlagForStatus(FordelAndelModell bgPrStatus) {
        return bgPrStatus.getBruttoPrÅr().orElse(BigDecimal.ZERO);
    }

    private boolean skalFlytteHeleGrunnlagetFraStatus(BigDecimal restBeløpSomMåFlyttes, BigDecimal flyttbartBeløp) {
        return restBeløpSomMåFlyttes.compareTo(flyttbartBeløp) >= 0;
    }

    private boolean harMerÅFlytte(BigDecimal skalFlyttesTilAktivitet) {
        return skalFlyttesTilAktivitet.compareTo(BigDecimal.ZERO) > 0;
    }

    private void adderBeløpTilBgForArbeidsforhold(FordelAndelModell arbeidsforholdForStatus, BigDecimal beløpSomSkalOmfordelesTilArbeidsforhold) {
        FordelAndelModell.oppdater(arbeidsforholdForStatus)
            .medFordeltPrÅr(arbeidsforholdForStatus.getBruttoPrÅr().isPresent() ? arbeidsforholdForStatus.getBruttoPrÅr().get().add(beløpSomSkalOmfordelesTilArbeidsforhold) : beløpSomSkalOmfordelesTilArbeidsforhold); // NOSONAR
    }

    private void adderBeløpTilRefusjonForArbeidsforhold(FordelAndelModell eksisterende,
                                                        FordelAndelModell aktivitet,
                                                        BigDecimal beløpSomSkalOmfordelesTilArbeidsforhold) {
        if (aktivitet.getAndelNr() != null) {
            return;
        }
        if (eksisterende.getGjeldendeRefusjonPrÅr().isEmpty()) {
            throw new IllegalStateException("Eksisterende andel har ikke refusjonskrav.");
        }
        if (eksisterende.getGjeldendeRefusjonPrÅr().get().compareTo(beløpSomSkalOmfordelesTilArbeidsforhold) < 0) { // NOSONAR
            throw new IllegalStateException("Skal ikke flytte mer av refusjonskravet.");
        }
	    BigDecimal nyRefusjon = eksisterende.getGjeldendeRefusjonPrÅr().get().subtract(beløpSomSkalOmfordelesTilArbeidsforhold); // NOSONAR
	    FordelAndelModell.oppdater(eksisterende)
		    .medGjeldendeRefusjonPrÅr(nyRefusjon)
            .medFordeltRefusjonPrÅr(nyRefusjon);

	    BigDecimal fordeltRefusjon = aktivitet.getGjeldendeRefusjonPrÅr().isPresent() ? aktivitet.getGjeldendeRefusjonPrÅr().get().add(beløpSomSkalOmfordelesTilArbeidsforhold) : beløpSomSkalOmfordelesTilArbeidsforhold; // NOSONAR
	    FordelAndelModell.oppdater(aktivitet)
		    .medGjeldendeRefusjonPrÅr(fordeltRefusjon)
            .medFordeltRefusjonPrÅr(fordeltRefusjon);
    }

    private boolean harBgSomKanFlyttes(FordelAndelModell beregningsgrunnlagPrStatus) {
        return beregningsgrunnlagPrStatus.getBruttoPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
    }
}
