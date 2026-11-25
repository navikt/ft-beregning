package no.nav.folketrygdloven.kalkulator.modell.diff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

class DiffForKopieringResultTest {

    @Test
    void skal_gi_diff_for_nytt_listeelement_uavhengig_av_sammenligningsrekkefolge() {
        var eksisterendeInntekt = new TilkommetInntektDto(AktivitetStatus.FRILANSER, null, InternArbeidsforholdRefDto.nullRef());
        var nyInntekt = new TilkommetInntektDto(AktivitetStatus.ARBEIDSTAKER,
            Arbeidsgiver.virksomhet("999999999"), InternArbeidsforholdRefDto.nullRef());
        var utenNyttElement = List.of(eksisterendeInntekt);
        var medNyttElement = List.of(eksisterendeInntekt, nyInntekt);
        var entity1 = utenNyttElement;
        var entity2 = medNyttElement;
        var config = new TraverseGraphConfig();
        config.setIgnoreNulls(false);
        config.setOnlyCheckTrackedFields(true);
        var traverser = new TraverseGraph(config);

        var resultat1 = new DiffForKopieringResult(traverser,
            traverser.traverse(entity1, "liste"), traverser.traverse(entity2, "liste"));

        assertThat(resultat1.getLeafDifferences()).isNotEmpty();
        assertThat(resultat1.isEmpty()).isFalse();

        // Snur diffen og sjekker andre veien
        var resultat2 = new DiffForKopieringResult(traverser,
            traverser.traverse(entity2, "liste"), traverser.traverse(entity1, "liste"));

        assertThat(resultat2.getLeafDifferences()).isNotEmpty();
        assertThat(resultat2.isEmpty()).isFalse();
    }
}
