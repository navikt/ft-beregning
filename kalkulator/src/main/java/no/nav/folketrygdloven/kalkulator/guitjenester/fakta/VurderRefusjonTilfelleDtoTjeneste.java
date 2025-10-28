package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.frist.InntektsmeldingMedRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.FaktaOmBeregningDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.RefusjonskravForSentDto;

class VurderRefusjonTilfelleDtoTjeneste {
    // TODO refusjon: Denne filen kan slettes når vi har flyttet aksjonspunktet og kjørt gjennom gamle saker

	public void lagDto(BeregningsgrunnlagGUIInput input, FaktaOmBeregningDto faktaOmBeregningDto) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var tilfeller = beregningsgrunnlag.getFaktaOmBeregningTilfeller();
		if (!tilfeller.contains(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT)) {
			return;
		}
        var refusjonskravForSentListe = lagListeMedRefusjonskravForSent(input);
		faktaOmBeregningDto.setRefusjonskravSomKommerForSentListe(refusjonskravForSentListe);
	}

	private List<RefusjonskravForSentDto> lagListeMedRefusjonskravForSent(BeregningsgrunnlagGUIInput input) {
        var refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
				.map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
				.orElse(Collections.emptyList());

        var arbeidsgivere = InntektsmeldingMedRefusjonTjeneste.finnArbeidsgivereSomHarSøktRefusjonForSent(input.getIayGrunnlag(),
				input.getBeregningsgrunnlagGrunnlag(),
				input.getKravperioderPrArbeidsgiver(),
				input.getFagsakYtelseType());
		return arbeidsgivere
				.stream()
				.map(arbeidsgiver -> {
                    var dto = new RefusjonskravForSentDto();
					dto.setArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
					sjekkStatusPåRefusjon(arbeidsgiver.getIdentifikator(), refusjonOverstyringer, input.getSkjæringstidspunktForBeregning()).ifPresent(dto::setErRefusjonskravGyldig);
					return dto;
				}).collect(Collectors.toList());
	}

	private Optional<Boolean> sjekkStatusPåRefusjon(String identifikator,
	                                                List<BeregningRefusjonOverstyringDto> refusjonOverstyringer, LocalDate skjæringstidspunktForBeregning) {
        var statusOpt = refusjonOverstyringer
				.stream()
				.filter(refusjonOverstyring -> refusjonOverstyring.getArbeidsgiver().getIdentifikator().equals(identifikator))
				.findFirst();
		if (statusOpt.isEmpty() && refusjonOverstyringer.isEmpty()) {
			return Optional.empty();
		}

		return getErFristUtvidet(statusOpt, skjæringstidspunktForBeregning);
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
