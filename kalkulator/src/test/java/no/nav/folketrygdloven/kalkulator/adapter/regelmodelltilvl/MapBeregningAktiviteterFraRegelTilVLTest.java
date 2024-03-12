package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public class MapBeregningAktiviteterFraRegelTilVLTest {

    private static final String ORGNR = "900050001";

    @Test
    public void mapFrilanserOgArbeidstakerAktiviteter() {
        // Arrange
        AktivitetStatusModell regelmodell = new AktivitetStatusModell();
        LocalDate idag = LocalDate.now();
        regelmodell.setSkjæringstidspunktForOpptjening(idag);
        var arbeidsforholdRef = InternArbeidsforholdRefDto.nyRef();
        LocalDate a0fom = idag.minusMonths(5);
        LocalDate a0tom = idag;
        AktivPeriode frilans = new AktivPeriode(Aktivitet.FRILANSINNTEKT, new Periode(a0fom, a0tom), Arbeidsforhold.frilansArbeidsforhold());
        regelmodell.leggTilEllerOppdaterAktivPeriode(frilans);
        LocalDate a1fom = idag.minusMonths(10);
        LocalDate a1tom = idag.minusMonths(4);
        AktivPeriode arbeidstaker = new AktivPeriode(Aktivitet.ARBEIDSTAKERINNTEKT, new Periode(a1fom, a1tom), Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR, arbeidsforholdRef.getReferanse()));
        regelmodell.leggTilEllerOppdaterAktivPeriode(arbeidstaker);

        // Act
        BeregningAktivitetAggregatDto aktivitetAggregat = MapBeregningAktiviteterFraRegelTilVL.map(regelmodell);

        // Assert
        List<BeregningAktivitetDto> beregningAktiviteter = aktivitetAggregat.getBeregningAktiviteter();
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
