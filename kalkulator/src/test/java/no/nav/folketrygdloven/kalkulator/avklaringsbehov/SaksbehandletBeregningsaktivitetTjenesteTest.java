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

class SaksbehandletBeregningsaktivitetTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    @Test
    void lagSaksbehandletVersjon_fjerner_ingen_aktiviteter() {
        // Arrange
        var periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT);
        var builder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        var arbeid = BeregningAktivitetDto.builder()
            .medPeriode(periode)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medArbeidsgiver(Arbeidsgiver.person(AktørId.dummy()))
            .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef())
            .build();
        builder.leggTilAktivitet(arbeid);

        var næring = BeregningAktivitetDto.builder()
            .medPeriode(periode)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.NÆRING)
            .build();
        builder.leggTilAktivitet(næring);
        var register = builder.build();
        var næringDto = mapTilDto(næring, true);

        // Act
        var saksbehandlet = SaksbehandletBeregningsaktivitetTjeneste.lagSaksbehandletVersjon(register, List.of(næringDto));

        // Assert
        assertThat(saksbehandlet.getBeregningAktiviteter()).hasSize(2);
    }

    @Test
    void lagSaksbehandletVersjon_fjerner_en_aktivitet() {
        // Arrange
        var periode = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT);
        var builder = BeregningAktivitetAggregatDto.builder()
            .medSkjæringstidspunktOpptjening(SKJÆRINGSTIDSPUNKT);
        var arbeid = BeregningAktivitetDto.builder()
            .medPeriode(periode)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .medArbeidsgiver(Arbeidsgiver.person(AktørId.dummy()))
            .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef())
            .build();
        builder.leggTilAktivitet(arbeid);

        var næring = BeregningAktivitetDto.builder()
            .medPeriode(periode)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.NÆRING)
            .build();
        builder.leggTilAktivitet(næring);
        var register = builder.build();
        var næringDto = mapTilDto(næring, false);

        // Act
        var saksbehandlet = SaksbehandletBeregningsaktivitetTjeneste.lagSaksbehandletVersjon(register, List.of(næringDto));

        // Assert
        assertThat(saksbehandlet.getBeregningAktiviteter()).hasSize(1);
        assertThat(saksbehandlet.getBeregningAktiviteter()).anySatisfy(ba ->
            assertThat(ba).isEqualTo(arbeid));
    }

    private BeregningsaktivitetLagreDto mapTilDto(BeregningAktivitetDto beregningAktivitet, boolean skalBrukes) {
        var builder = BeregningsaktivitetLagreDto.builder()
            .medOpptjeningAktivitetType(beregningAktivitet.getOpptjeningAktivitetType())
            .medFom(beregningAktivitet.getPeriode().getFomDato())
            .medTom(beregningAktivitet.getPeriode().getTomDato())
            .medArbeidsforholdRef(beregningAktivitet.getArbeidsforholdRef().getReferanse())
            .medSkalBrukes(skalBrukes);
        var arbeidsgiver = beregningAktivitet.getArbeidsgiver();
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
