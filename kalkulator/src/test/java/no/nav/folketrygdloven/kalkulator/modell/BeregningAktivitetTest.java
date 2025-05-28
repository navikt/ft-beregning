package no.nav.folketrygdloven.kalkulator.modell;


import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class BeregningAktivitetTest {


    private static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_REF = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.fra(AktørId.dummy());
    private static final Intervall PERIODE = Intervall.fraOgMedTilOgMed(LocalDate.of(2019, 1, 1), TIDENES_ENDE);

    @Test
    void skal_bruke_aktivitet_om_ingen_overstyringer_for_aktivitet() {
        // Arrange
	    var overstyringer = lagOverstyring(1);
	    var aktivitet = BeregningAktivitetDto.builder()
            .medPeriode(PERIODE)
            .medArbeidsforholdRef(InternArbeidsforholdRefDto.nyRef())
            .medArbeidsgiver(ARBEIDSGIVER)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .build();

        // Act
	    var skalBrukes = aktivitet.skalBrukes(overstyringer);

        // Assert
        assertThat(skalBrukes).isTrue();
    }

    @Test
    void skal_ikkje_bruke_aktivitet_om__det_finnes_overstyringer_for_aktivitet() {
        // Arrange
	    var overstyringer = lagOverstyring(1);
	    var aktivitet = BeregningAktivitetDto.builder()
            .medPeriode(PERIODE)
            .medArbeidsforholdRef(ARBEIDSFORHOLD_REF)
            .medArbeidsgiver(ARBEIDSGIVER)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .build();

        // Act
	    var skalBrukes = aktivitet.skalBrukes(overstyringer);

        // Assert
        assertThat(skalBrukes).isFalse();
    }

    @Test
    void skal_kaste_exception_om_det_finnes_flere_overstyringer_for_aktivitet() {
        // Arrange
	    var overstyringer = lagOverstyring(2);
	    var aktivitet = BeregningAktivitetDto.builder()
            .medPeriode(PERIODE)
            .medArbeidsforholdRef(ARBEIDSFORHOLD_REF)
            .medArbeidsgiver(ARBEIDSGIVER)
            .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
            .build();

        // Assert
        // Act
        Assertions.assertThrows(IllegalStateException.class, () -> {
            aktivitet.skalBrukes(overstyringer);
        });
    }

    private BeregningAktivitetOverstyringerDto lagOverstyring(int antallOverstyringer) {
	    var builder = BeregningAktivitetOverstyringerDto.builder();
        for (var i = 0; i < antallOverstyringer; i++) {
	        var overstyring = BeregningAktivitetOverstyringDto.builder()
                .medHandling(BeregningAktivitetHandlingType.IKKE_BENYTT)
                .medArbeidsforholdRef(ARBEIDSFORHOLD_REF)
                .medArbeidsgiver(ARBEIDSGIVER)
                .medPeriode(PERIODE)
                .medOpptjeningAktivitetType(OpptjeningAktivitetType.ARBEID)
                .build();
            builder.leggTilOverstyring(overstyring);
        }
        return builder.build();
    }
}
