package no.nav.folketrygdloven.kalkulator.guitjenester.ytelsegrunnlag;

import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_ANNEN_INNTEKT;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.AVKORTET_GRUNNET_LØPENDE_INNTEKT;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.FOR_LAVT_BG;
import static no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak.INGEN_FRILANS_I_PERIODE_UTEN_YTELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.EffektivÅrsinntektTjenesteFRISINN;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak;

public class MapTilAvslagsårsakerFRISINN {

    private static final BigDecimal ANTALL_G_FOR_OPPFYLT_VILKÅR = BigDecimal.valueOf(0.75);

    public static Optional<Avslagsårsak> map(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                             List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode,
                                             FrisinnGrunnlag frisinnGrunnlag,
                                             OppgittOpptjeningDto oppgittOpptjening,
                                             Beløp grunnbeløp,
                                             LocalDate skjæringstidspunkt) {
        LocalDate fomDato = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPeriodeFom();
        if (andel.getAktivitetStatus().equals(AktivitetStatus.FRILANSER) && frisinnGrunnlag.getSøkerYtelseForFrilans(fomDato)) {
            return finnAvslagsårsakForFrilans(andel, andelerISammePeriode, oppgittOpptjening, grunnbeløp);
        }
        if (andel.getAktivitetStatus().erSelvstendigNæringsdrivende() && frisinnGrunnlag.getSøkerYtelseForNæring(fomDato)) {
            return finnAvslagsårsakForNæring(andel, andelerISammePeriode, oppgittOpptjening, frisinnGrunnlag, grunnbeløp, skjæringstidspunkt);
        }
        return Optional.empty();
    }

    private static Optional<Avslagsårsak> finnAvslagsårsakForFrilans(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                     List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode,
                                                                     OppgittOpptjeningDto oppgittOpptjening,
                                                                     Beløp grunnbeløp) {
        LocalDate fomDato = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlagPeriodeFom();
        var førsteSøknadsdato = finnPeriodeinntekterFrilans(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        var sisteSøknadsdato = finnPeriodeinntekterFrilans(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getTomDato)
                .max(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        if (førsteSøknadsdato.isAfter(fomDato) || sisteSøknadsdato.isBefore(fomDato)) {
            return Optional.empty();
        }
        if (andel.getBeregnetPrÅr().compareTo(Beløp.ZERO) == 0) {
            return Optional.of(INGEN_FRILANS_I_PERIODE_UTEN_YTELSE);
        }
        if (andel.getAvkortetPrÅr() != null && andel.getAvkortetPrÅr().compareTo(Beløp.ZERO) == 0) {
            var antallGØvreGrenseverdi = KonfigTjeneste.getAntallGØvreGrenseverdi();
            var grunnlagFraArbeid = finnGrunnlagFraArbeid(andelerISammePeriode);
            var seksG = grunnbeløp.multipliser(antallGØvreGrenseverdi);
            if (grunnlagFraArbeid.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            return Optional.of(AVKORTET_GRUNNET_LØPENDE_INNTEKT);
        }
        return Optional.empty();
    }

    private static Optional<Avslagsårsak> finnAvslagsårsakForNæring(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                    List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode,
                                                                    OppgittOpptjeningDto oppgittOpptjening,
                                                                    FrisinnGrunnlag frisinnGrunnlag,
                                                                    Beløp grunnbeløp,
                                                                    LocalDate skjæringstidspunkt) {
        BeregningsgrunnlagPeriodeDto bgPeriode = andel.getBeregningsgrunnlagPeriode();
        Intervall periode = bgPeriode.getPeriode();
        var førsteSøknadsdato = finnPeriodeinntekterNæring(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getFomDato)
                .filter(d -> !d.isBefore(skjæringstidspunkt))
                .min(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        var sisteSøknadsdato = finnPeriodeinntekterNæring(oppgittOpptjening).stream()
                .map(OppgittPeriodeInntekt::getPeriode)
                .map(Intervall::getTomDato)
                .filter(d -> !d.isBefore(skjæringstidspunkt))
                .max(Comparator.naturalOrder())
                .orElse(TIDENES_BEGYNNELSE);
        if (førsteSøknadsdato.isAfter(periode.getFomDato()) || sisteSøknadsdato.isBefore(periode.getFomDato())) {
            return Optional.empty();
        }
        if (andel.getBeregnetPrÅr().compareTo(Beløp.ZERO) == 0) {
            return Optional.empty();
        }
        if (andel.getAvkortetPrÅr() != null && andel.getAvkortetPrÅr().compareTo(Beløp.ZERO) == 0) {
            var antallGØvreGrenseverdi = KonfigTjeneste.getAntallGØvreGrenseverdi();
            var grunnlagFraArbeid = finnGrunnlagFraArbeid(andelerISammePeriode);

            var seksG = grunnbeløp.multipliser(antallGØvreGrenseverdi);
            if (grunnlagFraArbeid.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            var løpendeInntektFrilans = finnLøpendeFrilansInntekt(andel, oppgittOpptjening);
            var grunnlagMedLøpendeFrilans = grunnlagFraArbeid.adder(løpendeInntektFrilans);
            if (!frisinnGrunnlag.getSøkerYtelseForFrilans(bgPeriode.getBeregningsgrunnlagPeriodeFom()) && grunnlagMedLøpendeFrilans.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            var grunnlagFraFrilans = finnKompensasjonsgrunnlagFrilans(andelerISammePeriode);
            var grunnlagMedKompensertFrilans = grunnlagFraArbeid.adder(grunnlagFraFrilans);
            if (frisinnGrunnlag.getSøkerYtelseForFrilans(bgPeriode.getBeregningsgrunnlagPeriodeFom()) && grunnlagMedKompensertFrilans.compareTo(seksG) >= 0) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            return Optional.of(AVKORTET_GRUNNET_LØPENDE_INNTEKT);
        }
        return Optional.empty();
    }

    private static Beløp finnKompensasjonsgrunnlagFrilans(List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode) {
        return andelerISammePeriode.stream()
                .filter(a -> a.getAktivitetStatus().erFrilanser())
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private static Beløp finnGrunnlagFraArbeid(List<BeregningsgrunnlagPrStatusOgAndelDto> andelerISammePeriode) {
        return andelerISammePeriode.stream()
                .filter(a -> a.getAktivitetStatus().erArbeidstaker())
                .map(BeregningsgrunnlagPrStatusOgAndelDto::getBeregnetPrÅr)
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    private static Beløp finnLøpendeFrilansInntekt(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                        OppgittOpptjeningDto oppgittOpptjening) {
        List<OppgittPeriodeInntekt> oppgittInntektFrilans = finnPeriodeinntekterFrilans(oppgittOpptjening);
        return finnInntektIPeriode(oppgittInntektFrilans, andel.getBeregningsgrunnlagPeriode().getPeriode());
    }

    private static List<OppgittPeriodeInntekt> finnPeriodeinntekterFrilans(OppgittOpptjeningDto oppgittOpptjening) {
        return oppgittOpptjening.getFrilans()
                .stream()
                .flatMap(oppgittFrilansDto -> oppgittFrilansDto.getOppgittFrilansInntekt().stream())
                .map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }

    private static List<OppgittPeriodeInntekt> finnPeriodeinntekterNæring(OppgittOpptjeningDto oppgittOpptjening) {
        return oppgittOpptjening.getEgenNæring()
                .stream()
                .map(i -> (OppgittPeriodeInntekt) i)
                .collect(Collectors.toList());
    }


    private static Beløp finnInntektIPeriode(List<OppgittPeriodeInntekt> periodeInntekter, Intervall periode) {
        return periodeInntekter.stream()
                .filter(i -> i.getPeriode().getFomDato().equals(periode.getFomDato()))
                .map(EffektivÅrsinntektTjenesteFRISINN::finnEffektivÅrsinntektForLøpenedeInntekt)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);
    }

    public static Optional<Avslagsårsak> finnForPeriode(BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                                        FrisinnGrunnlag frisinnGrunnlag,
                                                        Optional<OppgittOpptjeningDto> oppgittOpptjening,
                                                        Beløp gbeløp, LocalDate skjæringstidspunkt) {
        if (beregningsgrunnlagPeriode.getDagsats() != null && beregningsgrunnlagPeriode.getDagsats() > 0) {
            return Optional.empty();
        }

        LocalDate fom = beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom();
        if (!frisinnGrunnlag.getSøkerYtelseForNæring(fom) && !frisinnGrunnlag.getSøkerYtelseForFrilans(fom)) {
            return Optional.empty();
        }

        if (oppgittOpptjening.isPresent()) {

            List<BeregningsgrunnlagPrStatusOgAndelDto> andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
            Set<Avslagsårsak> avslagsårsaker = andeler.stream()
                    .flatMap(a -> map(a, andeler, frisinnGrunnlag, oppgittOpptjening.get(), gbeløp, skjæringstidspunkt).stream())
                    .collect(Collectors.toSet());
            if (harForLavtBeregningsgrunnlag(frisinnGrunnlag, gbeløp, fom, andeler)) {
                if (avslagsårsaker.contains(INGEN_FRILANS_I_PERIODE_UTEN_YTELSE)) {
                    return Optional.of(INGEN_FRILANS_I_PERIODE_UTEN_YTELSE);
                }
                return Optional.of(FOR_LAVT_BG);
            }
            if (avslagsårsaker.contains(AVKORTET_GRUNNET_ANNEN_INNTEKT)) {
                return Optional.of(AVKORTET_GRUNNET_ANNEN_INNTEKT);
            }
            if (avslagsårsaker.contains(AVKORTET_GRUNNET_LØPENDE_INNTEKT)) {
                return Optional.of(AVKORTET_GRUNNET_LØPENDE_INNTEKT);
            }
            return Optional.of(FOR_LAVT_BG);
        }

        return Optional.empty();

    }

    private static boolean harForLavtBeregningsgrunnlag(FrisinnGrunnlag frisinnGrunnlag, Beløp gbeløp, LocalDate fom, List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        var grunnlagFraSøkteStatuser = Beløp.ZERO;
        if (frisinnGrunnlag.getSøkerYtelseForFrilans(fom)) {
            var frilansBrutto = andeler.stream().filter(a -> a.getAktivitetStatus().erFrilanser())
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr).findFirst().orElse(Beløp.ZERO);
            grunnlagFraSøkteStatuser = grunnlagFraSøkteStatuser.adder(frilansBrutto);
        }
        if (frisinnGrunnlag.getSøkerYtelseForNæring(fom)) {
            var næringBrutto = andeler.stream().filter(a -> a.getAktivitetStatus().erSelvstendigNæringsdrivende())
                    .map(BeregningsgrunnlagPrStatusOgAndelDto::getBruttoPrÅr).findFirst().orElse(Beløp.ZERO);
            grunnlagFraSøkteStatuser = grunnlagFraSøkteStatuser.adder(næringBrutto);
        }

        return grunnlagFraSøkteStatuser.compareTo(gbeløp.multipliser(ANTALL_G_FOR_OPPFYLT_VILKÅR)) < 0;
    }
}
