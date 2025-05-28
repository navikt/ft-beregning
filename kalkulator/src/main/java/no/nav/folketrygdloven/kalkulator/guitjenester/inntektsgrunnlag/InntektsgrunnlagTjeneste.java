package no.nav.folketrygdloven.kalkulator.guitjenester.inntektsgrunnlag;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagDto;

public class InntektsgrunnlagTjeneste {
    private static final List<ArbeidType> FRILANS_TYPER = Arrays.asList(ArbeidType.FRILANSER, ArbeidType.FRILANSER_OPPDRAGSTAKER);

    public static Optional<InntektsgrunnlagDto> lagDto(BeregningsgrunnlagGUIInput input) {
        var harStatusSomKreverDetaljertInntektsgrunnlag = input.getBeregningsgrunnlag() != null && input.getBeregningsgrunnlag().getAktivitetStatuser().stream()
                .anyMatch(st -> st.getAktivitetStatus() != null && (st.getAktivitetStatus().erArbeidstaker()
                        || st.getAktivitetStatus().erFrilanser() || st.getAktivitetStatus().erSelvstendigNæringsdrivende()));
        if (!harStatusSomKreverDetaljertInntektsgrunnlag) {
            return Optional.empty();
        }
        var alleInntekter = input.getIayGrunnlag().getAktørInntektFraRegister().map(AktørInntektDto::getInntekt).orElse(Collections.emptyList());
        var frilansArbeidsgivere = finnFrilansArbeidsgivere(input);
        var atflSgPeriode = input.getBeregningsgrunnlag().getSammenligningsgrunnlagForStatus(SammenligningsgrunnlagType.SAMMENLIGNING_AT_FL)
                .map(sg -> Intervall.fraOgMedTilOgMed(sg.getSammenligningsperiodeFom(), sg.getSammenligningsperiodeTom()));
        var atflBgPeriode = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().getFirst()
                .getBeregningsgrunnlagPrStatusOgAndelList().stream()
				.filter(bg -> bg.getBeregningsperiode() != null && (bg.getAktivitetStatus().erArbeidstaker() || bg.getAktivitetStatus().erFrilanser()))
				.findFirst()
				.map(bg -> Intervall.fraOgMedTilOgMed(bg.getBeregningsperiodeFom(), bg.getBeregningsperiodeTom()));
        var mapper = new InntektsgrunnlagMapper(atflSgPeriode, atflBgPeriode, frilansArbeidsgivere);
        return mapper.map(alleInntekter);
    }

    private static List<Arbeidsgiver> finnFrilansArbeidsgivere(BeregningsgrunnlagGUIInput input) {
        var alleYrkesaktiviteter = input.getIayGrunnlag().getAktørArbeidFraRegister()
                .map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                .orElse(Collections.emptyList());
        return alleYrkesaktiviteter.stream().filter(ya -> FRILANS_TYPER.contains(ya.getArbeidType()))
                .filter(ya -> ya.getArbeidsgiver() != null
                        && ya.getArbeidsgiver().getIdentifikator() != null)
                .map(YrkesaktivitetDto::getArbeidsgiver)
                .collect(Collectors.toList());
    }
}
