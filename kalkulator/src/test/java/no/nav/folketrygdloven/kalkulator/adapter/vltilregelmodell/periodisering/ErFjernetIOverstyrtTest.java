package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;

class ErFjernetIOverstyrtTest {
    private static final LocalDate STP = LocalDate.of(2020, 1, 1);
    private static InntektArbeidYtelseGrunnlagDtoBuilder IAY_BUILDER;
    private static InntektArbeidYtelseAggregatBuilder BUILDER;
    private static InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder ARBEID_BUILDER;
    private static ArbeidsforholdInformasjonDtoBuilder ARBFOR_INFO_BUILDER;


    @BeforeEach
    public void setup() {
        // Nullstiller aggregat
        IAY_BUILDER = InntektArbeidYtelseGrunnlagDtoBuilder.nytt();
        BUILDER = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonTypeDto.REGISTER);
        ARBEID_BUILDER = BUILDER.getAktørArbeidBuilder();
        ARBFOR_INFO_BUILDER = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
    }

    @Test
    public void arbeidsforhold_som_var_i_permisjon_dagen_før_stp_men_ikkje_på_stp_og_er_ikke_fjernet() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitet(ag, ref);
        lagPermisjonForAG(ya, STP.minusMonths(3), STP.minusDays(1));
        InntektArbeidYtelseGrunnlagDto grunnlag = ferdigstillIAYGrunnlag();

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());

        var overstyring = BeregningAktivitetOverstyringerDto.builder()
                .leggTilOverstyring(BeregningAktivitetOverstyringDto.builder()
                        .medArbeidsgiver(Arbeidsgiver.virksomhet("123456789"))
                        .medHandling(BeregningAktivitetHandlingType.BENYTT)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).build())
                .build();

        // Act
        boolean erFjernet = ErFjernetIOverstyrt.erFjernetIOverstyrt(filter, ya.build(), Optional.of(overstyring), STP);

        assertThat(erFjernet).isFalse();
    }

    @Test
    public void arbeidsforhold_som_var_i_permisjon_og_ligger_i_overstyr_men_er_ikke_fjernet() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitet(ag, ref);
        lagPermisjonForAG(ya, STP.minusMonths(3), STP.plusMonths(2));
        InntektArbeidYtelseGrunnlagDto grunnlag = ferdigstillIAYGrunnlag();

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());

        var overstyring = BeregningAktivitetOverstyringerDto.builder()
                .leggTilOverstyring(BeregningAktivitetOverstyringDto.builder()
                        .medArbeidsforholdRef(ref)
                        .medArbeidsgiver(ag)
                        .medHandling(BeregningAktivitetHandlingType.BENYTT)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).build())
                .build();

        // Act
        boolean erFjernet = ErFjernetIOverstyrt.erFjernetIOverstyrt(filter, ya.build(),
                Optional.of(overstyring), STP);

        assertThat(erFjernet).isFalse();
    }

    @Test
    public void arbeidsforhold_som_var_i_permisjon_på_stp_ikke_fjernet() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitet(ag, ref);
        lagPermisjonForAG(ya, STP.minusMonths(3), STP.plusMonths(2));
        InntektArbeidYtelseGrunnlagDto grunnlag = ferdigstillIAYGrunnlag();

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());


        var overstyring = BeregningAktivitetOverstyringerDto.builder()
                .leggTilOverstyring(BeregningAktivitetOverstyringDto.builder()
                        .medArbeidsgiver(ag)
                        .medArbeidsforholdRef(ref)
                        .medHandling(BeregningAktivitetHandlingType.BENYTT)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).build())
                .build();
        // Act
        boolean erFjernet = ErFjernetIOverstyrt.erFjernetIOverstyrt(filter, ya.build(), Optional.of(overstyring), STP);

        assertThat(erFjernet).isFalse();
    }

    @Test
    public void arbeidsforhold_som_ikke_var_i_permisjon_på_stp_og_fjernet() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitet(ag, ref);
        InntektArbeidYtelseGrunnlagDto grunnlag = ferdigstillIAYGrunnlag();

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());

        var overstyring = BeregningAktivitetOverstyringerDto.builder()
                .leggTilOverstyring(BeregningAktivitetOverstyringDto.builder()
                        .medArbeidsgiver(ag)
                        .medArbeidsforholdRef(ref)
                        .medHandling(BeregningAktivitetHandlingType.IKKE_BENYTT)
                        .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID).build())
                .build();

        // Act
        boolean erFjernet = ErFjernetIOverstyrt.erFjernetIOverstyrt(filter, ya.build(), Optional.of(overstyring), STP);

        assertThat(erFjernet).isTrue();
    }

    @Test
    public void arbeidsforhold_uten_overstyring() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        InternArbeidsforholdRefDto ref = InternArbeidsforholdRefDto.nullRef();
        YrkesaktivitetDtoBuilder ya = lagYrkesaktivitet(ag, ref);
        InntektArbeidYtelseGrunnlagDto grunnlag = ferdigstillIAYGrunnlag();

        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(grunnlag.getArbeidsforholdInformasjon(), grunnlag.getAktørArbeidFraRegister());

        // Act
        boolean erFjernet = ErFjernetIOverstyrt.erFjernetIOverstyrt(filter, ya.build(), Optional.empty(), STP);

        assertThat(erFjernet).isFalse();
    }


    private InntektArbeidYtelseGrunnlagDto ferdigstillIAYGrunnlag() {
        IAY_BUILDER.medData(BUILDER);
        IAY_BUILDER.medInformasjon(ARBFOR_INFO_BUILDER.build());
        return IAY_BUILDER.build();
    }

    private void lagPermisjonForAG(YrkesaktivitetDtoBuilder ya, LocalDate fom, LocalDate tom) {
        ya.leggTilPermisjon(PermisjonDtoBuilder.ny().medPeriode(Intervall.fraOgMedTilOgMed(fom, tom)).medProsentsats(Stillingsprosent.HUNDRED).medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.VELFERDSPERMISJON));
    }

    private YrkesaktivitetDtoBuilder lagYrkesaktivitet(Arbeidsgiver ag, InternArbeidsforholdRefDto ref) {
        YrkesaktivitetDtoBuilder yaBuilder = ARBEID_BUILDER
                .getYrkesaktivitetBuilderForType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                .medArbeidsgiver(ag)
                .medArbeidsforholdId(ref)
                .leggTilAktivitetsAvtale(AktivitetsAvtaleDtoBuilder.ny().medPeriode(Intervall.fraOgMed(STP.minusYears(5))));
        ARBEID_BUILDER.leggTilYrkesaktivitet(yaBuilder);
        return yaBuilder;

    }


}
