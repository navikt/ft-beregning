package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.EgenNæringDto;

public class EgenNæringMapperTest {

    @Test
    public void skal_mappe_fra_entitet_til_dto() {
        OppgittOpptjeningDtoBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningDtoBuilder.EgenNæringBuilder.ny();

        egenNæringBuilder.medVirksomhetType(VirksomhetType.FISKE);
        egenNæringBuilder.medVirksomhet("923609016");
        egenNæringBuilder.medBegrunnelse("Dette e ren begrunnelse");
        egenNæringBuilder.medBruttoInntekt(Beløp.fra(123123123));
        egenNæringBuilder.medEndringDato(LocalDate.now().minusMonths(4));
        egenNæringBuilder.medVarigEndring(true);
        egenNæringBuilder.medNyoppstartet(false);

        OppgittEgenNæringDto egenNæring = egenNæringBuilder.build();

        EgenNæringDto dto = EgenNæringMapper.map(egenNæring);

        assertThat(dto).isNotNull();
        assertThat(dto.getBegrunnelse()).isEqualTo(egenNæring.getBegrunnelse());
        assertThat(dto.getEndringsdato()).isEqualTo(egenNæring.getEndringDato());
        assertThat(dto.getVirksomhetType()).isEqualTo(no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType.FISKE);
        assertThat(dto.getOppgittInntekt()).isEqualTo(ModellTyperMapper.beløpTilDto(egenNæring.getBruttoInntekt()));
        assertThat(dto.getOrgnr()).isEqualTo(egenNæring.getOrgnr());
        assertThat(dto.isErVarigEndret()).isEqualTo(egenNæring.getVarigEndring());
        assertThat(dto.isErNyoppstartet()).isEqualTo(egenNæring.getNyoppstartet());
    }

}
