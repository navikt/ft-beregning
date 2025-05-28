package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.ArbeidsforholdInformasjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.VersjonTypeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;


class UtledPermisjonTilDtoTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    @Test
    void skal_returne_empty_hvis_aktivitet_ikke_har_permisjon(){

        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet("1");
        var ref = InternArbeidsforholdRefDto.nyRef();
        var bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        var fom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var tom = SKJÆRINGSTIDSPUNKT.plusDays(1);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        var aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        var ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        var aktørArbeidBuilder = lagAktørArbeidBuilder(List.of(ya));

        var informasjonBuilder = ArbeidsforholdInformasjonDtoBuilder.oppdatere(Optional.empty());
        var overstyringBuilder = informasjonBuilder.getOverstyringBuilderFor(arbeidsgiver, ref);
        informasjonBuilder.leggTil(overstyringBuilder);

        var iayGrunnlag = lagGrunnlag(aktørArbeidBuilder);

        // Act
        var permisjonDto = UtledPermisjonTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).isEmpty();

    }

    @Test
    void skal_returne_empty_hvis_permisjon_slutter_før_stp(){

        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet("1");
        var ref = InternArbeidsforholdRefDto.nyRef();
        var bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        var fom = SKJÆRINGSTIDSPUNKT.minusYears(1);
        var tom = SKJÆRINGSTIDSPUNKT.minusDays(1);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        var aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        var ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        ya.leggTilPermisjon(PermisjonDtoBuilder.ny().medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET).medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medProsentsats(Stillingsprosent.HUNDRED));
        var aktørArbeidBuilder = lagAktørArbeidBuilder(List.of(ya));

        var iayGrunnlag = lagGrunnlag(aktørArbeidBuilder);

        // Act
        var permisjonDto = UtledPermisjonTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).isEmpty();

    }

    @Test
    void skal_returne_empty_hvis_permisjon_starter_etter_stp(){

        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet("1");
        var ref = InternArbeidsforholdRefDto.nyRef();
        var bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        var fom = SKJÆRINGSTIDSPUNKT.plusDays(1);
        var tom = SKJÆRINGSTIDSPUNKT.plusYears(1);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        var aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        var ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        ya.leggTilPermisjon(PermisjonDtoBuilder.ny().medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET).medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medProsentsats(Stillingsprosent.HUNDRED));
        var aktørArbeidBuilder = lagAktørArbeidBuilder(List.of(ya));


        var iayGrunnlag = lagGrunnlag(aktørArbeidBuilder);

        // Act
        var permisjonDto = UtledPermisjonTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).isEmpty();

    }

    @Test
    void skal_returne_empty_hvis_permisjon_er_mindre_enn_100_prosent(){

        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet("1");
        var ref = InternArbeidsforholdRefDto.nyRef();
        var bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        var fom = SKJÆRINGSTIDSPUNKT.plusDays(1);
        var tom = SKJÆRINGSTIDSPUNKT.plusYears(1);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        var aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        var ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        ya.leggTilPermisjon(PermisjonDtoBuilder.ny().medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET).medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medProsentsats(Stillingsprosent.fra(99)));
        var aktørArbeidBuilder = lagAktørArbeidBuilder(List.of(ya));

        var iayGrunnlag = lagGrunnlag(aktørArbeidBuilder);

        // Act
        var permisjonDto = UtledPermisjonTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).isEmpty();

    }

    @Test
    void skal_returne_permisjonDto(){

        // Arrange
        var arbeidsgiver = Arbeidsgiver.virksomhet("1");
        var ref = InternArbeidsforholdRefDto.nyRef();
        var bgAndelArbeidsforhold = lagBGAndelArbeidsforhold(Optional.of(arbeidsgiver), ref);
        var fom = SKJÆRINGSTIDSPUNKT.minusDays(1);
        var tom = SKJÆRINGSTIDSPUNKT.plusDays(1);

        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty());
        var aa = lagAktivitetsAvtaleBuilder(yaBuilder, fom, tom);
        var ya = lagYrkesaktivitetBuilder(yaBuilder, aa, arbeidsgiver, ref);
        ya.leggTilPermisjon(PermisjonDtoBuilder.ny().medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET).medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medProsentsats(Stillingsprosent.HUNDRED));
        var aktørArbeidBuilder = lagAktørArbeidBuilder(List.of(ya));

        var iayGrunnlag = lagGrunnlag(aktørArbeidBuilder);

        // Act
        var permisjonDto = UtledPermisjonTilDto.utled(iayGrunnlag, SKJÆRINGSTIDSPUNKT, bgAndelArbeidsforhold);

        // Assert
        assertThat(permisjonDto).hasValueSatisfying(p -> {
            assertThat(p.getPermisjonFom()).isEqualTo(fom);
            assertThat(p.getPermisjonTom()).isEqualTo(tom);
        });

    }

    private InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder lagAktørArbeidBuilder(List<YrkesaktivitetDtoBuilder> yrkesaktiviteter) {
        var aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder.oppdatere(Optional.empty());
        yrkesaktiviteter.forEach(aktørArbeidBuilder::leggTilYrkesaktivitet);
        return aktørArbeidBuilder;
    }

    private InntektArbeidYtelseGrunnlagDto lagGrunnlag(InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder) {
        var inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonTypeDto.REGISTER)
            .leggTilAktørArbeid(aktørArbeidBuilder);
        var inntektArbeidYtelseGrunnlagBuilder = InntektArbeidYtelseGrunnlagDtoBuilder.nytt()
            .medData(inntektArbeidYtelseAggregatBuilder);
        return inntektArbeidYtelseGrunnlagBuilder.build();
    }

    private YrkesaktivitetDtoBuilder lagYrkesaktivitetBuilder(YrkesaktivitetDtoBuilder yrkesaktivitetBuilder, AktivitetsAvtaleDtoBuilder aktivitetsAvtale,
                                                              Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto ref) {
        yrkesaktivitetBuilder
            .medArbeidsforholdId(ref)
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);
        return yrkesaktivitetBuilder;
    }

    private AktivitetsAvtaleDtoBuilder lagAktivitetsAvtaleBuilder(YrkesaktivitetDtoBuilder yrkesaktivitetBuilder, LocalDate fom, LocalDate tom) {
        return yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
    }

    private BGAndelArbeidsforholdDto lagBGAndelArbeidsforhold(Optional<Arbeidsgiver> arbeidsgiverOpt, InternArbeidsforholdRefDto ref) {
        var bg = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(Beløp.fra(500_000))
            .build();
        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
        var builder = BGAndelArbeidsforholdDto.builder()
            .medArbeidsforholdRef(ref);
        arbeidsgiverOpt.ifPresent(builder::medArbeidsgiver);
        var andel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBGAndelArbeidsforhold(builder)
            .build(periode);
        return andel.getBgAndelArbeidsforhold().orElseThrow();
    }

}
