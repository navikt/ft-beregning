package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.BeregningsaktivitetLagreDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class SaksbehandletBeregningsaktivitetTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    @Test
    public void lagSaksbehandletVersjon_fjerner_ingen_aktiviteter() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT);
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        BeregningAktivitetDto arbeid = BeregningAktivitetDto.builder()
            .medPeriode(periode)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medArbeidsgiver(Arbeidsgiver.person(AktørId.dummy()))
            .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef())
            .build();
        builder.leggTilAktivitet(arbeid);

        BeregningAktivitetDto næring = BeregningAktivitetDto.builder()
            .medPeriode(periode)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.NÆRING)
            .build();
        builder.leggTilAktivitet(næring);
        BeregningAktivitetAggregatDto register = builder.build();
        BeregningsaktivitetLagreDto næringDto = mapTilDto(næring, true);

        // Act
        BeregningAktivitetAggregatDto saksbehandlet = SaksbehandletBeregningsaktivitetTjeneste.lagSaksbehandletVersjon(register, List.of(næringDto));

        // Assert
        assertThat(saksbehandlet.getBeregningAktiviteter()).hasSize(2);
    }

    @Test
    public void lagSaksbehandletVersjon_fjerner_en_aktivitet() {
        // Arrange
        Intervall periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT);
        BeregningAktivitetAggregatDto.Builder builder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        BeregningAktivitetDto arbeid = BeregningAktivitetDto.builder()
            .medPeriode(periode)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medArbeidsgiver(Arbeidsgiver.person(AktørId.dummy()))
            .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef())
            .build();
        builder.leggTilAktivitet(arbeid);

        BeregningAktivitetDto næring = BeregningAktivitetDto.builder()
            .medPeriode(periode)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.NÆRING)
            .build();
        builder.leggTilAktivitet(næring);
        BeregningAktivitetAggregatDto register = builder.build();
        BeregningsaktivitetLagreDto næringDto = mapTilDto(næring, false);

        // Act
        BeregningAktivitetAggregatDto saksbehandlet = SaksbehandletBeregningsaktivitetTjeneste.lagSaksbehandletVersjon(register, List.of(næringDto));

        // Assert
        assertThat(saksbehandlet.getBeregningAktiviteter()).hasSize(1);
        assertThat(saksbehandlet.getBeregningAktiviteter()).anySatisfy(ba ->
            assertThat(ba).isEqualTo(arbeid));
    }

    private BeregningsaktivitetLagreDto mapTilDto(BeregningAktivitetDto beregningAktivitet, boolean skalBrukes) {
        BeregningsaktivitetLagreDto.Builder builder = BeregningsaktivitetLagreDto.builder()
            .medOpptjeningAktivitetType(beregningAktivitet.getOpptjeningAktivitetType())
            .medFom(beregningAktivitet.getPeriode().getFomDato())
            .medTom(beregningAktivitet.getPeriode().getTomDato())
            .medArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef().getReferanse())
            .medSkalBrukes(skalBrukes);
        Arbeidsgiver arbeidsgiver = beregningAktivitet.getArbeidsgiver();
        if (arbeidsgiver != null) {
            if (arbeidsgiver.getErVirksomhet()) {
                builder.medOppdragsgiverOrg(arbeidsgiver.getOrgnr());
            } else {
                builder.medArbeidsgiverIdentifikator(arbeidsgiver.getAktørId().getId());
            }
        }
        return builder.build();
    }
}
