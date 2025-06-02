package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

class FinnAndelerMedØktRefusjonTjenesteTest {
    List<RefusjonAndel> originaleAndeler = new ArrayList<>();
    List<RefusjonAndel> revurderingAndeler = new ArrayList<>();

    @Test
    void aggregat_til_spesifikk_samme_refusjon() {
        lagRefusjonandel(originaleAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(500000));
        lagRefusjonandel(revurderingAndeler, InternArbeidsforholdRefDto.nyRef(), BigDecimal.valueOf(500000));
        var resultat = kjørTjeneste();
        assertThat(resultat).isEmpty();
    }

    @Test
    void spesifikk_til_aggregat_økt_refusjon() {
        lagRefusjonandel(originaleAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(500000));
        lagRefusjonandel(revurderingAndeler, InternArbeidsforholdRefDto.nyRef(), BigDecimal.valueOf(500001));
        var resultat = kjørTjeneste();
        assertThat(resultat).hasSize(1);
        assertThat(resultat).containsAll(revurderingAndeler);
    }

    @Test
    void bare_spesifikk_andeler_økt_refusjon_på_en() {
        var ref1 = InternArbeidsforholdRefDto.nyRef();
        var ref2 = InternArbeidsforholdRefDto.nyRef();
        lagRefusjonandel(originaleAndeler, ref1, BigDecimal.valueOf(200000));
        lagRefusjonandel(originaleAndeler, ref2, BigDecimal.valueOf(100000));
        lagRefusjonandel(revurderingAndeler, ref1, BigDecimal.valueOf(200000));
        var andel = lagRefusjonandel(revurderingAndeler, ref2, BigDecimal.valueOf(150000));
        var resultat = kjørTjeneste();
        assertThat(resultat).hasSize(1);
        assertThat(resultat).containsExactly(andel);
    }

    @Test
    void spesifikke_til_aggregat_økt_refusjon() {
        var ref1 = InternArbeidsforholdRefDto.nyRef();
        var ref2 = InternArbeidsforholdRefDto.nyRef();
        lagRefusjonandel(originaleAndeler, ref1, BigDecimal.valueOf(200000));
        lagRefusjonandel(originaleAndeler, ref2, BigDecimal.valueOf(100000));
        lagRefusjonandel(revurderingAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(400000));
        var resultat = kjørTjeneste();
        assertThat(resultat).hasSize(1);
        assertThat(resultat).containsAll(revurderingAndeler);
    }

    @Test
    void spesifikke_til_blanding_økt_refusjon() {
        var ref1 = InternArbeidsforholdRefDto.nyRef();
        var ref2 = InternArbeidsforholdRefDto.nyRef();
        lagRefusjonandel(originaleAndeler, ref1, BigDecimal.valueOf(200000));
        lagRefusjonandel(originaleAndeler, ref2, BigDecimal.valueOf(100000));
        lagRefusjonandel(revurderingAndeler, ref1, BigDecimal.valueOf(200000));
        var andel1 = lagRefusjonandel(revurderingAndeler, ref2, BigDecimal.valueOf(150000));
        var andel2 = lagRefusjonandel(revurderingAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(100000));
        var resultat = kjørTjeneste();
        assertThat(resultat).hasSize(2);
        assertThat(resultat).containsAll(Arrays.asList(andel1, andel2));
    }

    @Test
    void blanding_til_spesifikke_økt_refusjon_for_en_spesifikk() {
        var ref1 = InternArbeidsforholdRefDto.nyRef();
        var ref2 = InternArbeidsforholdRefDto.nyRef();
        lagRefusjonandel(originaleAndeler, ref1, BigDecimal.valueOf(200000));
        lagRefusjonandel(originaleAndeler, ref2, BigDecimal.valueOf(100000));
        lagRefusjonandel(originaleAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(100000));
        lagRefusjonandel(revurderingAndeler, ref1, BigDecimal.valueOf(200000));
        lagRefusjonandel(revurderingAndeler, ref2, BigDecimal.valueOf(100000));
        var andel = lagRefusjonandel(revurderingAndeler, InternArbeidsforholdRefDto.nyRef(), BigDecimal.valueOf(150000));
        var resultat = kjørTjeneste();
        assertThat(resultat).hasSize(1);
        assertThat(resultat).containsExactly(andel);
    }

    @Test
    void blanding_til_spesifikke_økt_refusjon_for_to_andeler() {
        var ref1 = InternArbeidsforholdRefDto.nyRef();
        var ref2 = InternArbeidsforholdRefDto.nyRef();
        lagRefusjonandel(originaleAndeler, ref1, BigDecimal.valueOf(200000));
        lagRefusjonandel(originaleAndeler, ref2, BigDecimal.valueOf(100000));
        lagRefusjonandel(originaleAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(100000));
        lagRefusjonandel(revurderingAndeler, ref1, BigDecimal.valueOf(200000));
        var andel1 = lagRefusjonandel(revurderingAndeler, ref2, BigDecimal.valueOf(150000));
        var andel2 = lagRefusjonandel(revurderingAndeler, InternArbeidsforholdRefDto.nyRef(), BigDecimal.valueOf(150000));
        var resultat = kjørTjeneste();
        assertThat(resultat).hasSize(2);
        assertThat(resultat).containsAll(Arrays.asList(andel1, andel2));
    }

    @Test
    void blanding_til_blanding_økt_refusjon_for_andel_uten_ref() {
        var ref1 = InternArbeidsforholdRefDto.nyRef();
        lagRefusjonandel(originaleAndeler, ref1, BigDecimal.valueOf(200000));
        lagRefusjonandel(originaleAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(100000));
        var andel1 = lagRefusjonandel(revurderingAndeler, ref1, BigDecimal.valueOf(250000));
        var andel2 = lagRefusjonandel(revurderingAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(150000));
        var resultat = kjørTjeneste();
        assertThat(resultat).hasSize(2);
        assertThat(resultat).containsAll(Arrays.asList(andel1, andel2));
    }

    @Test
    void blanding_til_blanding_med_ny_andel_økt_refusjon_for_andel_uten_ref() {
        var ref1 = InternArbeidsforholdRefDto.nyRef();
        var ref2 = InternArbeidsforholdRefDto.nyRef();
        lagRefusjonandel(originaleAndeler, ref1, BigDecimal.valueOf(200000));
        lagRefusjonandel(originaleAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(100000));
        lagRefusjonandel(revurderingAndeler, ref1, BigDecimal.valueOf(200000));
        var andel1 = lagRefusjonandel(revurderingAndeler, ref2, BigDecimal.valueOf(250000));
        var andel2 = lagRefusjonandel(revurderingAndeler, InternArbeidsforholdRefDto.nullRef(), BigDecimal.valueOf(150000));
        var resultat = kjørTjeneste();
        assertThat(resultat).hasSize(2);
        assertThat(resultat).containsAll(Arrays.asList(andel2, andel1));
    }



    private List<RefusjonAndel> kjørTjeneste() {
        return FinnAndelerMedØktRefusjonTjeneste.finnAndelerPåSammeNøkkelMedØktRefusjon(revurderingAndeler, originaleAndeler);
    }

    private RefusjonAndel lagRefusjonandel(List<RefusjonAndel> listeAndelenLeggesI, InternArbeidsforholdRefDto referanse, BigDecimal refusjon) {
        // Brutto har ikke noe å si for logikken denne klassen skal teste, setter til 0
        // Arbeidsgiver skal alltid være lik for alle andeler når denne koden kjøres, hardkoder den.
        var andel = new RefusjonAndel(AktivitetStatus.ARBEIDSTAKER, Arbeidsgiver.virksomhet("999999999"), referanse, Beløp.ZERO, Beløp.fra(refusjon));
        listeAndelenLeggesI.add(andel);
        return andel;
    }

}
