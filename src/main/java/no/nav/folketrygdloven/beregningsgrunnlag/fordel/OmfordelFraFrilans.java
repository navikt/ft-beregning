package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class OmfordelFraFrilans extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 22.3.8";
    static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra frilans.";

    private Arbeidsforhold arbeidsforhold;

    OmfordelFraFrilans(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold.getArbeidsforhold();
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        Map<String, Object> resultater = omfordelFraFrilansOmMulig(beregningsgrunnlagPeriode);
        return beregnet(resultater);
    }

    private Map<String, Object> omfordelFraFrilansOmMulig(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        var aktivitet = finnArbeidsforholdMedFrilansInntektskategori(beregningsgrunnlagPeriode);
        return new OmfordelBGForArbeidsforhold(beregningsgrunnlagPeriode).omfordelBGForArbeidsforhold(aktivitet, this::finnFrilansMedOmfordelbartBg);
    }

    private BeregningsgrunnlagPrArbeidsforhold finnArbeidsforholdMedFrilansInntektskategori(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        Optional<BeregningsgrunnlagPrArbeidsforhold> frilansAndelForArbeidsforholdOpt = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getArbeidsforhold()
            .stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold)
                && (a.getInntektskategori() == null || a.getInntektskategori().equals(Inntektskategori.UDEFINERT) || a.getInntektskategori().equals(Inntektskategori.FRILANSER)))
            .findFirst();
        BeregningsgrunnlagPrArbeidsforhold aktivitet;
        if (frilansAndelForArbeidsforholdOpt.isEmpty()) {
            aktivitet = opprettNyAndel(beregningsgrunnlagPeriode);
        } else {
            aktivitet = frilansAndelForArbeidsforholdOpt.get();
        }
        BeregningsgrunnlagPrArbeidsforhold.builder(aktivitet).medInntektskategori(Inntektskategori.FRILANSER);
        return aktivitet;
    }

    private BeregningsgrunnlagPrArbeidsforhold opprettNyAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        BeregningsgrunnlagPrArbeidsforhold aktivitet;
        aktivitet = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(arbeidsforhold)
            .erNytt(true)
            .build();
        BeregningsgrunnlagPrStatus.builder(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL))
            .medArbeidsforhold(aktivitet);
        return aktivitet;
    }

    private Optional<BeregningsgrunnlagPrArbeidsforhold> finnFrilansMedOmfordelbartBg(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getFrilansArbeidsforhold()
            .stream()
            .filter(this::harBgSomKanFlyttes)
            .findFirst();
    }

    private boolean harBgSomKanFlyttes(BeregningsgrunnlagPrArbeidsforhold beregningsgrunnlagPrArbeidsforhold) {
        return beregningsgrunnlagPrArbeidsforhold.getBruttoPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
            && (beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr() == null || beregningsgrunnlagPrArbeidsforhold.getFordeltPrÅr().compareTo(BigDecimal.ZERO) > 0);
    }




}
