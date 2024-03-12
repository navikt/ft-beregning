package no.nav.folketrygdloven.kalkulator.guitjenester.refusjon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AndelerMedØktRefusjonTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;

public final class VurderRefusjonDtoTjeneste {

    private VurderRefusjonDtoTjeneste() {
        // Skjuler default
    }

    public static Optional<RefusjonTilVurderingDto> lagDto(BeregningsgrunnlagGUIInput input) {
        Optional<BeregningsgrunnlagDto> beregningsgrunnlag = input.getBeregningsgrunnlagGrunnlag().getBeregningsgrunnlagHvisFinnes();
        List<BeregningsgrunnlagDto> originaleGrunnlag = input.getBeregningsgrunnlagGrunnlagFraForrigeBehandling().stream()
                .flatMap(gr -> gr.getBeregningsgrunnlagHvisFinnes().stream()).collect(Collectors.toList());
        if (originaleGrunnlag.isEmpty() || beregningsgrunnlag.isEmpty()) {
            return Optional.empty();
        }
        var grenseverdi = beregningsgrunnlag.get().getGrunnbeløp().multipliser(KonfigTjeneste.getAntallGØvreGrenseverdi());

        Map<Intervall, List<RefusjonAndel>> andelerMedØktRefusjon = originaleGrunnlag.stream()
                .flatMap(originaltBg -> AndelerMedØktRefusjonTjeneste.finnAndelerMedØktRefusjon(beregningsgrunnlag.get(), originaltBg, grenseverdi, input.getYtelsespesifiktGrunnlag()).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, unikeElementer()));
        if (!andelerMedØktRefusjon.isEmpty()) {
            return LagVurderRefusjonDto.lagDto(andelerMedØktRefusjon, input);
        }

        return lagDtoBasertPåTidligereAvklaringer(input);
    }

    private static BinaryOperator<List<RefusjonAndel>> unikeElementer() {
        return (andeler1, andeler2) -> {
            var nyListe = new ArrayList<RefusjonAndel>();
            nyListe.addAll(andeler1);
            nyListe.addAll(andeler2);
            return nyListe.stream().distinct().toList();
        };
    }

    // Metode for å støtte visning av saker som tidligere er løst men som av ulike grunner ikke lenger gir samme resultat i avklaringsbehovutledning
    private static Optional<RefusjonTilVurderingDto> lagDtoBasertPåTidligereAvklaringer(BeregningsgrunnlagGUIInput input) {
        Intervall hardkodetIntervall = Intervall.fraOgMed(input.getSkjæringstidspunktForBeregning()); // Bruker hele perioden det kan kreves refusjon for
        List<RefusjonAndel> andeler = new ArrayList<>();
        List<BeregningRefusjonOverstyringDto> refusjonOverstyringer = input.getBeregningsgrunnlagGrunnlag().getRefusjonOverstyringer()
                .map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
                .orElse(Collections.emptyList())
                .stream()
                .filter(pa -> !pa.getRefusjonPerioder().isEmpty())
                .collect(Collectors.toList());
        refusjonOverstyringer.forEach(avkalring -> {
            List<RefusjonAndel> tidligereAvklaringerPåAG = avkalring.getRefusjonPerioder().stream()
                    .map(refusjonPeriode -> new RefusjonAndel(AktivitetStatus.ARBEIDSTAKER, avkalring.getArbeidsgiver(), refusjonPeriode.getArbeidsforholdRef(),
                            Beløp.ZERO, Beløp.ZERO)) // De to siste parameterne brukes ikke for å lage dto så kan settes til dummy-verdier
                    .collect(Collectors.toList());
            andeler.addAll(tidligereAvklaringerPåAG);
        });
        if (andeler.isEmpty()) {
            return Optional.empty();
        }
        Map<Intervall, List<RefusjonAndel>> avklaringMap = new HashMap<>();
        avklaringMap.put(hardkodetIntervall, andeler);
        return LagVurderRefusjonDto.lagDto(avklaringMap, input);
    }

}
