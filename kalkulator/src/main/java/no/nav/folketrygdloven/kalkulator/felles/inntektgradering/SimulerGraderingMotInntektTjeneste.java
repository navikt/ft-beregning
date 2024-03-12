package no.nav.folketrygdloven.kalkulator.felles.inntektgradering;

import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnPerioderForArbeid;
import static no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste.finnPerioderForStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.fastsett.MapFullføreBeregningsgrunnlagFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.StandardPeriodeSplittMappers;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.TilkommetInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.StatusOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt.TilkommetInntektPeriodeTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.Virkedager;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;
import no.nav.fpsak.tidsserie.LocalDateInterval;

/**
 * Simulerer resultat av gradering mot inntekt
 * Kun til bruk i forvaltning og statistikk enn så lenge.
 * Metoder for å bestemme inntekt må gås over funksjonelt før den kan brukes i automatisk saksbehandling.
 */
public class SimulerGraderingMotInntektTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(SimulerGraderingMotInntektTjeneste.class);

    private final MapFullføreBeregningsgrunnlagFraVLTilRegel mapFullføreBeregningsgrunnlagFraVLTilRegel = new MapFullføreBeregningsgrunnlagFraVLTilRegel();


    public List<ReduksjonVedGradering> simulerGraderingMotInntekt(BeregningsgrunnlagInput beregningsgrunnlagInput) {
        var nyttBg = lagInputGrunnlag(beregningsgrunnlagInput);
        return finnReduksjon(beregningsgrunnlagInput, nyttBg);
    }

    public List<Intervall> finnTilkommetAktivitetPerioder(BeregningsgrunnlagInput beregningsgrunnlagInput) {
        if (!(beregningsgrunnlagInput.getYtelsespesifiktGrunnlag() instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) || utbetalingsgradGrunnlag.getTilkommetInntektHensyntasFom().isEmpty()) {
            return Collections.emptyList();
        }
        var tilkommetTidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
                beregningsgrunnlagInput.getSkjæringstidspunktForBeregning(),
                beregningsgrunnlagInput.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                beregningsgrunnlagInput.getYtelsespesifiktGrunnlag(),
                beregningsgrunnlagInput.getIayGrunnlag()
        );
        var tidlinjeMedTilkommetAktivitet = tilkommetTidslinje.filterValue(v -> !v.isEmpty()).compress();
        var redusertTidslinje = tidlinjeMedTilkommetAktivitet.intersection(new LocalDateInterval(TilkommetInntektPeriodeTjeneste.FOM_DATO_GRADERING_MOT_INNTEKT, LocalDateInterval.TIDENES_ENDE));

        return redusertTidslinje.toSegments().stream()
                .map(s -> Intervall.fraOgMedTilOgMed(s.getFom(), s.getTom()))
                .toList();
    }

    public BeregningsgrunnlagDto lagInputGrunnlag(BeregningsgrunnlagInput beregningsgrunnlagInput) {
        var splittetGrunnlag = splittBeregningsgrunnlagOgLagTilkommet(beregningsgrunnlagInput.getIayGrunnlag(), beregningsgrunnlagInput.getBeregningsgrunnlag(),
                beregningsgrunnlagInput.getYtelsespesifiktGrunnlag());
        settInntektPåTilkomneInntektsforhold(beregningsgrunnlagInput.getIayGrunnlag(), beregningsgrunnlagInput.getYtelsespesifiktGrunnlag(), splittetGrunnlag);
        return splittetGrunnlag;
    }

    private List<ReduksjonVedGradering> finnReduksjon(BeregningsgrunnlagInput beregningsgrunnlagInput, BeregningsgrunnlagDto nyttBg) {
        var regelResultatPerioder = kjørRegel(beregningsgrunnlagInput, nyttBg);
        var erFordelt = erFordelt(nyttBg);
        return nyttBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkomneInntekter().isEmpty())
                .map(p -> {
                    var dagsatsFørGradering = p.getDagsats();
                    var dagsatsEtterGradering = finnDagsatsEtterGradering(regelResultatPerioder, p, dagsatsFørGradering);
                    var virkedager = getBeregnVirkedager(p.getPeriode());
                    return new ReduksjonVedGradering(dagsatsFørGradering, dagsatsEtterGradering,
                            p.getPeriode().getFomDato(),
                            p.getPeriode().getTomDato(),
                            virkedager,
                            erFordelt,
                            p.getTilkomneInntekter().stream().map(TilkommetInntektDto::getAktivitetStatus).collect(Collectors.toSet()));
                }).toList();
    }

    private static boolean erFordelt(BeregningsgrunnlagDto nyttBg) {
        return nyttBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkomneInntekter().isEmpty())
                .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
                .anyMatch(a -> a.getFordeltPrÅr() != null || a.getManueltFordeltPrÅr() != null);
    }

    private List<BeregningsgrunnlagPeriode> kjørRegel(BeregningsgrunnlagInput beregningsgrunnlagInput, BeregningsgrunnlagDto nyttBg) {
        var regelBg = mapFullføreBeregningsgrunnlagFraVLTilRegel.map(beregningsgrunnlagInput, nyttBg);
        Beregningsgrunnlag.builder(regelBg).leggTilToggle("GRADERING_MOT_INNTEKT", true);
        var regelPerioder = regelBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkommetInntektsforholdListe().isEmpty())
                .toList();
        regelPerioder.forEach(KalkulusRegler::finnGrenseverdi);
        return regelPerioder;
    }

    private static Long finnDagsatsEtterGradering(List<BeregningsgrunnlagPeriode> regelPerioder, BeregningsgrunnlagPeriodeDto p, Long dagsatsFørGradering) {
        return regelPerioder.stream().filter(it -> it.getPeriodeFom().equals(p.getPeriode().getFomDato())).findFirst()
                .map(BeregningsgrunnlagPeriode::getGrenseverdi)
                .map(gr -> gr.divide(KonfigTjeneste.getYtelsesdagerIÅr(), 2, RoundingMode.HALF_UP))
                .map(BigDecimal::longValue)
                .orElse(dagsatsFørGradering);
    }

    private static BeregningsgrunnlagDto splittBeregningsgrunnlagOgLagTilkommet(InntektArbeidYtelseGrunnlagDto iay, BeregningsgrunnlagDto mappetGrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var tilkommetTidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
                mappetGrunnlag.getSkjæringstidspunkt(),
                mappetGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                ytelsespesifiktGrunnlag,
                iay
        ).filterValue(v -> !v.isEmpty());
        return getPeriodeSplitter().splittPerioder(mappetGrunnlag, tilkommetTidslinje);
    }

    private static PeriodeSplitter<Set<StatusOgArbeidsgiver>> getPeriodeSplitter() {
        var spittPerioderConfig = new SplittPeriodeConfig<>(
                TilkommetInntektPeriodeTjeneste::opprettTilkommetInntekt,
                StandardPeriodeSplittMappers.settAvsluttetPeriodeårsak(Collections.emptyList(), PeriodeÅrsak.TILKOMMET_INNTEKT_AVSLUTTET));
        return new PeriodeSplitter<>(spittPerioderConfig);
    }

    private void settInntektPåTilkomneInntektsforhold(InntektArbeidYtelseGrunnlagDto iay, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BeregningsgrunnlagDto nyttBg) {
        nyttBg.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> !p.getTilkomneInntekter().isEmpty())
                .forEach(p -> {
                    var tilkomneInntekter = p.getTilkomneInntekter();
                    var nyeInntektsforhold = tilkomneInntekter.stream().map(
                            it -> mapMedInntekt(iay, ytelsespesifiktGrunnlag, p, it)
                    ).toList();
                    var oppdater = BeregningsgrunnlagPeriodeDto.oppdater(p);
                    nyeInntektsforhold.forEach(oppdater::leggTilTilkommetInntekt);
                });
    }

    private TilkommetInntektDto mapMedInntekt(InntektArbeidYtelseGrunnlagDto iay, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, BeregningsgrunnlagPeriodeDto p, TilkommetInntektDto it) {
        Beløp inntekt;
        if (it.getArbeidsgiver().isPresent()) {
            inntekt = finnInntektForArbeidsgiver(
                    it.getArbeidsgiver().get(),
                    it.getArbeidsforholdRef(),
                    p.getPeriode(),
                    iay,
                    ytelsespesifiktGrunnlag);

        } else if (it.getAktivitetStatus().equals(AktivitetStatus.FRILANSER)) {
            inntekt = finnInntektForFrilans(
                    p.getPeriode(),
                    iay,
                    ytelsespesifiktGrunnlag);
        } else {
            LOG.info("Fant tilkommet inntekt for status {}. Setter 0 i brutto inntekt.", it.getAktivitetStatus().getKode());
            inntekt = Beløp.ZERO;
        }
        return new TilkommetInntektDto(
                it.getAktivitetStatus(), it.getArbeidsgiver().orElse(null),
                it.getArbeidsforholdRef(),
                inntekt,
                utledTilkommetFraBrutto(inntekt, it, p.getPeriode(), ytelsespesifiktGrunnlag),
                true
        );
    }


    private static Beløp utledTilkommetFraBrutto(Beløp inntekt,
                                                      TilkommetInntektDto inntektsforhold,
                                                      Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            if (inntektsforhold.getAktivitetStatus().erArbeidstaker() && inntektsforhold.getArbeidsgiver().isPresent()) {
                var utbetalingsgradProsent = UtbetalingsgradTjeneste.finnUtbetalingsgradForArbeid(
                        inntektsforhold.getArbeidsgiver().get(),
                        inntektsforhold.getArbeidsforholdRef(),
                        periode,
                        ytelsespesifiktGrunnlag,
                        true);
                var utbetalingsgrad = utbetalingsgradProsent.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                return inntekt.multipliser(BigDecimal.ONE.subtract(utbetalingsgrad));
            } else if (inntektsforhold.getAktivitetStatus().equals(AktivitetStatus.FRILANSER)) {
                var utbetalingsgradProsent = UtbetalingsgradTjeneste.finnUtbetalingsgradForStatus(AktivitetStatus.FRILANSER, periode, ytelsespesifiktGrunnlag);
                var utbetalingsgrad = utbetalingsgradProsent.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                return inntekt.multipliser(BigDecimal.ONE.subtract(utbetalingsgrad));
            } else {
                return Beløp.ZERO;
            }
        }
        throw new IllegalStateException("Kun gyldig ved utbetalingsgradgrunnlag");
    }

    private static Beløp utledBruttoInntektFraTilkommetForArbeidstaker(Beløp tilkommetInntekt,
                                                                            Arbeidsgiver arbeidsgiver,
                                                                            InternArbeidsforholdRefDto internArbeidsforholdRefDto,
                                                                            Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            var snittFraværVektet = finnVektetSnittfraværForArbeidstaker(arbeidsgiver, internArbeidsforholdRefDto, periode, utbetalingsgradGrunnlag);
            var utbetalingsgrad = snittFraværVektet.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            if (utbetalingsgrad.equals(BigDecimal.ONE)) {
                return Beløp.ZERO;
            }
            return tilkommetInntekt.divider(BigDecimal.ONE.subtract(utbetalingsgrad), 10, RoundingMode.HALF_UP);
        }
        return Beløp.ZERO;
    }

    private static Beløp utledBruttoInntektFraTilkommetForFrilans(Beløp tilkommetInntekt,
                                                                       Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
            var snittFraværVektet = finnVektetSnittfraværForFrilans(periode, utbetalingsgradGrunnlag);
            var utbetalingsgrad = snittFraværVektet.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            if (utbetalingsgrad.equals(BigDecimal.ONE)) {
                return Beløp.ZERO;
            }
            return tilkommetInntekt.divider(BigDecimal.ONE.subtract(utbetalingsgrad), 10, RoundingMode.HALF_UP);
        }
        return Beløp.ZERO;
    }


    private static BigDecimal finnVektetSnittfraværForArbeidstaker(Arbeidsgiver arbeidsgiver,
                                                                   InternArbeidsforholdRefDto internArbeidsforholdRefDto,
                                                                   Intervall periode, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        var utbetalingsgradPrAktivitetDtos = finnPerioderForArbeid(utbetalingsgradGrunnlag,
                arbeidsgiver,
                internArbeidsforholdRefDto,
                true);
        return finnVektetSnittfravær(periode, utbetalingsgradPrAktivitetDtos);
    }

    private static BigDecimal finnVektetSnittfraværForFrilans(Intervall periode, UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        var utbetalingsgradPrAktivitetDtos = finnPerioderForStatus(AktivitetStatus.FRILANSER, utbetalingsgradGrunnlag);
        return finnVektetSnittfravær(periode, utbetalingsgradPrAktivitetDtos.map(List::of).orElse(Collections.emptyList()));
    }


    private static BigDecimal finnVektetSnittfravær(Intervall periode, List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetDtos) {
        return utbetalingsgradPrAktivitetDtos
                .stream()
                .flatMap(utb -> utb.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getPeriode().inkluderer(periode.getFomDato()))
                .map(p -> {
                    var virkedager = getBeregnVirkedager(p.getPeriode());
                    return BigDecimal.valueOf(virkedager).multiply(p.getUtbetalingsgrad().verdi());
                })
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(getBeregnVirkedager(periode)), 10, RoundingMode.HALF_UP);
    }

    private Beløp finnInntektForArbeidsgiver(Arbeidsgiver arbeidsgiver,
                                                  InternArbeidsforholdRefDto arbeidsforholdRef,
                                                  Intervall periode,
                                                  InntektArbeidYtelseGrunnlagDto iay, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var im = iay.getInntektsmeldinger().stream()
                .flatMap(ims -> ims.getAlleInntektsmeldinger().stream())
                .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef))
                .findFirst();

        if (im.isPresent()) {
            return im.get().getInntektBeløp().multipliser(KonfigTjeneste.getMånederIÅr());
        }
        var inntektFilterDto = new InntektFilterDto(iay.getAktørInntektFraRegister());
        var inntektsposter = inntektFilterDto.filterBeregningsgrunnlag()
                .filter(arbeidsgiver)
                .getFiltrertInntektsposter();

        var aktuellePoster = inntektsposter.stream().filter(i -> i.getPeriode().overlapper(
                        Intervall.fraOgMedTilOgMed(periode.getFomDato().minusMonths(3), periode.getFomDato().plusMonths(3))))
                .toList();

        var antallPoster = aktuellePoster.size();
        if (antallPoster == 0) {
            LOG.info("Fant ingen inntektsposter for arbeidsgiver {} i periode {}", arbeidsgiver, periode);
            return Beløp.ZERO;
        }
        var beløp = aktuellePoster.stream()
                .map(post -> finnBruttoInntektFraInntektspost(arbeidsgiver, arbeidsforholdRef, ytelsespesifiktGrunnlag, post))
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO)
                .divider(antallPoster, 10, RoundingMode.HALF_UP);
        return beløp;
    }

    private Beløp finnInntektForFrilans(Intervall periode,
                                             InntektArbeidYtelseGrunnlagDto iay,
                                             YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var inntektFilterDto = new InntektFilterDto(iay.getAktørInntektFraRegister());
        var frilansArbeidstaker = iay.getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter)
                .orElse(Collections.emptyList())
                .stream()
                .filter(ya -> ya.getArbeidType().equals(ArbeidType.FRILANSER_OPPDRAGSTAKER))
                .map(YrkesaktivitetDto::getArbeidsgiver)
                .distinct();
        return frilansArbeidstaker.map(a -> finnBeregnetÅrsinntekForArbeidSomFrilanser(periode, ytelsespesifiktGrunnlag, inntektFilterDto, a))
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO);


    }

    private static Beløp finnBeregnetÅrsinntekForArbeidSomFrilanser(Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektFilterDto inntektFilterDto, Arbeidsgiver a) {
        var inntektsposter = inntektFilterDto.filterBeregningsgrunnlag()
                .filter(a)
                .getFiltrertInntektsposter();

        var aktuellePoster = inntektsposter.stream().filter(i -> i.getPeriode().overlapper(
                        Intervall.fraOgMedTilOgMed(periode.getFomDato().minusMonths(1), periode.getTomDato().plusMonths(1))))
                .toList();

        var antallPoster = aktuellePoster.size();
        if (antallPoster == 0) {
            LOG.info("Fant ingen inntektsposter for arbeidsgiver {} i periode {}", a, periode);
            return Beløp.ZERO;
        }
        return aktuellePoster.stream().map(post -> finnBruttoInntektFraInntektspostForFrilans(ytelsespesifiktGrunnlag, post))
                .filter(Objects::nonNull)
                .reduce(Beløp::adder)
                .orElse(Beløp.ZERO)
                .divider(antallPoster, 10, RoundingMode.HALF_UP);
    }

    private static Beløp finnBruttoInntektFraInntektspost(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektspostDto post) {
        var postPeriode = post.getPeriode();
        var virkedagerIPeriode = getBeregnVirkedager(postPeriode);
        if (virkedagerIPeriode == 0) {
            LOG.info("Fant inntektspost uten virkedager for arbeidsgiver {} i periode {}", arbeidsgiver, post.getPeriode());
            return Beløp.ZERO;
        }
        var bruttoInntekt = utledBruttoInntektFraTilkommetForArbeidstaker(post.getBeløp(), arbeidsgiver, arbeidsforholdRef, postPeriode, ytelsespesifiktGrunnlag);
        return bruttoInntekt.divider(virkedagerIPeriode, 10, RoundingMode.HALF_UP).multipliser(KonfigTjeneste.getYtelsesdagerIÅr());
    }

    private static Beløp finnBruttoInntektFraInntektspostForFrilans(YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektspostDto post) {
        var postPeriode = post.getPeriode();
        var virkedagerIPeriode = getBeregnVirkedager(postPeriode);
        if (virkedagerIPeriode == 0) {
            return Beløp.ZERO;
        }
        var bruttoInntekt = utledBruttoInntektFraTilkommetForFrilans(post.getBeløp(), postPeriode, ytelsespesifiktGrunnlag);
        return bruttoInntekt.divider(virkedagerIPeriode, 10, RoundingMode.HALF_UP).multipliser(KonfigTjeneste.getYtelsesdagerIÅr());
    }


    private static int getBeregnVirkedager(Intervall postPeriode) {
        return Virkedager.beregnVirkedager(postPeriode.getFomDato(), postPeriode.getTomDato());
    }


    public record ReduksjonVedGradering(long gjeldendeDagsats,
                                        long gradertDagsats,
                                        LocalDate fom,
                                        LocalDate tom,
                                        int antallVirkedager,
                                        boolean erFordelt,
                                        Set<AktivitetStatus> tilkommetStatuser) {
    }

}
