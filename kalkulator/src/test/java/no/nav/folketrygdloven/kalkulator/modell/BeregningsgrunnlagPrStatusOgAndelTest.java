package no.nav.folketrygdloven.kalkulator.modell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class BeregningsgrunnlagPrStatusOgAndelTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final String ORGNR = "987";
    private static final InternArbeidsforholdRefDto ARBEIDSFORHOLD_ID = InternArbeidsforholdRefDto.namedRef("TEST-REF");
    private static final OpptjeningAktivitetType ARBEIDSFORHOLD_TYPE = OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE;
    private final LocalDate PERIODE_FOM = LocalDate.now();

    private BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode;
    private BeregningsgrunnlagPrStatusOgAndelDto prStatusOgAndel;

    @BeforeEach
    public void setup() {
        BeregningsgrunnlagDto beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriodeDto.Builder builder = lagBeregningsgrunnlagPeriodeBuilder();
        beregningsgrunnlagPeriode= builder.build(beregningsgrunnlag);
        prStatusOgAndel = lagBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriode);
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        assertThat(prStatusOgAndel.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(prStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdOrgnr).get()).isEqualTo(ORGNR);
        assertThat(prStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef).get()).isEqualTo(ARBEIDSFORHOLD_ID);
        assertThat(prStatusOgAndel.getArbeidsforholdType()).isEqualTo(ARBEIDSFORHOLD_TYPE);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = BeregningsgrunnlagPrStatusOgAndelDto.ny();
        try {
            builder.build(null);
            fail();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlagPeriode");
        }

        try {
            builder.build(beregningsgrunnlagPeriode);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("aktivitetStatus");
        }

        try {
            builder.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER);
            builder.build(beregningsgrunnlagPeriode);
            fail();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("bgAndelArbeidsforhold");
        }

        try {
            builder.medArbforholdType(OpptjeningAktivitetType.ARBEID);
            builder.build(beregningsgrunnlagPeriode);
            fail();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("bgAndelArbeidsforhold");
        }

        try {
            builder.medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder());
            fail();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("arbeidsgiver");
        }

        try {
            builder.medBeregningsperiode(PERIODE_FOM, PERIODE_FOM.plusDays(2));
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        assertThat(prStatusOgAndel).isNotEqualTo(null);
        assertThat(prStatusOgAndel).isNotEqualTo("blabla");
        assertThat(prStatusOgAndel).isEqualTo(prStatusOgAndel);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        BeregningsgrunnlagPrStatusOgAndelDto prStatusOgAndel2 = lagBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel).isEqualTo(prStatusOgAndel2);
        assertThat(prStatusOgAndel2).isEqualTo(prStatusOgAndel);
        assertThat(prStatusOgAndel.hashCode()).isEqualTo(prStatusOgAndel2.hashCode());
        assertThat(prStatusOgAndel2.hashCode()).isEqualTo(prStatusOgAndel.hashCode());

        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver.virksomhet(ORGNR));
        builder.medAktivitetStatus(AktivitetStatus.FRILANSER);
        prStatusOgAndel2 = builder.build(beregningsgrunnlagPeriode);
        assertThat(prStatusOgAndel).isNotEqualTo(prStatusOgAndel2);
        assertThat(prStatusOgAndel2).isNotEqualTo(prStatusOgAndel);
        assertThat(prStatusOgAndel.hashCode()).isNotEqualTo(prStatusOgAndel2.hashCode());
        assertThat(prStatusOgAndel2.hashCode()).isNotEqualTo(prStatusOgAndel.hashCode());
    }

    @Test
    public void skal_bruke_aktivitetStatus_i_equalsOgHashCode() {
        BeregningsgrunnlagPrStatusOgAndelDto prStatusOgAndel2 = lagBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel).isEqualTo(prStatusOgAndel2);
        assertThat(prStatusOgAndel.hashCode()).isEqualTo(prStatusOgAndel2.hashCode());

        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver.virksomhet(ORGNR));
        builder.medAktivitetStatus(AktivitetStatus.FRILANSER);
        prStatusOgAndel2 = builder.build(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel).isNotEqualTo(prStatusOgAndel2);
        assertThat(prStatusOgAndel.hashCode()).isNotEqualTo(prStatusOgAndel2.hashCode());
    }

    @Test
    public void skal_runde_av_og_sette_dagsats_riktig() {
        prStatusOgAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier(prStatusOgAndel)
            .medRedusertBrukersAndelPrÅr(Beløp.fra(BigDecimal.valueOf(377127.4)))
            .medRedusertRefusjonPrÅr(Beløp.fra(BigDecimal.valueOf(214892.574)))
            .build(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel.getDagsatsBruker()).isEqualTo(1450);
        assertThat(prStatusOgAndel.getDagsatsArbeidsgiver()).isEqualTo(827);

    }

    @Test
    public void skal_kunne_ha_privatperson_som_arbeidsgiver() {
        AktørId aktørId = AktørId.dummy();
        BeregningsgrunnlagPrStatusOgAndelDto.Builder builder = lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver.person(aktørId));
        builder.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER);
        BeregningsgrunnlagPrStatusOgAndelDto bgpsa = builder.build(beregningsgrunnlagPeriode);

        assertThat(bgpsa.getBgAndelArbeidsforhold().get().getArbeidsgiver().getIdentifikator()).isEqualTo(aktørId.getId());
    }

    @Test
    public void oppdatering_av_beregnet_skal_ikkje_endre_brutto_om_fordelt_er_satt() {
        prStatusOgAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier(prStatusOgAndel)
            .medFordeltPrÅr(Beløp.fra(BigDecimal.valueOf(377127.4)))
            .medBeregnetPrÅr(Beløp.fra(BigDecimal.valueOf(214892.574)))
            .build(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel.getBruttoPrÅr().verdi()).isEqualByComparingTo(BigDecimal.valueOf(377127.4));
    }

    @Test
    public void oppdatering_av_beregnet_skal_ikkje_endre_brutto_om_overstyrt_er_satt() {
        prStatusOgAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier(prStatusOgAndel)
            .medOverstyrtPrÅr(Beløp.fra(BigDecimal.valueOf(377127.4)))
            .medBeregnetPrÅr(Beløp.fra(BigDecimal.valueOf(214892.574)))
            .build(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel.getBruttoPrÅr().verdi()).isEqualByComparingTo(BigDecimal.valueOf(377127.4));
    }

    @Test
    public void oppdatering_av_overstyrt_skal_ikkje_endre_brutto_om_fordelt_er_satt() {
        prStatusOgAndel = BeregningsgrunnlagPrStatusOgAndelDto.kopier(prStatusOgAndel)
            .medFordeltPrÅr(Beløp.fra(BigDecimal.valueOf(377127.4)))
            .medOverstyrtPrÅr(Beløp.fra(BigDecimal.valueOf(214892.574)))
            .build(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel.getBruttoPrÅr().verdi()).isEqualByComparingTo(BigDecimal.valueOf(377127.4));
    }


    private BeregningsgrunnlagPrStatusOgAndelDto lagBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode) {
        return lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver.virksomhet(ORGNR)).build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPrStatusOgAndelDto.Builder lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver arbeidsgiver) {
        BGAndelArbeidsforholdDto.Builder bga = BGAndelArbeidsforholdDto.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdRef(ARBEIDSFORHOLD_ID)
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));

        return BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(ARBEIDSFORHOLD_TYPE);
    }

    private BeregningsgrunnlagPeriodeDto.Builder lagBeregningsgrunnlagPeriodeBuilder() {
        return BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(PERIODE_FOM, null);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        return lagBeregningsgrunnlagMedSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    private static BeregningsgrunnlagDto lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return BeregningsgrunnlagDto.builder().medSkjæringstidspunkt(skjæringstidspunkt).build();
    }
}
