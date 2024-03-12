package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.behandling.Skjæringstidspunkt;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class FastsettSkjæringstidspunktOgStatuserK14Test {
    private static final List<Grunnbeløp> G_VERDIER = List.of(new Grunnbeløp(TIDENES_BEGYNNELSE, TIDENES_ENDE, 100000L, 100000L));
    private static final LocalDate STP_OPPTJENING = LocalDate.of(2022,3,1);
    private static final LocalDate FØRSTE_UTTAKSDAG = LocalDate.of(2022,3,2);

    private final FastsettSkjæringstidspunktOgStatuserK14 fastsettSkjæringstidspunktOgStatuser = new FastsettSkjæringstidspunktOgStatuserK14();

    @Test
    public void en_aktivitet_avsluttes_etter_stp() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr, førSTP(200), etterStp(5)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(30)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr, førSTP(200), etterStp(5))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, STP_OPPTJENING);
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void en_aktivitet_avsluttes_før_stp() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr, førSTP(365), førSTP(5)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(5)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr, førSTP(365), førSTP(5))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, førSTP(4));
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void tester_arbeid_avsluttes_før_stp_og_militær_etter_stp_forventer_at_militær_ignoreres() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr, førSTP(365), førSTP(15)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(15)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var msOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(5)));
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening, msOpptjening));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr, førSTP(365), førSTP(15)),
                lagBGAktivitet(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, orgnr, førSTP(365), førSTP(5))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, førSTP(14));
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void skal_kun_velge_siste_aktiviteet() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr, førSTP(365), førSTP(15)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(15)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var msOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(10)));
        var aapOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.AAP,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(5)));
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening, msOpptjening, aapOpptjening));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr, førSTP(365), førSTP(15)),
                lagBGAktivitet(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, orgnr, førSTP(365), førSTP(10)),
                lagBGAktivitet(OpptjeningAktivitetType.AAP, orgnr, førSTP(365), etterStp(5))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, STP_OPPTJENING);
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
    }

    @Test
    public void søker_er_kun_militær() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr, førSTP(365), førSTP(15)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var msOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(10)));
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(msOpptjening));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, null, førSTP(365), førSTP(10))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, førSTP(9));
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.MILITÆR_ELLER_SIVIL);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.MILITÆR_ELLER_SIVIL);
    }

    @Test
    public void søker_er_arbeidstaker_og_frilanser() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr, førSTP(365), førSTP(15)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(15)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var frilansOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(15)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening, frilansOpptjening));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr, førSTP(365), førSTP(15)),
                lagBGAktivitet(OpptjeningAktivitetType.FRILANS, null, førSTP(365), førSTP(15))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, førSTP(14));
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.KOMBINERT_AT_FL);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER);
    }

    @Test
    public void søker_har_flere_frilansaktiviteter() {
        // Arrange
        var iay = ferdigstillIAY(Collections.emptyList(), Collections.emptyList());
        var frilansOpptjening1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(15)));
        var frilansOpptjening2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(15)));
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(frilansOpptjening1, frilansOpptjening2));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.FRILANS, null, førSTP(365), førSTP(15)),
                lagBGAktivitet(OpptjeningAktivitetType.FRILANS, null, førSTP(365), førSTP(15))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, førSTP(14));
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.FRILANSER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.FRILANSER);
    }

    @Test
    public void søker_har_flere_arbeidsforhold_ulike_virksomheter() {
        // Arrange
        var orgnr1 = "999999999";
        var orgnr2 = "999999998";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr1, førSTP(365), etterStp(15)), lagYA(orgnr2, førSTP(365), etterStp(15)));
        var imer = Arrays.asList(lagIM(orgnr1), lagIM(orgnr2));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)), orgnr1, null, InternArbeidsforholdRefDto.nyRef());
        var arbeidOpptjening2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)), orgnr2, null, InternArbeidsforholdRefDto.nyRef());
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening1, arbeidOpptjening2));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr1, førSTP(365), etterStp(15)),
                lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr2, førSTP(365), etterStp(15))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, STP_OPPTJENING);
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void søker_har_flere_arbeidsforhold_samme_virksomheter() {
        // Arrange
        var orgnr1 = "999999999";
        var arbId1 = InternArbeidsforholdRefDto.nyRef();
        var arbId2 = InternArbeidsforholdRefDto.nyRef();
        var arbeidsforhold = Arrays.asList(lagYA(orgnr1, førSTP(365), etterStp(15), arbId1), lagYA(orgnr1, førSTP(365), etterStp(15), arbId2));
        var imer = Arrays.asList(lagIM(orgnr1, arbId1), lagIM(orgnr1, arbId2));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening1 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)), orgnr1, null, arbId1);
        var arbeidOpptjening2 = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)), orgnr1, null, arbId2);
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening1, arbeidOpptjening2));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr1, arbId1, førSTP(365), etterStp(15)),
                lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr1, arbId2, førSTP(365), etterStp(15))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, STP_OPPTJENING);
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER);
    }


    @Test
    public void søker_er_arbeidstaker_og_næringsdrivede() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr, førSTP(365), førSTP(15)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(15)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var næringOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.NÆRING,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(15)));
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening, næringOpptjening));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr, førSTP(365), førSTP(15)),
                lagBGAktivitet(OpptjeningAktivitetType.NÆRING, null, førSTP(365), førSTP(15))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, førSTP(14));
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.KOMBINERT_AT_SN);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    public void kombinasjonstest() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYA(orgnr, førSTP(365), etterStp(15)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);

        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var næringOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.NÆRING,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)));
        var frilansOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.FRILANS,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)));
        var dpOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.DAGPENGER,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)));
        var msOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
                Intervall.fraOgMedTilOgMed(førSTP(365), etterStp(15)));
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening, næringOpptjening, frilansOpptjening, dpOpptjening, msOpptjening));

        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr, førSTP(365), etterStp(15)),
                lagBGAktivitet(OpptjeningAktivitetType.NÆRING, null, førSTP(365), etterStp(15)),
                lagBGAktivitet(OpptjeningAktivitetType.FRILANS, null, førSTP(365), etterStp(15)),
                lagBGAktivitet(OpptjeningAktivitetType.DAGPENGER, null, førSTP(365), etterStp(15)),
                lagBGAktivitet(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, null, førSTP(365), etterStp(15))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, STP_OPPTJENING);
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.KOMBINERT_AT_FL_SN, AktivitetStatus.DAGPENGER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, AktivitetStatus.FRILANSER, AktivitetStatus.DAGPENGER);
    }

    @Test
    public void arbeidstaker_med_permisjon() {
        // Arrange
        var orgnr = "999999999";
        var arbeidsforhold = Arrays.asList(lagYAMedPermisjon(orgnr, førSTP(365), etterStp(150), null, førSTP(30), etterStp(30)));
        var imer = Arrays.asList(lagIM(orgnr));
        var iay = ferdigstillIAY(arbeidsforhold, imer);
        var arbeidOpptjening = OpptjeningAktiviteterDto.nyPeriode(OpptjeningAktivitetType.ARBEID,
                Intervall.fraOgMedTilOgMed(førSTP(365), førSTP(30)), orgnr, null, InternArbeidsforholdRefDto.nyRef());
        var opptjening = new OpptjeningAktiviteterDto(Arrays.asList(arbeidOpptjening));
        var bgInput = new BeregningsgrunnlagInput(lagRef(), iay, opptjening, Collections.emptyList(), null);
        var bgAktivitetAgg = lagBGAktivitetAggregat(Arrays.asList(lagBGAktivitet(OpptjeningAktivitetType.ARBEID, orgnr, førSTP(365), førSTP(30))));

        // Act
        var resultat = fastsettSkjæringstidspunktOgStatuser.fastsett(bgInput, bgAktivitetAgg, G_VERDIER);

        // Assert
        assertStp(resultat, førSTP(29));
        verifiserGrunnbeløp(resultat);
        verifiserAktivitetStatuser(resultat, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(resultat, AktivitetStatus.ARBEIDSTAKER);
    }


    private void verifiserBeregningsgrunnlagPerioder(BeregningsgrunnlagRegelResultat resultat, AktivitetStatus... expectedArray) {
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        var bgPeriode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        var actualList = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getAktivitetStatus).collect(Collectors.toList());
        assertThat(actualList).containsOnly(expectedArray);
        bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(this::erArbeidstakerEllerFrilans)
                .forEach(this::verifiserBeregningsperiode);
        assertThat(actualList).hasSameSizeAs(expectedArray);
    }

    private boolean erArbeidstakerEllerFrilans(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return andel.getAktivitetStatus().erArbeidstaker() || andel.getAktivitetStatus().erFrilanser();
    }

    private void verifiserBeregningsperiode(BeregningsgrunnlagPrStatusOgAndelDto andel) {
        assertThat(andel.getBeregningsperiodeFom()).isNotNull();
        assertThat(andel.getBeregningsperiodeTom()).isNotNull();
    }

    private void verifiserAktivitetStatuser(BeregningsgrunnlagRegelResultat resultat, AktivitetStatus... forventedeStatuser) {
        var faktiskeStatuser = resultat.getBeregningsgrunnlag().getAktivitetStatuser().stream()
                .map(BeregningsgrunnlagAktivitetStatusDto::getAktivitetStatus)
                .collect(Collectors.toList());
        assertThat(faktiskeStatuser).containsExactlyInAnyOrder(forventedeStatuser);
    }

    private void verifiserGrunnbeløp(BeregningsgrunnlagRegelResultat resultat) {
        assertThat(resultat.getBeregningsgrunnlag().getGrunnbeløp().verdi().longValue()).isEqualTo(G_VERDIER.getFirst().getGVerdi());
    }

    private void assertStp(BeregningsgrunnlagRegelResultat resultat, LocalDate stp) {
        assertThat(resultat.getBeregningsgrunnlag().getSkjæringstidspunkt()).isEqualTo(stp);
    }

    private BeregningAktivitetAggregatDto lagBGAktivitetAggregat(List<BeregningAktivitetDto> aktiviteter) {
        var builder = BeregningAktivitetAggregatDto.builder().medSkjæringstidspunktOpptjening(STP_OPPTJENING);
        aktiviteter.forEach(builder::leggTilAktivitet);
        return builder.build();
    }


    private BeregningAktivitetDto lagBGAktivitet(OpptjeningAktivitetType opptjeningstype, String orgnr, LocalDate fom, LocalDate tom) {
        return lagBGAktivitet(opptjeningstype, orgnr, null, fom, tom);
    }

    private BeregningAktivitetDto lagBGAktivitet(OpptjeningAktivitetType opptjeningstype, String orgnr, InternArbeidsforholdRefDto ref, LocalDate fom, LocalDate tom) {
        var builder = BeregningAktivitetDto.builder()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medOpptjeningAktivitetType(opptjeningstype);
        if (ref != null) {
            builder.medArbeidsforholdRef(ref);
        }
        if (orgnr != null) {
            return builder.medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr)).build();
        } else return builder.build();
    }

    private InntektsmeldingDto lagIM(String orgnr) {
        return lagIM(orgnr, null);
    }

    private InntektsmeldingDto lagIM(String orgnr, InternArbeidsforholdRefDto ref) {
        return InntektsmeldingDtoBuilder.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr)).medStartDatoPermisjon(STP_OPPTJENING).medArbeidsforholdId(ref).build();
    }

    private KoblingReferanse lagRef() {
        var skjæringstidspunkt = Skjæringstidspunkt.builder().medSkjæringstidspunktOpptjening(STP_OPPTJENING).medFørsteUttaksdato(FØRSTE_UTTAKSDAG).build();
        return KoblingReferanse.fra(FagsakYtelseType.FORELDREPENGER, new AktørId("999999999999"), 0L, UUID.randomUUID(), Optional.empty(), skjæringstidspunkt);
    }


    private LocalDate etterStp(int dager) {
        return STP_OPPTJENING.plusDays(dager);
    }

    private LocalDate førSTP(int dager) {
        return STP_OPPTJENING.minusDays(dager);
    }

    private YrkesaktivitetDto lagYA(String orgnr, LocalDate fom, LocalDate tom) {
        return lagYA(orgnr, fom, tom, InternArbeidsforholdRefDto.nullRef());
    }

    private YrkesaktivitetDto lagYA(String orgnr, LocalDate fom, LocalDate tom, InternArbeidsforholdRefDto ref) {
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                .medArbeidsforholdId(ref)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        var aaBuilder = yaBuilder.getAktivitetsAvtaleBuilder().medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medErAnsettelsesPeriode(true);
        yaBuilder.leggTilAktivitetsAvtale(aaBuilder);
        return yaBuilder.build();
    }

    private YrkesaktivitetDto lagYAMedPermisjon(String orgnr, LocalDate fom, LocalDate tom, InternArbeidsforholdRefDto ref, LocalDate fomPerm, LocalDate tomPerm) {
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr))
                .medArbeidsforholdId(ref)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);

        var perm = PermisjonDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(fomPerm, tomPerm)).medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET).medProsentsats(Stillingsprosent.HUNDRED);
        yaBuilder.leggTilPermisjon(perm);

        var aaBuilder = yaBuilder.getAktivitetsAvtaleBuilder().medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medErAnsettelsesPeriode(true);
        yaBuilder.leggTilAktivitetsAvtale(aaBuilder);
        return yaBuilder.build();
    }

    private InntektArbeidYtelseGrunnlagDto ferdigstillIAY(List<YrkesaktivitetDto> yrkesaktiviteter, List<InntektsmeldingDto> imer) {
        var arbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        yrkesaktiviteter.forEach(arbeidBuilder::leggTilYrkesaktivitet);
        arbeidBuilder.build();
        var iayBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        iayBuilder.leggTilAktørArbeid(arbeidBuilder);

        return InntektArbeidYtelseGrunnlagDtoBuilder.nytt().medInntektsmeldinger(imer).medData(iayBuilder).build();
    }

}
