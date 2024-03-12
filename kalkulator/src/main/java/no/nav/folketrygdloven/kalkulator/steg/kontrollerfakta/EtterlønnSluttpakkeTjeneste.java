package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class EtterlønnSluttpakkeTjeneste {

    private EtterlønnSluttpakkeTjeneste() {
        // Skjul
    }

    public static boolean skalVurdereOmBrukerHarEtterlønnSluttpakke(BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return søkerErArbeidstaker(beregningsgrunnlag) && søkerHarBGAndelForEtterlønnSluttpakke(beregningsgrunnlag);
    }

    private static boolean søkerHarBGAndelForEtterlønnSluttpakke(BeregningsgrunnlagDto beregningsgrunnlag) {
        List<BeregningsgrunnlagPrStatusOgAndelDto> alleAndeler = beregningsgrunnlag.getBeregningsgrunnlagPerioder()
            .stream()
            .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        return alleAndeler.stream().anyMatch(andel -> OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE.equals(andel.getArbeidsforholdType()));
    }

    private static boolean søkerErArbeidstaker(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getAktivitetStatuser().stream().anyMatch(as -> as.getAktivitetStatus().erArbeidstaker());
    }
}
