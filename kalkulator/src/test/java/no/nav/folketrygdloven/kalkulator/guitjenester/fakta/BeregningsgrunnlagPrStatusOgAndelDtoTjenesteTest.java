package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.Dekningsgrad;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

public class BeregningsgrunnlagPrStatusOgAndelDtoTjenesteTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final Inntektskategori INNTEKTSKATEGORI = Inntektskategori.ARBEIDSTAKER;
    private static final Beløp AVKORTET_PR_AAR = Beløp.fra(150000);
    private static final Beløp BRUTTO_PR_AAR = Beløp.fra(300000);
    private static final Beløp REDUSERT_PR_AAR = Beløp.fra(500000);
    private static final Beløp OVERSTYRT_PR_AAR = Beløp.fra(500);
    private static final LocalDate ANDEL_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate ANDEL_TOM = LocalDate.now();
    private static final String ORGNR = "973093681";
    private static final Long ANDELSNR = 1L;
    private static final Beløp RAPPORTERT_PR_AAR = Beløp.fra(300000);
    private static final BigDecimal AVVIK_OVER_25_PROSENT = BigDecimal.valueOf(500L);
    private static final BigDecimal AVVIK_UNDER_25_PROSENT = BigDecimal.valueOf(30L);
    private static final LocalDate SAMMENLIGNING_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate SAMMENLIGNING_TOM = LocalDate.now();
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock();
    private BeregningsgrunnlagGrunnlagDto grunnlag;

    @Test
    public void skalFastsetteGrunnlagForSnNårAvvikOver25ProsentOgGammeltSammenligningsgrunnlag() {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        byggAndelSn(bgPeriode, arbeidsgiver, 3L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, null);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(2).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalFastsetteGrunnlagForBådeFlOgAtSnNårAvvikOver25ProsentOgGammeltSammenligningsgrunnlagOgKunFlOgAtAndel() {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, null);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(true);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalFastsetteGrunnlagForSnNårAvvikErUnder25ProsentOgNyIArbeidslivet() {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagUtenSammenligningsgrunnlag();
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        byggAndelSn(bgPeriode, arbeidsgiver, 3L);
        var fakta = FaktaAggregatDto.builder().medFaktaAktør(FaktaAktørDto.builder()
                .medErNyIArbeidslivetSNFastsattAvSaksbehandler(true)
                .build()).build();
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, fakta);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(2).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalFastsetteGrunnlagForSnNårAvvikErOver25Prosent() {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_SN);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelSn(bgPeriode, arbeidsgiver, 1L);
        FaktaAggregatDto fakta = FaktaAggregatDto.builder().medFaktaAktør(FaktaAktørDto.builder()
                .medErNyIArbeidslivetSNFastsattAvSaksbehandler(false)
                .build()).build();
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, fakta);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalIkkeKasteExceptionNårDetFinnesHverkenAtFlEllerSnAndelOgDetFinnesSammenligningsgrunnlagPrStatus() throws Exception {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_ATFL_SN);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelerAapDpVentelønn(bgPeriode, arbeidsgiver);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, null);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
    }

    @Test
    public void skalFastsetteGrunnlagForAtNårAvvikStørreEnn25ProsentForAtAndelOgSammenligningsgrunnlagMedTypeSammenliningAt() {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType.SAMMENLIGNING_AT);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, null);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(true);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(false);
    }

    @Test
    public void skalFastsetteGrunnlagForAtOgFlBasertPåSammenligningsgrunnlagNårAvvikStørreEnn25ProsentForAtAndelOgIngenSammenligningsgrunnlagPrStatus() {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, null);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(true);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalFastsetteGrunnlagForKunSnBasertPåSammenligningsgrunnlagNårAvvikStørreEnn25ProsentForAlleAndelerOgIngenSammenligningsgrunnlagPrStatus() {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag(SammenligningsgrunnlagType.SAMMENLIGNING_SN);
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelAt(bgPeriode, arbeidsgiver, 1L);
        byggAndelFl(bgPeriode, arbeidsgiver, 2L);
        byggAndelSn(bgPeriode, arbeidsgiver, 3L);
        FaktaAggregatDto fakta = FaktaAggregatDto.builder().medFaktaAktør(FaktaAktørDto.builder()
                .medErNyIArbeidslivetSNFastsattAvSaksbehandler(false)
                .build()).build();
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, fakta);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        var beregningsgrunnlagPrStatusOgAndelDto = tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
        // Assert
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(0).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(1).getSkalFastsetteGrunnlag()).isEqualTo(false);
        assertThat(beregningsgrunnlagPrStatusOgAndelDto.get(2).getSkalFastsetteGrunnlag()).isEqualTo(true);
    }

    @Test
    public void skalIkkeKasteExceptionNårDetFinnesHverkenAtFlEllerSnAndelOgDeIkkeFinnesSammenligningsgrunnlagPrStatus() throws Exception {
        //Arange
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        BeregningsgrunnlagDto Beregningsgrunnlag = lagBeregningsgrunnlagMedAvvikUnder25ProsentMedKunSammenligningsgrunnlag();
        BeregningsgrunnlagPeriodeDto bgPeriode = buildBeregningsgrunnlagPeriode(Beregningsgrunnlag);
        byggAndelerAapDpVentelønn(bgPeriode, arbeidsgiver);
        lagBehandling(Beregningsgrunnlag, arbeidsgiver, null);
        var ytelsespesifiktGrunnlag = new ForeldrepengerGrunnlag(Dekningsgrad.DEKNINGSGRAD_100, false);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(Collections.emptyList()).build();
        var input = new BeregningsgrunnlagGUIInput(lagReferanseMedStp(koblingReferanse), iayGrunnlag, List.of(),  ytelsespesifiktGrunnlag).medBeregningsgrunnlagGrunnlag(grunnlag);
        BeregningsgrunnlagPrStatusOgAndelDtoTjeneste tjeneste = new BeregningsgrunnlagPrStatusOgAndelDtoTjeneste();
        //Act
        tjeneste.lagBeregningsgrunnlagPrStatusOgAndelDto(input,
                grunnlag.getBeregningsgrunnlagHvisFinnes().get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList());
    }


    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedAvvikOver25Prosent(SammenligningsgrunnlagType sammenligningsgrunnlagType) {
        return BeregningsgrunnlagDto.builder()
                .medGrunnbeløp(Beløp.fra(99_858))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilSammenligningsgrunnlag(SammenligningsgrunnlagPrStatusDto.builder()
                        .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                        .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                        .medSammenligningsgrunnlagType(sammenligningsgrunnlagType)
                        .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
                        .build())
                .build();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedAvvikOver25ProsentMedKunSammenligningsgrunnlag(SammenligningsgrunnlagType sammenligningsgrunnlagType) {
        SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagDto = SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(sammenligningsgrunnlagType)
                .medAvvikPromilleNy(AVVIK_OVER_25_PROSENT)
                .build();

        return BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagDto)
                .medGrunnbeløp(Beløp.fra(99_858))
                .build();
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagUtenSammenligningsgrunnlag() {
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .build();

        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedAvvikUnder25ProsentMedKunSammenligningsgrunnlag() {
        SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlagDto = SammenligningsgrunnlagPrStatusDto.builder()
                .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
                .medRapportertPrÅr(RAPPORTERT_PR_AAR)
                .medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)
                .medAvvikPromilleNy(AVVIK_UNDER_25_PROSENT)
                .build();

        return BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilSammenligningsgrunnlag(sammenligningsgrunnlagDto)
                .medGrunnbeløp(Beløp.fra(99_858))
                .build();
    }

    private void byggAndelAt(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Arbeidsgiver arbeidsgiver, Long andelsNr) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(andelsNr)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .medAvkortetPrÅr(AVKORTET_PR_AAR)
                .medRedusertPrÅr(REDUSERT_PR_AAR)
                .build(beregningsgrunnlagPeriode);
    }

    private void byggAndelFl(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Arbeidsgiver arbeidsgiver, Long andelsNr) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(andelsNr)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .build(beregningsgrunnlagPeriode);
    }

    private void byggAndelSn(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Arbeidsgiver arbeidsgiver, Long andelsNr) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(andelsNr)
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .medAvkortetPrÅr(AVKORTET_PR_AAR)
                .medRedusertPrÅr(REDUSERT_PR_AAR)
                .medOverstyrtPrÅr(OVERSTYRT_PR_AAR)
                .build(beregningsgrunnlagPeriode);
    }

    private void byggAndelerAapDpVentelønn(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, Arbeidsgiver arbeidsgiver) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
                .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(ANDELSNR)
                .medAktivitetStatus(AktivitetStatus.DAGPENGER)
                .build(beregningsgrunnlagPeriode);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(ANDELSNR + 1)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSAVKLARINGSPENGER)
                .build(beregningsgrunnlagPeriode);

        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medBGAndelArbeidsforhold(bga)
                .medInntektskategori(INNTEKTSKATEGORI)
                .medAndelsnr(ANDELSNR + 2)
                .medAktivitetStatus(AktivitetStatus.VENTELØNN_VARTPENGER)
                .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagDto lagBehandling(BeregningsgrunnlagDto beregningsgrunnlag, Arbeidsgiver arbeidsgiver, FaktaAggregatDto fakta) {
        BeregningAktivitetAggregatDto beregningAktiviteter = lagBeregningAktiviteter(arbeidsgiver);
        BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medFaktaAggregat(fakta)
                .medRegisterAktiviteter(beregningAktiviteter)
                .medBeregningsgrunnlag(beregningsgrunnlag).build(BeregningsgrunnlagTilstand.OPPRETTET);

        this.grunnlag = beregningsgrunnlagGrunnlag;

        return beregningsgrunnlag;
    }

    private BeregningAktivitetAggregatDto lagBeregningAktiviteter(Arbeidsgiver arbeidsgiver) {
        return lagBeregningAktiviteter(BeregningAktivitetAggregatDto.builder(), arbeidsgiver);
    }

    private BeregningAktivitetAggregatDto lagBeregningAktiviteter(BeregningAktivitetAggregatDto.Builder builder, Arbeidsgiver arbeidsgiver) {
        return builder.medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT)
                .leggTilAktivitet(BeregningAktivitetDto.builder()
                        .medArbeidsgiver(arbeidsgiver)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                        .medPeriode(Intervall.fraOgMedTilOgMed(ANDEL_FOM, ANDEL_TOM))
                        .build())
                .build();
    }

    private KoblingReferanse lagReferanseMedStp(KoblingReferanse koblingReferanse) {
        Skjæringstidspunkt skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktBeregning(SKJÆRINGSTIDSPUNKT)
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT).build();
        return koblingReferanse.medSkjæringstidspunkt(skjæringstidspunkt);
    }

    private BeregningsgrunnlagPeriodeDto buildBeregningsgrunnlagPeriode(BeregningsgrunnlagDto beregningsgrunnlag) {
        return BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(ANDEL_FOM, null)
                .build(beregningsgrunnlag);
    }
}
