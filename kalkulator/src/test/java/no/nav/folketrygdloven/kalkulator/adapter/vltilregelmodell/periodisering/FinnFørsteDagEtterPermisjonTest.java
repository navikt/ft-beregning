package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;

class FinnFørsteDagEtterPermisjonTest {
    private static final LocalDate STP = LocalDate.of(2022,6,1);


    @Test
    public void tester_ingen_permisjoner() {
        var ya = lagYA("999999999", InternArbeidsforholdRefDto.nullRef(), førStp(100), etterStp(100));
        var filter = new PermisjonFilter(Collections.emptyList(), Collections.singletonList(ya), STP);
        var førsteAnsettelsesdagUtenPerm = FinnFørsteDagEtterPermisjon.finn(Collections.singletonList(ya), Periode.of(førStp(100), etterStp(100)), STP, filter);

        assertThat(førsteAnsettelsesdagUtenPerm).isPresent();
        assertThat(førsteAnsettelsesdagUtenPerm.get()).isEqualTo(førStp(100));
    }

    @Test
    public void tester_en_permisjon() {
        var ya = lagYA("999999999", InternArbeidsforholdRefDto.nullRef(), førStp(100), etterStp(100), lagPerm(førStp(50), etterStp(10)));
        var filter = new PermisjonFilter(Collections.emptyList(), Collections.singletonList(ya), STP);
        var førsteAnsettelsesdagUtenPerm = FinnFørsteDagEtterPermisjon.finn(Collections.singletonList(ya), Periode.of(førStp(100), etterStp(100)), STP, filter);

        assertThat(førsteAnsettelsesdagUtenPerm).isPresent();
        assertThat(førsteAnsettelsesdagUtenPerm.get()).isEqualTo(etterStp(11));
    }

    @Test
    public void tester_flere_ya_en_med_permisjon() {
        var ya1 = lagYA("999999999", InternArbeidsforholdRefDto.nyRef(), førStp(100), etterStp(100), lagPerm(førStp(50), etterStp(10)));
        var ya2 = lagYA("999999999", InternArbeidsforholdRefDto.nyRef(), førStp(100), etterStp(100));
        var filter = new PermisjonFilter(Collections.emptyList(), Arrays.asList(ya1, ya2), STP);
        var førsteAnsettelsesdagUtenPerm = FinnFørsteDagEtterPermisjon.finn(Arrays.asList(ya1, ya2), Periode.of(førStp(100), etterStp(100)), STP, filter);

        assertThat(førsteAnsettelsesdagUtenPerm).isPresent();
        assertThat(førsteAnsettelsesdagUtenPerm.get()).isEqualTo(førStp(100));
    }

    private LocalDate førStp(int dager) {
        return STP.minusDays(dager);
    }

    private LocalDate etterStp(int dager) {
        return STP.plusDays(dager);
    }

    private PermisjonDtoBuilder lagPerm(LocalDate fom, LocalDate tom) {
        return PermisjonDtoBuilder.ny()
                .medPeriode(Intervall.fraOgMedTilOgMed(fom, tom))
                .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.PERMISJON)
                .medProsentsats(Stillingsprosent.HUNDRED);
    }

    private YrkesaktivitetDto lagYA(String orgnr, InternArbeidsforholdRefDto ref, LocalDate fom, LocalDate tom, PermisjonDtoBuilder... permisjoner) {
        var yaBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty()).medArbeidsgiver(Arbeidsgiver.virksomhet(orgnr)).medArbeidsforholdId(ref);
        var aaBuilder = yaBuilder.getAktivitetsAvtaleBuilder().medErAnsettelsesPeriode(true).medPeriode(Intervall.fraOgMedTilOgMed(fom, tom));
        yaBuilder.leggTilAktivitetsAvtale(aaBuilder);
        Arrays.asList(permisjoner).forEach(yaBuilder::leggTilPermisjon);
        return yaBuilder.build();
    }

}
