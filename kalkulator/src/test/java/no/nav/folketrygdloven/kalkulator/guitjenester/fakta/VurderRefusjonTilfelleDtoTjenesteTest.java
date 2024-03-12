package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static no.nav.folketrygdloven.kalkulator.OpprettKravPerioderFraInntektsmeldinger.opprett;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.testutilities.BeregningInntektsmeldingTestUtil;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

class VurderRefusjonTilfelleDtoTjenesteTest {
    private static final String ORGNR = "974760673";
    private static final String ORGNR2 = "915933149";

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);

    private final VurderRefusjonTilfelleDtoTjeneste vurderRefusjonTilfelleDtoTjeneste = new VurderRefusjonTilfelleDtoTjeneste();

    @Test
    void skal_lage_dto_for_arbeidsgiver_som_har_søkt_refusjon_for_sent() {
        // Arrange
        Map<Arbeidsgiver, LocalDate> førsteInnsendingMap = new HashMap<>();
        InntektArbeidYtelseAggregatBuilder registerBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        BeregningAktivitetAggregatDto aktivitetAggregat = leggTilAktivitet(registerBuilder, List.of(ORGNR, ORGNR2));
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(ORGNR);
        Arbeidsgiver arbeidsgiver2 = Arbeidsgiver.virksomhet(ORGNR2);
        InntektsmeldingDto im1 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR, SKJÆRINGSTIDSPUNKT, Beløp.fra(10), Beløp.fra(10));
        førsteInnsendingMap.put(arbeidsgiver, SKJÆRINGSTIDSPUNKT.plusMonths(4));
        InntektsmeldingDto im2 = BeregningInntektsmeldingTestUtil.opprettInntektsmelding(ORGNR2, SKJÆRINGSTIDSPUNKT, Beløp.fra(10), Beløp.fra(10));
        førsteInnsendingMap.put(arbeidsgiver2, SKJÆRINGSTIDSPUNKT.plusMonths(2));
        BeregningsgrunnlagGrunnlagDtoBuilder grunnlag = byggGrunnlag(aktivitetAggregat, List.of(arbeidsgiver, arbeidsgiver2));
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medData(registerBuilder)
                .medInntektsmeldinger(List.of(im1, im2)).build();
        BeregningsgrunnlagGUIInput input = lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse, grunnlag, BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER, iayGrunnlag, førsteInnsendingMap);

        // Act
        FaktaOmBeregningDto faktaOmBeregningDto = new FaktaOmBeregningDto();
        vurderRefusjonTilfelleDtoTjeneste.lagDto(input, faktaOmBeregningDto);

        // Assert
        assertThat(faktaOmBeregningDto.getRefusjonskravSomKommerForSentListe()).hasSize(1);
        assertThat(faktaOmBeregningDto.getRefusjonskravSomKommerForSentListe().iterator().next().getArbeidsgiverIdent()).isEqualTo(arbeidsgiver.getIdentifikator());
    }

    private BeregningsgrunnlagGrunnlagDtoBuilder byggGrunnlag(BeregningAktivitetAggregatDto aktivitetAggregat, List<Arbeidsgiver> arbeidsgivere) {
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRegisterAktiviteter(aktivitetAggregat)
                .medBeregningsgrunnlag(lagBeregningsgrunnlag(arbeidsgivere.stream().map(a -> {
                    Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(a.getOrgnr());
                    return virksomhet;
                }).collect(Collectors.toList())));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag(List<Arbeidsgiver> ags) {

        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
                .leggTilFaktaOmBeregningTilfeller(List.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT))
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
                .build(bg);
        ags.forEach(ag -> BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ag))
                .build(periode)
        );
        return bg;
    }

    private BeregningAktivitetAggregatDto leggTilAktivitet(InntektArbeidYtelseAggregatBuilder iayAggregatBuilder, List<String> orgnr) {
        Intervall arbeidsperiode1 = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), TIDENES_ENDE);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        BeregningAktivitetAggregatDto.Builder aktivitetAggregatBuilder = BeregningAktivitetAggregatDto.builder()
                .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        for (String nr : orgnr) {
            leggTilYrkesaktivitet(arbeidsperiode1, aktørArbeidBuilder, nr);
            Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(nr);
            aktivitetAggregatBuilder.leggTilAktivitet(lagAktivitet(arbeidsperiode1, virksomhet));
        }
        iayAggregatBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        return aktivitetAggregatBuilder.build();
    }

    private BeregningAktivitetDto lagAktivitet(Intervall arbeidsperiode1, Arbeidsgiver ag) {
        return BeregningAktivitetDto.builder().medArbeidsgiver(ag).medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).medPeriode(Intervall.fraOgMedTilOgMed(arbeidsperiode1.getFomDato(), arbeidsperiode1.getTomDato())).build();
    }

    private void leggTilYrkesaktivitet(Intervall arbeidsperiode, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder, String orgnr) {
        Arbeidsgiver arbeidsgiver = Arbeidsgiver.virksomhet(orgnr);
        AktivitetsAvtaleDtoBuilder aaBuilder1 = AktivitetsAvtaleDtoBuilder.ny()
                .medPeriode(arbeidsperiode);
        YrkesaktivitetDtoBuilder yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                .medArbeidsgiver(arbeidsgiver)
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .leggTilAktivitetsAvtale(aaBuilder1);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yaBuilder);
    }


    public static BeregningsgrunnlagGUIInput lagInputMedBeregningsgrunnlagOgIAY(KoblingReferanse koblingReferanse,
                                                                                BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagBuilder,
                                                                                BeregningsgrunnlagTilstand tilstand,
                                                                                InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                Map<Arbeidsgiver, LocalDate> førsteInnsendingAvRefusjonMap) {
        BeregningsgrunnlagGUIInput input = new BeregningsgrunnlagGUIInput(koblingReferanse, iayGrunnlag,
                opprett(iayGrunnlag, koblingReferanse.getSkjæringstidspunktBeregning(), førsteInnsendingAvRefusjonMap), null);
        BeregningsgrunnlagGrunnlagDto grunnlag = beregningsgrunnlagGrunnlagBuilder.build(tilstand);
        return input.medBeregningsgrunnlagGrunnlag(grunnlag);
    }

}
