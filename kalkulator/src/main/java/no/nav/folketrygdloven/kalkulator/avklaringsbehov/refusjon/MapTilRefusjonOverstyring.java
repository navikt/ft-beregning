package no.nav.folketrygdloven.kalkulator.avklaringsbehov.refusjon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KalkulatorException;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonAndelBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto.VurderRefusjonBeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public final class MapTilRefusjonOverstyring {

    private MapTilRefusjonOverstyring() {
        // Skjuler default
    }

    public static BeregningRefusjonOverstyringerDto map(VurderRefusjonBeregningsgrunnlagDto dto, BeregningsgrunnlagInput input) {
        var eksisterendeOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList());
        var nyttRefusjonAggregat = BeregningRefusjonOverstyringerDto.builder();
        var vurderingerSortertPåAG = dto.getAndeler().stream()
                .collect(Collectors.groupingBy(MapTilRefusjonOverstyring::lagArbeidsgiver));

        lagListeMedRefusjonOverstyringer(vurderingerSortertPåAG, eksisterendeOverstyringer)
                .forEach(nyttRefusjonAggregat::leggTilOverstyring);

        return nyttRefusjonAggregat.build();
    }

    private static List<BeregningRefusjonOverstyringDto> lagListeMedRefusjonOverstyringer(Map<Arbeidsgiver, List<VurderRefusjonAndelBeregningsgrunnlagDto>> vurderingerSortertPåAG, List<BeregningRefusjonOverstyringDto> eksisterendeOverstyringer) {
        List<BeregningRefusjonOverstyringDto> liste = new ArrayList<>();
        for (var entry : vurderingerSortertPåAG.entrySet()) {
            var ag = entry.getKey();
            var eksisterendeOverstyringForAG = finnKorrektOverstyring(ag, eksisterendeOverstyringer);
            if (eksisterendeOverstyringForAG.isPresent()) {
                var eksisterendeOverstyring = eksisterendeOverstyringForAG.get();

                validerStartdato(eksisterendeOverstyring, entry.getValue());

                var nyeRefusjonsperioder = lagListeMedRefusjonsperioder(entry.getValue());

                var oppdatertOverstyring = new BeregningRefusjonOverstyringDto(ag,
                        eksisterendeOverstyring.getFørsteMuligeRefusjonFom().orElse(null),
                        nyeRefusjonsperioder, eksisterendeOverstyring.getErFristUtvidet().orElse(null));
                liste.add(oppdatertOverstyring);
            } else {
                var nyRefusjonOverstyring = lagNyOverstyring(ag, entry.getValue());
                liste.add(nyRefusjonOverstyring);
            }
        }
        return liste;
    }

    private static void validerStartdato(BeregningRefusjonOverstyringDto eksisterendeOverstyring, List<VurderRefusjonAndelBeregningsgrunnlagDto> avklarteStartdatoer) {
        if (eksisterendeOverstyring.getFørsteMuligeRefusjonFom().isPresent()) {
            var tidligsteStartdato = eksisterendeOverstyring.getFørsteMuligeRefusjonFom().get();
            var ugyldigOverstyring = avklarteStartdatoer.stream().filter(os -> os.getFastsattRefusjonFom().isBefore(tidligsteStartdato)).findFirst();
            if (ugyldigOverstyring.isPresent()) {
                throw new KalkulatorException("FT-401650",
                        String.format("Det finnes en startdato for refusjon dato som er før tidligste tillate startdato for refusjon. Startdato var %s og tidligste tillate startdato var %s", ugyldigOverstyring.get().getFastsattRefusjonFom(), tidligsteStartdato));
            }
        }
    }

    private static BeregningRefusjonOverstyringDto lagNyOverstyring(Arbeidsgiver ag, List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsatteAndeler) {
        var refusjonsperioder = lagListeMedRefusjonsperioder(fastsatteAndeler);
        return new BeregningRefusjonOverstyringDto(ag, null, refusjonsperioder, null);
    }

    private static List<BeregningRefusjonPeriodeDto> lagListeMedRefusjonsperioder(List<VurderRefusjonAndelBeregningsgrunnlagDto> fastsattAndel) {
        return fastsattAndel.stream()
                .map(MapTilRefusjonOverstyring::lagRefusjonsperiode)
                .collect(Collectors.toList());
    }

    private static BeregningRefusjonPeriodeDto lagRefusjonsperiode(VurderRefusjonAndelBeregningsgrunnlagDto fastsattAndel) {
        return new BeregningRefusjonPeriodeDto(utledReferanse(fastsattAndel), fastsattAndel.getFastsattRefusjonFom());
    }

    private static InternArbeidsforholdRefDto utledReferanse(VurderRefusjonAndelBeregningsgrunnlagDto fastsattAndel) {
        return fastsattAndel.getInternArbeidsforholdRef() != null ? InternArbeidsforholdRefDto.ref(fastsattAndel.getInternArbeidsforholdRef()) : null;
    }

    private static Optional<BeregningRefusjonOverstyringDto> finnKorrektOverstyring(Arbeidsgiver ag, List<BeregningRefusjonOverstyringDto> refusjonOverstyringer) {
        return refusjonOverstyringer.stream().filter(os -> os.getArbeidsgiver().equals(ag)).findFirst();
    }

    private static Arbeidsgiver lagArbeidsgiver(VurderRefusjonAndelBeregningsgrunnlagDto fastsattAndel) {
        if (fastsattAndel.getArbeidsgiverOrgnr() != null) {
            return Arbeidsgiver.virksomhet(fastsattAndel.getArbeidsgiverOrgnr());
        } else {
            return Arbeidsgiver.person(new AktørId(fastsattAndel.getArbeidsgiverAktørId()));
        }
    }

}
