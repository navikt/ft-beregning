package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

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

abstract class OmfordelFraATFL extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private Arbeidsforhold arbeidsforhold;

    OmfordelFraATFL(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, String id, String beskrivelse) {
        super(id, beskrivelse);
        this.arbeidsforhold = arbeidsforhold.getArbeidsforhold();
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        Map<String, Object> resultater = omfordelFraAktivitetOmMulig(beregningsgrunnlagPeriode);
        return beregnet(resultater);
    }

    private Map<String, Object> omfordelFraAktivitetOmMulig(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        boolean harAktivitetMedOmfordelbartGrunnlag = finnAktivitetMedOmfordelbartBg(beregningsgrunnlagPeriode).isPresent();
        if (!harAktivitetMedOmfordelbartGrunnlag) {
            return Map.of();
        }
        var aktivitet = finnArbeidsforholdMedRiktigInntektskategori(beregningsgrunnlagPeriode);
        return new OmfordelBGForArbeidsforhold(beregningsgrunnlagPeriode).omfordelBGForArbeidsforhold(aktivitet, this::finnAktivitetMedOmfordelbartBg);
    }

    private BeregningsgrunnlagPrArbeidsforhold finnArbeidsforholdMedRiktigInntektskategori(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        Optional<BeregningsgrunnlagPrArbeidsforhold> andelForArbeidsforholdOpt = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getArbeidsforhold()
            .stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold)
                && (a.getInntektskategori() == null || a.getInntektskategori().equals(Inntektskategori.UDEFINERT) || a.getInntektskategori().equals(finnInntektskategori())))
            .findFirst();
        BeregningsgrunnlagPrArbeidsforhold aktivitet;
        if (andelForArbeidsforholdOpt.isEmpty()) {
            aktivitet = opprettNyAndel(beregningsgrunnlagPeriode);
        } else {
            aktivitet = andelForArbeidsforholdOpt.get();
        }
        BeregningsgrunnlagPrArbeidsforhold.builder(aktivitet).medInntektskategori(finnInntektskategori());
        return aktivitet;
    }

    protected abstract Inntektskategori finnInntektskategori();

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

    protected abstract Optional<BeregningsgrunnlagPrArbeidsforhold> finnAktivitetMedOmfordelbartBg(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode);


}
