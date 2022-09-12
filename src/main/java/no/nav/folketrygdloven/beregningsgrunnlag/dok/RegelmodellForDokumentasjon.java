package no.nav.folketrygdloven.beregningsgrunnlag.dok;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.UUID;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

public class RegelmodellForDokumentasjon {

    protected static final BeregningsgrunnlagPeriode regelmodellMedEttArbeidsforhold =
            Beregningsgrunnlag.builder()
                .medSkjæringstidspunkt(LocalDate.now())
                .medAktivitetStatuser(Arrays.asList(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_30)))
                .medInntektsgrunnlag(new Inntektsgrunnlag())
                .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), 90000L, 90000L)))
                .medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.builder()
                        .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                                .medAktivitetStatus(AktivitetStatus.ATFL)
                                .medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
                                        .medArbeidsforhold(opprettArbeidsforhold())
                                        .medAndelNr(1)
                                        .build())
                                .build())
                        .medPeriode(Periode.of(LocalDate.now(), LocalDate.now().plusYears(1)))
                        .build())
                .build()
                .getBeregningsgrunnlagPerioder()
                .get(0);

    private RegelmodellForDokumentasjon() {
        super();
    }

    private static Arbeidsforhold opprettArbeidsforhold() {
        return Arbeidsforhold.builder()
                .medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
                .medOrgnr("987")
                .medArbeidsforholdId(UUID.randomUUID().toString())
                .build();
    }

}
