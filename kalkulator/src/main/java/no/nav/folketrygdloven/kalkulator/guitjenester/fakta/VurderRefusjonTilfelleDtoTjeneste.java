package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravSomKommerForSentDto;

class VurderRefusjonTilfelleDtoTjeneste {

    public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        BeregningsgrunnlagDto beregningsgrunnlag = input.getBeregningsgrunnlag();
        List<FaktaOmBeregningTilfelle> tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
        if (!tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT)) {
            return;
        }
        List<RefusjonskravSomKommerForSentDto> refusjonskravSomKommerForSentList = lagListeMedKravSomKommerForSent(input);
        faktaOmBeregningDto.setRefusjonskravSomKommerForSentListe(refusjonskravSomKommerForSentList);
    }

    private List<RefusjonskravSomKommerForSentDto> lagListeMedKravSomKommerForSent(BeregningsgrunnlagGUIInput input) {
        List<BeregningRefusjonOverstyringDto> refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
            .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
            .orElse(Collections.emptyList());

        Set<Arbeidsgiver> arbeidsgivere = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgiverSomHarSøktRefusjonForSent(
                input.getKoblingReferanse(),
                input.getIayGrunnlag(),
                input.getBeregningsgrunnlagGrunnlag(),
                input.getKravperioderPrArbeidsgiver(),
                input.getFagsakYtelseType());
        return arbeidsgivere
            .stream()
            .map(arbeidsgiver -> {
                RefusjonskravSomKommerForSentDto dto = new RefusjonskravSomKommerForSentDto();
                dto.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
                dto.setErRefusjonskravGyldig(sjekkStatusPåRefusjon(arbeidsgiver.getIdentifikator(), refusjonOverstyringer, input.getSkjæringstidspunktForBeregning()));

                return dto;
            }).collect(Collectors.toList());
    }

    private Boolean sjekkStatusPåRefusjon(String identifikator,
                                          List<BeregningRefusjonOverstyringDto> refusjonOverstyringer, LocalDate skjæringstidspunktForBeregning) {
        Optional<BeregningRefusjonOverstyringDto> statusOpt = refusjonOverstyringer
            .stream()
            .filter(refusjonOverstyring -> refusjonOverstyring.getArbeidsgiver().getIdentifikator().equals(identifikator))
            .findFirst();
        if (statusOpt.isEmpty() && refusjonOverstyringer.isEmpty()) {
            return null;
        }

        Optional<Boolean> erFristUtvidet = getErFristUtvidet(statusOpt, skjæringstidspunktForBeregning);
        return erFristUtvidet.orElse(false);
    }

    private Optional<Boolean> getErFristUtvidet(Optional<BeregningRefusjonOverstyringDto> statusOpt, LocalDate skjæringstidspunktForBeregning) {
        return statusOpt.flatMap(o -> {
                    if (o.getFørsteMuligeRefusjonFom().isPresent()) {
                        return Optional.of(skjæringstidspunktForBeregning.isEqual(o.getFørsteMuligeRefusjonFom().get()));
                    }
                    return o.getErFristUtvidet();
                }
        );
    }
}
