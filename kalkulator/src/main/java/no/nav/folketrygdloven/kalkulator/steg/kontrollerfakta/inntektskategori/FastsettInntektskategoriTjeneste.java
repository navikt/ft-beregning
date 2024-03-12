package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.inntektskategori;

import static no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSTAKER;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.MIDLERTIDIG_INAKTIV;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.folketrygdloven.kalkulus.kodeverk.VirksomhetType;

public class FastsettInntektskategoriTjeneste {

    private FastsettInntektskategoriTjeneste() {
        // hide me
    }

    public static BeregningsgrunnlagDto fastsettInntektskategori(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var nyttGrunnlag = BeregningsgrunnlagDto.builder(beregningsgrunnlag).build();
        nyttGrunnlag.getBeregningsgrunnlagPerioder().stream()
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .forEach(andel -> BeregningsgrunnlagPrStatusOgAndelDto.Builder.oppdatere(Optional.of(andel))
                        .medInntektskategori(finnInntektskategoriForStatus(andel, iayGrunnlag, beregningsgrunnlag.getAktivitetStatuser())));

        return nyttGrunnlag;

    }

    static Optional<Inntektskategori> finnHøyestPrioriterteInntektskategoriForSN(List<Inntektskategori> inntektskategorier) {
        if (inntektskategorier.isEmpty()) { //NOSONAR Style - Method excessively uses methods of another class. Klassen fastsetter inntektskategori og prioritet av inntektskategori
            return Optional.empty();
        }
        if (inntektskategorier.size() == 1) {
            return Optional.of(inntektskategorier.get(0));
        }
        if (inntektskategorier.contains(Inntektskategori.FISKER)) {
            return Optional.of(Inntektskategori.FISKER);
        }
        if (inntektskategorier.contains(Inntektskategori.JORDBRUKER)) {
            return Optional.of(Inntektskategori.JORDBRUKER);
        }
        if (inntektskategorier.contains(Inntektskategori.DAGMAMMA)) {
            return Optional.of(Inntektskategori.DAGMAMMA);
        }
        return Optional.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private static Inntektskategori finnInntektskategoriForStatus(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                  InntektArbeidYtelseGrunnlagDto grunnlag,
                                                                  List<BeregningsgrunnlagAktivitetStatusDto> aktivitetStatuser) {
        if (SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getAktivitetStatus())) {
            return finnInntektskategoriForSelvstendigNæringsdrivende(grunnlag);
        }
        if (aktivitetStatuser.stream().anyMatch(a -> MIDLERTIDIG_INAKTIV.equals(a.getAktivitetStatus()))) {
            return Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER; // Bruker arbeidstaker uten feriepenger for å unngå feriepenger for midlertidige inaktive
        }
        if (ARBEIDSTAKER.equals(andel.getAktivitetStatus()) && erSjøfolk(andel, grunnlag)) {
            return Inntektskategori.SJØMANN;
        }
        return andel.getAktivitetStatus().getInntektskategori();
    }

    private static boolean erSjøfolk(BeregningsgrunnlagPrStatusOgAndelDto andel, InntektArbeidYtelseGrunnlagDto grunnlag) {
        Collection<InntektDto> alleInntekter = grunnlag.getAktørInntektFraRegister()
                .map(AktørInntektDto::getInntekt)
                .orElse(Collections.emptyList());
        List<Arbeidsgiver> arbeidsgivereSjøfolk = alleInntekter.stream()
                .filter(innt -> innt.getInntektsKilde().equals(InntektskildeType.INNTEKT_BEREGNING))
                .filter(innt -> innt.getArbeidsgiver() != null)
                .filter(innt -> finnesInntektspostMedSkatteregelSjømann(innt.getAlleInntektsposter(), andel.getBeregningsperiode()))
                .map(InntektDto::getArbeidsgiver)
                .collect(Collectors.toList());
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver).map(arbeidsgivereSjøfolk::contains).orElse(false);
    }

    private static boolean finnesInntektspostMedSkatteregelSjømann(Collection<InntektspostDto> alleInntektsposter, Intervall beregningsperiode) {
        return alleInntektsposter.stream()
                .filter(ip -> ip.getPeriode().overlapper(beregningsperiode))
                .anyMatch(ip -> SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK.equals(ip.getSkatteOgAvgiftsregelType())
                        || SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK.equals(ip.getSkatteOgAvgiftsregelType()));
    }

    private static Inntektskategori finnInntektskategoriForSelvstendigNæringsdrivende(InntektArbeidYtelseGrunnlagDto grunnlag) {
        Optional<OppgittOpptjeningDto> oppgittOpptjening = grunnlag.getOppgittOpptjening();
        if (oppgittOpptjening.isPresent() && !oppgittOpptjening.get().getEgenNæring().isEmpty()) {
            Set<VirksomhetType> virksomhetTypeSet = oppgittOpptjening.get().getEgenNæring().stream()
                    .map(OppgittEgenNæringDto::getVirksomhetType)
                    .collect(Collectors.toSet());

            List<Inntektskategori> inntektskategorier = virksomhetTypeSet.stream()
                    .map(FastsettInntektskategoriTjeneste::finnInntektskategoriFraNæringstype)
                    .collect(Collectors.toList());

            return finnHøyestPrioriterteInntektskategoriForSN(inntektskategorier)
                    .orElse(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        }
        return Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
    }

    private static Inntektskategori finnInntektskategoriFraNæringstype(VirksomhetType virksomhetstype) {
        return switch (virksomhetstype) {
            case DAGMAMMA -> Inntektskategori.DAGMAMMA;
            case FISKE -> Inntektskategori.FISKER;
            case FRILANSER -> Inntektskategori.FRILANSER;
            case JORDBRUK_SKOGBRUK -> Inntektskategori.JORDBRUKER;
            case ANNEN, UDEFINERT -> Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
        };
    }
}
