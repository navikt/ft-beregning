package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.folketrygdloven.kalkulus.mappers.KontraktTestJsonMapper;

class FastsettBeregningsgrunnlagATFLHåndteringDtoTest {

    @Test
    void serTest() throws JsonProcessingException {
        var input = new FastsettBeregningsgrunnlagATFLHåndteringDto(List.of(new InntektPrAndelDto(100, 1L)), 1000);
        var resultat = KontraktTestJsonMapper.getMapper().writeValueAsString(input);
        assertThat(resultat).isNotBlank();
    }

    @Test
    void desTest() throws JsonProcessingException {
        var input = """
            {
                "avklaringsbehovKode": "FASTSETT_BG_AT_FL",
                "inntektFrilanser": 1000,
                "avklaringsbehovDefinisjon": "FASTSETT_BG_AT_FL",
                "avbrutt": false,
                "inntektPrAndelList": [{
                        "inntekt": 100,
                        "andelsnr": 1
                    }
                ]
            }
            """;

        var resutat = KontraktTestJsonMapper.getMapper().readValue(input, FastsettBeregningsgrunnlagATFLHåndteringDto.class);

        assertThat(resutat).isNotNull();
        assertThat(resutat.getAvklaringsbehovDefinisjon()).isNotNull().isEqualTo(FASTSETT_BG_AT_FL);
    }
}
