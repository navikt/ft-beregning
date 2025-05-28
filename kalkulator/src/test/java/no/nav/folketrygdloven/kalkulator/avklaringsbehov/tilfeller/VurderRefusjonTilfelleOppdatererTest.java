package no.nav.folketrygdloven.kalkulator.avklaringsbehov.tilfeller;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.FaktaBeregningLagreDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.RefusjonskravPrArbeidsgiverVurderingDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

class VurderRefusjonTilfelleOppdatererTest {
    private static final Arbeidsgiver VIRKSOMHET = Arbeidsgiver.virksomhet("973861778");
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = Beløp.fra(600000);

    private KoblingReferanse referanse = new KoblingReferanseMock(SKJÆRINGSTIDSPUNKT);


    @Test
    void oppdater_når_ikkje_gyldig_utvidelse() {
        // Arrange
        var førsteInnsendingAvRefusjonskrav = SKJÆRINGSTIDSPUNKT.plusMonths(4);
        var førsteDatoMedRefusjonskrav = SKJÆRINGSTIDSPUNKT;
        var refusjonskravDatoDto = lagArbeidsgiverSøktForSent(førsteDatoMedRefusjonskrav, førsteInnsendingAvRefusjonskrav);
        var beregningsgrunnlagDto = lagBeregningsgrunnlag();
        var input = new BeregningsgrunnlagInput(referanse, null, null, List.of(refusjonskravDatoDto), null);
        var beregningsgrunnlagInput = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlagDto).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));
        var dto = lagDto(false);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag());
        VurderRefusjonTilfelleOppdaterer.oppdater(dto, beregningsgrunnlagInput, oppdatere);

        var nyttGrunnlag = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Assert
        assertOverstyringAvRefusjon(nyttGrunnlag, null, false);
    }

    @Test
    void oppdater_når_gyldig_utvidelse() {
        // Arrange
        var førsteInnsendingAvRefusjonskrav = SKJÆRINGSTIDSPUNKT.plusMonths(4);
        var førsteDatoMedRefusjonskrav = SKJÆRINGSTIDSPUNKT;
        var refusjonskravDatoDto = lagArbeidsgiverSøktForSent(førsteDatoMedRefusjonskrav, førsteInnsendingAvRefusjonskrav);
        var beregningsgrunnlagDto = lagBeregningsgrunnlag();
        var input = new BeregningsgrunnlagInput(referanse, null, null, List.of(refusjonskravDatoDto), null);
        var beregningsgrunnlagInput = input.medBeregningsgrunnlagGrunnlag(BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty()).medBeregningsgrunnlag(beregningsgrunnlagDto).build(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER));
        var dto = lagDto(true);

        // Act
        var oppdatere = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(beregningsgrunnlagInput.getBeregningsgrunnlagGrunnlag());
        VurderRefusjonTilfelleOppdaterer.oppdater(dto, beregningsgrunnlagInput, oppdatere);

        var nyttGrunnlag = oppdatere.build(BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Assert
        assertOverstyringAvRefusjon(nyttGrunnlag, førsteDatoMedRefusjonskrav, true);
    }


    private void assertOverstyringAvRefusjon(BeregningsgrunnlagGrunnlagDto nyttGrunnlag, LocalDate førsteMuligeDato, Boolean skalUtvideGydlighet) {
        assertThat(nyttGrunnlag.getRefusjonOverstyringer()).isPresent();
        var beregningRefusjonOverstyringer = nyttGrunnlag.getRefusjonOverstyringer().get();
        var overstyringer = beregningRefusjonOverstyringer.getRefusjonOverstyringer();
        assertThat(overstyringer).hasSize(1);
        assertThat(overstyringer.get(0).getArbeidsgiver()).isEqualTo(VIRKSOMHET);
        assertThat(overstyringer.get(0).getFørsteMuligeRefusjonFom().orElse(null)).isEqualTo(førsteMuligeDato);
        assertThat(overstyringer.get(0).getErFristUtvidet().orElse(null)).isEqualTo(skalUtvideGydlighet);

    }

    private FaktaBeregningLagreDto lagDto(boolean skalUtvideGyldighet) {
        var dto = new FaktaBeregningLagreDto(List.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT));

        var ref1 = new RefusjonskravPrArbeidsgiverVurderingDto(VIRKSOMHET.getIdentifikator(), skalUtvideGyldighet);
        dto.setRefusjonskravGyldighet(List.of(ref1));
        return dto;
    }

    private KravperioderPrArbeidsforholdDto lagArbeidsgiverSøktForSent(LocalDate førsteDagMedRefusjonskrav, LocalDate førsteInnsendingAvRefusjonskrav) {
        return new KravperioderPrArbeidsforholdDto(VIRKSOMHET, InternArbeidsforholdRefDto.nullRef(),
                List.of(new PerioderForKravDto(førsteInnsendingAvRefusjonskrav, List.of(new RefusjonsperiodeDto(
                        Intervall.fraOgMedTilOgMed(førsteDagMedRefusjonskrav, TIDENES_ENDE),
                        Beløp.fra(10))))),
                List.of(Intervall.fraOgMedTilOgMed(førsteDagMedRefusjonskrav, TIDENES_ENDE)));
    }

    private BeregningsgrunnlagDto lagBeregningsgrunnlag() {
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilFaktaOmBeregningTilfeller(List.of(FaktaOmBeregningTilfelle.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT))
            .build();
        var periode1 = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforholdDto.builder().medArbeidsgiver(VIRKSOMHET))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode1);
        return beregningsgrunnlag;
    }
}
