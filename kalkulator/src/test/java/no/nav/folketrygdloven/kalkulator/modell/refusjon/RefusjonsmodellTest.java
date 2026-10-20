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
        var refusjonPrMnd = Beløp.fra(new BigDecimal("1234.56"));
        var begrunnelse = "Refusjon er redusert for perioden.";
        var original = new RefusjonsperiodeDto(periode, refusjonPrMnd, RefusjonUtfall.REDUSERT,
                RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT, begrunnelse);
        var kopi = new RefusjonsperiodeDto(original.periode(), original.refusjonPrMnd(), original.utfall(),
                original.utfallÅrsak(), original.utfallBegrunnelse());

        assertThat(original.periode()).isEqualTo(periode);
        assertThat(original.refusjonPrMnd()).isEqualByComparingTo(refusjonPrMnd);
        assertThat(original.refusjonPrMnd().verdi()).isEqualByComparingTo("1234.56");
        assertThat(original.utfall()).isEqualTo(RefusjonUtfall.REDUSERT);
        assertThat(original.utfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT);
        assertThat(original.utfallBegrunnelse()).isEqualTo(begrunnelse);
        assertThat(kopi.periode()).isEqualTo(periode);
        assertThat(kopi.refusjonPrMnd()).isEqualByComparingTo(refusjonPrMnd);
        assertThat(kopi.utfall()).isEqualTo(RefusjonUtfall.REDUSERT);
        assertThat(kopi.utfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT);
        assertThat(kopi.utfallBegrunnelse()).isEqualTo(begrunnelse);
        assertThat(original.toString()).doesNotContain(begrunnelse);
        assertThat(kopi.toString()).doesNotContain(begrunnelse);
    }

    @Test
    void toString_viser_perioder_rekursivt_uten_sensitive_felter() {
        var førstePeriode = new RefusjonsperiodeDto(
                Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
                Beløp.fra(new BigDecimal("1234.56")), RefusjonUtfall.REDUSERT,
                RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT, "SENSITIV FRITEKST 1");
        var andrePeriode = new RefusjonsperiodeDto(
                Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)),
                Beløp.fra(new BigDecimal("4321.09")), RefusjonUtfall.INNVILGET,
                RefusjonUtfallÅrsak.UTMELDT, "SENSITIV FRITEKST 2");
        var tredjePeriode = new RefusjonsperiodeDto(
                Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
                Beløp.fra(new BigDecimal("9876.54")), RefusjonUtfall.AVSLÅTT,
                RefusjonUtfallÅrsak.IKKE_INNENFOR_FRIST, "SENSITIV FRITEKST 3");
        var virksomhetsref = InternArbeidsforholdRefDto.namedRef("SENSITIV-REF-1");
        var personref = InternArbeidsforholdRefDto.namedRef("SENSITIV-REF-2");
        var virksomhetsandel = new RefusjonsandelDto(ARBEIDSGIVER, virksomhetsref, RefusjonKilde.SAKSBEHANDLER, null,
                List.of(førstePeriode, andrePeriode));
        var personandel = new RefusjonsandelDto(Arbeidsgiver.person(new AktørId("1111111111111")), personref,
                RefusjonKilde.INNTEKTSMELDING, new JournalpostId("SENSITIV-JOURNALPOST"),
                List.of(tredjePeriode, andrePeriode));
        var grunnlag = new FordelingGrunnlagDto(List.of(virksomhetsandel, personandel));

        var førstePeriodeTekst = "RefusjonsperiodeDto[periode="
                + Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))
                + ", utfall=REDUSERT, utfallÅrsak=ALLEREDE_DIREKTE_UTBETALT]";
        var andrePeriodeTekst = "RefusjonsperiodeDto[periode="
                + Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))
                + ", utfall=INNVILGET, utfallÅrsak=UTMELDT]";
        var tredjePeriodeTekst = "RefusjonsperiodeDto[periode="
                + Intervall.fraOgMedTilOgMed(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31))
                + ", utfall=AVSLÅTT, utfallÅrsak=IKKE_INNENFOR_FRIST]";
        var virksomhetsandelTekst = "RefusjonsandelDto[kilde=SAKSBEHANDLER, refusjonsperioder=["
                + førstePeriodeTekst + ", " + andrePeriodeTekst + "]]";
        var personandelTekst = "RefusjonsandelDto[kilde=INNTEKTSMELDING, refusjonsperioder=["
                + tredjePeriodeTekst + ", " + andrePeriodeTekst + "]]";

        assertThat(førstePeriode.toString()).isEqualTo(førstePeriodeTekst);
        assertThat(andrePeriode.toString()).isEqualTo(andrePeriodeTekst);
        assertThat(tredjePeriode.toString()).isEqualTo(tredjePeriodeTekst);
        assertThat(virksomhetsandel.toString()).isEqualTo(virksomhetsandelTekst);
        assertThat(personandel.toString()).isEqualTo(personandelTekst);
        assertThat(grunnlag.toString())
                .isEqualTo("FordelingGrunnlagDto[refusjonsandeler=[" + virksomhetsandelTekst + ", " + personandelTekst + "]]");

        var utdata = List.of(førstePeriode.toString(), andrePeriode.toString(), tredjePeriode.toString(), virksomhetsandel.toString(),
                personandel.toString(), grunnlag.toString());
        var sensitiveVerdier = List.of("123456789", "1111111111111", virksomhetsref.getUUIDReferanse().toString(),
                personref.getUUIDReferanse().toString(), "SENSITIV-JOURNALPOST", "1234.56", "4321.09", "9876.54",
                "SENSITIV FRITEKST 1", "SENSITIV FRITEKST 2", "SENSITIV FRITEKST 3");
        for (var tekst : utdata) {
            assertThat(tekst).doesNotContain(sensitiveVerdier.toArray(String[]::new));
        }
    }

    @Test
    void grunnlagsconstructor_tar_uforanderlig_snapshot_av_liste() {
        var andeler = new ArrayList<RefusjonsandelDto>();

        var grunnlag = new FordelingGrunnlagDto(andeler);
        andeler.add(null);

        assertThat(grunnlag.refusjonsandeler()).isEmpty();
        assertThatThrownBy(() -> grunnlag.refusjonsandeler().clear()).isInstanceOf(UnsupportedOperationException.class);
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

            var grunnlag = new FordelingGrunnlagDto(List.of(inntektsmelding, saksbehandler));

            assertThat(grunnlag.refusjonsandeler()).containsExactly(inntektsmelding, saksbehandler);
        }

        @Test
        void samme_triple_avvises_også_når_journalpostene_er_ulike() {
            var inntektsmelding = andel("A-1", RefusjonKilde.INNTEKTSMELDING, JOURNALPOST);
            var saksbehandler = andel("A-1", RefusjonKilde.SAKSBEHANDLER, null);

            assertThatThrownBy(() -> new FordelingGrunnlagDto(
                    List.of(inntektsmelding, andel("A-1", RefusjonKilde.INNTEKTSMELDING, ANNEN_JOURNALPOST))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
            assertThatThrownBy(() -> new FordelingGrunnlagDto(List.of(saksbehandler,
                    andel("A-1", RefusjonKilde.SAKSBEHANDLER, null))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
        }

        @Test
        void like_referanseverdier_i_ulike_objekter_regnes_som_duplikater() {
            var førsteRef = InternArbeidsforholdRefDto.namedRef("A-1");
            var andreRef = InternArbeidsforholdRefDto.ref(førsteRef.getUUIDReferanse());
            var førsteAndel = new RefusjonsandelDto(ARBEIDSGIVER, førsteRef, RefusjonKilde.SAKSBEHANDLER, null, List.of());
            var andreAndel = new RefusjonsandelDto(ARBEIDSGIVER, andreRef, RefusjonKilde.SAKSBEHANDLER, null, List.of());

            assertThatThrownBy(() -> new FordelingGrunnlagDto(List.of(førsteAndel, andreAndel)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
        }

        @Test
        void nullreferanse_er_en_egen_nøkkel_ikke_et_vilkårlig_arbeidsforhold() {
            var utenSpesifiktArbeidsforhold = new RefusjonsandelDto(ARBEIDSGIVER, InternArbeidsforholdRefDto.nullRef(),
                    RefusjonKilde.SAKSBEHANDLER, null, List.of());
            var spesifiktArbeidsforhold = andel("A-1", RefusjonKilde.SAKSBEHANDLER, null);
            var grunnlag = new FordelingGrunnlagDto(List.of(utenSpesifiktArbeidsforhold, spesifiktArbeidsforhold));

            assertThat(grunnlag.refusjonsandeler()).containsExactly(utenSpesifiktArbeidsforhold, spesifiktArbeidsforhold);
            assertThatThrownBy(() -> new FordelingGrunnlagDto(List.of(utenSpesifiktArbeidsforhold, utenSpesifiktArbeidsforhold)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Flere refusjonsandeler med samme arbeidsgiver, arbeidsforholdsreferanse og kilde.");
        }

        @Test
        void ulike_arbeidsgivere_og_arbeidsforhold_godtas() {
            var sammeArbeidsforholdUlikArbeidsgiver = new FordelingGrunnlagDto(List.of(
                    andel(ARBEIDSGIVER, "A-1", RefusjonKilde.SAKSBEHANDLER, null),
                    andel(ANNEN_ARBEIDSGIVER, "A-1", RefusjonKilde.SAKSBEHANDLER, null)));
            var sammeArbeidsgiverUliktArbeidsforhold = new FordelingGrunnlagDto(List.of(
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
            var andel = new RefusjonsandelDto(ARBEIDSGIVER, InternArbeidsforholdRefDto.namedRef("A-1"),
                    RefusjonKilde.INNTEKTSMELDING, JOURNALPOST, perioder);
            var andeler = new ArrayList<>(List.of(andel));
            var grunnlag = new FordelingGrunnlagDto(andeler);
            var kopiertAndel = new RefusjonsandelDto(andel.arbeidsgiver(), andel.arbeidsforholdRef(), andel.kilde(),
                    andel.journalpostId(), andel.refusjonsperioder());
            var kopi = new FordelingGrunnlagDto(grunnlag.refusjonsandeler());

            perioder.add(februar);
            andeler.clear();

            assertThat(andel.arbeidsforholdRef()).isEqualTo(InternArbeidsforholdRefDto.namedRef("A-1"));
            assertThat(kopiertAndel.arbeidsforholdRef()).isEqualTo(andel.arbeidsforholdRef());
            assertThat(andel.kilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
            assertThat(andel.journalpostId()).isEqualTo(JOURNALPOST);
            assertThat(andel.refusjonsperioder()).containsExactly(januar);
            assertThat(januar.refusjonPrMnd()).isEqualByComparingTo(Beløp.fra(new BigDecimal("1234.56")));
            assertThat(februar.refusjonPrMnd()).isEqualByComparingTo(Beløp.fra(new BigDecimal("4321.09")));
            assertThat(grunnlag.refusjonsandeler()).containsExactly(andel);
            assertThat(kopi.refusjonsandeler()).containsExactly(andel);
            assertThatThrownBy(() -> grunnlag.refusjonsandeler().clear()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> andel.refusjonsperioder().clear()).isInstanceOf(UnsupportedOperationException.class);
        }

        private RefusjonsandelDto andel(String referanseSeed, RefusjonKilde kilde, JournalpostId journalpostId) {
            return andel(ARBEIDSGIVER, referanseSeed, kilde, journalpostId);
        }

        private RefusjonsandelDto andel(Arbeidsgiver arbeidsgiver, String referanseSeed, RefusjonKilde kilde,
                JournalpostId journalpostId) {
            return new RefusjonsandelDto(arbeidsgiver, InternArbeidsforholdRefDto.namedRef(referanseSeed), kilde,
                    journalpostId, List.of());
        }

        private RefusjonsperiodeDto periode(String fom, String tom, String refusjonPrMnd) {
            return new RefusjonsperiodeDto(Intervall.fraOgMedTilOgMed(LocalDate.parse(fom), LocalDate.parse(tom)),
                    Beløp.fra(new BigDecimal(refusjonPrMnd)), RefusjonUtfall.INNVILGET, RefusjonUtfallÅrsak.UTMELDT, null);
        }

    }
}
