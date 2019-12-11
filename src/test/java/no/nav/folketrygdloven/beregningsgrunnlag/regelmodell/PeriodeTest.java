package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class PeriodeTest {

    @Test
    public void overlapper_A_starter_før_og_slutter_etter_B() {
        // Arrange
        Periode a = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31));
        Periode b = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

        // Act
        boolean overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isTrue();
    }

    @Test
    public void overlapper_B_starter_før_og_slutter_etter_A() {
        // Arrange
        Periode a = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
        Periode b = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31));

        // Act
        boolean overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isTrue();
    }

    @Test
    public void overlapper_når_en_dag_felles_a_starter_før_b() {
        // Arrange
        Periode a = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1));
        Periode b = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

        // Act
        boolean overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isTrue();
    }

    @Test
    public void overlapper_når_en_dag_felles_b_starter_før_a() {
        // Arrange
        Periode a = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
        Periode b = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1));

        // Act
        boolean overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isTrue();
    }

    @Test
    public void overlapper_ikke() {
        // Arrange
        Periode a = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
        Periode b = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));

        // Act
        boolean overlapper = a.overlapper(b);

        // Assert
        assertThat(overlapper).isFalse();
    }
}
