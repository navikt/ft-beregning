package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import no.nav.folketrygdloven.kalkulator.guitjenester.ModellTyperMapper;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.EgenNæringDto;

public final class EgenNæringMapper {

    private EgenNæringMapper() {
        // Skjuler default
    }

    public static EgenNæringDto map(OppgittEgenNæringDto egenNæring) {
        EgenNæringDto dto = new EgenNæringDto();
        dto.setOrgnr(egenNæring.getOrgnr());
        dto.setVirksomhetType(egenNæring.getVirksomhetType());
        dto.setBegrunnelse(egenNæring.getBegrunnelse());
        dto.setEndringsdato(egenNæring.getEndringDato());
        dto.setOppstartsdato(egenNæring.getPeriode() != null ? egenNæring.getFraOgMed()  : null);
        dto.setOpphørsdato(egenNæring.getPeriode() != null ? egenNæring.getTilOgMed()  : null);
        dto.setErVarigEndret(egenNæring.getVarigEndring());
        dto.setErNyoppstartet(egenNæring.getNyoppstartet());
        dto.setErNyIArbeidslivet(egenNæring.getNyIArbeidslivet());
        dto.setRegnskapsførerNavn(egenNæring.getRegnskapsførerNavn());
        dto.setRegnskapsførerTlf(egenNæring.getRegnskapsførerTlf());
        dto.setOppgittInntekt(ModellTyperMapper.beløpTilDto(egenNæring.getBruttoInntekt()));
        return dto;
    }

}
