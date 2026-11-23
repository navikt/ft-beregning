package no.nav.folketrygdloven.kalkulator.avklaringsbehov;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.OverstyrRefusjonDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonOverstyringAndel.RefusjonOverstyringPeriode;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;

class RefusjonOverstyringHåndtererTest {

    private static final Arbeidsgiver ARBEIDSGIVER = Arbeidsgiver.virksomhet("999999999");
    private static final Arbeidsgiver ANNEN_ARBEIDSGIVER = Arbeidsgiver.virksomhet("888888888");
    private static final InternArbeidsforholdRefDto REF = InternArbeidsforholdRefDto.nullRef();
    private static final LocalDate FOM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TOM = LocalDate.of(2026, 1, 31);

    private final Refusjonsandel inntektsmelding = new Refusjonsandel(ARBEIDSGIVER, REF, RefusjonKilde.INNTEKTSMELDING,
        new JournalpostId("1"), List.of(periode(10000)));
    private final Refusjonsandel gammelSaksbehandlerandel = new Refusjonsandel(ARBEIDSGIVER, REF, RefusjonKilde.SAKSBEHANDLER, null,
        List.of(periode(20000)));

    @Test
    void erstatter_saksbehandlers_andel_og_beholder_inntektsmeldingen() {
        var forrige = new FordelingGrunnlag(List.of(inntektsmelding, gammelSaksbehandlerandel));

        var nytt = RefusjonOverstyringHåndterer.lagNyttFordelingGrunnlag(forrige,
            overstyring(overstyrtAndel(ARBEIDSGIVER, RefusjonKilde.SAKSBEHANDLER, 15000)));

        assertThat(nytt.refusjonsandeler()).hasSize(2).contains(inntektsmelding).doesNotContain(gammelSaksbehandlerandel);
        assertThat(saksbehandlerandel(nytt).refusjonsperioder()).containsExactly(periode(15000));
        assertThat(saksbehandlerandel(nytt).journalpostId()).isNull();
        assertThat(forrige.refusjonsandeler()).containsExactly(inntektsmelding, gammelSaksbehandlerandel);
    }

    @Test
    void legger_til_saksbehandlers_andel_når_den_ikke_finnes_fra_før() {
        var forrige = new FordelingGrunnlag(List.of(inntektsmelding));

        var nytt = RefusjonOverstyringHåndterer.lagNyttFordelingGrunnlag(forrige,
            overstyring(overstyrtAndel(ANNEN_ARBEIDSGIVER, RefusjonKilde.SAKSBEHANDLER, 5000)));

        assertThat(nytt.refusjonsandeler()).hasSize(2).contains(inntektsmelding);
        assertThat(saksbehandlerandel(nytt).arbeidsgiver()).isEqualTo(ANNEN_ARBEIDSGIVER);
    }

    @Test
    void ignorerer_overstyrte_andeler_med_kilde_inntektsmelding() {
        var forrige = new FordelingGrunnlag(List.of(inntektsmelding));

        var nytt = RefusjonOverstyringHåndterer.lagNyttFordelingGrunnlag(forrige,
            overstyring(overstyrtAndel(ARBEIDSGIVER, RefusjonKilde.INNTEKTSMELDING, 99999)));

        assertThat(nytt.refusjonsandeler()).containsExactly(inntektsmelding);
    }

    @Test
    void siste_overstyrte_andel_vinner_ved_duplikater() {
        var forrige = new FordelingGrunnlag(List.of());

        var nytt = RefusjonOverstyringHåndterer.lagNyttFordelingGrunnlag(forrige, overstyring(
            overstyrtAndel(ARBEIDSGIVER, RefusjonKilde.SAKSBEHANDLER, 15000),
            overstyrtAndel(ARBEIDSGIVER, RefusjonKilde.SAKSBEHANDLER, 12000)));

        assertThat(nytt.refusjonsandeler()).hasSize(1);
        assertThat(saksbehandlerandel(nytt).refusjonsperioder()).containsExactly(periode(12000));
    }

    private static Refusjonsandel saksbehandlerandel(FordelingGrunnlag grunnlag) {
        return grunnlag.refusjonsandeler().stream().filter(a -> a.kilde() == RefusjonKilde.SAKSBEHANDLER).findFirst().orElseThrow();
    }

    private static OverstyrRefusjonDto overstyring(RefusjonOverstyringAndel... andeler) {
        return new OverstyrRefusjonDto(List.of(andeler));
    }

    private static RefusjonOverstyringAndel overstyrtAndel(Arbeidsgiver arbeidsgiver, RefusjonKilde kilde, int refusjonPrMnd) {
        return new RefusjonOverstyringAndel(arbeidsgiver, REF, kilde,
            List.of(new RefusjonOverstyringPeriode(FOM, TOM, BigDecimal.valueOf(refusjonPrMnd), RefusjonUtfall.INNVILGET, null)));
    }

    private static Refusjonsperiode periode(int refusjonPrMnd) {
        return new Refusjonsperiode(Intervall.fraOgMedTilOgMed(FOM, TOM), Beløp.fra(BigDecimal.valueOf(refusjonPrMnd)),
            RefusjonUtfall.INNVILGET, null);
    }
}
