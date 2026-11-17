package no.nav.folketrygdloven.kalkulator.modell.refusjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class RefusjonsmodellTest {

    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("123456789");
    private static final Arbeidsgiver ANNEN_ARBEIDSGIVER = Arbeidsgiver.virksomhet("987654321");
    private static final JournalpostId JOURNALPOST = new JournalpostId("JP-1");
    private static final JournalpostId ANNEN_JOURNALPOST = new JournalpostId("JP-2");

    @Test
    void periode_bevarer_verdier_ved_kopiering() {
        var periode = Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        var refusjonsbeløpPrMnd = Beløp.fra(new BigDecimal("1234.56"));
        var original = new Refusjonsperiode(periode, refusjonsbeløpPrMnd, RefusjonUtfall.REDUSERT,
                RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT);
        var kopi = new Refusjonsperiode(original.periode(), original.refusjonsbeløpPrMnd(), original.utfall(),
                original.utfallÅrsak());

        assertThat(original.periode()).isEqualTo(periode);
        assertThat(original.refusjonsbeløpPrMnd()).isEqualByComparingTo(refusjonsbeløpPrMnd);
        assertThat(original.refusjonsbeløpPrMnd().verdi()).isEqualByComparingTo("1234.56");
        assertThat(original.utfall()).isEqualTo(RefusjonUtfall.REDUSERT);
        assertThat(original.utfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT);
        assertThat(kopi.periode()).isEqualTo(periode);
        assertThat(kopi.refusjonsbeløpPrMnd()).isEqualByComparingTo(refusjonsbeløpPrMnd);
        assertThat(kopi.utfall()).isEqualTo(RefusjonUtfall.REDUSERT);
        assertThat(kopi.utfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT);
    }

    @Test
    void grunnlagsconstructor_tar_uforanderlig_snapshot_av_liste() {
        var andeler = new ArrayList<Refusjonsandel>();

        var grunnlag = new FordelingGrunnlag(andeler);
        andeler.add(null);

        var refusjonsandeler = grunnlag.refusjonsandeler();
        assertThat(refusjonsandeler).isEmpty();
        assertThatThrownBy(refusjonsandeler::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Nested
    class AndelOgGrunnlag {

        @Test
        void journalpostkrav_for_kildene() {
            var inntektsmelding = andel("A-1", RefusjonKilde.INNTEKTSMELDING, JOURNALPOST);
            var saksbehandler = andel("A-1", RefusjonKilde.SAKSBEHANDLER, null);

            assertThat(inntektsmelding.journalpostId()).isEqualTo(JOURNALPOST);
            assertThat(saksbehandler.journalpostId()).isNull();
            assertThatThrownBy(() -> andel("A-1", RefusjonKilde.INNTEKTSMELDING, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Journalpost er påkrevd for kilde INNTEKTSMELDING.");
            assertThatThrownBy(() -> andel("A-1", RefusjonKilde.SAKSBEHANDLER, JOURNALPOST))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Journalpost er ikke tillatt for kilde SAKSBEHANDLER.");
        }

        @Test
        void samme_arbeidsgiver_og_arbeidsforhold_med_ulike_kilder_godtas() {
            var inntektsmelding = andel("A-1", RefusjonKilde.INNTEKTSMELDING, JOURNALPOST);
            var saksbehandler = andel("A-1", RefusjonKilde.SAKSBEHANDLER, null);

            var grunnlag = new FordelingGrunnlag(List.of(inntektsmelding, saksbehandler));

            assertThat(grunnlag.refusjonsandeler()).containsExactly(inntektsmelding, saksbehandler);
        }

        @Test
        void samme_triple_avvises_også_når_journalpostene_er_ulike() {
            var inntektsmelding = andel("A-1", RefusjonKilde.INNTEKTSMELDING, JOURNALPOST);
            var saksbehandler = andel("A-1", RefusjonKilde.SAKSBEHANDLER, null);
            var inntektsmeldingsandeler = List.of(inntektsmelding, andel("A-1", RefusjonKilde.INNTEKTSMELDING, ANNEN_JOURNALPOST));
            var saksbehandlerandeler = List.of(saksbehandler, andel("A-1", RefusjonKilde.SAKSBEHANDLER, null));

            assertThatThrownBy(() -> new FordelingGrunnlag(inntektsmeldingsandeler))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
            assertThatThrownBy(() -> new FordelingGrunnlag(saksbehandlerandeler))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
        }

        @Test
        void like_referanseverdier_i_ulike_objekter_regnes_som_duplikater() {
            var førsteRef = InternArbeidsforholdRefDto.namedRef("A-1");
            var andreRef = InternArbeidsforholdRefDto.ref(førsteRef.getUUIDReferanse());
            var førsteAndel = new Refusjonsandel(ARBEIDSGIVER, førsteRef, RefusjonKilde.SAKSBEHANDLER, null, List.of());
            var andreAndel = new Refusjonsandel(ARBEIDSGIVER, andreRef, RefusjonKilde.SAKSBEHANDLER, null, List.of());
            var andeler = List.of(førsteAndel, andreAndel);

            assertThatThrownBy(() -> new FordelingGrunnlag(andeler))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
        }

        @Test
        void nullreferanse_er_en_egen_nøkkel_ikke_et_vilkårlig_arbeidsforhold() {
            var utenSpesifiktArbeidsforhold = new Refusjonsandel(ARBEIDSGIVER, InternArbeidsforholdRefDto.nullRef(),
                    RefusjonKilde.SAKSBEHANDLER, null, List.of());
            var spesifiktArbeidsforhold = andel("A-1", RefusjonKilde.SAKSBEHANDLER, null);
            var grunnlag = new FordelingGrunnlag(List.of(utenSpesifiktArbeidsforhold, spesifiktArbeidsforhold));
            var duplikateAndeler = List.of(utenSpesifiktArbeidsforhold, utenSpesifiktArbeidsforhold);

            assertThat(grunnlag.refusjonsandeler()).containsExactly(utenSpesifiktArbeidsforhold, spesifiktArbeidsforhold);
            assertThatThrownBy(() -> new FordelingGrunnlag(duplikateAndeler))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
        }

        @Test
        void ulike_arbeidsgivere_og_arbeidsforhold_godtas() {
            var sammeArbeidsforholdUlikArbeidsgiver = new FordelingGrunnlag(List.of(
                    andel(ARBEIDSGIVER, "A-1", RefusjonKilde.SAKSBEHANDLER, null),
                    andel(ANNEN_ARBEIDSGIVER, "A-1", RefusjonKilde.SAKSBEHANDLER, null)));
            var sammeArbeidsgiverUliktArbeidsforhold = new FordelingGrunnlag(List.of(
                    andel(ARBEIDSGIVER, "A-1", RefusjonKilde.SAKSBEHANDLER, null),
                    andel(ARBEIDSGIVER, "A-2", RefusjonKilde.SAKSBEHANDLER, null)));

            assertThat(sammeArbeidsforholdUlikArbeidsgiver.refusjonsandeler()).hasSize(2);
            assertThat(sammeArbeidsgiverUliktArbeidsforhold.refusjonsandeler()).hasSize(2);
        }

        @Test
        void aggregat_og_kopiering_bevarer_perioder_og_snapshot_av_lister() {
            var januar = periode("2026-01-01", "2026-01-31", "1234.56");
            var februar = periode("2026-02-01", "2026-02-28", "4321.09");
            var perioder = new ArrayList<>(List.of(januar));
            var andel = new Refusjonsandel(ARBEIDSGIVER, InternArbeidsforholdRefDto.namedRef("A-1"),
                    RefusjonKilde.INNTEKTSMELDING, JOURNALPOST, perioder);
            var andeler = new ArrayList<>(List.of(andel));
            var grunnlag = new FordelingGrunnlag(andeler);
            var kopiertAndel = new Refusjonsandel(andel.arbeidsgiver(), andel.arbeidsforholdRef(), andel.kilde(),
                    andel.journalpostId(), andel.refusjonsperioder());
            var kopi = new FordelingGrunnlag(grunnlag.refusjonsandeler());

            perioder.add(februar);
            andeler.clear();

            assertThat(andel.arbeidsforholdRef()).isEqualTo(InternArbeidsforholdRefDto.namedRef("A-1"));
            assertThat(kopiertAndel.arbeidsforholdRef()).isEqualTo(andel.arbeidsforholdRef());
            assertThat(andel.kilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
            assertThat(andel.journalpostId()).isEqualTo(JOURNALPOST);
            assertThat(andel.refusjonsperioder()).containsExactly(januar);
            assertThat(januar.refusjonsbeløpPrMnd()).isEqualByComparingTo(Beløp.fra(new BigDecimal("1234.56")));
            assertThat(februar.refusjonsbeløpPrMnd()).isEqualByComparingTo(Beløp.fra(new BigDecimal("4321.09")));
            assertThat(grunnlag.refusjonsandeler()).containsExactly(andel);
            assertThat(kopi.refusjonsandeler()).containsExactly(andel);
            var refusjonsandeler = grunnlag.refusjonsandeler();
            var refusjonsperioder = andel.refusjonsperioder();
            assertThatThrownBy(refusjonsandeler::clear).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(refusjonsperioder::clear).isInstanceOf(UnsupportedOperationException.class);
        }

        private Refusjonsandel andel(String referanseSeed, RefusjonKilde kilde, JournalpostId journalpostId) {
            return andel(ARBEIDSGIVER, referanseSeed, kilde, journalpostId);
        }

        private Refusjonsandel andel(Arbeidsgiver arbeidsgiver, String referanseSeed, RefusjonKilde kilde,
                                     JournalpostId journalpostId) {
            return new Refusjonsandel(arbeidsgiver, InternArbeidsforholdRefDto.namedRef(referanseSeed), kilde,
                    journalpostId, List.of());
        }

        private Refusjonsperiode periode(String fom, String tom, String refusjonsbeløpPrMnd) {
            return new Refusjonsperiode(Intervall.fraOgMedTilOgMed(LocalDate.parse(fom), LocalDate.parse(tom)),
                    Beløp.fra(new BigDecimal(refusjonsbeløpPrMnd)), RefusjonUtfall.INNVILGET, RefusjonUtfallÅrsak.UTMELDT);
        }

    }
}
