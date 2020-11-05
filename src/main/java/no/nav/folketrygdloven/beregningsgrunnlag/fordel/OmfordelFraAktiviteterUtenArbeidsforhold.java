package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class OmfordelFraAktiviteterUtenArbeidsforhold extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final String ID = "FP_BR 22.3.6";
    private static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra aktivitet uten arbeidsforhold";
    private final Comparator<BeregningsgrunnlagPrStatus> AVKORTING_COMPARATOR = Comparator.comparingInt(a -> a.getAktivitetStatus().getAvkortingPrioritet());

    private Arbeidsforhold arbeidsforhold;

    OmfordelFraAktiviteterUtenArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold.getArbeidsforhold();
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        Map<String, Object> resultater = omfordelFraBgPrStatusUtenArbeidsforholdIPrioritertRekkefølge(beregningsgrunnlagPeriode);
        return beregnet(resultater);
    }

    private Map<String, Object> omfordelFraBgPrStatusUtenArbeidsforholdIPrioritertRekkefølge(BeregningsgrunnlagPeriode bgPeriode) {
        BigDecimal restÅFlytte = finnRestSomMåOmfordeles(bgPeriode);
        Map<String, Object> resultater = new HashMap<>();
        Optional<BeregningsgrunnlagPrStatus> bgPrStatusMedBeløpSomKanFlyttes = finnStatusMedDisponibeltBeløpOgHøyestAvkortingPrioritet(bgPeriode);
        while (harMerÅFlytte(restÅFlytte) && bgPrStatusMedBeløpSomKanFlyttes.isPresent()) {
            BeregningsgrunnlagPrStatus bgPrStatus = bgPrStatusMedBeløpSomKanFlyttes.get();
            BeregningsgrunnlagPrArbeidsforhold arbforholdForStatus = finnArbeidsforholdAndelMedRiktigInntektskategori(bgPeriode, bgPrStatus);
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

    private BeregningsgrunnlagPrArbeidsforhold finnArbeidsforholdAndelMedRiktigInntektskategori(BeregningsgrunnlagPeriode bgPeriode, BeregningsgrunnlagPrStatus bgPrStatus) {
        Optional<BeregningsgrunnlagPrArbeidsforhold> arbforholdForStatusOpt = finnAndelForArbforholdMedSammeInntektskategori(bgPeriode, bgPrStatus);
        BeregningsgrunnlagPrArbeidsforhold arbforholdForStatus;
        if (arbforholdForStatusOpt.isEmpty()) {
            arbforholdForStatus = opprettNyAndel(bgPeriode);
        } else {
            arbforholdForStatus = arbforholdForStatusOpt.get();
        }
        BeregningsgrunnlagPrArbeidsforhold.builder(arbforholdForStatus).medInntektskategori(bgPrStatus.getInntektskategori());
        return arbforholdForStatus;
    }

    private BigDecimal flyttHeleGrunnlagetForStatus(BeregningsgrunnlagPeriode bgPeriode,
                                                    BigDecimal restSomSkalFlyttesTilArbforhold,
                                                    BeregningsgrunnlagPrStatus bgPrStatus,
                                                    BeregningsgrunnlagPrArbeidsforhold arbforholdForStatus,
                                                    BigDecimal maksimaltBeløpForOmfordelingPrStatus) {
        settFordeltForStatusTilNull(bgPrStatus);
        adderBeløpTilBgForArbeidsforhold(arbforholdForStatus, maksimaltBeløpForOmfordelingPrStatus);
        adderBeløpTilRefusjonForArbeidsforhold(finnEksisterende(bgPeriode), arbforholdForStatus, maksimaltBeløpForOmfordelingPrStatus);
        restSomSkalFlyttesTilArbforhold = reduserRestbeløpSomSkalOmfordeles(restSomSkalFlyttesTilArbforhold, maksimaltBeløpForOmfordelingPrStatus);
        return restSomSkalFlyttesTilArbforhold;
    }

    private BigDecimal flyttDelerAvGrunnagetForStatus(BeregningsgrunnlagPeriode bgPeriode, BigDecimal restSomSkalFlyttesTilArbforhold, BeregningsgrunnlagPrStatus bgPrStatus, BeregningsgrunnlagPrArbeidsforhold arbforholdForStatus) {
        reduserFordeltForStatus(restSomSkalFlyttesTilArbforhold, bgPrStatus);
        adderBeløpTilBgForArbeidsforhold(arbforholdForStatus, restSomSkalFlyttesTilArbforhold);
        adderBeløpTilRefusjonForArbeidsforhold(finnEksisterende(bgPeriode), arbforholdForStatus, restSomSkalFlyttesTilArbforhold);
        restSomSkalFlyttesTilArbforhold = BigDecimal.ZERO;
        return restSomSkalFlyttesTilArbforhold;
    }

    private Optional<BeregningsgrunnlagPrArbeidsforhold> finnAndelForArbforholdMedSammeInntektskategori(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, BeregningsgrunnlagPrStatus bgPrStatus) {
        return getGrunnlagForArbeidsforhold(beregningsgrunnlagPeriode)
            .stream().filter(a -> a.getInntektskategori() == null || a.getInntektskategori().equals(Inntektskategori.UDEFINERT) || a.getInntektskategori().equals(bgPrStatus.getInntektskategori())).findFirst();
    }

    private BeregningsgrunnlagPrArbeidsforhold opprettNyAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPrArbeidsforhold arbeidsforholdForStatus;
        arbeidsforholdForStatus = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(arbeidsforhold)
            .erNytt(true)
            .build();
        BeregningsgrunnlagPrStatus atfl = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BeregningsgrunnlagPrStatus.builder(atfl).medArbeidsforhold(arbeidsforholdForStatus);
        return arbeidsforholdForStatus;
    }

    private BeregningsgrunnlagPrArbeidsforhold finnEksisterende(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getArbeidsforhold().stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold) && a.getAndelNr() != null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke eksisterende BeregningsgrunnlagPrArbeidsforhold for " + arbeidsforhold));
    }

    private BigDecimal reduserRestbeløpSomSkalOmfordeles(BigDecimal restbeløpSomSkalOmfordelesTilAktivitet, BigDecimal maksimaltBeløpForOmfordelingPrStatus) {
        restbeløpSomSkalOmfordelesTilAktivitet = restbeløpSomSkalOmfordelesTilAktivitet.subtract(maksimaltBeløpForOmfordelingPrStatus);
        return restbeløpSomSkalOmfordelesTilAktivitet;
    }

    private void settFordeltForStatusTilNull(BeregningsgrunnlagPrStatus bgPrStatus) {
        BeregningsgrunnlagPrStatus.builder(bgPrStatus)
            .medFordeltPrÅr(BigDecimal.ZERO);
    }

    private void reduserFordeltForStatus(BigDecimal restbeløpSomSkalOmfordelesTilAktivitet, BeregningsgrunnlagPrStatus bgPrStatus) {
        BigDecimal fordelt = bgPrStatus.getBruttoPrÅr().subtract(restbeløpSomSkalOmfordelesTilAktivitet);
        if (fordelt.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Kan ikke fordele et negativt beløp til " + bgPrStatus.getAktivitetStatus());
        }
        BeregningsgrunnlagPrStatus.builder(bgPrStatus)
            .medFordeltPrÅr(fordelt);
    }

    private BigDecimal finnRestSomMåOmfordeles(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        List<BeregningsgrunnlagPrArbeidsforhold> grunnlagForArbeidsforhold = getGrunnlagForArbeidsforhold(beregningsgrunnlagPeriode);
        BigDecimal refusjonskravPrÅr = grunnlagForArbeidsforhold.stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getGjeldendeRefusjonPrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        BigDecimal bruttoBgForArbeidsforhold = grunnlagForArbeidsforhold.stream().map(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        if (refusjonskravPrÅr.compareTo(bruttoBgForArbeidsforhold) <= 0) {
            throw new IllegalStateException("Skal ikke flytte beregningsgrunnlag til arbeidsforhold der refusjon ikke overstiger beregningsgrunnlag som allerede er satt.");
        }
        return refusjonskravPrÅr.subtract(bruttoBgForArbeidsforhold);
    }

    private List<BeregningsgrunnlagPrArbeidsforhold> getGrunnlagForArbeidsforhold(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold()
            .stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold))
            .collect(Collectors.toList());
    }

    private Optional<BeregningsgrunnlagPrStatus> finnStatusMedDisponibeltBeløpOgHøyestAvkortingPrioritet(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusSomSkalBrukes()
            .stream()
            .filter(this::harBgSomKanFlyttes)
            .filter(bgprStatus -> bgprStatus.getArbeidsforhold().isEmpty())
            .max(AVKORTING_COMPARATOR);
    }

    private BigDecimal finnFlyttbartGrunnlagForStatus(BeregningsgrunnlagPrStatus bgPrStatus) {
        return bgPrStatus.getBruttoPrÅr();
    }

    private boolean skalFlytteHeleGrunnlagetFraStatus(BigDecimal restBeløpSomMåFlyttes, BigDecimal flyttbartBeløp) {
        return restBeløpSomMåFlyttes.compareTo(flyttbartBeløp) >= 0;
    }

    private boolean harMerÅFlytte(BigDecimal skalFlyttesTilAktivitet) {
        return skalFlyttesTilAktivitet.compareTo(BigDecimal.ZERO) > 0;
    }

    private void adderBeløpTilBgForArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforholdForStatus, BigDecimal beløpSomSkalOmfordelesTilArbeidsforhold) {
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforholdForStatus)
            .medFordeltPrÅr(arbeidsforholdForStatus.getBruttoPrÅr().isPresent() ? arbeidsforholdForStatus.getBruttoPrÅr().get().add(beløpSomSkalOmfordelesTilArbeidsforhold) : beløpSomSkalOmfordelesTilArbeidsforhold);
    }

    private void adderBeløpTilRefusjonForArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold eksisterende,
                                                        BeregningsgrunnlagPrArbeidsforhold aktivitet,
                                                        BigDecimal beløpSomSkalOmfordelesTilArbeidsforhold) {
        if (aktivitet.getAndelNr() != null) {
            return;
        }
        if (eksisterende.getGjeldendeRefusjonPrÅr().isEmpty()) {
            throw new IllegalStateException("Eksisterende andel har ikke refusjonskrav.");
        }
        if (eksisterende.getGjeldendeRefusjonPrÅr().get().compareTo(beløpSomSkalOmfordelesTilArbeidsforhold) < 0) {
            throw new IllegalStateException("Skal ikke flytte mer av refusjonskravet.");
        }
	    BigDecimal nyRefusjon = eksisterende.getGjeldendeRefusjonPrÅr().get().subtract(beløpSomSkalOmfordelesTilArbeidsforhold);
	    BeregningsgrunnlagPrArbeidsforhold.builder(eksisterende)
		    .medGjeldendeRefusjonPrÅr(nyRefusjon)
            .medFordeltRefusjonPrÅr(nyRefusjon);

	    BigDecimal fordeltRefusjon = aktivitet.getGjeldendeRefusjonPrÅr().isPresent() ? aktivitet.getGjeldendeRefusjonPrÅr().get().add(beløpSomSkalOmfordelesTilArbeidsforhold) : beløpSomSkalOmfordelesTilArbeidsforhold;
	    BeregningsgrunnlagPrArbeidsforhold.builder(aktivitet)
		    .medGjeldendeRefusjonPrÅr(fordeltRefusjon)
            .medFordeltRefusjonPrÅr(fordeltRefusjon);
    }


    private boolean harBgSomKanFlyttes(BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus) {
        return beregningsgrunnlagPrStatus.getBruttoPrÅr().compareTo(BigDecimal.ZERO) > 0;
    }
}
