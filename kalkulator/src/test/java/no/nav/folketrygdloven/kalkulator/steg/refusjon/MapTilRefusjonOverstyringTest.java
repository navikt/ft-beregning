package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.BeregningsgrunnlagInputTestUtil;
import no.nav.folketrygdloven.kalkulator.KoblingReferanseMock;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon.MapTilRefusjonOverstyring;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

class MapTilRefusjonOverstyringTest {
    private BeregningRefusjonOverstyringerDto eksisterendeOverstyringer;
    private KoblingReferanse koblingReferanse = new KoblingReferanseMock(LocalDate.now().minusDays(5));
    private BeregningsgrunnlagInput input;

    @Test
    public void skal_oppdatere_en_refusjonsperiode_når_ingen_finnes_fra_før() {
        // Arrange
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering = lagDtoForAG(Arbeidsgiver.virksomhet("999999999"), null, LocalDate.of(2020, 7, 1));

        // Act
        BeregningRefusjonOverstyringerDto resultat = håndter(lagDto(oppdatering));

        // Assert
        assertResultat(resultat, oppdatering);
    }

    @Test
    public void skal_oppdatere_flere_refusjonsperioder_ulik_ag_når_ingen_finnes_fra_før() {
        // Arrange
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering = lagDtoForAG(Arbeidsgiver.virksomhet("999999999"), null, LocalDate.of(2020, 7, 1));
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering2 = lagDtoForAG(Arbeidsgiver.virksomhet("999999998"), null, LocalDate.of(2020, 6, 1));
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering3 = lagDtoForAG(Arbeidsgiver.virksomhet("999999997"), null, LocalDate.of(2020, 5, 1));

        // Act
        BeregningRefusjonOverstyringerDto resultat = håndter(lagDto(oppdatering, oppdatering2, oppdatering3));

        // Assert
        assertResultat(resultat, oppdatering, oppdatering2, oppdatering3);
    }

    @Test
    public void skal_oppdatere_flere_refusjonsperioder_samme_ag_når_ingen_finnes_fra_før() {
        // Arrange
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering = lagDtoForAG(Arbeidsgiver.virksomhet("999999999"), UUID.randomUUID().toString(), LocalDate.of(2020, 7, 1));
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering2 = lagDtoForAG(Arbeidsgiver.virksomhet("999999999"), UUID.randomUUID().toString(), LocalDate.of(2020, 6, 1));
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering3 = lagDtoForAG(Arbeidsgiver.virksomhet("999999999"), UUID.randomUUID().toString(), LocalDate.of(2020, 5, 1));

        // Act
        BeregningRefusjonOverstyringerDto resultat = håndter(lagDto(oppdatering, oppdatering2, oppdatering3));

        // Assert
        assertResultat(resultat, oppdatering, oppdatering2, oppdatering3);
    }

    @Test
    public void skal_oppdatere_flere_refusjonsperioder_samme_ag_når_fra_dato_finnes_fra_før() {
        // Arrange
        Arbeidsgiver ag = Arbeidsgiver.virksomhet("999999999");
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering = lagDtoForAG(ag, UUID.randomUUID().toString(), LocalDate.of(2020, 7, 1));
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering2 = lagDtoForAG(ag, UUID.randomUUID().toString(), LocalDate.of(2020, 6, 1));
        VurderRefusjonAndelBeregningsgrunnlagDto oppdatering3 = lagDtoForAG(ag, UUID.randomUUID().toString(), LocalDate.of(2020, 5, 1));
        lagTidligereOverstyringBeregningsgrunnlag(ag);
        // Act
        BeregningRefusjonOverstyringerDto resultat = håndter(lagDto(oppdatering, oppdatering2, oppdatering3));

        // Assert
        assertResultat(resultat, oppdatering, oppdatering2, oppdatering3);
    }



    private void assertResultat(BeregningRefusjonOverstyringerDto resultat, VurderRefusjonAndelBeregningsgrunnlagDto... oppdateringDtoer) {
        List<VurderRefusjonAndelBeregningsgrunnlagDto> oppdateringer = Arrays.asList(oppdateringDtoer);
        List<BeregningRefusjonOverstyringDto> refusjonOverstyringer = resultat.getRefusjonOverstyringer();
        oppdateringer.forEach(dto -> assertDto(refusjonOverstyringer, dto));
    }

    private void assertDto(List<BeregningRefusjonOverstyringDto> refusjonOverstyringer, VurderRefusjonAndelBeregningsgrunnlagDto dto) {
        Arbeidsgiver ag;
        if (dto.getArbeidsgiverOrgnr() != null) {
            ag = Arbeidsgiver.virksomhet(dto.getArbeidsgiverOrgnr());
        } else {
            ag = Arbeidsgiver.person(new AktørId(dto.getArbeidsgiverAktørId()));
        }
        InternArbeidsforholdRefDto ref = dto.getInternArbeidsforholdRef() != null ? InternArbeidsforholdRefDto.ref(dto.getInternArbeidsforholdRef()) : null;
        BeregningRefusjonOverstyringDto refusjonOverstyringMatch = refusjonOverstyringer.stream().filter(os -> os.getArbeidsgiver().equals(ag)).findFirst().orElse(null);
        assertThat(refusjonOverstyringMatch).isNotNull();
        assertThat(refusjonOverstyringMatch.getArbeidsgiver()).isEqualTo(ag);
        BeregningRefusjonPeriodeDto matchendeRefusjonPeriode = refusjonOverstyringMatch.getRefusjonPerioder().stream()
                .filter(os -> Objects.equals(os.getArbeidsforholdRef(), ref))
                .findFirst().orElse(null);
        assertThat(matchendeRefusjonPeriode).isNotNull();
        if (ref == null) {
            assertThat(matchendeRefusjonPeriode.getArbeidsforholdRef().getReferanse()).isNull();
        } else {
            assertThat(matchendeRefusjonPeriode.getArbeidsforholdRef()).isEqualTo(ref);
        }
        assertThat(matchendeRefusjonPeriode.getStartdatoRefusjon()).isEqualTo(dto.getFastsattRefusjonFom());
    }

    private BeregningRefusjonOverstyringerDto håndter(VurderRefusjonBeregningsgrunnlagDto dto) {
        lagBehandlingMedBeregningsgrunnlag();
        return MapTilRefusjonOverstyring.map(dto, input);
    }

    private VurderRefusjonBeregningsgrunnlagDto lagDto(VurderRefusjonAndelBeregningsgrunnlagDto... dto) {
        return new VurderRefusjonBeregningsgrunnlagDto(Arrays.asList(dto));
    }

    private VurderRefusjonAndelBeregningsgrunnlagDto lagDtoForAG(Arbeidsgiver ag, String ref, LocalDate refusjonFra) {
        String aktørId = ag.getAktørId() != null ? ag.getIdentifikator() : null;
        String orgnr = ag.getOrgnr() != null ? ag.getIdentifikator() : null;
        return new VurderRefusjonAndelBeregningsgrunnlagDto(orgnr, aktørId, ref, refusjonFra, null);
    }

    private void lagTidligereOverstyringBeregningsgrunnlag(Arbeidsgiver ag) {
        BeregningRefusjonOverstyringDto refOS = new BeregningRefusjonOverstyringDto(ag, LocalDate.of(2020,4,1), false);
        eksisterendeOverstyringer = BeregningRefusjonOverstyringerDto.builder().leggTilOverstyring(refOS).build();
    }

    private void lagBehandlingMedBeregningsgrunnlag() {
        BeregningsgrunnlagGrunnlagDtoBuilder beregningsgrunnlagGrunnlagDto = BeregningsgrunnlagGrunnlagDtoBuilder.oppdatere(Optional.empty())
                .medRefusjonOverstyring(eksisterendeOverstyringer);
        input = BeregningsgrunnlagInputTestUtil.lagInputMedBeregningsgrunnlagGrunnlag(koblingReferanse, beregningsgrunnlagGrunnlagDto, BeregningsgrunnlagTilstand.VURDERT_REFUSJON);
    }

}
