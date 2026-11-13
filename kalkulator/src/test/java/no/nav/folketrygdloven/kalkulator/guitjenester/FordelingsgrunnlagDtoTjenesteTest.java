package no.nav.folketrygdloven.kalkulator.guitjenester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsandelDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfallÅrsak;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class FordelingsgrunnlagDtoTjenesteTest {

    private static final LocalDate PERIODE_FOM = LocalDate.of(2026, 1, 1);
    private static final LocalDate PERIODE_MELLOM = LocalDate.of(2026, 1, 31);
    private static final LocalDate PERIODE_TOM = LocalDate.of(2026, 2, 28);
    private static final String ORGNR = "000000000";
    private static final String BEGRUNNELSE = "Vurdert etter dokumentasjon.";

    private final BeregningsgrunnlagGuiTjeneste guiTjeneste = new BeregningsgrunnlagGuiTjeneste();

    @Test
    void mapper_fordelingsgrunnlag_med_andeler_perioder_og_valgfrie_felt() {
        var virksomhetsref = InternArbeidsforholdRefDto.namedRef("test-virksomhet");
        var virksomhetsandel = new RefusjonsandelDto(
                Arbeidsgiver.virksomhet(ORGNR),
                virksomhetsref,
                RefusjonKilde.SAKSBEHANDLER,
                null,
                List.of(
                        new RefusjonsperiodeDto(
                                Intervall.fraOgMedTilOgMed(PERIODE_FOM, PERIODE_MELLOM),
                                Beløp.fra(new BigDecimal("1250.50")),
                                RefusjonUtfall.REDUSERT,
                                RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT,
                                BEGRUNNELSE),
                        new RefusjonsperiodeDto(
                                Intervall.fraOgMedTilOgMed(PERIODE_MELLOM.plusDays(1), PERIODE_TOM),
                                Beløp.fra(new BigDecimal("875.25")),
                                RefusjonUtfall.INNVILGET,
                                null,
                                null)));
        var journalpostId = new JournalpostId("JP-test-1");
        var aktørId = AktørId.dummy();
        var personandel = new RefusjonsandelDto(
                Arbeidsgiver.person(aktørId),
                InternArbeidsforholdRefDto.nullRef(),
                RefusjonKilde.INNTEKTSMELDING,
                journalpostId,
                List.of(new RefusjonsperiodeDto(
                        Intervall.fraOgMedTilOgMed(PERIODE_FOM, PERIODE_TOM),
                        Beløp.fra(new BigDecimal("640.00")),
                        null,
                        null,
                        null)));
        var grunnlag = new FordelingGrunnlagDto(List.of(virksomhetsandel, personandel));

        var resultat = guiTjeneste.lagFordelingsgrunnlagDto(inputMedFordelingsgrunnlag(grunnlag));

        assertThat(resultat.refusjonsandeler()).hasSize(2);
        var virksomhetsDto = resultat.refusjonsandeler().get(0);
        assertThat(virksomhetsDto.arbeidsgiverIdent()).isEqualTo(ORGNR);
        assertThat(virksomhetsDto.arbeidsforholdId()).isEqualTo(virksomhetsref.getReferanse());
        assertThat(virksomhetsDto.kilde()).isEqualTo(RefusjonKilde.SAKSBEHANDLER);
        assertThat(virksomhetsDto.journalpostId()).isNull();
        assertThat(virksomhetsDto.refusjonsperioder()).hasSize(2);

        var førstePeriode = virksomhetsDto.refusjonsperioder().get(0);
        assertThat(førstePeriode.fom()).isEqualTo(PERIODE_FOM);
        assertThat(førstePeriode.tom()).isEqualTo(PERIODE_MELLOM);
        assertThat(førstePeriode.refusjonPrMnd().verdi()).isEqualByComparingTo("1250.50");
        assertThat(førstePeriode.utfall()).isEqualTo(RefusjonUtfall.REDUSERT);
        assertThat(førstePeriode.utfallÅrsak()).isEqualTo(RefusjonUtfallÅrsak.ALLEREDE_DIREKTE_UTBETALT);

        var andrePeriode = virksomhetsDto.refusjonsperioder().get(1);
        assertThat(andrePeriode.fom()).isEqualTo(PERIODE_MELLOM.plusDays(1));
        assertThat(andrePeriode.tom()).isEqualTo(PERIODE_TOM);
        assertThat(andrePeriode.refusjonPrMnd().verdi()).isEqualByComparingTo("875.25");
        assertThat(andrePeriode.utfall()).isEqualTo(RefusjonUtfall.INNVILGET);
        assertThat(andrePeriode.utfallÅrsak()).isNull();

        var personDto = resultat.refusjonsandeler().get(1);
        assertThat(personDto.arbeidsgiverIdent()).isEqualTo(aktørId.getId());
        assertThat(personDto.arbeidsforholdId()).isNull();
        assertThat(personDto.kilde()).isEqualTo(RefusjonKilde.INNTEKTSMELDING);
        assertThat(personDto.journalpostId()).isEqualTo(journalpostId);
        assertThat(personDto.refusjonsperioder()).hasSize(1);
        var personperiode = personDto.refusjonsperioder().get(0);
        assertThat(personperiode.fom()).isEqualTo(PERIODE_FOM);
        assertThat(personperiode.tom()).isEqualTo(PERIODE_TOM);
        assertThat(personperiode.refusjonPrMnd().verdi()).isEqualByComparingTo("640.00");
        assertThat(personperiode.utfall()).isNull();
        assertThat(personperiode.utfallÅrsak()).isNull();
    }

    @Test
    void tomt_fordelingsgrunnlag_mappes_til_tom_liste() {
        var resultat = guiTjeneste.lagFordelingsgrunnlagDto(inputMedFordelingsgrunnlag(new FordelingGrunnlagDto(List.of())));

        assertThat(resultat.refusjonsandeler()).isEmpty();
    }

    @Test
    void manglende_fordelingsgrunnlag_gir_tydelig_feil_uten_sensitive_verdier() {
        var input = new BeregningsgrunnlagGUIInput(new KoblingReferanseMock(PERIODE_FOM), null, List.of(), null);

        assertThatThrownBy(() -> guiTjeneste.lagFordelingsgrunnlagDto(input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Fordelingsgrunnlag mangler i GUI-input.");
    }

    private static BeregningsgrunnlagGUIInput inputMedFordelingsgrunnlag(FordelingGrunnlagDto grunnlag) {
        var input = new BeregningsgrunnlagGUIInput(new KoblingReferanseMock(PERIODE_FOM), null, List.of(), null);
        return input.medFordelingGrunnlag(grunnlag);
    }
}
