package no.nav.folketrygdloven.kalkulus.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.felles.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.felles.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.FordelingGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.RefusjonsandelDto;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.RefusjonsperiodeDto;

class FordelingGrunnlagDtoTest {

    @Test
    void serialiserer_og_deserialiserer_fordelingsgrunnlag() throws Exception {
        var source = new FordelingGrunnlagDto(List.of(
                new RefusjonsandelDto(new Arbeidsgiver(null, "1234567890123"),
                        new InternArbeidsforholdRefDto("arb-forhold-syntetisk-987"),
                        RefusjonKilde.INNTEKTSMELDING, new JournalpostId("journalpost-syntetisk-123"),
                        List.of(new RefusjonsperiodeDto(
                                new Periode(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31")),
                                Beløp.fra(new BigDecimal("12345.67")), RefusjonUtfall.INNVILGET,
                                RefusjonUtfallÅrsak.UTMELDT, "syntetisk begrunnelse"))),
                new RefusjonsandelDto(new Arbeidsgiver("888888888", null),
                        new InternArbeidsforholdRefDto("arb-forhold-syntetisk-654"),
                        RefusjonKilde.SAKSBEHANDLER, null,
                        List.of(new RefusjonsperiodeDto(
                                new Periode(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-02-28")),
                                Beløp.fra(0), RefusjonUtfall.REDUSERT, null, null)))));

        var json = KontraktTestJsonMapper.WRITER_JSON.writeValueAsString(source);
        var deserialized = (FordelingGrunnlagDto) KontraktTestJsonMapper.READER_JSON.forType(FordelingGrunnlagDto.class).readValue(json);

        assertThat(json).contains("\"refusjonsandeler\"", "\"arbeidsforholdRef\"", "\"kilde\"",
                "\"journalpostId\"", "\"refusjonsperioder\"", "\"refusjonPrMnd\"", "\"utfallÅrsak\"",
                "\"utfallBegrunnelse\"", "journalpost-syntetisk-123");
        assertThat(deserialized.refusjonsandeler()).hasSize(2);

        var fullAndel = deserialized.refusjonsandeler().getFirst();
        assertThat(fullAndel.arbeidsgiver().getArbeidsgiverOrgnr()).isNull();
        assertThat(fullAndel.arbeidsgiver().getArbeidsgiverAktørId()).isEqualTo("1234567890123");
        assertThat(fullAndel.arbeidsforholdRef().getAbakusReferanse()).isEqualTo("arb-forhold-syntetisk-987");
        assertThat(fullAndel.kilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
        assertThat(fullAndel.journalpostId().getId()).isEqualTo("journalpost-syntetisk-123");
        var fullPeriode = fullAndel.refusjonsperioder().getFirst();
        assertThat(fullPeriode.periode().getFom()).isEqualTo(LocalDate.parse("2026-01-01"));
        assertThat(fullPeriode.periode().getTom()).isEqualTo(LocalDate.parse("2026-01-31"));
        assertThat(fullPeriode.refusjonPrMnd().verdi()).isEqualByComparingTo("12345.67");
        assertThat(fullPeriode.utfall()).isEqualTo(RefusjonUtfall.INNVILGET);
        assertThat(fullPeriode.utfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.UTMELDT);
        assertThat(fullPeriode.utfallBegrunnelse()).isEqualTo("syntetisk begrunnelse");

        var optionalAndel = deserialized.refusjonsandeler().get(1);
        assertThat(optionalAndel.arbeidsgiver().getArbeidsgiverOrgnr()).isEqualTo("888888888");
        assertThat(optionalAndel.arbeidsgiver().getArbeidsgiverAktørId()).isNull();
        assertThat(optionalAndel.journalpostId()).isNull();
        var optionalPeriode = optionalAndel.refusjonsperioder().getFirst();
        assertThat(optionalPeriode.utfallÅrsak()).isNull();
        assertThat(optionalPeriode.utfallBegrunnelse()).isNull();
    }

    @Test
    void ignorerer_ukjente_felter_og_stotter_tomme_grunnlag_og_valgfrie_felter() throws Exception {
        var json = """
                {
                  "refusjonsandeler": [
                    {
                      "arbeidsgiver": {"arbeidsgiverOrgnr": "777777777", "ukjentArbeidsgiverfelt": true},
                      "arbeidsforholdRef": {"abakusReferanse": "syntetisk-ref", "ukjentReferansefelt": "ignorer"},
                      "kilde": "SAKSBEHANDLER",
                      "ukjentAndelsfelt": "ignorer",
                      "refusjonsperioder": [
                        {
                          "periode": {"fom": "2026-03-01", "tom": "2026-03-31", "ukjentPeriodefelt": "ignorer"},
                          "refusjonPrMnd": 321.50,
                          "utfall": "AVSLÅTT",
                          "ukjentPeriodeDtoFelt": "ignorer"
                        }
                      ]
                    }
                  ],
                  "ukjentGrunnlagsfelt": "ignorer"
                }
                """;
        var reader = KontraktTestJsonMapper.READER_JSON.forType(FordelingGrunnlagDto.class);
        var deserialized = (FordelingGrunnlagDto) reader.readValue(json);
        var empty = (FordelingGrunnlagDto) reader.readValue("""
                {"refusjonsandeler": [], "ukjentGrunnlagsfelt": "ignorer"}
                """);

        assertThat(deserialized.refusjonsandeler()).hasSize(1);
        assertThat(deserialized.refusjonsandeler().getFirst().journalpostId()).isNull();
        var periode = deserialized.refusjonsandeler().getFirst().refusjonsperioder().getFirst();
        assertThat(periode.refusjonPrMnd().verdi()).isEqualByComparingTo("321.50");
        assertThat(periode.utfallÅrsak()).isNull();
        assertThat(periode.utfallBegrunnelse()).isNull();
        assertThat(empty.refusjonsandeler()).isEmpty();
    }

    @Test
    void toString_utelater_identifikatorer_journalpost_belop_og_fritekst() {
        var period = new RefusjonsperiodeDto(
                new Periode(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31")),
                Beløp.fra(new BigDecimal("12345.67")), RefusjonUtfall.INNVILGET,
                RefusjonUtfallÅrsak.UTMELDT, "syntetisk begrunnelse");
        var andel = new RefusjonsandelDto(new Arbeidsgiver(null, "1234567890123"),
                new InternArbeidsforholdRefDto("arb-forhold-syntetisk-987"),
                RefusjonKilde.INNTEKTSMELDING, new JournalpostId("journalpost-syntetisk-123"),
                List.of(period));
        var grunnlag = new FordelingGrunnlagDto(List.of(andel));
        var sensitiveValues = new String[] {
                "1234567890123", "arb-forhold-syntetisk-987",
                "journalpost-syntetisk-123", "12345.67", "syntetisk begrunnelse"
        };

        assertThat(period.toString()).doesNotContain(sensitiveValues);
        assertThat(andel.toString()).doesNotContain(sensitiveValues);
        assertThat(grunnlag.toString()).doesNotContain(sensitiveValues);
    }
}
