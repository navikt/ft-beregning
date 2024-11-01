package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta.FastsattePerioderTidsbegrensetDto;
import no.nav.folketrygdloven.kalkulus.mappers.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL;
import static org.assertj.core.api.Assertions.assertThat;

class FastsettBeregningsgrunnlagATFLHåndteringDtoTest {

    @Test
    void serTest() throws JsonProcessingException {
        var input = new FastsettBeregningsgrunnlagATFLHåndteringDto(List.of(new InntektPrAndelDto(100, 1L)), 1000, List.of(new FastsattePerioderTidsbegrensetDto(LocalDate.now(), LocalDate.now().plusDays(10), List.of())));
        var resultat = JsonMapper.getMapper().writeValueAsString(input);
        assertThat(resultat).isNotBlank();
    }

    @Test
    void desTest() throws JsonProcessingException {
        var input = "{\"avklaringsbehovKode\":\"FASTSETT_BG_AT_FL\",\"inntektFrilanser\":1000,\"fastsatteTidsbegrensedePerioder\":[{\"periodeFom\":\"2024-10-31\",\"periodeTom\":\"2024-11-10\",\"fastsatteTidsbegrensedeAndeler\":[]}],\"avklaringsbehovDefinisjon\":\"FASTSETT_BG_AT_FL\",\"avbrutt\":false,\"inntektPrAndelList\":[{\"inntekt\":100,\"andelsnr\":1}]}";

        var resutat = JsonMapper.getMapper().readValue(input, FastsettBeregningsgrunnlagATFLHåndteringDto.class);

        assertThat(resutat).isNotNull();
        assertThat(resutat.getAvklaringsbehovDefinisjon()).isNotNull().isEqualTo(FASTSETT_BG_AT_FL);
    }
}