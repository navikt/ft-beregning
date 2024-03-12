package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.LønnsendringTjeneste.finnAktiviteterMedLønnsendringUtenInntektsmeldingIBeregningsperiodenOgTilStp;

import java.util.Collection;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;

public class VurderLønnsendringOppdaterer {

    private VurderLønnsendringOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, Optional<BeregningsgrunnlagDto> forrigeBg, BeregningsgrunnlagInput input, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        var lønnsendringDto = dto.getVurdertLonnsendring();
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var arbeidstakerAndeler = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().stream()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
                .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
                .toList();
        var aktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringUtenInntektsmeldingIBeregningsperiodenOgTilStp(beregningsgrunnlag, input.getIayGrunnlag(), input.getInntektsmeldinger());
        var faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        arbeidstakerAndeler.stream().map(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(arb -> aktiviteterMedLønnsendring.stream().anyMatch(ya -> ya.getArbeidsgiver().equals(arb.getArbeidsgiver()) &&
                        ya.getArbeidsforholdRef().gjelderFor(arb.getArbeidsforholdRef())))
                .forEach(arb -> {
                    FaktaArbeidsforholdDto.Builder faktaArbBuilder = faktaAggregatBuilder.getFaktaArbeidsforholdBuilderFor(arb.getArbeidsgiver(), arb.getArbeidsforholdRef())
                            .medHarLønnsendringIBeregningsperiodenFastsattAvSaksbehandler(lønnsendringDto.erLønnsendringIBeregningsperioden());
                    faktaAggregatBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
                });
        try {
            var faktaAggregat = faktaAggregatBuilder.build();
            grunnlagBuilder.medFaktaAggregat(faktaAggregat);
        } catch (Exception e) {
            var iayArbeid = input.getIayGrunnlag().getAktørArbeidFraRegister().orElse(null);
            String feilmelding = String.format("Klarte ikke bygge faktaaggregat. Input fra frontend: %s. Forrige bg: %s. iayArbeid: %s. Beregningsgrunnlag %s", dto.getVurdertLonnsendring(), forrigeBg, iayArbeid, beregningsgrunnlag);
            throw new IllegalStateException(feilmelding + e);
        }

    }
}
