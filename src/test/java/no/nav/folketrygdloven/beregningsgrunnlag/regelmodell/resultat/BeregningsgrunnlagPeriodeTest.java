package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;

public class BeregningsgrunnlagPeriodeTest {

    /*
    Rekkefølge for beregning av BeregningsgrunnlagPrStatus er viktig pga avhengigheter. Denne testen tester at status MS, SN
     og ATFL_SN returneres sist.
     */

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

    @Test
    void skal_teste_at_aktivitetstatuser_MS_og_SN_og_ATFLSN_returneres_sist() {
        //Arrange
        List<AktivitetStatusMedHjemmel> alleStatuser = Stream.of(AktivitetStatus.values())
                .map(as -> new AktivitetStatusMedHjemmel(as, null))
                .collect(Collectors.toList());
        BeregningsgrunnlagPeriode bgPeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null))
            .build();
        Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medAktivitetStatuser(alleStatuser)
            .medBeregningsgrunnlagPeriode(bgPeriode)
            .medGrunnbeløpSatser(List.of(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), 90000L, 90000L)))
            .build();
        //Act
        List<AktivitetStatusMedHjemmel> aktivitetStatuser = bgPeriode.getAktivitetStatuser();
        //Assert
        List<AktivitetStatus> treSisteStatuser = aktivitetStatuser.stream()
            .skip(aktivitetStatuser.size()-3)
            .map(AktivitetStatusMedHjemmel::getAktivitetStatus)
            .collect(Collectors.toList());
        assertThat(treSisteStatuser).containsExactlyInAnyOrder(AktivitetStatus.MS, AktivitetStatus.SN, AktivitetStatus.ATFL_SN);
    }

}
