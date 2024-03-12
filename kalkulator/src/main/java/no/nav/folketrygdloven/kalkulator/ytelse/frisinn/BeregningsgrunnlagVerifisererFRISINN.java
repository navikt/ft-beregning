package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public final class BeregningsgrunnlagVerifisererFRISINN {

    private BeregningsgrunnlagVerifisererFRISINN() {
    }

    public static void verifiserOppdatertBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
        Objects.requireNonNull(beregningsgrunnlag.getSkjæringstidspunkt(), "Skjæringstidspunkt");
        verifiserIkkeTomListe(beregningsgrunnlag.getBeregningsgrunnlagPerioder(), "BeregningsgrunnlagPerioder");
        verifiserIkkeTomListe(beregningsgrunnlag.getAktivitetStatuser(), "Aktivitetstatuser");
        verfiserBeregningsgrunnlagPerioder(beregningsgrunnlag);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(p -> verfiserBeregningsgrunnlagAndeler(p, BeregningsgrunnlagVerifisererFRISINN::verifiserOpprettetAndel));
    }

    private static void verfiserBeregningsgrunnlagPerioder(BeregningsgrunnlagDto beregningsgrunnlag) {
        List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (int i = 0; i < beregningsgrunnlagPerioder.size(); i++) {
            BeregningsgrunnlagPeriodeDto periode = beregningsgrunnlagPerioder.get(i);
            Objects.requireNonNull(periode.getBeregningsgrunnlagPeriodeFom(), "BeregningsgrunnlagperiodeFom");
            verifiserIkkeTomListe(periode.getBeregningsgrunnlagPrStatusOgAndelList(), "BeregningsgrunnlagPrStatusOgAndelList");
            if (i > 0) {
                verifiserIkkeTomListe(periode.getPeriodeÅrsaker(), "PeriodeÅrsaker");
            }
        }
    }

    private static void verfiserBeregningsgrunnlagAndeler(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriodeDto, Consumer<BeregningsgrunnlagPrStatusOgAndelDto> verifiserAndel) {
        beregningsgrunnlagPeriodeDto.getBeregningsgrunnlagPrStatusOgAndelList().forEach(verifiserAndel);
    }

    private static void verifiserOpprettetAndel(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        Objects.requireNonNull(andel.getAktivitetStatus(), "Aktivitetstatus " + andel.toString());
        Objects.requireNonNull(andel.getAndelsnr(), "Andelsnummer " + andel.toString());
        Objects.requireNonNull(andel.getArbeidsforholdType(), "ArbeidsforholdType " + andel.toString());
        if (andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)) {
            if (andel.getArbeidsforholdType().equals(OpptjeningAktivitetType.ARBEID)) {
                verifiserOptionalPresent(andel.getBgAndelArbeidsforhold(), "BgAndelArbeidsforhold " + andel.toString());
            }
            Objects.requireNonNull(andel.getBeregningsperiodeFom(), "BeregningsperiodeFom " + andel.toString());
            Objects.requireNonNull(andel.getBeregningsperiodeTom(), "BeregningsperiodeTom " + andel.toString());
            if (andel.getBgAndelArbeidsforhold().isPresent()) {
                BGAndelArbeidsforholdDto arbFor = andel.getBgAndelArbeidsforhold().get();
                Objects.requireNonNull(arbFor.getArbeidsperiodeFom(), "arbeidsperiodeFom " + andel.toString());
                Objects.requireNonNull(arbFor.getArbeidsperiodeTom(), "arbeidsperiodeTom " + andel.toString());
            }
        }
    }

    public static void verifiserForeslåttBeregningsgrunnlag(BeregningsgrunnlagDto beregningsgrunnlag) {
        verifiserOppdatertBeregningsgrunnlag(beregningsgrunnlag);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(p -> verfiserBeregningsgrunnlagAndeler(p, lagVerifiserForeslåttAndelConsumer(p)));
        beregningsgrunnlag.getSammenligningsgrunnlagPrStatusListe().forEach(sg -> {
            Objects.requireNonNull(sg.getRapportertPrÅr(), "RapportertPrÅr");
            Objects.requireNonNull(sg.getAvvikPromilleNy(), "AvvikPromille");
            Objects.requireNonNull(sg.getSammenligningsgrunnlagType(), "sammenligningsgrunnlagType");
            Objects.requireNonNull(sg.getSammenligningsperiodeFom(), "SammenligningsperiodeFom");
            Objects.requireNonNull(sg.getSammenligningsperiodeTom(), "SammenligningsperiodeTom");
        });

    }

    private static Consumer<BeregningsgrunnlagPrStatusOgAndelDto> lagVerifiserForeslåttAndelConsumer(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        return (BeregningsgrunnlagPrStatusOgAndelDto andel) -> {
            Objects.requireNonNull(andel.getGjeldendeInntektskategori(), "Inntektskategori");
            LocalDate bgPeriodeFom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom();
            String andelBeskrivelse = "andel" + andel.getAktivitetStatus() + " i perioden fom " + bgPeriodeFom;
            Objects.requireNonNull(andel.getBruttoPrÅr(), "BruttoPrÅr er null for " + andelBeskrivelse);
            Objects.requireNonNull(andel.getBeregnetPrÅr(), "beregnetPrÅr er null for " + andelBeskrivelse);
            if (andel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER) || andel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)) {
                Objects.requireNonNull(andel.getÅrsbeløpFraTilstøtendeYtelse(), "ÅrsbeløpFraTilstøtendeYtelse");
                Objects.requireNonNull(andel.getOrginalDagsatsFraTilstøtendeYtelse(), "originalDagsatsFraTilstøtendeYtelse");
            }
        };
    }

    private static void verifiserIkkeTomListe(Collection<?> liste, String obj) {
        Objects.requireNonNull(liste, "Liste");
        if (liste.isEmpty()) {
            throw new KalkulatorException("FT-370744", String.format("Postcondition feilet: Beregningsgrunnlag i ugyldig tilstand etter steg. Listen %s er tom, men skulle ikke vært det.", obj));
        }
    }

    private static void verifiserOptionalPresent(Optional<?> opt, String obj) {
        Objects.requireNonNull(opt, "Optional");
        if (opt.isEmpty()) {
            throw new KalkulatorException("FT-370745", String.format("Postcondition feilet: Beregningsgrunnlag i ugyldig tilstand etter steg. Optional %s er ikke present, men skulle ha vært det.", obj));
        }
    }
}
