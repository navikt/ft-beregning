package no.nav.folketrygdloven.kalkulator.input;


import java.util.Map;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class OpptjeningsaktiviteterPerYtelse {

    private static final Map<FagsakYtelseType, Set<OpptjeningAktivitetType>> EKSKLUDERTE_AKTIVITETER_PER_YTELSE = Map.of(
            FagsakYtelseType.FORELDREPENGER, Set.of(
                    OpptjeningAktivitetType.VIDERE_ETTERUTDANNING,
                    OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD),
            FagsakYtelseType.FRISINN, Set.of(
                    OpptjeningAktivitetType.VIDERE_ETTERUTDANNING,
                    OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD),
            FagsakYtelseType.SVANGERSKAPSPENGER, Set.of(
                    OpptjeningAktivitetType.VIDERE_ETTERUTDANNING,
                    OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD,
                    OpptjeningAktivitetType.DAGPENGER,
                    OpptjeningAktivitetType.AAP,
                    OpptjeningAktivitetType.VENTELØNN_VARTPENGER,
                    OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE),
            FagsakYtelseType.PLEIEPENGER_SYKT_BARN, Set.of(
                    OpptjeningAktivitetType.VIDERE_ETTERUTDANNING,
                    OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD,
                    OpptjeningAktivitetType.AAP,
                    OpptjeningAktivitetType.VENTELØNN_VARTPENGER,
                    OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE),
            FagsakYtelseType.OPPLÆRINGSPENGER, Set.of(
                    OpptjeningAktivitetType.VIDERE_ETTERUTDANNING,
                    OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD,
                    OpptjeningAktivitetType.AAP,
                    OpptjeningAktivitetType.VENTELØNN_VARTPENGER,
                    OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE),
            FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, Set.of(
                    OpptjeningAktivitetType.VIDERE_ETTERUTDANNING,
                    OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD,
                    OpptjeningAktivitetType.AAP,
                    OpptjeningAktivitetType.VENTELØNN_VARTPENGER,
                    OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE),
            FagsakYtelseType.OMSORGSPENGER, Set.of(
                    OpptjeningAktivitetType.VIDERE_ETTERUTDANNING,
                    OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD,
                    OpptjeningAktivitetType.DAGPENGER,
                    OpptjeningAktivitetType.AAP,
                    OpptjeningAktivitetType.VENTELØNN_VARTPENGER,
                    OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE));

    private final Set<OpptjeningAktivitetType> ekskluderteAktiviteter;

    public OpptjeningsaktiviteterPerYtelse(FagsakYtelseType fagsakYtelseType) {
        ekskluderteAktiviteter = EKSKLUDERTE_AKTIVITETER_PER_YTELSE.get(fagsakYtelseType);
    }

    public boolean erRelevantAktivitet(OpptjeningAktivitetType opptjeningAktivitetType, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        if (OpptjeningAktivitetType.FRILANS.equals(opptjeningAktivitetType)) {
            return harOppgittFrilansISøknad(iayGrunnlag);
        }
        return erRelevantAktivitet(opptjeningAktivitetType);
    }

    public boolean erRelevantAktivitet(OpptjeningAktivitetType opptjeningAktivitetType) {
        return !ekskluderteAktiviteter.contains(opptjeningAktivitetType);
    }

    private boolean harOppgittFrilansISøknad(InntektArbeidYtelseGrunnlagDto grunnlag) {
        return grunnlag.getOppgittOpptjening().stream()
                .flatMap(oppgittOpptjening -> oppgittOpptjening.getAnnenAktivitet().stream())
                .anyMatch(annenAktivitet -> annenAktivitet.getArbeidType().equals(ArbeidType.FRILANSER));
    }
}
