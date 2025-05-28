package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PeriodeTest {

    @Test
    void overlapper_A_starter_før_og_slutter_etter_B() {
        // Arrange
        var a = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31));
        var b = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

        // Act
        var overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isTrue();
    }

    @Test
    void overlapper_B_starter_før_og_slutter_etter_A() {
        // Arrange
        var a = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
        var b = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31));

        // Act
        var overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isTrue();
    }

    @Test
    void overlapper_når_en_dag_felles_a_starter_før_b() {
        // Arrange
        var a = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1));
        var b = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

        // Act
        var overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isTrue();
    }

    @Test
    void overlapper_når_en_dag_felles_b_starter_før_a() {
        // Arrange
        var a = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
        var b = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1));

        // Act
        var overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isTrue();
    }

    @Test
    void overlapper_ikke() {
        // Arrange
        var a = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
        var b = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

        // Act
        var overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isFalse();
    }
}
