package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonskravPrArbeidsgiverVurderingDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;

public class VurderRefusjonTilfelleOppdaterer {


    private VurderRefusjonTilfelleOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        var gyldighetPrArbeidsgiver = dto.getRefusjonskravGyldighet();
        var frist = input.getBeregningsgrunnlag().getSkjæringstidspunkt();
        var beregningRefusjonOverstyringer = map(gyldighetPrArbeidsgiver, frist);
        grunnlagBuilder.medRefusjonOverstyring(beregningRefusjonOverstyringer);
    }

    private static BeregningRefusjonOverstyringerDto map(List<RefusjonskravPrArbeidsgiverVurderingDto> dto, LocalDate frist) {
        var builder = BeregningRefusjonOverstyringerDto.builder();
        for (var vurderingDto : dto) {
            var arbeidsgiver = finnArbeidsgiver(vurderingDto.getArbeidsgiverId());
            if (vurderingDto.isSkalUtvideGyldighet()) {
                builder.leggTilOverstyring(new BeregningRefusjonOverstyringDto(arbeidsgiver, frist, true));
            } else {
                builder.leggTilOverstyring(new BeregningRefusjonOverstyringDto(arbeidsgiver,false));
            }
        }
        return builder.build();
    }

    private static Arbeidsgiver finnArbeidsgiver(String identifikator) {
        if (OrgNummer.erGyldigOrgnr(identifikator)) {
            return Arbeidsgiver.virksomhet(identifikator);
        }
        return Arbeidsgiver.fra(new AktørId(identifikator));
    }

}
