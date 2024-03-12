package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AktivitetGradering;
import no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

public class ManuellBehandlingRefusjonGraderingDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.now();
    private final static String ORGNR = "123456780";
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet(ORGNR);
    private final static String ORGNR2 = "123456781";
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet(ORGNR2);
    public static final int GRUNNBELØP = 90_000;
    private static final long ANDELSNR2 = 2L;


    private BeregningAktivitetAggregatDto aktivitetAggregatEntitet;

    @BeforeEach
    public void setUp() {
        aktivitetAggregatEntitet = BeregningAktivitetAggregatDto.builder()
                .leggTilAktivitet(lagAktivitet(ARBEIDSGIVER))
                .leggTilAktivitet(lagAktivitet(ARBEIDSGIVER2))
                .leggTilAktivitet(lagNæring())
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING).build();
    }

    private BeregningAktivitetDto lagAktivitet(Arbeidsgiver arbeidsgiver) {
        return BeregningAktivitetDto.builder()
                .medArbeidsgiver(arbeidsgiver).medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1))).build();
    }

    private BeregningAktivitetDto lagNæring() {
        return BeregningAktivitetDto.builder().medOpptjeningAktivitetType(OpptjeningAktivitetType.NÆRING)
                .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1))).build();
    }


    @Test
    public void skalKunneEndreInntektEtterRedusertRefusjonTilUnder6G() {
        // Arrange
        AndelGradering graderinger = lagGradering();
        BeregningsgrunnlagDto bgFørFordeling = lagBeregningsgrunnlagFørFordeling();

        List<InntektsmeldingDto> inntektsmeldinger = lagInntektsmeldingOver6GRefusjon();

        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregatEntitet)
                .medBeregningsgrunnlag(bgFørFordeling).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Act
        boolean kreverManuellBehandling = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereInntekt(grunnlag,
                new AktivitetGradering(graderinger), bgFørFordeling.getBeregningsgrunnlagPerioder().get(0), bgFørFordeling.getBeregningsgrunnlagPerioder(), inntektsmeldinger, Collections.emptyList());

        // Assert
        assertThat(kreverManuellBehandling).isTrue();
    }


    @Test
    public void skalKunneEndreInntektOmTidligerePeriodeHarGraderingForAndelSomVilBliAvkortetTil0() {
        // Arrange
        LocalDate graderingTom = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1);
        AndelGradering graderingNæring = lagGraderingForNæringFraSTP(graderingTom);
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(GRUNNBELØP))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE))
                .build();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(bg, SKJÆRINGSTIDSPUNKT_OPPTJENING, graderingTom);
        leggTilArbeidstakerOver6GOgNæring(periode);
        BeregningsgrunnlagPeriodeDto periode2 = lagPeriode(bg, graderingTom.plusDays(1), null);
        leggTilArbeidstakerOver6GOgNæring(periode2);

        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregatEntitet)
                .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Act
        boolean kreverManuellBehandling1 = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereInntekt(
                grunnlag,
                new AktivitetGradering(graderingNæring),
                periode,
                bg.getBeregningsgrunnlagPerioder(),
                List.of(), Collections.emptyList());

        boolean kreverManuellBehandling2 = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereInntekt(
                grunnlag,
                new AktivitetGradering(graderingNæring),
                periode2,
                bg.getBeregningsgrunnlagPerioder(),
                List.of(), Collections.emptyList());


        // Assert
        assertThat(kreverManuellBehandling1).isTrue();
        assertThat(kreverManuellBehandling2).isTrue();
    }

    @Test
    public void skalKunneEndreRefusjonEtterRedusertRefusjonTilUnder6G() {
        // Arrange
        AndelGradering graderinger = lagGradering();
        BeregningsgrunnlagDto bgFørFordeling = lagBeregningsgrunnlagFørFordeling();
        List<InntektsmeldingDto> inntektsmeldinger = lagInntektsmeldingOver6GRefusjon();

        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregatEntitet)
                .medBeregningsgrunnlag(bgFørFordeling).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_REFUSJON_OG_GRADERING);

        // Act
        boolean kreverManuellBehandlingAvRefusjon = ManuellBehandlingRefusjonGraderingDtoTjeneste.skalSaksbehandlerRedigereRefusjon(
                grunnlag,
            new AktivitetGradering(graderinger),
            bgFørFordeling.getBeregningsgrunnlagPerioder().get(0), inntektsmeldinger, Beløp.fra(GRUNNBELØP), Collections.emptyList());

        // Assert
        assertThat(kreverManuellBehandlingAvRefusjon).isTrue();
    }

    private AndelGradering lagGraderingForNæringFraSTP(LocalDate graderingTom) {
        return AndelGradering.builder()
                .medStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .leggTilGradering(new AndelGradering.Gradering(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING,
                        graderingTom), Aktivitetsgrad.fra(50)))
                .build();
    }

    private BeregningsgrunnlagPeriodeDto lagPeriode(BeregningsgrunnlagDto bg, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(fom, tom).build(bg);
    }

    private void leggTilArbeidstakerOver6GOgNæring(BeregningsgrunnlagPeriodeDto periode) {
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER).medRefusjonskravPrÅr(Beløp.fra(GRUNNBELØP * 7), Utfall.GODKJENT))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(Beløp.fra(3*GRUNNBELØP))
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(ANDELSNR2)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(Beløp.fra(3*GRUNNBELØP))
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(3L)
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medBeregnetPrÅr(Beløp.fra(10))
                .build(periode);
    }

    private no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering lagGradering() {
        return no.nav.folketrygdloven.kalkulator.modell.gradering.AndelGradering.builder()
                .medStatus(AktivitetStatus.ARBEIDSTAKER)
                .leggTilGradering(new AndelGradering.Gradering(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, TIDENES_ENDE), Aktivitetsgrad.fra(50)))
                .medArbeidsgiver(ARBEIDSGIVER2).build();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagFørFordeling() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .medGrunnbeløp(Beløp.fra(GRUNNBELØP))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .build();
        BeregningsgrunnlagPeriodeDto periode = lagPeriode(bg, SKJÆRINGSTIDSPUNKT_OPPTJENING, null);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER).medRefusjonskravPrÅr(Beløp.fra(GRUNNBELØP * 7), Utfall.GODKJENT))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(Beløp.fra(10))
                .build(periode);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAndelsnr(ANDELSNR2)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER2))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(Beløp.fra(10))
                .build(periode);
        return bg;
    }

    private List<InntektsmeldingDto> lagInntektsmeldingOver6GRefusjon() {
        return List.of(InntektsmeldingDtoBuilder.builder().medArbeidsgiver(ARBEIDSGIVER).medRefusjon(Beløp.fra(GRUNNBELØP * 7)).build());
    }


}
