package no.nav.folketrygdloven.kalkulator.steg.besteberegning;


import no.nav.folketrygdloven.besteberegning.modell.input.YtelseAktivitetType;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelsegrunnlagAndel;
import no.nav.folketrygdloven.kalkulator.modell.besteberegning.Ytelseandel;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Arbeidskategori;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapTilBesteberegningRegelmodellTest {

    @Test
    public void skal_mappe_ytelse_andel() {
        //Arrange
        Ytelseandel ytelseandelArbeidsKategori = new Ytelseandel(Arbeidskategori.INAKTIV, 100L);
        Ytelseandel ytelseandelAktivitetsstatus = new Ytelseandel(AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER,250L);
        //Act
        List<YtelsegrunnlagAndel> grunnlagsandeler = MapTilBesteberegningRegelmodell.mapYtelseandeler(List.of(ytelseandelAktivitetsstatus, ytelseandelArbeidsKategori));
        //Assert
        assertThat(grunnlagsandeler.size()).isEqualTo(2);
        assertThat(grunnlagsandeler.get(0).getAktivitet()).isEqualTo(YtelseAktivitetType.YTELSE_FOR_ARBEID);
        assertThat(grunnlagsandeler.get(1).getAktivitet()).isEqualTo(YtelseAktivitetType.YTELSE_FOR_DAGPENGER);
    }
}
