package no.nav.folketrygdloven.kalkulator.modell;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.SammenligningsgrunnlagPrStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

public class SammenligningsgrunnlagTest {
    private static final BigDecimal RAPPORTERT_PR_ÅR = BigDecimal.valueOf(400000d);
    private static final BigDecimal AVVIK_PROMILLE = BigDecimal.valueOf(240L);
    private final LocalDate PERIODE_FOM = LocalDate.now();
    private final LocalDate PERIODE_TOM = LocalDate.now().plusWeeks(6);
    private static SammenligningsgrunnlagType SAMMENLIGNING_TYPE = SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL;

    private SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag;

    @BeforeEach
    public void setup() {
        sammenligningsgrunnlag = lagSammenligningsgrunnlagBuilder().build();
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeFom()).isEqualTo(PERIODE_FOM);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeTom()).isEqualTo(PERIODE_TOM);
        assertThat(sammenligningsgrunnlag.getRapportertPrÅr()).isEqualTo(Beløp.fra(RAPPORTERT_PR_ÅR));
        assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagType()).isEqualTo(SAMMENLIGNING_TYPE);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagBuilder = SammenligningsgrunnlagPrStatusDto.builder();
        try {
            sammenligningsgrunnlagBuilder.build();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("sammenligningsgrunnlagType");
        }
        try {
            sammenligningsgrunnlagBuilder.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL).build();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("sammenligningsperiodePeriode");
        }
        try {
            sammenligningsgrunnlagBuilder.medSammenligningsperiode(PERIODE_FOM, null).build();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Til og med dato må være satt");
        }
        try {
            sammenligningsgrunnlagBuilder.medSammenligningsperiode(PERIODE_FOM, PERIODE_TOM).build();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("rapportertPrÅr");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        assertThat(sammenligningsgrunnlag).isNotEqualTo(null);
        assertThat(sammenligningsgrunnlag).isNotEqualTo("blabla");
        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlagBuilder().build();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag2).isEqualTo(sammenligningsgrunnlag);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());
        assertThat(sammenligningsgrunnlag2.hashCode()).isEqualTo(sammenligningsgrunnlag.hashCode());

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medSammenligningsperiode(LocalDate.now().minusDays(1), PERIODE_TOM);
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build();
        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag2).isNotEqualTo(sammenligningsgrunnlag);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
        assertThat(sammenligningsgrunnlag2.hashCode()).isNotEqualTo(sammenligningsgrunnlag.hashCode());
    }

    @Test
    public void skal_bruke_sammenligningsgrunnlagFom_i_equalsOgHashCode() {
        SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlagBuilder().build();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medSammenligningsperiode(LocalDate.now().minusDays(1), PERIODE_TOM);
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build();

        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
    }

    @Test
    public void skal_bruke_sammenligningsgrunnlagTom_i_equalsOgHashCode() {
        SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlagBuilder().build();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medSammenligningsperiode(PERIODE_FOM, LocalDate.now().plusWeeks(5));
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build();

        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
    }

    @Test
    public void skal_bruke_rapportertPrÅr_i_equalsOgHashCode() {
        SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlagBuilder().build();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medRapportertPrÅr(Beløp.fra(RAPPORTERT_PR_ÅR.add(BigDecimal.valueOf(1))));
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build();

        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
    }

    @Test
    public void skal_bruke_sammenligningtype_i_equalsOgHashCode() {
        SammenligningsgrunnlagPrStatusDto sammenligningsgrunnlag2 = lagSammenligningsgrunnlagBuilder().build();

        assertThat(sammenligningsgrunnlag).isEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isEqualTo(sammenligningsgrunnlag2.hashCode());

        SammenligningsgrunnlagPrStatusDto.Builder sammenligningsgrunnlagBuilder = lagSammenligningsgrunnlagBuilder();
        sammenligningsgrunnlagBuilder.medSammenligningsgrunnlagType(SammenligningsgrunnlagType.SAMMENLIGNING_SN);
        sammenligningsgrunnlag2 = sammenligningsgrunnlagBuilder.build();

        assertThat(sammenligningsgrunnlag).isNotEqualTo(sammenligningsgrunnlag2);
        assertThat(sammenligningsgrunnlag.hashCode()).isNotEqualTo(sammenligningsgrunnlag2.hashCode());
    }


    private SammenligningsgrunnlagPrStatusDto.Builder lagSammenligningsgrunnlagBuilder() {
        return SammenligningsgrunnlagPrStatusDto.builder()
            .medSammenligningsgrunnlagType(SAMMENLIGNING_TYPE)
            .medSammenligningsperiode(PERIODE_FOM, PERIODE_TOM)
            .medRapportertPrÅr(Beløp.fra(RAPPORTERT_PR_ÅR))
            .medAvvikPromilleNy(AVVIK_PROMILLE);
    }
}
