package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderMilitærDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.FaktaAktørDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class VurderMilitærOppdaterer {

    private VurderMilitærOppdaterer() {
    }

    public static void oppdater(FaktaBeregningLagreDto dto, BeregningsgrunnlagGrunnlagDtoBuilder grunnlagBuilder) {
        VurderMilitærDto militærDto = dto.getVurderMilitaer();
        BeregningsgrunnlagDto beregningsgrunnlag = grunnlagBuilder.getBeregningsgrunnlagBuilder().getBeregningsgrunnlag();
        if (militærDto.getHarMilitaer()) {
            leggTilMilitærstatusOgAndelHvisIkkeFinnes(beregningsgrunnlag);
        } else {
            slettMilitærStatusOgAndelHvisFinnes(beregningsgrunnlag);
        }

        // Setter fakta aggregat
        FaktaAggregatDto.Builder faktaAggregatBuilder = grunnlagBuilder.getFaktaAggregatBuilder();
        FaktaAktørDto.Builder faktaAktørBuilder = faktaAggregatBuilder.getFaktaAktørBuilder();
        faktaAktørBuilder.medErMilitærSiviltjenesteFastsattAvSaksbehandler(militærDto.getHarMilitaer());
        faktaAggregatBuilder.medFaktaAktør(faktaAktørBuilder.build());
        grunnlagBuilder.medFaktaAggregat(faktaAggregatBuilder.build());
    }

    private static void slettMilitærStatusOgAndelHvisFinnes(BeregningsgrunnlagDto nyttBeregningsgrunnlag) {
        BeregningsgrunnlagDto.Builder grunnlagUtenMilitærBuilder = BeregningsgrunnlagDto.Builder.oppdater(Optional.of(nyttBeregningsgrunnlag));
        if (harMilitærstatus(nyttBeregningsgrunnlag)) {
            grunnlagUtenMilitærBuilder.fjernAktivitetstatus(AktivitetStatus.MILITÆR_ELLER_SIVIL);
        }
        BeregningsgrunnlagDto grunnlagUtenMilitær = grunnlagUtenMilitærBuilder.build();
        grunnlagUtenMilitær.getBeregningsgrunnlagPerioder().forEach(periode -> {
            if (harMilitærandel(periode)) {
                fjernMilitærFraPeriode(grunnlagUtenMilitær, periode);
            }
        });
    }

    private static void fjernMilitærFraPeriode(BeregningsgrunnlagDto grunnlagUtenMilitær, BeregningsgrunnlagPeriodeDto periode) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleMilitærandeler = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> andel.getAktivitetStatus().equals(AktivitetStatus.MILITÆR_ELLER_SIVIL))
            .collect(Collectors.toList());
        BeregningsgrunnlagPeriodeDto.Builder periodeBuilder = BeregningsgrunnlagPeriodeDto.oppdater(periode);
        alleMilitærandeler.forEach(periodeBuilder::fjernBeregningsgrunnlagPrStatusOgAndel);
        periodeBuilder.build(grunnlagUtenMilitær);
    }

    private static void leggTilMilitærstatusOgAndelHvisIkkeFinnes(BeregningsgrunnlagDto nyttBeregningsgrunnlag) {
        nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(VurderMilitærOppdaterer::leggTilMilitærAndelOmDenIkkeFinnes);
        if (!harMilitærstatus(nyttBeregningsgrunnlag)) {
            BeregningsgrunnlagAktivitetStatusDto.Builder aktivitetBuilder = BeregningsgrunnlagAktivitetStatusDto.builder().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL).medHjemmel(Hjemmel.F_14_7);
            BeregningsgrunnlagDto.Builder.oppdater(Optional.of(nyttBeregningsgrunnlag)).leggTilAktivitetStatus(aktivitetBuilder);
        }
    }

    private static void leggTilMilitærAndelOmDenIkkeFinnes(BeregningsgrunnlagPeriodeDto periode) {
        if (!harMilitærandel(periode)) {
            BeregningsgrunnlagPrStatusOgAndelDto.Builder.ny().medAktivitetStatus(AktivitetStatus.MILITÆR_ELLER_SIVIL).medInntektskategori(Inntektskategori.ARBEIDSTAKER).build(periode);
        }
    }

    private static boolean harMilitærandel(BeregningsgrunnlagPeriodeDto førstePeriode) {
        return førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .anyMatch(andel -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(andel.getAktivitetStatus()));
    }

    private static boolean harMilitærstatus(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream()
            .anyMatch(status -> AktivitetStatus.MILITÆR_ELLER_SIVIL.equals(status.getAktivitetStatus()));
    }

}
