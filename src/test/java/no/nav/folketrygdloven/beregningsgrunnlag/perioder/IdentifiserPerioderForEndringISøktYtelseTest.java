package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGraderingImpl;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Gradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

public class IdentifiserPerioderForEndringISøktYtelseTest {

    @Test
    public void fårSplittForFørstePeriode() {
        // Arrange
        LocalDate fom = LocalDate.now();
        var andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.AT)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
            .medGraderinger(List.of(new Gradering(Periode.of(fom, fom.plusMonths(1)), BigDecimal.valueOf(50))))
            .build();

        // Act
        Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger.identifiser(andelGradering);

        // Assert
        assertThat(periodesplitter).hasSize(1);
        assertThat(periodesplitter.iterator().next().getFom()).isEqualTo(fom);
        assertThat(periodesplitter.iterator().next().getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
    }

    @Test
    public void fårSplittForPerioderMedForskjelligUtbetaling() {
        // Arrange
        LocalDate fom = LocalDate.now();
        Periode p1 = Periode.of(fom, fom.plusMonths(1));
        Periode p2 = Periode.of(fom.plusMonths(1).plusDays(1), fom.plusMonths(2));
        var andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.AT)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
            .medGraderinger(List.of(
                new Gradering(p1, BigDecimal.valueOf(50)),
                new Gradering(p2, BigDecimal.valueOf(40))
            ))
            .build();

        // Act
        Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger.identifiser(andelGradering);

        // Assert
        assertThat(periodesplitter).hasSize(2);
        assertThat(periodesplitter).anySatisfy(splitt -> {
            assertThat(splitt.getFom()).isEqualTo(p1.getFom());
            assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        });
        assertThat(periodesplitter).anySatisfy(splitt -> {
            assertThat(splitt.getFom()).isEqualTo(p2.getFom());
            assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        });
    }

    @Test
    public void fårBareSplittForFørstePeriodeMedLikUtbetaling() {
        // Arrange
        LocalDate fom = LocalDate.now();
        Periode p0 = Periode.of(fom.minusDays(14), fom.minusDays(1));
        Periode p1 = Periode.of(fom, fom.plusMonths(1));
        Periode p2 = Periode.of(fom.plusMonths(1).plusDays(1), fom.plusMonths(2));
        var andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.AT)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
            .medGraderinger(List.of(
                new Gradering(p0, BigDecimal.valueOf(0)),
                new Gradering(p1, BigDecimal.valueOf(50)),
                new Gradering(p2, BigDecimal.valueOf(50))
            ))
            .build();

        // Act
        Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger.identifiser(andelGradering);

        // Assert
        assertThat(periodesplitter).hasSize(1);
        assertThat(periodesplitter).anySatisfy(splitt -> {
            assertThat(splitt.getFom()).isEqualTo(p1.getFom());
            assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        });
    }


    @Test
    public void fårSplittForFlerePerioderMedUlikUtbetaling() {
        // Arrange
        LocalDate fom = LocalDate.now();
        Periode p1 = Periode.of(fom, fom.plusMonths(1));
        Periode p2 = Periode.of(fom.plusMonths(1).plusDays(1), fom.plusMonths(2));
        Periode p3 = Periode.of(fom.plusMonths(2).plusDays(1), fom.plusMonths(3));
        Periode p4 = Periode.of(fom.plusMonths(3).plusDays(1), fom.plusMonths(4));
        Periode p5 = Periode.of(fom.plusMonths(4).plusDays(1), fom.plusMonths(5));
        Periode p6 = Periode.of(fom.plusMonths(4).plusDays(1), fom.plusMonths(5));
        var andelGradering = AndelGraderingImpl.builder()
            .medAktivitetStatus(AktivitetStatusV2.AT)
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123"))
            .medGraderinger(List.of(
                new Gradering(p1, BigDecimal.valueOf(50)),
                new Gradering(p2, BigDecimal.valueOf(50)),
                new Gradering(p3, BigDecimal.valueOf(51)),
                new Gradering(p4, BigDecimal.valueOf(0)),
                new Gradering(p5, BigDecimal.valueOf(100)),
                new Gradering(p6, BigDecimal.valueOf(100))
            ))
            .build();

        // Act
        Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForEndringISøktYtelseSvangerskapspenger.identifiser(andelGradering);

        // Assert
        assertThat(periodesplitter).hasSize(4);
        assertThat(periodesplitter).anySatisfy(splitt -> {
            assertThat(splitt.getFom()).isEqualTo(p1.getFom());
            assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        });
        assertThat(periodesplitter).anySatisfy(splitt -> {
            assertThat(splitt.getFom()).isEqualTo(p3.getFom());
            assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        });
        assertThat(periodesplitter).anySatisfy(splitt -> {
            assertThat(splitt.getFom()).isEqualTo(p4.getFom());
            assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        });
        assertThat(periodesplitter).anySatisfy(splitt -> {
            assertThat(splitt.getFom()).isEqualTo(p5.getFom());
            assertThat(splitt.getPeriodeÅrsak()).isEqualTo(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        });
    }

}
