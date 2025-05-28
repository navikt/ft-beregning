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
    void en_måned_søkt_fl() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var frisinnPeriode = new FrisinnPeriode(april, true, false);
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
    }

    @Test
    void en_måned_søkt_sn() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var frisinnPeriode = new FrisinnPeriode(april, false, true);
        var oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.fra(1000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .build();

        // Act
        var resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, snAndel(1000)));
    }

    @Test
    void en_måned_søkt_sn_har_fl() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var frisinnPeriode = new FrisinnPeriode(april, false, true);
        var oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.fra(1000)));
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, snAndel(1000)));
    }

    @Test
    void en_måned_søkt_fl_har_sn() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var frisinnPeriode = new FrisinnPeriode(april, true, false);
        var oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.fra(1000)));
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
    }

    @Test
    void en_måned_søkt_fl_sn_samme_periode() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var frisinnPeriode = new FrisinnPeriode(april, true, true);
        var oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.fra(1000)));
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(april, snAndel(1000),  flAndel(5000)));
    }

    @Test
    void en_måned_søkt_fl_sn_ulik_periode() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var aprilDel1 = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 10));
        var aprilDel2 = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 11), LocalDate.of(2020, 4, 30));

        var frisinnPeriode = new FrisinnPeriode(aprilDel1, true, false);
        var frisinnPeriode2 = new FrisinnPeriode(aprilDel2, true, true);
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)));
        var oppgittNæring = List.of(lagOppgittInntekt(aprilDel2, Beløp.fra(1000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(aprilDel1, flAndel(5000)));
        assertPeriode(resultat.get(1), periode(aprilDel2, flAndel(5000), snAndel(1000)));
    }

    @Test
    void to_måneder_søkt_fl() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        var frisinnPeriode = new FrisinnPeriode(april, true, false);
        var frisinnPeriode2 = new FrisinnPeriode(mai, true, false);
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
        assertPeriode(resultat.get(1), periode(mai, flAndel(0)));
    }

    @Test
    void to_måneder_søkt_fl_oppgit_sn_i_periode_2() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        var frisinnPeriode = new FrisinnPeriode(april, true, false);
        var frisinnPeriode2 = new FrisinnPeriode(mai, true, false);
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        var oppgittNæring = List.of(lagOppgittInntekt(mai, Beløp.fra(1000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
        assertPeriode(resultat.get(1), periode(mai, flAndel(0)));
    }

    @Test
    void to_måneder_søkt_fl_søker_sn_i_periode_2() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        var frisinnPeriode = new FrisinnPeriode(april, true, false);
        var frisinnPeriode2 = new FrisinnPeriode(mai, true, true);
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        var oppgittNæring = List.of(lagOppgittInntekt(mai, Beløp.fra(1000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000)));
        assertPeriode(resultat.get(1), periode(mai, flAndel(0), snAndel(1000)));
    }

    @Test
    void to_måneder_søkt_fl_sn_i_begge_perioder() {
        // Arrange
        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        var frisinnPeriode = new FrisinnPeriode(april, true, true);
        var frisinnPeriode2 = new FrisinnPeriode(mai, true, true);
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        var oppgittNæring = List.of(lagOppgittInntekt(april, Beløp.ZERO), lagOppgittInntekt(mai, Beløp.fra(1000)));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Arrays.asList(frisinnPeriode, frisinnPeriode2));

        // Assert
        assertThat(resultat).hasSize(2);
        assertPeriode(resultat.get(0), periode(april, flAndel(5000), snAndel(0)));
        assertPeriode(resultat.get(1), periode(mai, flAndel(0), snAndel(1000)));
    }

    @Test
    void skaL_mappe_arbeidsinntekt() {
        // Arrange
        var mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        var frisinnPeriode = new FrisinnPeriode(mai, true, true);
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        var oppgittNæring = List.of(lagOppgittInntekt(mai, Beløp.fra(1000)));
        var arbfor = lagOppgittArbeidsinntekt(mai, Beløp.fra(500));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilOppgittArbeidsforhold(arbfor)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Collections.singletonList(frisinnPeriode));

        // Assert
        assertThat(resultat).hasSize(1);
        assertPeriode(resultat.get(0), periode(mai, 500, flAndel(0), snAndel(1000)));
    }


    @Test
    void to_måneder_første_måned_delt() {
        // Arrange
        var førstePeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 15));
        var andrePeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 16), LocalDate.of(2020, 4, 30));
        var tredjePeriode = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));

        var frisinnEn = new FrisinnPeriode(førstePeriode, true, false);
        var frisinnTo = new FrisinnPeriode(andrePeriode, true, true);
        var frisinnTre = new FrisinnPeriode(tredjePeriode, true, true);

        var april = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30));
        var halveApril = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 16), LocalDate.of(2020, 4, 30));
        var mai = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));
        var oppgittNæring = List.of(lagOppgittInntekt(halveApril, Beløp.fra(1000)), lagOppgittInntekt(mai, Beløp.ZERO));
        var oppgittFrilansInntekt = List.of(lagOppgittFrilansInntekt(april, Beløp.fra(5000)), lagOppgittFrilansInntekt(mai, Beløp.ZERO));
        var oo = OppgittOpptjeningDtoBuilder.ny()
                .leggTilEgneNæringer(oppgittNæring)
                .leggTilFrilansOpplysninger(new OppgittFrilansDto(false, oppgittFrilansInntekt))
                .build();

        // Act
        var resultat = kjørMapper(oo, Arrays.asList(frisinnEn, frisinnTo, frisinnTre));

        // Assert
        var førsteHalvdelAvApril = Intervall.fraOgMedTilOgMed(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 15));
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
            var forventetAndel = faktisk.stream().filter(forv -> forv.getStatusSøktFor().equals(f.getStatusSøktFor())).findFirst().orElse(null);
            assertThat(forventetAndel).isNotNull();
            assertThat(forventetAndel.getStatusSøktFor()).isEqualTo(f.getStatusSøktFor());
            assertThat(forventetAndel.getOppgittInntekt()).isEqualByComparingTo(f.getOppgittInntekt());
        });
    }

    private FrisinnPeriodeDto periode(Intervall periode, FrisinnAndelDto... andeler) {
        return periode(periode, null, andeler);
    }

    private FrisinnPeriodeDto periode(Intervall periode, Integer atInntekt, FrisinnAndelDto... andeler) {
        var dto = new FrisinnPeriodeDto();
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
