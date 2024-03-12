package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnPeriode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnAndelDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnPeriodeDto;

class MapTilPerioderFRISINNTest {

    @Test
    public void en_måned_søkt_fl() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, true, false);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
    }

    @Test
    public void en_måned_søkt_sn() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, false, true);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.fra(1000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, snAndel(1000)));
    }

    @Test
    public void en_måned_søkt_sn_har_fl() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, false, true);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.fra(1000)));
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, snAndel(1000)));
    }

    @Test
    public void en_måned_søkt_fl_har_sn() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, true, false);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.fra(1000)));
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
    }

    @Test
    public void en_måned_søkt_fl_sn_samme_periode() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, true, true);
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.fra(1000)));
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, snAndel(1000),  flAndel(5000)));
    }

    @Test
    public void en_måned_søkt_fl_sn_ulik_periode() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall aprilDel1 = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 10));
        Intervall aprilDel2 = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 11), LocalDate.of(2020, 4, 30));

        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(aprilDel1, true, false);
        FrisinnPeriode frisinnPeriode2 = new FrisinnPeriode(aprilDel2, true, true);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(aprilDel2, Beløp.fra(1000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(aprilDel1, flAndel(5000)));
        assertPeriode(resultat.get(1), periode(aprilDel2, flAndel(5000), snAndel(1000)));
    }

    @Test
    public void to_måneder_søkt_fl() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, true, false);
        FrisinnPeriode frisinnPeriode2 = new FrisinnPeriode(mai, true, false);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
        assertPeriode(resultat.get(1), periode(mai, flAndel(0)));
    }

    @Test
    public void to_måneder_søkt_fl_oppgit_sn_i_periode_2() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, true, false);
        FrisinnPeriode frisinnPeriode2 = new FrisinnPeriode(mai, true, false);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(mai, Beløp.fra(1000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
        assertPeriode(resultat.get(1), periode(mai, flAndel(0)));
    }

    @Test
    public void to_måneder_søkt_fl_søker_sn_i_periode_2() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, true, false);
        FrisinnPeriode frisinnPeriode2 = new FrisinnPeriode(mai, true, true);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(mai, Beløp.fra(1000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
        assertPeriode(resultat.get(1), periode(mai, flAndel(0), snAndel(1000)));
    }

    @Test
    public void to_måneder_søkt_fl_sn_i_begge_perioder() {
        // Arrange
        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(april, true, true);
        FrisinnPeriode frisinnPeriode2 = new FrisinnPeriode(mai, true, true);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.ZERO), lagOppgittInntekt(mai, Beløp.fra(1000)));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000), snAndel(0)));
        assertPeriode(resultat.get(1), periode(mai, flAndel(0), snAndel(1000)));
    }

    @Test
    public void skaL_mappe_arbeidsinntekt() {
        // Arrange
        Intervall mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        FrisinnPeriode frisinnPeriode = new FrisinnPeriode(mai, true, true);
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(mai, Beløp.fra(1000)));
        OppgittOpptjeningDtoBuilder.OppgittArbeidsforholdBuilder arbfor = lagOppgittArbeidsinntekt(mai, Beløp.fra(500));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilOppgittArbeidsforhold(arbfor)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(mai, 500, flAndel(0), snAndel(1000)));
    }


    @Test
    public void to_måneder_første_måned_delt() {
        // Arrange
        Intervall førstePeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 15));
        Intervall andrePeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 16), LocalDate.of(2020, 4, 30));
        Intervall tredjePeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        FrisinnPeriode frisinnEn = new FrisinnPeriode(førstePeriode, true, false);
        FrisinnPeriode frisinnTo = new FrisinnPeriode(andrePeriode, true, true);
        FrisinnPeriode frisinnTre = new FrisinnPeriode(tredjePeriode, true, true);

        Intervall april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        Intervall halveApril = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 16), LocalDate.of(2020, 4, 30));
        Intervall mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));
        List<OppgittOpptjeningDtoBuilder.EgenNæringBuilder> oppgittNæring = List.of(lagOppgittInntekt(halveApril, Beløp.fra(1000)), lagOppgittInntekt(mai, Beløp.ZERO));
        List<OppgittFrilansInntektDto> oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        OppgittOpptjeningDto oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        List<FrisinnPeriodeDto> resultat = kjørMapper(oo, Arrays.asList(frisinnEn, frisinnTo, frisinnTre));

        // Assert
        Intervall førsteHalvdelAvApril = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 15));
        assertThat(resultat).hasSize(3);
        assertPeriode(resultat.get(0), periode(førsteHalvdelAvApril, flAndel(5000)));
        assertPeriode(resultat.get(1),periode(andrePeriode, flAndel(5000), snAndel(1000)));
        assertPeriode(resultat.get(2),periode(tredjePeriode, flAndel(0), snAndel(0)));
    }

    private List<FrisinnPeriodeDto> kjørMapper(OppgittOpptjeningDto oo, List<FrisinnPeriode> frisinnPeriodes) {
        return MapTilPerioderFRISINN.map(frisinnPeriodes, oo).stream()
                .sorted(Comparator.comparing(FrisinnPeriodeDto::getFom))
                .collect(Collectors.toList());
    }

    private void assertPeriode(FrisinnPeriodeDto faktisk, FrisinnPeriodeDto forventet) {
        assertThat(faktisk.getFom()).isEqualTo(forventet.getFom());
        assertThat(faktisk.getTom()).isEqualTo(forventet.getTom());
        assertThat(faktisk.getOppgittArbeidsinntekt()).isEqualTo(forventet.getOppgittArbeidsinntekt());
        assertAndeler(faktisk.getFrisinnAndeler(), forventet.getFrisinnAndeler());
    }

    private void assertAndeler(List<FrisinnAndelDto> faktisk, List<FrisinnAndelDto> forventet) {
        assertThat(faktisk).hasSameSizeAs(forventet);
        forventet.forEach(f -> {
            FrisinnAndelDto forventetAndel = faktisk.stream().filter(forv -> forv.getStatusSøktFor().equals(f.getStatusSøktFor())).findFirst().orElse(null);
            assertThat(forventetAndel).isNotNull();
            assertThat(forventetAndel.getStatusSøktFor()).isEqualTo(f.getStatusSøktFor());
            assertThat(forventetAndel.getOppgittInntekt()).isEqualByComparingTo(f.getOppgittInntekt());
        });
    }

    private FrisinnPeriodeDto periode(Intervall periode, FrisinnAndelDto... andeler) {
        return periode(periode, null, andeler);
    }

    private FrisinnPeriodeDto periode(Intervall periode, Integer atInntekt, FrisinnAndelDto... andeler) {
        FrisinnPeriodeDto dto = new FrisinnPeriodeDto();
        dto.setTom(periode.getTomDato());
        dto.setFom(periode.getFomDato());
        dto.setFrisinnAndeler(Arrays.asList(andeler));
        if (atInntekt != null) {
            dto.setOppgittArbeidsinntekt(ModellTyperMapper.beløpTilDto(Beløp.fra(atInntekt)));
        }
        return dto;
    }

    private FrisinnAndelDto flAndel(int sum) {
        return new FrisinnAndelDto(ModellTyperMapper.beløpTilDto(Beløp.fra(sum)), AktivitetStatus.FRILANSER);
    }

    private FrisinnAndelDto snAndel(int sum) {
        return new FrisinnAndelDto(ModellTyperMapper.beløpTilDto(Beløp.fra(sum)), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private OppgittOpptjeningDtoBuilder.OppgittArbeidsforholdBuilder lagOppgittArbeidsinntekt(Intervall periode, Beløp beløp) {
        return OppgittOpptjeningDtoBuilder.OppgittArbeidsforholdBuilder.ny()
                .medPeriode(periode)
                .medInntekt(beløp);
    }


    private OppgittOpptjeningDtoBuilder.EgenNæringBuilder lagOppgittInntekt(Intervall april, Beløp periodeInntekt) {
        return OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny()
                .medPeriode(april)
                .medVirksomhet("999999999")
                .medBruttoInntekt(periodeInntekt);
    }

    private OppgittFrilansInntektDto lagOppgittFrilansInntekt(Intervall april, Beløp periodeInntekt) {
        return new OppgittFrilansInntektDto(april, periodeInntekt);
    }


}
