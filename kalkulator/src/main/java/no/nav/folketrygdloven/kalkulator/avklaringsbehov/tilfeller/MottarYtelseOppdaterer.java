package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.ArbeidstakerandelUtenIMMottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.MottarYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaArbeidsforholdDto;

public class MottarYtelseOppdaterer {

    private MottarYtelseOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto,
                                BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        MottarYtelseDto mottarYtelseDto = dto.getMottarYtelse();
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        var andelListe = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        FaktaAggregatDto.Builder faktaBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        if  (mottarYtelseDto.getFrilansMottarYtelse() != null) {
            settMottarYtelseForFrilans(mottarYtelseDto, faktaBuilder);
        }
        mottarYtelseDto.getArbeidstakerUtenIMMottarYtelse()
                .forEach(arbMottarYtelse -> settMottarYtelseForArbeid(andelListe, faktaBuilder, arbMottarYtelse));
        grunnlagBuilder.medFaktaAggregat(faktaBuilder.build());
    }

    private static void settMottarYtelseForFrilans(MottarYtelseDto mottarYtelseDto, FaktaAggregatDto.Builder faktaBuilder) {
        FaktaAktørDto.Builder faktaAktørBuilder = faktaBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medHarFLMottattYtelseFastsattAvSaksbehandler(mottarYtelseDto.getFrilansMottarYtelse());
        faktaBuilder.medFaktaAktør(faktaAktørBuilder.build());
    }

    private static void settMottarYtelseForArbeid(List<BeregningsgrunnlagPrStatusOgAndelDto> andelListe, FaktaAggregatDto.Builder faktaBuilder, ArbeidstakerandelUtenIMMottarYtelseDto arbMottarYtelse) {
        andelListe.stream()
                .filter(a -> a.getAndelsnr().equals(arbMottarYtelse.getAndelsnr()))
                .findFirst()
                .flatMap(BeregningsgrunnlagPrStatusOgAndelDto::getBgAndelArbeidsforhold)
                .ifPresent(arb -> settMottarYtelseForArbeidsforhold(faktaBuilder, arbMottarYtelse, arb));
    }

    private static void settMottarYtelseForArbeidsforhold(FaktaAggregatDto.Builder faktaBuilder, ArbeidstakerandelUtenIMMottarYtelseDto arbMottarYtelse, BGAndelArbeidsforholdDto arb) {
        FaktaArbeidsforholdDto.Builder faktaArbBuilder = faktaBuilder.getFaktaArbeidsforholdBuilderFor(arb.getArbeidsgiver(), arb.getArbeidsforholdRef())
                .medHarMottattYtelseFastsattAvSaksbehandler(arbMottarYtelse.getMottarYtelse());
        faktaBuilder.erstattEksisterendeEllerLeggTil(faktaArbBuilder.build());
    }

}
