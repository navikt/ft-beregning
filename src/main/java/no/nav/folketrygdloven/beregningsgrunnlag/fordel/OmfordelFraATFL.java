package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

abstract class OmfordelFraATFL extends LeafSpecification<FordelModell> {

    OmfordelFraATFL(String id, String beskrivelse) {
        super(id, beskrivelse);
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
        throw new IllegalStateException("Utviklerquiz: Hvorfor slår denne til?");
    }

	@Override
	public Evaluation evaluate(FordelModell modell, ServiceArgument argument) {
		var arbeidsforhold = ((FordelAndelModell) argument.verdi()).getArbeidsforhold().orElseThrow();
		Map<String, Object> resultater = omfordelFraAktivitetOmMulig(modell, arbeidsforhold);
		return beregnet(resultater);
	}

    protected Map<String, Object> omfordelFraAktivitetOmMulig(FordelModell modell, Arbeidsforhold arbeidsforhold) {
        boolean harAktivitetMedOmfordelbartGrunnlag = finnAktivitetMedOmfordelbartBg(modell.getInput()).isPresent();
        if (!harAktivitetMedOmfordelbartGrunnlag) {
            return new HashMap<>();
        }
        var aktivitet = finnArbeidsforholdMedRiktigInntektskategori(modell.getInput(), arbeidsforhold);
        return new OmfordelBGForArbeidsforhold(modell).omfordelForArbeidsforhold(aktivitet, this::finnAktivitetMedOmfordelbartBg);
    }

    protected FordelAndelModell finnArbeidsforholdMedRiktigInntektskategori(FordelPeriodeModell beregningsgrunnlagPeriode, Arbeidsforhold arbeidsforhold) {
        Optional<FordelAndelModell> andelForArbeidsforholdOpt = beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT)
            .stream()
            .filter(a -> Objects.equals(a.getArbeidsforhold().orElse(null), arbeidsforhold)
                && (a.getInntektskategori() == null || a.getInntektskategori().equals(Inntektskategori.UDEFINERT) || a.getInntektskategori().equals(finnInntektskategori())))
            .findFirst();
        FordelAndelModell aktivitet;
        if (andelForArbeidsforholdOpt.isEmpty()) {
            aktivitet = opprettNyAndel(beregningsgrunnlagPeriode, arbeidsforhold);
        } else {
            aktivitet = andelForArbeidsforholdOpt.get();
        }
	    FordelAndelModell.oppdater(aktivitet).medInntektskategori(finnInntektskategori()).build();
	    return aktivitet;
    }

    protected abstract Inntektskategori finnInntektskategori();

    private FordelAndelModell opprettNyAndel(FordelPeriodeModell beregningsgrunnlagPeriode, Arbeidsforhold arbeidsforhold) {
        FordelAndelModell aktivitet = FordelAndelModell.builder()
            .medArbeidsforhold(arbeidsforhold)
	        .medAktivitetStatus(AktivitetStatus.AT)
            .erNytt(true)
            .build();
        beregningsgrunnlagPeriode.leggTilAndel(aktivitet);
        return aktivitet;
    }

    protected abstract Optional<FordelAndelModell> finnAktivitetMedOmfordelbartBg(FordelPeriodeModell beregningsgrunnlagPeriode);


}
