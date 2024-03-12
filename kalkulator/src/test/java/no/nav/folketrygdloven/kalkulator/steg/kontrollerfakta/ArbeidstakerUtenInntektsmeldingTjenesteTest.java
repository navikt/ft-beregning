package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class ArbeidstakerUtenInntektsmeldingTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private static final String ORGNR = "3482934982384";
    private static final InternArbeidsforholdRefDto ARB_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    private static final AktørId AKTØR_ID_ARBEIDSGIVER = AktørId.dummy();

    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;

    @BeforeEach
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(Beløp.fra(91425))
            .build();
        periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
    }

    @Test
    public void skal_returnere_andeler_uten_inntektsmelding() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InntektArbeidYtelseGrunnlagDto iayGrunnlagMock = mock(InntektArbeidYtelseGrunnlagDto.class);
        when(iayGrunnlagMock.getInntektsmeldinger()).thenReturn(Optional.empty());
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
            .build(periode);

        // Act
        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, Collections.emptyList());

        // Assert
        assertThat(andelerUtenInntektsmelding).hasSize(1);
    }

    @Test
    public void skal_returnere_andeler_uten_inntektsmelding_privatperson_som_arbeidsgiver() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.person(AKTØR_ID_ARBEIDSGIVER);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
            .build(periode);

        // Act
        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, Collections.emptyList());

        // Assert
        assertThat(andelerUtenInntektsmelding).hasSize(1);
    }


    @Test
    public void skal_tom_liste_med_andeler_om_ingen_arbeidstakere_uten_inntektsmelding() {
        // Arrange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder().medArbeidsgiver(arbeidsgiver).medArbeidsforholdId(ARB_ID).build();
        InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder aggregat = InntektsmeldingAggregatDto.InntektsmeldingAggregatDtoBuilder.ny();
        aggregat.leggTil(im);
        InntektsmeldingAggregatDto imAgg = aggregat.build();
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsforholdRef(ARB_ID).medArbeidsgiver(arbeidsgiver))
            .build(periode);

        // Act
        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, imAgg.getAlleInntektsmeldinger());

        // Assert
        assertThat(andelerUtenInntektsmelding).isEmpty();
    }

    @Test
    public void skal_returnere_tom_liste_med_andeler_arbeidstaker_uten_inntektsmelding_status_DP_på_skjæringstidspunktet() {
        // Arrange
        InntektArbeidYtelseGrunnlagDto iayGrunnlagMock = mock(InntektArbeidYtelseGrunnlagDto.class);
        when(iayGrunnlagMock.getInntektsmeldinger()).thenReturn(Optional.empty());
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.DAGPENGER)
            .medInntektskategori(Inntektskategori.DAGPENGER)
            .build(periode);

        // Act
        Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerUtenInntektsmelding = ArbeidstakerUtenInntektsmeldingTjeneste
            .finnArbeidstakerAndelerUtenInntektsmelding(beregningsgrunnlag, Collections.emptyList());

        // Assert
        assertThat(andelerUtenInntektsmelding).isEmpty();
    }
}
