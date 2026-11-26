package no.nav.folketrygdloven.kalkulator.modell.diff;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class DiffForKopieringResultTest {

    @Test
    void skal_gi_diff_for_nytt_listeelement_med_kun_nullfelter_uavhengig_av_sammenligningsrekkefolge() {
        var eksisterendeElement = new DummyRecord(5);
        var nyttElement = new DummyRecord(null);
        var utenNyttElement = List.of(eksisterendeElement);
        var medNyttElement = List.of(eksisterendeElement, nyttElement);
        var entity1 = utenNyttElement;
        var entity2 = medNyttElement;
        var config = new TraverseGraphConfig();
        config.setIgnoreNulls(false);
        config.setOnlyCheckTrackedFields(true);
        var traverser = new TraverseGraph(config);

        var resultat1 = new DiffForKopieringResult(traverser, traverser.traverse(entity1, "liste"), traverser.traverse(entity2, "liste"));

        assertThat(resultat1.getLeafDifferences()).isNotEmpty();
        assertThat(resultat1.isEmpty()).isFalse();

        // Snur diffen og sjekker andre veien
        var resultat2 = new DiffForKopieringResult(traverser, traverser.traverse(entity2, "liste"), traverser.traverse(entity1, "liste"));

        assertThat(resultat2.getLeafDifferences()).isNotEmpty();
        assertThat(resultat2.isEmpty()).isFalse();
    }


    public record DummyRecord(@SjekkVedKopiering Integer dummyField) {
    }

}
