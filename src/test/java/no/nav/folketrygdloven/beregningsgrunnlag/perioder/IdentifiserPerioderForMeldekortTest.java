package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MeldekortPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

class IdentifiserPerioderForMeldekortTest {


    public static final LocalDate SKJÆRINGSTIDSPUNKTET = LocalDate.now();

    @Test
    void skal_splitte_ved_start_av_meldekort_etter_skjæringstidspunktet() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKTET.plusDays(1);
        LocalDate tom = SKJÆRINGSTIDSPUNKTET.plusDays(20);
        PeriodeModell periodeModell = new PeriodeModell.Builder().medMeldekortPerioder(List.of(
            new MeldekortPeriode(new Periode(fom, tom),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            )
        ))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKTET)
            .build();

        // Act
        Set<PeriodeSplittData> periodeSplittData = IdentifiserPerioderForMeldekort.identifiserPerioderForEndringerIMeldekort(periodeModell);

        // Assert
        assertThat(periodeSplittData.size()).isEqualTo(2);
        Iterator<PeriodeSplittData> iterator = periodeSplittData.iterator();
        assertThat(iterator.next().getFom()).isEqualTo(fom);
        assertThat(iterator.next().getFom()).isEqualTo(tom.plusDays(1));

    }


    @Test
    void skal_ikkje_splitte_ved_start_og_slutt_av_meldekort_før_skjæringstidspunktet() {
        // Arrange
        PeriodeModell periodeModell = new PeriodeModell.Builder().medMeldekortPerioder(List.of(
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.minusDays(1), SKJÆRINGSTIDSPUNKTET.minusDays(1)),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            )
        ))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKTET)
            .build();

        // Act
        Set<PeriodeSplittData> periodeSplittData = IdentifiserPerioderForMeldekort.identifiserPerioderForEndringerIMeldekort(periodeModell);

        // Assert
        assertThat(periodeSplittData.size()).isEqualTo(0);
    }

    @Test
    void skal_splitte_ved_endring_av_meldekort_etter_skjæringstidspunktet() {
        // Arrange
        LocalDate fom = SKJÆRINGSTIDSPUNKTET.plusDays(21);
        LocalDate tom = SKJÆRINGSTIDSPUNKTET.plusDays(22);
        PeriodeModell periodeModell = new PeriodeModell.Builder().medMeldekortPerioder(List.of(
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.minusDays(1), SKJÆRINGSTIDSPUNKTET.plusDays(20)),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            ),

            new MeldekortPeriode(new Periode(fom, tom),
                BigDecimal.TEN,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            )
        ))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKTET)
            .build();


        // Act
        Set<PeriodeSplittData> periodeSplittData = IdentifiserPerioderForMeldekort.identifiserPerioderForEndringerIMeldekort(periodeModell);

        // Assert
        assertThat(periodeSplittData.size()).isEqualTo(2);
        Iterator<PeriodeSplittData> iterator = periodeSplittData.iterator();
        assertThat(iterator.next().getFom()).isEqualTo(fom);
        assertThat(iterator.next().getFom()).isEqualTo(tom.plusDays(1));

    }

    @Test
    void skal_kun_lage_splitt_for_opphør_ved_start_av_meldekort_før_skjæringstidspunktet() {
        // Arrange
        LocalDate tom = SKJÆRINGSTIDSPUNKTET.plusDays(22);
        PeriodeModell periodeModell = new PeriodeModell.Builder().medMeldekortPerioder(List.of(
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.minusDays(10), SKJÆRINGSTIDSPUNKTET.minusDays(2)),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            ),

            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.minusDays(1), tom),
                BigDecimal.TEN,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            )
        ))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKTET)
            .build();

        // Act
        Set<PeriodeSplittData> periodeSplittData = IdentifiserPerioderForMeldekort.identifiserPerioderForEndringerIMeldekort(periodeModell);

        // Assert
        assertThat(periodeSplittData.size()).isEqualTo(1);
        Iterator<PeriodeSplittData> iterator = periodeSplittData.iterator();
        assertThat(iterator.next().getFom()).isEqualTo(tom.plusDays(1));
    }

    @Test
    void skal_lage_flere_splitter_ved_endring_av_meldekort_etter_skjæringstidspunktet() {
        // Arrange
        PeriodeModell periodeModell = new PeriodeModell.Builder().medMeldekortPerioder(List.of(
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.minusDays(10), SKJÆRINGSTIDSPUNKTET.plusDays(2)),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            ),
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.plusDays(3), SKJÆRINGSTIDSPUNKTET.plusDays(4)),
                BigDecimal.TEN,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            ),
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.plusDays(5), SKJÆRINGSTIDSPUNKTET.plusDays(6)),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            )
        ))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKTET)
            .build();

        // Act
        Set<PeriodeSplittData> periodeSplittData = IdentifiserPerioderForMeldekort.identifiserPerioderForEndringerIMeldekort(periodeModell);

        // Assert
        assertThat(periodeSplittData.size()).isEqualTo(3);
        Iterator<PeriodeSplittData> iterator = periodeSplittData.iterator();
        assertThat(iterator.next().getFom()).isEqualTo(SKJÆRINGSTIDSPUNKTET.plusDays(3));
        assertThat(iterator.next().getFom()).isEqualTo(SKJÆRINGSTIDSPUNKTET.plusDays(5));
        assertThat(iterator.next().getFom()).isEqualTo(SKJÆRINGSTIDSPUNKTET.plusDays(6).plusDays(1));

    }

    @Test
    void skal_ikke_lage_splitt_for_meldekort_uten_endring() {
        // Arrange
        LocalDate tom = SKJÆRINGSTIDSPUNKTET.plusDays(4);
        PeriodeModell periodeModell = new PeriodeModell.Builder().medMeldekortPerioder(List.of(
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.minusDays(10), SKJÆRINGSTIDSPUNKTET.plusDays(2)),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            ),
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.plusDays(3), tom),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            )
        ))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKTET)
            .build();

        // Act
        Set<PeriodeSplittData> periodeSplittData = IdentifiserPerioderForMeldekort.identifiserPerioderForEndringerIMeldekort(periodeModell);

        // Assert
        assertThat(periodeSplittData.size()).isEqualTo(1);
        Iterator<PeriodeSplittData> iterator = periodeSplittData.iterator();
        assertThat(iterator.next().getFom()).isEqualTo(tom.plusDays(1));

    }

    @Test
    void skal_lage_splitt_ved_opphør_av_meldekort() {
        // Arrange
        LocalDate tom = SKJÆRINGSTIDSPUNKTET.plusDays(2);
        PeriodeModell periodeModell = new PeriodeModell.Builder().medMeldekortPerioder(List.of(
            new MeldekortPeriode(new Periode(SKJÆRINGSTIDSPUNKTET.minusDays(10), tom),
                BigDecimal.ONE,
                BigDecimal.ONE,
                AktivitetStatusV2.DP
            )
        ))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKTET)
            .build();

        // Act
        Set<PeriodeSplittData> periodeSplittData = IdentifiserPerioderForMeldekort.identifiserPerioderForEndringerIMeldekort(periodeModell);

        // Assert
        assertThat(periodeSplittData.size()).isEqualTo(1);
        assertThat(periodeSplittData.iterator().next().getFom()).isEqualTo(tom.plusDays(1));
    }

}
