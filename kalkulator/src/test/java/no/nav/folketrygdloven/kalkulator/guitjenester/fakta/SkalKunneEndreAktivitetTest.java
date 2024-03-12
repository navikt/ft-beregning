package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;


public class SkalKunneEndreAktivitetTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private BeregningsgrunnlagDto beregningsgrunnlag;
    private BeregningsgrunnlagPeriodeDto periode;


    @BeforeEach
    public void setUp() {
        beregningsgrunnlag = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).medGrunnbeløp(Beløp.fra(600000)).build();
        periode = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
                .build(beregningsgrunnlag);
    }

    @Test
    public void skalIkkjeKunneEndreAktivitetOmLagtTilAvSaksbehandlerOgDagpenger() {
        BeregningsgrunnlagPrStatusOgAndelDto dagpengeAndel = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.DAGPENGER)
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(dagpengeAndel, false);

        assertThat(skalKunneEndreAktivitet).isFalse();
    }

    @Test
    public void skalIkkjeKunneEndreAktivitetOmIkkjeLagtTilAvSaksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelDto frilans = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(frilans, false);

        assertThat(skalKunneEndreAktivitet).isFalse();
    }

    @Test
    public void skalKunneEndreAktivitetOmLagtTilAvSaksbehandler() {
        BeregningsgrunnlagPrStatusOgAndelDto frilans = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medKilde(AndelKilde.SAKSBEHANDLER_KOFAKBER)
                .build(periode);

        Boolean skalKunneEndreAktivitet = SkalKunneEndreAktivitet.skalKunneEndreAktivitet(frilans, false);

        assertThat(skalKunneEndreAktivitet).isTrue();
    }
}
