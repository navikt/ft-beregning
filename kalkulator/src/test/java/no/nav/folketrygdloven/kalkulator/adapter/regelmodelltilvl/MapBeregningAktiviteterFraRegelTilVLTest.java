package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

class MapBeregningAktiviteterFraRegelTilVLTest {

    private static final String ORGNR = "900050001";

    @Test
    void mapFrilanserOgArbeidstakerAktiviteter() {
        // Arrange
        var regelmodell = new AktivitetStatusModell();
        var idag = LocalDate.now();
        regelmodell.setSkjæringstidspunktForOpptjening(idag);
        var arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        var a0fom = idag.minusMonths(5);
        var a0tom = idag;
        var frilans = new AktivPeriode(Aktivitet.FRILANSINNTEKT, new Periode(a0fom, a0tom), Arbeidsforhold.frilansArbeidsforhold());
        regelmodell.leggTilEllerOppdaterAktivPeriode(frilans);
        var a1fom = idag.minusMonths(10);
        var a1tom = idag.minusMonths(4);
        var arbeidstaker = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, new Periode(a1fom, a1tom), Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR, arbeidsforholdRef.getReferanse()));
        regelmodell.leggTilEllerOppdaterAktivPeriode(arbeidstaker);

        // Act
        var aktivitetAggregat = MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);

        // Assert
        var beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
        assertThat(beregningAktiviteter).hasSize(2);
        assertThat(beregningAktiviteter.get(0)).satisfies(aktivitet -> {
            assertThat(aktivitet.getArbeidsforholdRef()).isEqualTo(InternArbeidsforholdRefDto.nullRef());
            assertThat(aktivitet.getArbeidsgiver()).isNull();
            assertThat(aktivitet.getOpptjeningAktivitetType()).isEqualTo(OpptjeningAktivitetType.FRILANS);
            assertThat(aktivitet.getPeriode().getFomDato()).isEqualTo(a0fom);
            assertThat(aktivitet.getPeriode().getTomDato()).isEqualTo(a0tom);
        });
        assertThat(beregningAktiviteter.get(1)).satisfies(aktivitet -> {
            assertThat(aktivitet.getArbeidsforholdRef()).isEqualTo(arbeidsforholdRef);
            assertThat(aktivitet.getArbeidsgiver().getErVirksomhet()).isTrue();
            assertThat(aktivitet.getArbeidsgiver().getIdentifikator()).isEqualTo(ORGNR);
            assertThat(aktivitet.getOpptjeningAktivitetType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
            assertThat(aktivitet.getPeriode().getFomDato()).isEqualTo(a1fom);
            assertThat(aktivitet.getPeriode().getTomDato()).isEqualTo(a1tom);
        });
    }
}
