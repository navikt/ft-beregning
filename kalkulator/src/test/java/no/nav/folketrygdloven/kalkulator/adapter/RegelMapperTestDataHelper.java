package no.nav.folketrygdloven.kalkulator.adapter;

import static java.util.Collections.singletonList;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GRUNNBELØP_2018;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2013;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2014;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2015;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2016;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2017;
import static no.nav.folketrygdloven.kalkulator.GrunnbeløpTestKonstanter.GSNITT_2018;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

public class RegelMapperTestDataHelper {
    public static final LocalDate NOW = LocalDate.now();
    public static final LocalDate MINUS_DAYS_5 = LocalDate.now().minusDays(5);
    public static final LocalDate MINUS_DAYS_10 = LocalDate.now().minusDays(10);
    public static final LocalDate MINUS_DAYS_20 = LocalDate.now().minusDays(20);
    public static final LocalDate MINUS_YEARS_1 = LocalDate.now().minusYears(1);
    public static final LocalDate MINUS_YEARS_2 = LocalDate.now().minusYears(2);
    public static final LocalDate MINUS_YEARS_3 = LocalDate.now().minusYears(3);

    public static final List<Grunnbeløp> GRUNNBELØPLISTE = List.of(
        new Grunnbeløp(LocalDate.of(2013, 05, 01), LocalDate.of(2014, 04, 30), 85245L, GSNITT_2013),
        new Grunnbeløp(LocalDate.of(2014, 05, 01), LocalDate.of(2015, 04, 30), 88370L, GSNITT_2014),
        new Grunnbeløp(LocalDate.of(2015, 05, 01), LocalDate.of(2016, 04, 30), 90068L, GSNITT_2015),
        new Grunnbeløp(LocalDate.of(2016, 05, 01), LocalDate.of(2017, 04, 30), 92576L, GSNITT_2016),
        new Grunnbeløp(LocalDate.of(2017, 05, 01), LocalDate.of(2018, 04, 30), GRUNNBELØP_2017, GSNITT_2017),
        new Grunnbeløp(LocalDate.of(2018, 05, 01), LocalDate.MAX, GRUNNBELØP_2018, GSNITT_2018));

    public static BeregningsgrunnlagDto buildVLBeregningsgrunnlag() {
        return BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(MINUS_DAYS_5)
            .medGrunnbeløp(Beløp.ZERO)
            .build();
    }

    public static void buildVLBGAktivitetStatus(BeregningsgrunnlagDto beregningsgrunnlag, AktivitetStatus aktivitetStatus) {
        BeregningsgrunnlagAktivitetStatusDto.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medHjemmel(Hjemmel.F_14_7_8_30)
            .build(beregningsgrunnlag);
    }

    public static SammenligningsgrunnlagPrStatusDto buildSammenligningsgrunnlagPrStatus(SammenligningsgrunnlagType sammenligningsgrunnlagType) {
        return SammenligningsgrunnlagPrStatusDto.builder()
            .medSammenligningsperiode(MINUS_YEARS_1, NOW)
            .medRapportertPrÅr(Beløp.fra(BigDecimal.valueOf(1098318.12)))
            .medAvvikPromilleNy(BigDecimal.valueOf(220L))
            .medSammenligningsgrunnlagType(sammenligningsgrunnlagType)
            .build();
    }

    public static BeregningsgrunnlagPeriodeDto buildVLBGPeriode(BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(beregningsgrunnlag.getSkjæringstidspunkt(), beregningsgrunnlag.getSkjæringstidspunkt().plusYears(3))
            .medBruttoPrÅr(Beløp.fra(BigDecimal.valueOf(534343.55)))
            .medAvkortetPrÅr(Beløp.fra(BigDecimal.valueOf(223421.334)))
            .medRedusertPrÅr(Beløp.fra(BigDecimal.valueOf(23412.32)))
            .build(beregningsgrunnlag);
    }

    public static void buildVLBGPStatusForSN(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        buildVLBGPStatus(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, MINUS_DAYS_10, MINUS_DAYS_5);
    }

    public static void buildVLBGPStatus(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                        AktivitetStatus aktivitetStatus,
                                        Inntektskategori inntektskategori, LocalDate fom, LocalDate tom) {
        buildVLBGPStatus(beregningsgrunnlagPeriode, aktivitetStatus, inntektskategori, fom, tom,
            null, AktivitetStatus.FRILANSER.equals(aktivitetStatus) ? OpptjeningAktivitetType.FRILANS : null);
    }

    public static void buildVLBGPStatus(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                        AktivitetStatus aktivitetStatus,
                                        Inntektskategori inntektskategori, LocalDate fom, LocalDate tom,
                                        Arbeidsgiver arbeidsgiver,
                                        OpptjeningAktivitetType arbforholdType) {

        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(aktivitetStatus)
            .medInntektskategori(inntektskategori)
            .medBeregningsperiode(fom, tom)
            .medArbforholdType(arbforholdType)
            .medBeregnetPrÅr(Beløp.fra(BigDecimal.valueOf(1000.01)))
            .medOverstyrtPrÅr(Beløp.fra(BigDecimal.valueOf(4444432.32)))
            .medAvkortetPrÅr(Beløp.fra(BigDecimal.valueOf(12.12)))
            .medRedusertPrÅr(Beløp.fra(BigDecimal.valueOf(34.34)))
            .medRedusertRefusjonPrÅr(Beløp.fra(BigDecimal.valueOf(52000.0)))
            .medRedusertBrukersAndelPrÅr(Beløp.fra(BigDecimal.valueOf(26000.0)));

        if (AktivitetStatus.ARBEIDSTAKER.equals(aktivitetStatus)) {
            BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsgiver(arbeidsgiver)
                .medRefusjonskravPrÅr(Beløp.fra(BigDecimal.valueOf(42.00)), Utfall.GODKJENT)
                .medNaturalytelseBortfaltPrÅr(Beløp.fra(BigDecimal.valueOf(3232.32)))
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsforholdRef((InternArbeidsforholdRefDto) null);
            builder.medBGAndelArbeidsforhold(bga);
        }
        builder.build(beregningsgrunnlagPeriode);
    }

    public static SammenligningsGrunnlag buildRegelSammenligningsG() {
        return SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(new Periode(MINUS_YEARS_1, MINUS_DAYS_20))
            .medRapportertPrÅr(BigDecimal.valueOf(42))
            .medAvvikProsent(BigDecimal.ZERO)
            .build();
    }

    public static SammenligningsGrunnlag buildRegelSammenligningsG(SammenligningGrunnlagType type) {
        return SammenligningsGrunnlag.builder()
                .medSammenligningstype(type)
                .medSammenligningsperiode(new Periode(MINUS_YEARS_1, MINUS_DAYS_20))
                .medRapportertPrÅr(BigDecimal.valueOf(42))
                .medAvvikProsent(BigDecimal.ZERO)
                .build();
    }


    public static BeregningsgrunnlagPrStatus buildRegelBGPeriode(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode regelBGP, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus status, Periode periode) {
        if (no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.erArbeidstakerEllerFrilanser(status)) {
            final BeregningsgrunnlagPrStatus regelBGPStatus = BeregningsgrunnlagPrStatus.builder(regelBGP.getBeregningsgrunnlagPrStatus(status))
                .medBeregningsperiode(periode)
                .build();
            return regelBGPStatus;
        } else {
            final BeregningsgrunnlagPrStatus regelBGPStatus = BeregningsgrunnlagPrStatus.builder(regelBGP.getBeregningsgrunnlagPrStatus(status))
                .medFordeltPrÅr(BigDecimal.valueOf(400000.42))
                .medBeregnetPrÅr(BigDecimal.valueOf(400000.42))
                .medAvkortetPrÅr(BigDecimal.valueOf(789.789))
                .medRedusertPrÅr(BigDecimal.valueOf(901.901))
                .medBeregningsperiode(periode)
                .build();
            return regelBGPStatus;
        }
    }

    public static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag buildRegelBeregningsgrunnlag(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus,
                                                                                                                         no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori inntektskategori,
                                                                                                                         BeregningsgrunnlagHjemmel hjemmel) {
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode periode = buildRegelBGPeriode(aktivitetStatus, inntektskategori);
        return no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medSkjæringstidspunkt(NOW)
            .medAktivitetStatuser(singletonList(new AktivitetStatusMedHjemmel(aktivitetStatus, hjemmel)))
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .build();
    }

    private static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode buildRegelBGPeriode(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori inntektskategori) {
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode.Builder periodeBuilder =
            no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(NOW, null));
        long andelNr = 1;
        if (no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.erKombinasjonMedSelvstendig(aktivitetStatus)) {
            BeregningsgrunnlagPrStatus prStatusATFL = buildPrStatus(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL, inntektskategori, null);
            BeregningsgrunnlagPrStatus prStatusSN = buildPrStatus(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.SN, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, andelNr);
            return periodeBuilder
                .medBeregningsgrunnlagPrStatus(prStatusATFL)
                .medBeregningsgrunnlagPrStatus(prStatusSN)
                .build();
        }
        return periodeBuilder
            .medBeregningsgrunnlagPrStatus(buildPrStatus(aktivitetStatus, inntektskategori, andelNr))
            .build();
    }

    private static BeregningsgrunnlagPrStatus buildPrStatus(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori inntektskategori, Long andelNr) {
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medInntektskategori(inntektskategori)
            .medAndelNr(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus.erArbeidstaker(aktivitetStatus) ? null : andelNr)
            .build();
    }
}
