package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon.PeriodiserOgFastsettRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class PeriodiserOgFastsettRefusjonTjenesteTest {
    private static final Arbeidsgiver AG1 = Arbeidsgiver.virksomhet("999999999");
    private static final Arbeidsgiver AG2 = Arbeidsgiver.virksomhet("999999998");
    private static final Arbeidsgiver AG3 = Arbeidsgiver.virksomhet("999999997");

    private static final InternArbeidsforholdRefDto REF1 = InternArbeidsforholdRefDto.nyRef();
    private static final InternArbeidsforholdRefDto REF2 = InternArbeidsforholdRefDto.nyRef();
    private static final InternArbeidsforholdRefDto REF3 = InternArbeidsforholdRefDto.nyRef();
    private static final LocalDate STP = LocalDate.now();
    private static final LocalDate UENDELIG = TIDENES_ENDE;

    private BeregningsgrunnlagDto.Builder grunnlagBuilder = BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(STP);
    private List<VurderRefusjonAndelBeregningsgrunnlagDto> saksbehandlerAvklaringer = new ArrayList<>();
    @Test
    public void skal_ikke_splitte_noe_hvis_refusjon_aggregat_er_tomt() {
        lagBGPeriode(STP, null, lagBGAndel(AG1, REF1, 0));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
    }

    @Test
    public void skal_splitte_når_en_periode_med_en_andel_finnes_uten_refusjon() {
        lagBGPeriode(STP, null, lagBGAndel(AG1, REF1, 0));
        lagSaksbehandlerDto(AG1, REF1, dagerEtterSTP(10));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertPeriode(STP, dagerEtterSTP(9), AG1, REF1, 0, resultat, 1);
        assertPeriode(dagerEtterSTP(10), UENDELIG, AG1, REF1, 0, resultat, 1, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }

    @Test
    public void skal_ikke_splitte_når_dato_er_stp() {
        lagBGPeriode(STP, UENDELIG, lagBGAndel(AG1, REF1, 250000));
        lagSaksbehandlerDto(AG1, REF1, STP);

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertPeriode(STP, UENDELIG, AG1, REF1, 250000, resultat, 1);
    }


    @Test
    public void skal_kopiere_med_tidligere_andeler_som_ikke_er_arbeidstakerandeler() {
        lagBGPeriode(STP, null, lagBGAndel(AG1, REF1, 150000), lagBGAndel(null, null, 0));
        lagSaksbehandlerDto(AG1, REF1, dagerEtterSTP(10));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertPeriode(STP, dagerEtterSTP(9), AG1, REF1, 0, resultat, 2);
        assertPeriode(dagerEtterSTP(10), UENDELIG, AG1, REF1, 150000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }

    @Test
    public void skal_splitte_når_en_periode_med_en_andel_finnes_med_refusjon() {
        lagBGPeriode(STP, UENDELIG, lagBGAndel(AG1, REF1, 500000));
        lagSaksbehandlerDto(AG1, REF1, dagerEtterSTP(10));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertPeriode(STP, dagerEtterSTP(9), AG1, REF1, 0, resultat, 1);
        assertPeriode(dagerEtterSTP(10), UENDELIG, AG1, REF1, 500000, resultat, 1, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }

    @Test
    public void skal_legge_til_periodeårsak_når_splitt_finnes_fra_før() {
        lagBGPeriode(STP, dagerEtterSTP(10), lagBGAndel(AG1, REF1, 500000));
        lagBGPeriode(dagerEtterSTP(11), UENDELIG, PeriodeÅrsak.NATURALYTELSE_BORTFALT, lagBGAndel(AG1, REF1, 500000));
        lagSaksbehandlerDto(AG1, REF1, dagerEtterSTP(11));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);
        assertPeriode(STP, dagerEtterSTP(10), AG1, REF1, 0, resultat, 1);
        assertPeriode(dagerEtterSTP(11), UENDELIG, AG1, REF1, 500000, resultat, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }

    @Test
    public void skal_splitte_når_en_periode_med_to_andeler_finnes_kun_en_med_overstyring() {
        lagBGPeriode(STP, null, lagBGAndel(AG1, REF1, 500000), lagBGAndel(AG2, REF2, 100000));
        lagSaksbehandlerDto(AG2, REF2, dagerEtterSTP(15));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);

        assertPeriode(STP, dagerEtterSTP(14), AG1, REF1, 500000, resultat, 2);
        assertPeriode(STP, dagerEtterSTP(14), AG2, REF2, 0, resultat, 2);

        assertPeriode(dagerEtterSTP(15), UENDELIG, AG1, REF1, 500000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(15), UENDELIG, AG2, REF2, 100000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }

    @Test
    public void skal_splitte_når_en_periode_med_to_andeler_finnes_med_hver_sin_overstyring() {
        lagBGPeriode(STP, null, lagBGAndel(AG1, REF1, 500000), lagBGAndel(AG2, REF2, 100000));
        lagSaksbehandlerDto(AG1, REF1, dagerEtterSTP(10));
        lagSaksbehandlerDto(AG2, REF2, dagerEtterSTP(15));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(3);

        assertPeriode(STP, dagerEtterSTP(9), AG1, REF1, 0, resultat, 2);
        assertPeriode(STP, dagerEtterSTP(9), AG2, REF2, 0, resultat, 2);

        assertPeriode(dagerEtterSTP(10), dagerEtterSTP(14), AG1, REF1, 500000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(10), dagerEtterSTP(14), AG2, REF2, 0, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);

        assertPeriode(dagerEtterSTP(15), UENDELIG, AG1, REF1, 500000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(15), UENDELIG, AG2, REF2, 100000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }

    @Test
    public void skal_splitte_grunnlag_når_3_perioder_finnes_fra_før_og_samme_ag_med_2_ref_overstyres() {
        lagBGPeriode(STP, dagerEtterSTP(10), lagBGAndel(AG1, REF1, 250000), lagBGAndel(AG1, REF2, 100000));
        lagBGPeriode(dagerEtterSTP(11), dagerEtterSTP(30), PeriodeÅrsak.NATURALYTELSE_BORTFALT, lagBGAndel(AG1, REF1, 250000), lagBGAndel(AG1, REF2, 100000));
        lagBGPeriode(dagerEtterSTP(31), UENDELIG, PeriodeÅrsak.GRADERING, lagBGAndel(AG1, REF1, 250000), lagBGAndel(AG1, REF2, 100000));

        lagSaksbehandlerDto(AG1, REF1, dagerEtterSTP(20));
        lagSaksbehandlerDto(AG1, REF2, dagerEtterSTP(50));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(5);

        assertPeriode(STP, dagerEtterSTP(10), AG1, REF1, 0, resultat, 2);
        assertPeriode(STP, dagerEtterSTP(10), AG1, REF2, 0, resultat, 2);

        assertPeriode(dagerEtterSTP(11), dagerEtterSTP(19), AG1, REF1, 0, resultat, 2, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        assertPeriode(dagerEtterSTP(11), dagerEtterSTP(19), AG1, REF2, 0, resultat, 2, PeriodeÅrsak.NATURALYTELSE_BORTFALT);

        assertPeriode(dagerEtterSTP(20), dagerEtterSTP(30), AG1, REF1, 250000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(20), dagerEtterSTP(30), AG1, REF2, 0, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);

        assertPeriode(dagerEtterSTP(31), dagerEtterSTP(49), AG1, REF1, 250000, resultat, 2, PeriodeÅrsak.GRADERING);
        assertPeriode(dagerEtterSTP(31), dagerEtterSTP(49), AG1, REF2, 0, resultat, 2, PeriodeÅrsak.GRADERING);

        assertPeriode(dagerEtterSTP(50), UENDELIG, AG1, REF1, 250000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(50), UENDELIG, AG1, REF2, 100000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);

    }

    @Test
    public void skal_splitte_grunnlag_når_tilkommet_andel_søker_refusjon_og_splitt_skjer_samme_dag_som_annen_splitt() {
        lagBGPeriode(STP, dagerEtterSTP(30), lagBGAndel(AG1, REF1, 250000), lagBGAndel(AG1, REF2, 100000));
        lagBGPeriode(dagerEtterSTP(31), dagerEtterSTP(50), PeriodeÅrsak.GRADERING, lagBGAndel(AG1, REF1, 250000), lagBGAndel(AG1, REF2, 100000), lagBGAndel(AG3, REF3, 300000));
        lagBGPeriode(dagerEtterSTP(51), UENDELIG, PeriodeÅrsak.NATURALYTELSE_TILKOMMER, lagBGAndel(AG1, REF1, 250000), lagBGAndel(AG1, REF2, 100000), lagBGAndel(AG3, REF3, 300000));

        lagSaksbehandlerDto(AG1, REF1, dagerEtterSTP(45));
        lagSaksbehandlerDto(AG3, REF3, dagerEtterSTP(51));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(4);

        assertPeriode(STP, dagerEtterSTP(30), AG1, REF1, 0, resultat, 2);
        assertPeriode(STP, dagerEtterSTP(30), AG1, REF2, 100000, resultat, 2);

        assertPeriode(dagerEtterSTP(31), dagerEtterSTP(44), AG1, REF1, 0, resultat, 3, PeriodeÅrsak.GRADERING);
        assertPeriode(dagerEtterSTP(31), dagerEtterSTP(44), AG1, REF2, 100000, resultat, 3, PeriodeÅrsak.GRADERING);
        assertPeriode(dagerEtterSTP(31), dagerEtterSTP(44), AG3, REF3, 0, resultat, 3, PeriodeÅrsak.GRADERING);

        assertPeriode(dagerEtterSTP(45), dagerEtterSTP(50), AG1, REF1, 250000, resultat, 3, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(45), dagerEtterSTP(50), AG1, REF2, 100000, resultat, 3, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(45), dagerEtterSTP(50), AG3, REF3, 0, resultat, 3, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);

        assertPeriode(dagerEtterSTP(51), UENDELIG, AG1, REF1, 250000, resultat, 3, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        assertPeriode(dagerEtterSTP(51), UENDELIG, AG1, REF2, 100000, resultat, 3, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        assertPeriode(dagerEtterSTP(51), UENDELIG, AG3, REF3, 300000, resultat, 3, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);

    }

    @Test
    public void skal_splitte_og_sette_delvis_refusjon_før_startdato() {
        lagBGPeriode(STP, null, lagBGAndel(AG1, REF1, 500000), lagBGAndel(AG2, REF2, 100000));
        lagSaksbehandlerDto(AG2, REF2, dagerEtterSTP(15), 1000);

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(2);

        assertPeriode(STP, dagerEtterSTP(14), AG1, REF1, 500000, resultat, 2);
        assertPeriode(STP, dagerEtterSTP(14), AG2, REF2, 12000, resultat, 2);

        assertPeriode(dagerEtterSTP(15), UENDELIG, AG1, REF1, 500000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(15), UENDELIG, AG2, REF2, 100000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }

    @Test
    public void skal_splitte_periode_når_det_finnes_andeler_med_og_uten_referanse() {
        lagBGPeriode(STP, null, lagBGAndel(AG1, InternArbeidsforholdRefDto.nullRef(), 500000), lagBGAndel(AG1, REF1, 100000));
        lagSaksbehandlerDto(AG1, InternArbeidsforholdRefDto.nullRef(), dagerEtterSTP(15));
        lagSaksbehandlerDto(AG1, REF1, dagerEtterSTP(30));

        BeregningsgrunnlagDto resultat = oppdater();

        assertThat(resultat).isEqualTo(grunnlagBuilder.build());
        assertThat(resultat.getBeregningsgrunnlagPerioder()).hasSize(3);

        assertPeriode(STP, dagerEtterSTP(14), AG1, InternArbeidsforholdRefDto.nullRef(), 0, resultat, 2);
        assertPeriode(STP, dagerEtterSTP(14), AG1, REF1, 0, resultat, 2);

        assertPeriode(dagerEtterSTP(15), dagerEtterSTP(29), AG1, InternArbeidsforholdRefDto.nullRef(), 500000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(15), dagerEtterSTP(29), AG1, REF1, 0, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);

        assertPeriode(dagerEtterSTP(30), UENDELIG, AG1, InternArbeidsforholdRefDto.nullRef(), 500000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertPeriode(dagerEtterSTP(30), UENDELIG, AG1, REF1, 100000, resultat, 2, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
    }



    private BeregningsgrunnlagDto oppdater() {
        return PeriodiserOgFastsettRefusjonTjeneste.periodiserOgFastsett(grunnlagBuilder.build(), saksbehandlerAvklaringer);
    }

    private void assertPeriode(LocalDate fom, LocalDate tom, Arbeidsgiver ag, InternArbeidsforholdRefDto ref, int refusjon, BeregningsgrunnlagDto resultat, int antallAndelerIPerioden, PeriodeÅrsak... periodeÅrsaker) {
        Intervall periodeUtenRef = Intervall.fraOgMedTilOgMed(fom, tom);
        List<BeregningsgrunnlagPeriodeDto> perioderSomMatcher = resultat.getBeregningsgrunnlagPerioder().stream()
                .filter(bgp -> bgp.getPeriode().overlapper(periodeUtenRef)).collect(Collectors.toList());
        assertThat(perioderSomMatcher).isNotEmpty();
        perioderSomMatcher.forEach(bgp -> {
            assertThat(bgp.getPeriodeÅrsaker()).containsExactlyInAnyOrder(periodeÅrsaker);
            assertThat(bgp.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndelerIPerioden);
            BeregningsgrunnlagPrStatusOgAndelDto matchetAndel = bgp.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> matcher(andel, ag, ref)).findFirst().orElse(null);
            assertThat(matchetAndel).isNotNull();
            assertThat(hentRefusjon(matchetAndel)).isEqualByComparingTo(Beløp.fra(refusjon));
        });

    }

    private Beløp hentRefusjon(BeregningsgrunnlagPrStatusOgAndelDto matchetAndel) {
        return matchetAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(null);
    }

    private boolean matcher(BeregningsgrunnlagPrStatusOgAndelDto andel, Arbeidsgiver ag, InternArbeidsforholdRefDto ref) {
        Arbeidsgiver andelAG = andel.getArbeidsgiver().orElse(null);
        InternArbeidsforholdRefDto andelRef = andel.getArbeidsforholdRef().orElse(null);
        return Objects.equals(andelAG, ag) && Objects.equals(andelRef, ref);
    }

    private void lagSaksbehandlerDto(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdReferanse, LocalDate refusjonFom) {
        lagSaksbehandlerDto(arbeidsgiver, arbeidsforholdReferanse, refusjonFom, null);
    }

    private void lagSaksbehandlerDto(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdReferanse, LocalDate refusjonFom, Integer delvisRefusjonBeløp) {
        saksbehandlerAvklaringer.add(new VurderRefusjonAndelBeregningsgrunnlagDto(arbeidsgiver.getOrgnr(),
                arbeidsgiver.getAktørId() == null ? null : arbeidsgiver.getAktørId().getId(),
                arbeidsforholdReferanse.getReferanse(),
                refusjonFom,
                delvisRefusjonBeløp));
    }

    private LocalDate dagerEtterSTP(int i) {
        return STP.plusDays(i);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagBGAndel(Arbeidsgiver ag, InternArbeidsforholdRefDto ref, int refusjonPrÅr) {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(ag == null ? AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE : AktivitetStatus.ARBEIDSTAKER);
        if (ag != null) {
            BGAndelArbeidsforholdDto.Builder arbforBuilder = BGAndelArbeidsforholdDto.builder()
                    .medRefusjonskravPrÅr(Beløp.fra(refusjonPrÅr), Utfall.GODKJENT)
                    .medArbeidsforholdRef(ref)
                    .medArbeidsgiver(ag);
            andelBuilder.medBGAndelArbeidsforhold(arbforBuilder);
        }
        return andelBuilder;
    }

    private void lagBGPeriode(LocalDate fom, LocalDate tom, BeregningsgrunnlagPrStatusOgAndelDto.Builder... andeler) {
        lagBGPeriode(fom, tom, null, andeler);
    }

    private void lagBGPeriode(LocalDate fom, LocalDate tom, PeriodeÅrsak periodeÅrsak, BeregningsgrunnlagPrStatusOgAndelDto.Builder... andeler) {
        BeregningsgrunnlagPeriodeDto.Builder bgPeriodeBuilder = BeregningsgrunnlagPeriodeDto.ny().medBeregningsgrunnlagPeriode(fom, tom);
        if (periodeÅrsak != null) {
            bgPeriodeBuilder.leggTilPeriodeÅrsak(periodeÅrsak);
        }
        List<BeregningsgrunnlagPrStatusOgAndelDto.Builder> bgAndeler = Arrays.asList(andeler);
        bgAndeler.forEach(bgPeriodeBuilder::leggTilBeregningsgrunnlagPrStatusOgAndel);
        grunnlagBuilder.leggTilBeregningsgrunnlagPeriode(bgPeriodeBuilder);
    }

}
