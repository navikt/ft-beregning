package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class FastsettMånedsinntektUtenInntektsmeldingTilfelleUtlederTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2018, 1, 1);
    public static final String ORGNR = "21348714121";
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);

    @Test
    public void skal_gi_tilfelle_om_beregningsgrunnlag_har_andel_med_kunstig_arbeid() {
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(true);
        FaktaOmBeregningInput faktaOmBeregningInput = lagFaktaOmBeregningInput(grunnlag, List.of());
        Optional<FaktaOmBeregningTilfelle> tilfelle = new FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder().utled(faktaOmBeregningInput, grunnlag);
        assertThat(tilfelle.get()).isEqualTo(FaktaOmBeregningTilfelle.FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING);
    }

    @Test
    public void skal_ikkje_gi_tilfelle_om_beregningsgrunnlag_ikkje_har_andel_med_kunstig_arbeid() {
        BeregningsgrunnlagGrunnlagDto grunnlag = lagGrunnlag(false);
        FaktaOmBeregningInput faktaOmBeregningInput = lagFaktaOmBeregningInput(grunnlag, List.of());

        Optional<FaktaOmBeregningTilfelle> tilfelle = new FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder().utled(faktaOmBeregningInput, grunnlag);
        assertThat(tilfelle).isNotPresent();
    }

    @Test
    public void skal_gi_tilfelle_om_beregningsgrunnlag_har_andeler_for_samme_virksomhet_med_og_uten_inntektsmelding() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef()))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder()
                        .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR)).medArbeidsforholdRef(ref))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                .medArbeidsforholdId(ref)
                .medBeløp(Beløp.fra(10)).build();
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);

        FaktaOmBeregningInput faktaOmBeregningInput = lagFaktaOmBeregningInput(grunnlag, List.of(im));


        Optional<FaktaOmBeregningTilfelle> tilfelle = new FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder().utled(faktaOmBeregningInput, grunnlag);
        assertThat(tilfelle).isPresent();
    }

    @Test
    public void skal_ikkje_gi_tilfelle_om_beregningsgrunnlag_kun_har_andeler_for_samme_virksomhet_med_inntektsmelding() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(bg);
        InternArbeidsforholdRefDto ref2 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                        .medArbeidsforholdRef(ref2))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        InternArbeidsforholdRefDto ref1 = InternArbeidsforholdRefDto.nyRef();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                        .medArbeidsforholdRef(ref1))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        BeregningsgrunnlagGrunnlagDto grunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);


        InntektsmeldingDto im = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                .medArbeidsforholdId(ref1)
                .medBeløp(Beløp.fra(10)).build();
        InntektsmeldingDto im2 = InntektsmeldingDtoBuilder.builder()
                .medArbeidsgiver(Arbeidsgiver.virksomhet(ORGNR))
                .medArbeidsforholdId(ref2)
                .medBeløp(Beløp.fra(10)).build();

        FaktaOmBeregningInput faktaOmBeregningInput = lagFaktaOmBeregningInput(grunnlag, List.of(im, im2));

        Optional<FaktaOmBeregningTilfelle> tilfelle = new FastsettMånedsinntektUtenInntektsmeldingTilfelleUtleder().utled(faktaOmBeregningInput, grunnlag);
        assertThat(tilfelle).isNotPresent();
    }

    private FaktaOmBeregningInput lagFaktaOmBeregningInput(BeregningsgrunnlagGrunnlagDto grunnlag, List<InntektsmeldingDto> inntektsmeldinger) {
        BeregningsgrunnlagInput input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagOgIAY(koblingReferanse,
                BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(grunnlag),
                BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER,
                InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                        .medInntektsmeldinger(inntektsmeldinger)
                        .build());
        return new FaktaOmBeregningInput(input.getKoblingReferanse(), input.getIayGrunnlag(), null, input.getKravPrArbeidsgiver(), input.getYtelsespesifiktGrunnlag());
    }

    private BeregningsgrunnlagGrunnlagDto lagGrunnlag(boolean medKunstigArbeid) {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriodeDto periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(bg);
        String orgnr = medKunstigArbeid ? OrgNummer.KUNSTIG_ORG : ORGNR;
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr)))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);
        return BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(bg).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER);
    }
}
