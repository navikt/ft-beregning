package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

class AvklaringsbehovutlederVurderRefusjonTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2024, 1, 1);
    private static final Beløp GRUNNBELØP = Beløp.fra(125000);
    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("974760673");

    // Delvis revurdering av en periode som ligger etter skjæringstidspunktet.
    private static final Intervall FORLENGELSE_ETTER_STP = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.plusMonths(6),
        SKJÆRINGSTIDSPUNKT.plusMonths(7));
    // Delvis revurdering av en periode som i sin helhet ligger før skjæringstidspunktet.
    private static final Intervall FORLENGELSE_FØR_STP = Intervall.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(2),
        SKJÆRINGSTIDSPUNKT.minusMonths(1));

    @Test
    void delvis_revurdering_etter_stp_skal_gi_ap_naar_koblingen_tidligere_var_refusjonsvurdert() {
        var input = lagInput(List.of(FORLENGELSE_ETTER_STP), lagForrigeGrunnlag(true));

        var resultat = AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, lagBeregningsgrunnlag());

        assertThat(resultat).isTrue();
    }

    @Test
    void delvis_revurdering_etter_stp_skal_ikke_gi_ap_naar_koblingen_ikke_var_refusjonsvurdert() {
        var input = lagInput(List.of(FORLENGELSE_ETTER_STP), lagForrigeGrunnlag(false));

        var resultat = AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, lagBeregningsgrunnlag());

        assertThat(resultat).isFalse();
    }

    @Test
    void full_revurdering_uten_forlengelse_skal_ikke_gi_ap_via_ny_utledning() {
        var input = lagInput(List.of(), lagForrigeGrunnlag(true));

        var resultat = AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, lagBeregningsgrunnlag());

        assertThat(resultat).isFalse();
    }

    @Test
    void forlengelse_som_ligger_helt_foer_stp_skal_ikke_gi_ap() {
        var input = lagInput(List.of(FORLENGELSE_FØR_STP), lagForrigeGrunnlag(true));

        var resultat = AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, lagBeregningsgrunnlag());

        assertThat(resultat).isFalse();
    }

    private VurderRefusjonBeregningsgrunnlagInput lagInput(List<Intervall> forlengelseperioder, BeregningsgrunnlagGrunnlagDto forrigeGrunnlag) {
        var koblingReferanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT, FagsakYtelseType.PLEIEPENGER_SYKT_BARN);
        var baseInput = new BeregningsgrunnlagInput(koblingReferanse, null, null, List.of(), null);
        baseInput.setForlengelseperioder(forlengelseperioder);
        var stegInput = new StegProsesseringInput(baseInput, BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
        return new VurderRefusjonBeregningsgrunnlagInput(stegInput)
            .medBeregningsgrunnlagGrunnlagFraForrigeBehandling(List.of(forrigeGrunnlag));
    }

    private BeregningsgrunnlagGrunnlagDto lagForrigeGrunnlag(boolean medRefusjonOverstyring) {
        var builder = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
            .medBeregningsgrunnlag(lagBeregningsgrunnlag());
        if (medRefusjonOverstyring) {
            var overstyring = new BeregningRefusjonOverstyringDto(ARBEIDSGIVER, SKJÆRINGSTIDSPUNKT, List.of(), false);
            builder.medRefusjonOverstyring(BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(overstyring).build());
        }
        return builder.build(BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        return BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER))
            .build();
    }
}
