package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;

class VurderLønnsendringDtoTjenesteTest {

    public static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.now();
    public static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("994507508");
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(LocalDate.now());

    private VurderLønnsendringDtoTjeneste tjeneste = new VurderLønnsendringDtoTjeneste();

    @Test
    void skal_legge_til_arbeid_med_lønnsendring_dersom_ikke_vurdert_automatisk() {

        var sisteLønnsendringsdato = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1);
        var dto = new FaktaOmBeregningDto();
        var iayGrunnlag = lagIAYGrunnlagMedLønnsendring(sisteLønnsendringsdato);
        var bgGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.nytt()
                .medBeregningsgrunnlag(lagBeregningsgrunnlagMedArbeid())
                .build(BeregningsgrunnlagTilstand.FASTSATT);
        BeregningsgrunnlagGUIInput input = lagInput(iayGrunnlag, bgGrunnlag);

        tjeneste.lagDto(input, dto);

        assertThat(dto.getArbeidsforholdMedLønnsendringUtenIM().size()).isEqualTo(1);
    }

    @Test
    void skal_ikke_legge_til_arbeid_med_lønnsendring_dersom_vurdert_automatisk() {

        var sisteLønnsendringsdato = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1);
        var dto = new FaktaOmBeregningDto();
        var iayGrunnlag = lagIAYGrunnlagMedLønnsendring(sisteLønnsendringsdato);
        var faktaBuilder = FaktaAggregatDto.builder();
        var faktaArbeidBuilder = faktaBuilder.getFaktaArbeidsforholdBuilderFor(ARBEIDSGIVER, InternArbeidsforholdRefDto.nullRef());
        faktaArbeidBuilder.medHarLønnsendringIBeregningsperioden(new FaktaVurdering(true, FaktaVurderingKilde.KALKULATOR));
        faktaBuilder.erstattEksisterendeEllerLeggTil(faktaArbeidBuilder.build());
        var bgGrunnlag = BeregningsgrunnlagGrunnlagDtoBuilder.nytt()
                .medFaktaAggregat(faktaBuilder.build())
                .medBeregningsgrunnlag(lagBeregningsgrunnlagMedArbeid())
                .build(BeregningsgrunnlagTilstand.FASTSATT);
        BeregningsgrunnlagGUIInput input = lagInput(iayGrunnlag, bgGrunnlag);

        tjeneste.lagDto(input, dto);

        assertThat(dto.getArbeidsforholdMedLønnsendringUtenIM()).isNull();
    }

    private BeregningsgrunnlagGUIInput lagInput(InntektArbeidYtelseGrunnlagDto iayGrunnlag, BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlagDto) {
        BeregningsgrunnlagGUIInput input = new BeregningsgrunnlagGUIInput(
                koblingReferanse,
                iayGrunnlag,
                List.of(),
                null
        );
        input = input.medBeregningsgrunnlagGrunnlag(beregningsgrunnlagGrunnlagDto);
        return input;
    }

    private InntektArbeidYtelseGrunnlagDto lagIAYGrunnlagMedLønnsendring(LocalDate sisteLønnsendringsdato) {
        var dataBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        var yrkesaktivitetBuilder = lagYrkesaktivitetMedLønnsendring(sisteLønnsendringsdato);
        aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
        dataBuilder.leggTilAktørArbeid(aktørArbeidBuilder);
        var iayGrunnlag = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
                .medData(dataBuilder).build();
        return iayGrunnlag;
    }

    private YrkesaktivitetDtoBuilder lagYrkesaktivitetMedLønnsendring(LocalDate sisteLønnsendringsdato) {
        var yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        yrkesaktivitetBuilder.leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny()
                .medErAnsettelsesPeriode(true)
                .medPeriode(Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10)))
                .medSisteLønnsendringsdato(sisteLønnsendringsdato))
                        .medArbeidsgiver(ARBEIDSGIVER)
                .medArbeidsforholdId(InternArbeidsforholdRefDto.nullRef())
                .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        return yrkesaktivitetBuilder;
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlagMedArbeid() {
        BeregningsgrunnlagDto bg = BeregningsgrunnlagDto.builder()
                .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
                .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
                .leggTilFaktaOmBeregningTilfeller(List.of(FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING))
                .build();
        BeregningsgrunnlagPeriodeDto bgPeriode = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(bg);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.withDayOfMonth(1).minusDays(1))
                .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(ARBEIDSGIVER)
                        .medArbeidsperiodeFom(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10)).medArbeidsperiodeTom(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10))
                        .medArbeidsforholdRef(InternArbeidsforholdRefDto.nullRef()))
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .build(bgPeriode);
        return bg;
    }

}
