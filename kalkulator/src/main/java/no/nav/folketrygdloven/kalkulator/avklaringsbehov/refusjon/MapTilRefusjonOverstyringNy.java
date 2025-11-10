package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public final class MapTilRefusjonOverstyringNy {

    private MapTilRefusjonOverstyringNy() {
        // Skjuler default
    }

    public static BeregningRefusjonOverstyringerDto map(VurderRefusjonBeregningsgrunnlagDto dto, LocalDate skjæringstidspunkt) {
        var nyttRefusjonAggregat = BeregningRefusjonOverstyringerDto.builder();
        // TODO: Undersøk om vi trenger Map. Kan det finnes flere andeler per arbeidsgiver?
        var vurderingerSortertPåAG = dto.getAndeler().stream()
            .collect(Collectors.groupingBy(VurderRefusjonAndelBeregningsgrunnlagDto::getArbeidsgiver));

        lagListeMedRefusjonOverstyringer(vurderingerSortertPåAG, skjæringstidspunkt)
            .forEach(nyttRefusjonAggregat::leggTilOverstyring);

        return nyttRefusjonAggregat.build();
    }

    private static List<BeregningRefusjonOverstyringDto> lagListeMedRefusjonOverstyringer(Map<Arbeidsgiver, List<VurderRefusjonAndelBeregningsgrunnlagDto>> vurderingerSortertPåAG, LocalDate skjæringstidspunkt) {
        List<BeregningRefusjonOverstyringDto> liste = new ArrayList<>();

        for (var entry : vurderingerSortertPåAG.entrySet()) {
            var ag = entry.getKey();
            var nyRefusjonOverstyring = lagNyOverstyringMedRefusjonskravVurdering(ag, entry.getValue(), skjæringstidspunkt);
            nyRefusjonOverstyring.getFørsteMuligeRefusjonFom().ifPresent(dato-> validerStartdato(dato, entry.getValue()));
            liste.add(nyRefusjonOverstyring);
        }
        return liste;
    }

    private static void validerStartdato(LocalDate tidligsteStartdato, List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler) {
        var ugyldigOverstyring = fastsatteAndeler.stream()
            .filter(andel -> andel.getFastsattRefusjonFom() != null && andel.getFastsattRefusjonFom().isBefore(tidligsteStartdato))
            .findFirst();
        if (ugyldigOverstyring.isPresent()) {
            throw new KalkulatorException("FT-401650", String.format(
                "Det finnes en startdato for refusjon dato som er før tidligste tillate startdato for refusjon. Startdato var %s og tidligste tillate startdato var %s",
                ugyldigOverstyring.get().getFastsattRefusjonFom(), tidligsteStartdato));
        }
    }

    private static BeregningRefusjonOverstyringDto lagNyOverstyringMedRefusjonskravVurdering(Arbeidsgiver ag, List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler, LocalDate stp) {
        var refusjonsperioder = lagListeMedRefusjonsperioder(fastsatteAndeler);
        var erFristUtvidet = fastsatteAndeler.stream()
            .map(VurderRefusjonAndelBeregningsgrunnlagDto::getErFristUtvidet)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(null);

        LocalDate førsteMuligeRefusjonFom = Boolean.TRUE.equals(erFristUtvidet) ? stp : null;
        return new BeregningRefusjonOverstyringDto(ag, førsteMuligeRefusjonFom, refusjonsperioder, erFristUtvidet);
    }

    private static List<BeregningRefusjonPeriodeDto> lagListeMedRefusjonsperioder(List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsattAndel) {
        return fastsattAndel.stream()
                .filter(andel -> andel.getFastsattRefusjonFom() != null)
                .map(MapTilRefusjonOverstyringNy::lagRefusjonsperiode)
                .toList();
    }

    private static BeregningRefusjonPeriodeDto lagRefusjonsperiode(VurderRefusjonAndelBeregningsgrunnlagDto fastsattAndel) {
        return new BeregningRefusjonPeriodeDto(utledReferanse(fastsattAndel), fastsattAndel.getFastsattRefusjonFom());
    }

    private static InternArbeidsforholdRefDto utledReferanse(VurderRefusjonAndelBeregningsgrunnlagDto fastsattAndel) {
        return fastsattAndel.getInternArbeidsforholdRef() != null ? InternArbeidsforholdRefDto.ref(fastsattAndel.getInternArbeidsforholdRef()) : null;
    }
}
