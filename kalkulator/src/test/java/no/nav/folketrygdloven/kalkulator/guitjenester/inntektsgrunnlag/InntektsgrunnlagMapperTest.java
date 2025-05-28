package no.nav.folketrygdloven.kalkulator.guitjenester.inntektsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PGIType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagInntektDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagMånedDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.PGIGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.PGIPrÅrDto;

class InntektsgrunnlagMapperTest {
    private static final LocalDate STP = LocalDate.now();
    private static final Intervall SG_PERIODE = Intervall.fraOgMedTilOgMed(LocalDate.now().minusMonths(12), LocalDate.now());
    private static final Intervall BG_PERIODE = Intervall.fraOgMedTilOgMed(LocalDate.now().minusMonths(3), LocalDate.now());

	@Test
	void skal_teste_at_korrekte_inntekter_mappes_til_måneder() {
        var mapper = new InntektsgrunnlagMapper(Optional.of(SG_PERIODE), Optional.of(BG_PERIODE), Collections.emptyList());
        var feilKilde = lagInntekt("123", InntektskildeType.INNTEKT_BEREGNING);
        var korrektKilde = lagInntekt("123", InntektskildeType.INNTEKT_SAMMENLIGNING);
		feilKilde.leggTilInntektspost(lagInntektspost(feilKilde, 5000, månederFør(3)));
		feilKilde.leggTilInntektspost(lagInntektspost(feilKilde, 5000, månederFør(2)));
		korrektKilde.leggTilInntektspost(lagInntektspost(korrektKilde, 5000, månederFør(3)));
		korrektKilde.leggTilInntektspost(lagInntektspost(korrektKilde, 5000, månederFør(2)));

        var dto = mapper.map(Arrays.asList(feilKilde.build(), korrektKilde.build()));

		assertThat(dto).isPresent();
		assertThat(dto.get().getMåneder()).hasSize(2);
		assertThat(dto.get().getMåneder().get(0).getInntekter()).hasSize(1);
		assertThat(dto.get().getMåneder().get(1).getInntekter()).hasSize(1);
	}

    @Test
    void skal_teste_at_inntekter_mappes() {
        var mapper = new InntektsgrunnlagMapper(Optional.of(SG_PERIODE), Optional.of(BG_PERIODE), Collections.emptyList());
        var beregningsinntekter = lagInntekt("123", InntektskildeType.INNTEKT_BEREGNING);
        var sammenligningsinntekter = lagInntekt("123", InntektskildeType.INNTEKT_SAMMENLIGNING);
        beregningsinntekter.leggTilInntektspost(lagInntektspost(beregningsinntekter, 5000, månederFør(3)));
        beregningsinntekter.leggTilInntektspost(lagInntektspost(beregningsinntekter, 5000, månederFør(2)));
        sammenligningsinntekter.leggTilInntektspost(lagInntektspost(sammenligningsinntekter, 5000, månederFør(3)));
        sammenligningsinntekter.leggTilInntektspost(lagInntektspost(sammenligningsinntekter, 5000, månederFør(2)));

        var dto = mapper.map(Arrays.asList(beregningsinntekter.build(), sammenligningsinntekter.build()));

        assertThat(dto).isPresent();
        assertThat(dto.get().getSammenligningsgrunnlagInntekter()).hasSize(2);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(0).getInntekter()).hasSize(1);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(1).getInntekter()).hasSize(1);
		assertThat(dto.get().getBeregningsgrunnlagInntekter()).hasSize(1);
        assertThat(dto.get().getBeregningsgrunnlagInntekter().getFirst().getInntekter()).hasSize(1);
    }

    @Test
    void skal_teste_at_inntekter_uten_arbeidsgiver_mappes_til_ytelse() {
        var mapper = new InntektsgrunnlagMapper(Optional.of(SG_PERIODE), Optional.of(BG_PERIODE), Collections.emptyList());
        var sammenligningsinntekter = lagInntekt(null, InntektskildeType.INNTEKT_SAMMENLIGNING);
        sammenligningsinntekter.leggTilInntektspost(lagInntektspost(sammenligningsinntekter, 5000, månederFør(3), InntektspostType.YTELSE));
        sammenligningsinntekter.leggTilInntektspost(lagInntektspost(sammenligningsinntekter, 5000, månederFør(2), InntektspostType.YTELSE));

        var dto = mapper.map(Collections.singletonList(sammenligningsinntekter.build()));

        assertThat(dto).isPresent();
        assertThat(dto.get().getSammenligningsgrunnlagInntekter()).hasSize(2);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(0).getInntekter()).hasSize(1);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(1).getInntekter()).hasSize(1);
        var alleInntekter = dto.map(InntektsgrunnlagDto::getSammenligningsgrunnlagInntekter)
                .orElse(Collections.emptyList())
                .stream()
                .map(InntektsgrunnlagMånedDto::getInntekter)
                .flatMap(Collection::stream)
                .toList();
        assertThat(alleInntekter.stream().allMatch(innt -> innt.getInntektAktivitetType().equals(InntektAktivitetType.YTELSEINNTEKT))).isTrue();
    }

    @Test
    void skal_teste_at_frilans_merkes_korrekt() {
        var mapper = new InntektsgrunnlagMapper(Optional.of(SG_PERIODE), Optional.of(BG_PERIODE), Collections.singletonList(Arbeidsgiver.virksomhet("321")));
        var inntektFL = lagInntekt("321", InntektskildeType.INNTEKT_SAMMENLIGNING);
        var inntektAT = lagInntekt("123", InntektskildeType.INNTEKT_SAMMENLIGNING);
        inntektFL.leggTilInntektspost(lagInntektspost(inntektFL, 3000, månederFør(3)));
        inntektFL.leggTilInntektspost(lagInntektspost(inntektFL, 3000, månederFør(2)));
        inntektAT.leggTilInntektspost(lagInntektspost(inntektAT, 5000, månederFør(3)));
        inntektAT.leggTilInntektspost(lagInntektspost(inntektAT, 5000, månederFør(2)));

        var dto = mapper.map(Arrays.asList(inntektFL.build(), inntektAT.build()));

        assertThat(dto).isPresent();
        assertThat(dto.get().getSammenligningsgrunnlagInntekter()).hasSize(2);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(0).getInntekter()).hasSize(2);

        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(0).getInntekter()).hasSize(2);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(0).getInntekter().stream()
                .anyMatch(innt -> innt.getInntektAktivitetType().equals(InntektAktivitetType.FRILANSINNTEKT)
                        && innt.getBeløp().compareTo(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(3000)) == 0))
                .isTrue();
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(0).getInntekter().stream()
                .anyMatch(innt -> innt.getInntektAktivitetType().equals(InntektAktivitetType.ARBEIDSTAKERINNTEKT)
                        && innt.getBeløp().compareTo(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(5000)) == 0))
                .isTrue();

        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(1).getInntekter()).hasSize(2);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(1).getInntekter().stream()
                .anyMatch(innt -> innt.getInntektAktivitetType().equals(InntektAktivitetType.FRILANSINNTEKT)
                        && innt.getBeløp().compareTo(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(3000)) == 0))
                .isTrue();
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(1).getInntekter().stream()
                .anyMatch(innt -> innt.getInntektAktivitetType().equals(InntektAktivitetType.ARBEIDSTAKERINNTEKT)
                        && innt.getBeløp().compareTo(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(5000)) == 0))
                .isTrue();
    }

    @Test
    void skal_teste_at_pgidata_mappes_alene() {
        var mapper = new InntektsgrunnlagMapper(Optional.of(SG_PERIODE), Optional.of(BG_PERIODE), Collections.emptyList());
        var sigrun = lagInntekt(null, InntektskildeType.SIGRUN);
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 200000, 2020, InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 50000, 2020, InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 60000, 2020, InntektspostType.LØNN));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 450000, 2021, InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 100000, 2021, InntektspostType.LØNN));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 600000, 2022, InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE));

        var dto = mapper.map(Collections.singletonList(sigrun.build()));

        assertThat(dto).isPresent();
        var pgiGrunnlag = dto.get().getPgiGrunnlag().stream().sorted(Comparator.comparing(PGIPrÅrDto::getÅr)).toList();
        assertThat(pgiGrunnlag).hasSize(3);
        assertThat(pgiGrunnlag.get(0).getÅr()).isEqualTo(2020);
        var inntekterÅr2020 = pgiGrunnlag.get(0).getInntekter();
        assertThat(inntekterÅr2020).hasSize(2);
        assertThat(finnPGIGrunnlag(inntekterÅr2020, 250000, PGIType.NÆRING)).isPresent();
        assertThat(finnPGIGrunnlag(inntekterÅr2020, 60000, PGIType.LØNN)).isPresent();

        assertThat(pgiGrunnlag.get(1).getÅr()).isEqualTo(2021);
        var inntekterÅr2021 = pgiGrunnlag.get(1).getInntekter();
        assertThat(inntekterÅr2021).hasSize(2);
        assertThat(finnPGIGrunnlag(inntekterÅr2021, 450000, PGIType.NÆRING)).isPresent();
        assertThat(finnPGIGrunnlag(inntekterÅr2021, 100000, PGIType.LØNN)).isPresent();

        assertThat(pgiGrunnlag.get(2).getÅr()).isEqualTo(2022);
        var inntekterÅr2022 = pgiGrunnlag.get(2).getInntekter();
        assertThat(inntekterÅr2022).hasSize(1);
        assertThat(finnPGIGrunnlag(inntekterÅr2022, 600000, PGIType.NÆRING)).isPresent();
    }

    @Test
    void skal_teste_at_pgidata_mappes_med_inntekter() {
        var mapper = new InntektsgrunnlagMapper(Optional.of(SG_PERIODE), Optional.of(BG_PERIODE), Collections.emptyList());
        var beregningsinntekter = lagInntekt("123", InntektskildeType.INNTEKT_BEREGNING);
        var sammenligningsinntekter = lagInntekt("123", InntektskildeType.INNTEKT_SAMMENLIGNING);
        beregningsinntekter.leggTilInntektspost(lagInntektspost(beregningsinntekter, 5000, månederFør(3)));
        beregningsinntekter.leggTilInntektspost(lagInntektspost(beregningsinntekter, 5000, månederFør(2)));
        sammenligningsinntekter.leggTilInntektspost(lagInntektspost(sammenligningsinntekter, 5000, månederFør(3)));
        sammenligningsinntekter.leggTilInntektspost(lagInntektspost(sammenligningsinntekter, 5000, månederFør(2)));

        var inntekter = new ArrayList<InntektDto>();
        inntekter.add(beregningsinntekter.build());
        inntekter.add(sammenligningsinntekter.build());

        var sigrun = lagInntekt(null, InntektskildeType.SIGRUN);
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 200000, 2020, InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 50000, 2020, InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 60000, 2020, InntektspostType.LØNN));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 450000, 2021, InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 100000, 2021, InntektspostType.LØNN));
        sigrun.leggTilInntektspost(lagPGIPost(sigrun, 600000, 2022, InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE));

        inntekter.add(sigrun.build());
        var dto = mapper.map(inntekter);

        assertThat(dto).isPresent();
        assertThat(dto.get().getSammenligningsgrunnlagInntekter()).hasSize(2);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(0).getInntekter()).hasSize(1);
        assertThat(dto.get().getSammenligningsgrunnlagInntekter().get(1).getInntekter()).hasSize(1);
		assertThat(dto.get().getBeregningsgrunnlagInntekter()).hasSize(1);
        assertThat(dto.get().getBeregningsgrunnlagInntekter().getFirst().getInntekter()).hasSize(1);

        assertThat(dto).isPresent();
        var pgiGrunnlag = dto.get().getPgiGrunnlag().stream().sorted(Comparator.comparing(PGIPrÅrDto::getÅr)).toList();
        assertThat(pgiGrunnlag).hasSize(3);
        assertThat(pgiGrunnlag.get(0).getÅr()).isEqualTo(2020);
        var inntekterÅr2020 = pgiGrunnlag.get(0).getInntekter();
        assertThat(inntekterÅr2020).hasSize(2);
        assertThat(finnPGIGrunnlag(inntekterÅr2020, 250000, PGIType.NÆRING)).isPresent();
        assertThat(finnPGIGrunnlag(inntekterÅr2020, 60000, PGIType.LØNN)).isPresent();

        assertThat(pgiGrunnlag.get(1).getÅr()).isEqualTo(2021);
        var inntekterÅr2021 = pgiGrunnlag.get(1).getInntekter();
        assertThat(inntekterÅr2021).hasSize(2);
        assertThat(finnPGIGrunnlag(inntekterÅr2021, 450000, PGIType.NÆRING)).isPresent();
        assertThat(finnPGIGrunnlag(inntekterÅr2021, 100000, PGIType.LØNN)).isPresent();

        assertThat(pgiGrunnlag.get(2).getÅr()).isEqualTo(2022);
        var inntekterÅr2022 = pgiGrunnlag.get(2).getInntekter();
        assertThat(inntekterÅr2022).hasSize(1);
        assertThat(finnPGIGrunnlag(inntekterÅr2022, 600000, PGIType.NÆRING)).isPresent();
    }

    private Optional<PGIGrunnlagDto> finnPGIGrunnlag(List<PGIGrunnlagDto> grunnlag, int inntekt, PGIType type) {
        return grunnlag
                .stream()
                .filter(i -> i.getBeløp().compareTo(ModellTyperMapper.beløpTilDto(Beløp.fra(inntekt))) == 0 && i.getPgiType().equals(type))
                .findFirst();
    }

    private InntektspostDtoBuilder lagPGIPost(InntektDtoBuilder builder, int inntekt, int år, InntektspostType type) {
        return builder.getInntektspostBuilder()
                .medPeriode(LocalDate.of(år, 1, 1), LocalDate.of(år, 12, 31))
                .medBeløp(Beløp.fra(inntekt))
                .medInntektspostType(type);
    }

    private InntektspostDtoBuilder lagInntektspost(InntektDtoBuilder builder, int inntekt, LocalDate fom) {
        return lagInntektspost(builder, inntekt, fom, InntektspostType.LØNN);
    }

    private InntektspostDtoBuilder lagInntektspost(InntektDtoBuilder builder, int inntekt, LocalDate fom, InntektspostType type) {
        return builder.getInntektspostBuilder()
                .medPeriode(fom, fom.with(TemporalAdjusters.lastDayOfMonth()))
                .medBeløp(Beløp.fra(inntekt))
                .medInntektspostType(type);
    }

    private LocalDate månederFør(int månederFør) {
        return STP.minusMonths(månederFør).withDayOfMonth(1);
    }

    private InntektDtoBuilder lagInntekt(String orgnr, InntektskildeType kilde) {
        return InntektDtoBuilder.oppdatere(Optional.empty()).medInntektsKilde(kilde).medArbeidsgiver(orgnr == null ? null :  Arbeidsgiver.virksomhet(orgnr));
    }

}
