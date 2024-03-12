package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningIAYTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.utils.BeregningsgrunnlagTestUtil;

public class KontrollerFaktaBeregningFrilanserTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, 9, 30);
    private InntektArbeidYtelseGrunnlagDtoBuilder iayGrunnlagBuilder;

    @BeforeEach
    public void setup() {
        iayGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
    }


    @Test
    public void skal_gi_nyoppstartet_om_oppgitt_i_søknad() {
        //Arrange
        iayGrunnlagBuilder.medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny().leggTilFrilansOpplysninger(new OppgittFrilansDto(true)));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.of(iayGrunnlag), AktivitetStatus.FRILANSER);
        BeregningsgrunnlagGrunnlagDto gr = BeregningsgrunnlagGrunnlagDtoBuilder.nytt().medBeregningsgrunnlag(beregningsgrunnlagDto)
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                        .build()).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        //Act
        var erNyoppstartet = KontrollerFaktaBeregningFrilanserTjeneste.erNyoppstartetFrilanser(gr, iayGrunnlag);

        //Assert
        assertThat(erNyoppstartet).isTrue();
    }

    @Test
    public void skal_gi_nyoppstartet_om_oppgitt_i_søknad_og_periode() {
        //Arrange
        iayGrunnlagBuilder.medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny()
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(true)));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.of(iayGrunnlag), AktivitetStatus.FRILANSER);
        BeregningsgrunnlagGrunnlagDto gr = BeregningsgrunnlagGrunnlagDtoBuilder.nytt().medBeregningsgrunnlag(beregningsgrunnlagDto)
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                        .leggTilAktivitet(lagFrilans(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1)))
                        .build()).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        //Act
        var erNyoppstartet = KontrollerFaktaBeregningFrilanserTjeneste.erNyoppstartetFrilanser(gr, iayGrunnlag);

        //Assert
        assertThat(erNyoppstartet).isTrue();
    }

    @Test
    public void skal_gi_nyoppstartet_om_kun_oppgitt_periode() {
        //Arrange
        iayGrunnlagBuilder.medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny()
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false)));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.of(iayGrunnlag), AktivitetStatus.FRILANSER);
        BeregningsgrunnlagGrunnlagDto gr = BeregningsgrunnlagGrunnlagDtoBuilder.nytt().medBeregningsgrunnlag(beregningsgrunnlagDto)
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                        .leggTilAktivitet(lagFrilans(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1)))
                        .build()).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        //Act
        var erNyoppstartet = KontrollerFaktaBeregningFrilanserTjeneste.erNyoppstartetFrilanser(gr, iayGrunnlag);

        //Assert
        assertThat(erNyoppstartet).isTrue();
    }

    @Test
    public void skal_ikke_gi_nyoppstartet_om_ikke_oppgitt_i_søknad_eller_periode() {
        //Arrange
        iayGrunnlagBuilder.medOppgittOpptjening(OppgittOpptjeningDtoBuilder.ny()
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false)));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.of(iayGrunnlag), AktivitetStatus.FRILANSER);
        BeregningsgrunnlagGrunnlagDto gr = BeregningsgrunnlagGrunnlagDtoBuilder.nytt().medBeregningsgrunnlag(beregningsgrunnlagDto)
                .medRegisterAktiviteter(BeregningAktivitetAggregatDto.builder()
                        .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                        .leggTilAktivitet(lagFrilans(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4)))
                        .build()).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
        //Act
        var erNyoppstartet = KontrollerFaktaBeregningFrilanserTjeneste.erNyoppstartetFrilanser(gr, iayGrunnlag);

        //Assert
        assertThat(erNyoppstartet).isFalse();
    }



    @Test
    public void ikkeFrilansISammeArbeidsforholdHvisBareArbeidstaker() {
        //Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
                SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.of(iayGrunnlag));

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = KontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(
                beregningsgrunnlagDto, iayGrunnlag);

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon).isEmpty();
    }

    @Test
    public void ikkeFrilansISammeArbeidsforholdHvisFrilansHosAnnenOppdragsgiver() {
        //Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
        String orgnrFrilans = "987654320";
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2),
                null, Arbeidsgiver.virksomhet(orgnrFrilans), ArbeidType.FRILANSER_OPPDRAGSTAKER,
            singletonList(Beløp.fra(10)), false, Optional.empty(), iayGrunnlagBuilder);
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.empty(), AktivitetStatus.KOMBINERT_AT_FL);

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = KontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(
                beregningsgrunnlagDto, iayGrunnlagBuilder.build());

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon).isEmpty();
    }

    @Test
    public void frilansISammeArbeidsforhold() {
        //Arrange
        var arbId = InternArbeidsforholdRefDto.nyRef();
        String orgnr = "123456780";
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, Arbeidsgiver.virksomhet(orgnr), iayGrunnlagBuilder);
        BeregningIAYTestUtil.byggArbeidForBehandling(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), null, Arbeidsgiver.virksomhet(orgnr),
            ArbeidType.FRILANSER_OPPDRAGSTAKER, singletonList(Beløp.fra(10)), false, Optional.empty(), iayGrunnlagBuilder);
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = iayGrunnlagBuilder.build();
        BeregningsgrunnlagDto beregningsgrunnlagDto = BeregningsgrunnlagTestUtil.lagGjeldendeBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, Optional.of(iayGrunnlag), AktivitetStatus.KOMBINERT_AT_FL);

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = KontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(
                beregningsgrunnlagDto,
            iayGrunnlag);

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon).hasSize(1);
    }

    private BeregningAktivitetDto lagFrilans(LocalDate startFrilans) {
        return BeregningAktivitetDto.builder().medOpptjeningAktivitetType(OpptjeningAktivitetType.FRILANS)
                .medPeriode(Intervall.fraOgMed(startFrilans))
                .build();
    }

}
