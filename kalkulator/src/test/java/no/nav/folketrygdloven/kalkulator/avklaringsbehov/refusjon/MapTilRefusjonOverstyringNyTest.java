package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;

class MapTilRefusjonOverstyringNyTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final Arbeidsgiver ARBEIDSGIVER1 = Arbeidsgiver.virksomhet("999999999");
    private static final Arbeidsgiver ARBEIDSGIVER2 = Arbeidsgiver.virksomhet("999999998");

    @Test
    void skal_oppdatere_for_refusjonskrav_og_overlapp_samme_arbeidsgiver() {
        var refusjonFra = SKJÆRINGSTIDSPUNKT.plusMonths(4);
        var dto = new VurderRefusjonBeregningsgrunnlagDto(List.of(lagVurderRefusjonAndel(ARBEIDSGIVER1, refusjonFra, Boolean.TRUE)));

        var resultat = MapTilRefusjonOverstyringNy.map(dto, SKJÆRINGSTIDSPUNKT);

        assertThat(resultat.getRefusjonOverstyringer()).hasSize(1);
        var refusjonOverstyring = resultat.getRefusjonOverstyringer().getFirst();
        assertThat(refusjonOverstyring.getArbeidsgiver()).isEqualTo(ARBEIDSGIVER1);
        assertThat(refusjonOverstyring.getErFristUtvidet()).hasValue(Boolean.TRUE);
        assertThat(refusjonOverstyring.getRefusjonPerioder()).hasSize(1);
        assertThat(refusjonOverstyring.getRefusjonPerioder().getFirst().getStartdatoRefusjon()).isEqualTo(refusjonFra);
    }

    @Test
    void skal_oppdatere_for_refusjonskrav_og_overlapp_forskjellige_arbeidsgivere() {
        var refusjonFra = SKJÆRINGSTIDSPUNKT.plusMonths(4);
        var dto = new VurderRefusjonBeregningsgrunnlagDto(
            List.of(lagVurderRefusjonAndel(ARBEIDSGIVER1, refusjonFra, null), lagVurderRefusjonAndel(ARBEIDSGIVER2, null, true)));

        var resultat = MapTilRefusjonOverstyringNy.map(dto, SKJÆRINGSTIDSPUNKT);

        assertThat(resultat.getRefusjonOverstyringer()).hasSize(2)
            .extracting(BeregningRefusjonOverstyringDto::getArbeidsgiver)
            .contains(ARBEIDSGIVER1, ARBEIDSGIVER2);

        var refusjonOverstyring1 = getRefusjonOverstyringForAG(resultat, ARBEIDSGIVER1);
        assertThat(refusjonOverstyring1.getErFristUtvidet()).isEmpty();
        assertThat(refusjonOverstyring1.getRefusjonPerioder()).hasSize(1);
        assertThat(refusjonOverstyring1.getRefusjonPerioder().getFirst().getStartdatoRefusjon()).isEqualTo(refusjonFra);

        var refusjonOverstyring2 = getRefusjonOverstyringForAG(resultat, ARBEIDSGIVER2);
        assertThat(refusjonOverstyring2.getErFristUtvidet()).hasValue(Boolean.TRUE);
        assertThat(refusjonOverstyring2.getRefusjonPerioder()).isEmpty();
    }

    @Test
    void skal_oppdatere_for_bare_refusjonskrav() {
        var dto = new VurderRefusjonBeregningsgrunnlagDto(List.of(lagVurderRefusjonAndel(ARBEIDSGIVER1, null, Boolean.FALSE)));

        var resultat = MapTilRefusjonOverstyringNy.map(dto, SKJÆRINGSTIDSPUNKT);

        assertThat(resultat.getRefusjonOverstyringer()).hasSize(1);
        var refusjonOverstyring = resultat.getRefusjonOverstyringer().getFirst();
        assertThat(refusjonOverstyring.getArbeidsgiver()).isEqualTo(ARBEIDSGIVER1);
        assertThat(refusjonOverstyring.getErFristUtvidet()).hasValue(Boolean.FALSE);
        assertThat(refusjonOverstyring.getRefusjonPerioder()).isEmpty();
    }

    @Test
    void skal_oppdatere_for_bare_overlapp() {
        var refusjonFra = SKJÆRINGSTIDSPUNKT.plusMonths(2);
        var dto = new VurderRefusjonBeregningsgrunnlagDto(List.of(lagVurderRefusjonAndel(ARBEIDSGIVER1, refusjonFra, null)));

        var resultat = MapTilRefusjonOverstyringNy.map(dto, SKJÆRINGSTIDSPUNKT);

        assertThat(resultat.getRefusjonOverstyringer()).hasSize(1);
        var refusjonOverstyring = resultat.getRefusjonOverstyringer().getFirst();
        assertThat(refusjonOverstyring.getArbeidsgiver()).isEqualTo(ARBEIDSGIVER1);
        assertThat(refusjonOverstyring.getErFristUtvidet()).isEmpty();
        assertThat(refusjonOverstyring.getRefusjonPerioder()).hasSize(1);
        assertThat(refusjonOverstyring.getRefusjonPerioder().getFirst().getStartdatoRefusjon()).isEqualTo(refusjonFra);
    }

    private VurderRefusjonAndelBeregningsgrunnlagDto lagVurderRefusjonAndel(Arbeidsgiver arbeidsgiver,
                                                                            LocalDate refusjonFra,
                                                                            Boolean erFristUtvidet) {
        return new VurderRefusjonAndelBeregningsgrunnlagDto(arbeidsgiver.getOrgnr(), null, null, refusjonFra, null, erFristUtvidet);
    }

    private BeregningRefusjonOverstyringDto getRefusjonOverstyringForAG(BeregningRefusjonOverstyringerDto resultat, Arbeidsgiver arbeidsgiver) {
        return resultat.getRefusjonOverstyringer().stream().filter(r -> Objects.equals(r.getArbeidsgiver(), arbeidsgiver)).findFirst().orElseThrow();
    }
}
