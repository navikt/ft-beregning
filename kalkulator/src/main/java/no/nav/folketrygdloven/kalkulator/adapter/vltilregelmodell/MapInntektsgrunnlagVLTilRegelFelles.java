package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static no.nav.folketrygdloven.kalkulator.felles.ytelseovergang.DirekteOvergangTjeneste.finnAnvisningerForDirekteOvergangFraKap8;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class MapInntektsgrunnlagVLTilRegelFelles implements MapInntektsgrunnlagVLTilRegel {
	private static final String INNTEKT_RAPPORTERING_FRIST_DATO = "inntekt.rapportering.frist.dato";

	public Inntektsgrunnlag mapInntektsgrunnlagFørStpBeregning(BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning) {
		Objects.requireNonNull(skjæringstidspunktBeregning, "skjæringstidspunktBeregning");
        var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.setInntektRapporteringFristDag((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
		mapInntektArbeidYtelse(inntektsgrunnlag, input, skjæringstidspunktBeregning);

		return inntektsgrunnlag;
	}

	public Inntektsgrunnlag mapForenkletGrunnlagFørStpOpptjening(BeregningsgrunnlagInput input) {
        var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.setInntektRapporteringFristDag((Integer) input.getKonfigVerdi(INNTEKT_RAPPORTERING_FRIST_DATO));
		mapRegisterinntekter(inntektsgrunnlag, input, input.getSkjæringstidspunktOpptjening());
		return inntektsgrunnlag;
	}

	private void lagInntektBeregning(Inntektsgrunnlag inntektsgrunnlag, InntektFilterDto filter, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
		filter.filterBeregningsgrunnlag()
				.filter(i -> i.getArbeidsgiver() != null)
				.forFilter((inntekt, inntektsposter) -> mapInntekt(inntektsgrunnlag, inntekt, inntektsposter, yrkesaktiviteter));
	}

	private void mapInntekt(Inntektsgrunnlag inntektsgrunnlag,
	                        InntektDto inntekt,
	                        Collection<InntektspostDto> inntektsposter,
	                        Collection<YrkesaktivitetDto> yrkesaktiviteter) {
		inntektsposter.forEach(inntektspost -> {

            var arbeidsgiver = mapYrkesaktivitet(inntekt.getArbeidsgiver(), yrkesaktiviteter);


			if (Objects.isNull(arbeidsgiver)) {
				throw new IllegalStateException("Arbeidsgiver må være satt.");
			} else if (Objects.isNull(inntektspost.getPeriode().getFomDato())) {
				throw new IllegalStateException("Inntektsperiode må være satt.");
			} else if (Objects.isNull(inntektspost.getBeløp().verdi())) {
				throw new IllegalStateException("Inntektsbeløp må være satt.");
			}

			inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
					.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
					.medArbeidsgiver(arbeidsgiver)
					.medMåned(inntektspost.getPeriode().getFomDato())
					.medInntekt(inntektspost.getBeløp().verdi())
					.build());
		});
	}

	private Arbeidsforhold mapYrkesaktivitet(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
		var arbeidsforhold = erFrilanser(arbeidsgiver, yrkesaktiviteter)
				? Arbeidsforhold.frilansArbeidsforhold()
				: lagNyttArbeidsforholdHosArbeidsgiver(arbeidsgiver);

		settAnsettelsesperiode(arbeidsgiver, yrkesaktiviteter, arbeidsforhold);

		return arbeidsforhold;
	}

	private void settAnsettelsesperiode(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter, Arbeidsforhold arbeidsforhold) {
		var minMaksAnsettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(yrkesaktiviteter.stream()
				.filter(ya -> ya.getArbeidsgiver() != null)
				.filter(ya -> ya.getArbeidsgiver().equals(arbeidsgiver))
				.flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
				.collect(Collectors.toList()));
		minMaksAnsettelsesPeriode.ifPresent(Arbeidsforhold.builder(arbeidsforhold)::medAnsettelsesPeriode);
	}

	private boolean erFrilanser(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
		final var arbeidType = yrkesaktiviteter
				.stream()
				.filter(it -> it.getArbeidsgiver() != null)
				.filter(it -> it.getArbeidsgiver().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
				.map(YrkesaktivitetDto::getArbeidType)
				.distinct()
				.collect(Collectors.toList());
        var erFrilanser = yrkesaktiviteter.stream()
				.map(YrkesaktivitetDto::getArbeidType)
				.anyMatch(ArbeidType.FRILANSER::equals);
		return (arbeidType.isEmpty() && erFrilanser) || arbeidType.contains(ArbeidType.FRILANSER_OPPDRAGSTAKER);
	}

	private Arbeidsforhold lagNyttArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver) {
		if (arbeidsgiver.getErVirksomhet()) {
			return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getIdentifikator());
		} else if (arbeidsgiver.erAktørId()) {
			return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getIdentifikator());
		}
		throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet, men var: " + arbeidsgiver);
	}

	private void mapInntektsmelding(Inntektsgrunnlag inntektsgrunnlag,
	                                Collection<InntektsmeldingDto> inntektsmeldinger,
	                                YrkesaktivitetFilterDto filterYaRegister,
	                                LocalDate skjæringstidspunktBeregning) {
		inntektsmeldinger.stream()
				.filter(im -> slutterPåEllerEtterSkjæringstidspunkt(im, filterYaRegister, skjæringstidspunktBeregning))
				.map(im -> mapInntektOgNaturalytelser(im, filterYaRegister.getYrkesaktiviteterForBeregning()))
				.forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
	}

	private Periodeinntekt mapInntektOgNaturalytelser(InntektsmeldingDto im, Collection<YrkesaktivitetDto> yrkesaktiviteter) {

		try {
            var arbeidsforhold = MapArbeidsforholdFraVLTilRegel.mapForInntektsmelding(im);
			settAnsettelsesperiode(im.getArbeidsgiver(), yrkesaktiviteter, arbeidsforhold);
            var inntekt = Beløp.safeVerdi(im.getInntektBeløp());
            var naturalytelser = im.getNaturalYtelser().stream()
					.map(ny -> new no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse(ny.getBeloepPerMnd().verdi(),
							ny.getPeriode().getFomDato(), ny.getPeriode().getTomDato()))
					.collect(Collectors.toList());
            var naturalYtelserBuilder = Periodeinntekt.builder()
					.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
					.medArbeidsgiver(arbeidsforhold)
					.medInntekt(inntekt)
					.medNaturalYtelser(naturalytelser);
			im.getStartDatoPermisjon().ifPresent(dato -> naturalYtelserBuilder.medMåned(dato.minusMonths(1).withDayOfMonth(1)));
			return naturalYtelserBuilder.build();
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(String.format("Kunne ikke mappe inntektsmelding [journalpostId=%s]: %s",
					im.getJournalpostId(), e.getMessage()), e);
		}
	}

	private boolean slutterPåEllerEtterSkjæringstidspunkt(InntektsmeldingDto im, YrkesaktivitetFilterDto filterYaRegister, LocalDate skjæringstidspunktBeregning) {
		return filterYaRegister.getYrkesaktiviteter().stream()
				.filter(ya -> ya.gjelderFor(im))
				.anyMatch(ya -> FinnAnsettelsesPeriode.finnMinMaksPeriode(ya.getAlleAktivitetsAvtaler(), skjæringstidspunktBeregning)
						.map(periode -> !periode.getTom().isBefore(skjæringstidspunktBeregning)).orElse(false));
	}

	private void mapTilstøtendeYtelseAAP(Inntektsgrunnlag inntektsgrunnlag,
	                                     YtelseFilterDto ytelseFilter,
	                                     LocalDate skjæringstidspunkt) {

        var nyesteVedtakForDagsats = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(YtelseType.ARBEIDSAVKLARINGSPENGER));

		if (nyesteVedtakForDagsats.isEmpty()) {
			return;
		}

        var aap = mapYtelseFraArenavedtak(ytelseFilter, skjæringstidspunkt, nyesteVedtakForDagsats.get(), YtelseType.ARBEIDSAVKLARINGSPENGER);
		inntektsgrunnlag.leggTilPeriodeinntekt(aap);
	}

	private Periodeinntekt mapYtelseFraArenavedtak(YtelseFilterDto ytelseFilter, LocalDate skjæringstidspunkt, YtelseDto nyesteVedtakForDagsats, YtelseType arenaYtelse) {
		BigDecimal dagsats;
		BigDecimal utbetalingsfaktor;

        var sisteUtbetalingFørStp = MeldekortUtils.sisteHeleMeldekortFørStp(ytelseFilter, nyesteVedtakForDagsats,
				skjæringstidspunkt,
				Set.of(arenaYtelse));

		utbetalingsfaktor = sisteUtbetalingFørStp
				.flatMap(YtelseAnvistDto::getUtbetalingsgradProsent)
				.map(Stillingsprosent::verdi)
				.map(verdi -> nyesteVedtakForDagsats.harKildeKelvin() ? verdi.divide(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_KELVIN, 10, RoundingMode.HALF_UP) : verdi.divide(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG_ARENA, 10, RoundingMode.HALF_UP))
				.orElse(BigDecimal.ONE);

		dagsats = nyesteVedtakForDagsats.getVedtaksDagsats().map(Beløp::verdi)
				.orElse(sisteUtbetalingFørStp.flatMap(YtelseAnvistDto::getBeløp).map(Beløp::verdi).orElse(BigDecimal.ZERO));
		return Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
				.medInntekt(dagsats)
				.medMåned(skjæringstidspunkt)
				.medUtbetalingsfaktor(utbetalingsfaktor)
				.build();
	}

	private void mapTilstøtendeYtelseDagpenger(Inntektsgrunnlag inntektsgrunnlag,
	                                           YtelseFilterDto ytelseFilter,
	                                           LocalDate skjæringstidspunkt) {
        var nyesteVedtakForDagsats = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilter, skjæringstidspunkt, Set.of(YtelseType.DAGPENGER));
		if (nyesteVedtakForDagsats.isEmpty()) {
			return;
		}
        var dagpenger = mapYtelseFraArenavedtak(ytelseFilter, skjæringstidspunkt, nyesteVedtakForDagsats.get(), YtelseType.DAGPENGER);
		inntektsgrunnlag.leggTilPeriodeinntekt(dagpenger);
	}


	private void lagInntektSammenligning(Inntektsgrunnlag inntektsgrunnlag, InntektFilterDto filter) {
        var månedsinntekter = filter.filterSammenligningsgrunnlag().getFiltrertInntektsposter().stream()
				.collect(Collectors.groupingBy(ip -> ip.getPeriode().getFomDato(), Collectors.reducing(BigDecimal.ZERO,
						ip -> ip.getBeløp().verdi(), BigDecimal::add)));

		månedsinntekter.forEach((måned, inntekt) -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
				.medMåned(måned)
				.medInntekt(inntekt)
				.build()));
	}

	private void lagInntekterSN(Inntektsgrunnlag inntektsgrunnlag, InntektFilterDto filter) {
		filter.filterBeregnetSkatt().getFiltrertInntektsposter()
				.forEach(inntektspost -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
						.medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
						.medInntekt(inntektspost.getBeløp().verdi())
						.medPeriode(Periode.of(inntektspost.getPeriode().getFomDato(), inntektspost.getPeriode().getTomDato()))
						.build()));
	}

	private void mapInntektArbeidYtelse(Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning) {
        var iayGrunnlag = input.getIayGrunnlag();
        var inntektsmeldinger = input.getInntektsmeldinger();
		var arbeidsfilter = lagArbeidsfilter(iayGrunnlag);

		mapRegisterinntekter(inntektsgrunnlag, input, skjæringstidspunktBeregning);

		mapInntektsmelding(inntektsgrunnlag, inntektsmeldinger, arbeidsfilter, skjæringstidspunktBeregning);

		mapYtelseinntekter(inntektsgrunnlag, input, skjæringstidspunktBeregning, iayGrunnlag);

		mapOppgitteInntekter(inntektsgrunnlag, iayGrunnlag);
	}

	private void mapOppgitteInntekter(Inntektsgrunnlag inntektsgrunnlag, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var oppgittOpptjeningOpt = iayGrunnlag.getOppgittOpptjening();
		oppgittOpptjeningOpt.ifPresent(oppgittOpptjening -> mapOppgittOpptjening(inntektsgrunnlag, oppgittOpptjening));
	}

	private void mapYtelseinntekter(Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
		var ytelseFilter = new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister()).før(skjæringstidspunktBeregning);
		if (harStatus(input, no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus.ARBEIDSAVKLARINGSPENGER)) {
			mapTilstøtendeYtelseAAP(inntektsgrunnlag, ytelseFilter, skjæringstidspunktBeregning);
		}
		if (erDagpenger(input)) {
			mapTilstøtendeYtelseDagpenger(inntektsgrunnlag, ytelseFilter, skjæringstidspunktBeregning);
		}
		leggTilFraYtelseVedtak(iayGrunnlag, skjæringstidspunktBeregning, inntektsgrunnlag);
	}

	private void mapRegisterinntekter(Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagInput input, LocalDate skjæringstidspunkt) {
        var iayGrunnlag = input.getIayGrunnlag();
		var inntektsfilter = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister()).før(skjæringstidspunkt);
		var arbeidsfilter = lagArbeidsfilter(iayGrunnlag);

		if (!inntektsfilter.isEmpty()) {
			List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>();
			yrkesaktiviteter.addAll(arbeidsfilter.getYrkesaktiviteterForBeregning());
			yrkesaktiviteter.addAll(arbeidsfilter.getFrilansOppdrag());

			var bekreftetAnnenOpptjening = iayGrunnlag.getBekreftetAnnenOpptjening();
			var filterYaBekreftetAnnenOpptjening = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), bekreftetAnnenOpptjening)
					.før(skjæringstidspunkt);
			yrkesaktiviteter.addAll(filterYaBekreftetAnnenOpptjening.getYrkesaktiviteterForBeregning());

			lagInntektBeregning(inntektsgrunnlag, inntektsfilter, yrkesaktiviteter);
			lagInntektSammenligning(inntektsgrunnlag, inntektsfilter);
			lagInntekterSN(inntektsgrunnlag, inntektsfilter);
		}
	}

	private YrkesaktivitetFilterDto lagArbeidsfilter(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
		var aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister();
		return new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid);
	}

	private void leggTilFraYtelseVedtak(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
	                                    LocalDate skjæringstidspunktBeregning,
	                                    Inntektsgrunnlag inntektsgrunnlag) {
		finnAnvisningerForDirekteOvergangFraKap8(iayGrunnlag, skjæringstidspunktBeregning).stream()
				.flatMap(ya -> ya.getAnvisteAndeler()
						.stream().map(a -> mapTilPeriodeInntekt(ya, a)))
				.forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
	}

	private Periodeinntekt mapTilPeriodeInntekt(YtelseAnvistDto ya, AnvistAndel a) {
		return Periodeinntekt.builder().medInntekt(Beløp.safeVerdi(a.getDagsats()))
				.medInntektskildeOgPeriodeType(Inntektskilde.YTELSE_VEDTAK)
				.medInntektskategori(Inntektskategori.valueOf(a.getInntektskategori().getKode()))
				.medPeriode(Periode.of(ya.getAnvistFOM(), ya.getAnvistTOM()))
				.medUtbetalingsfaktor(BigDecimal.ONE)
				.build();
	}

	private boolean erDagpenger(BeregningsgrunnlagInput input) {
		List<BeregningsgrunnlagAktivitetStatusDto> statuser = input.getBeregningsgrunnlag() == null
				? Collections.emptyList()
				: input.getBeregningsgrunnlag().getAktivitetStatuser();
		return statuser.stream().anyMatch(akt -> akt.getAktivitetStatus().erDagpenger());
	}


	private boolean harStatus(BeregningsgrunnlagInput input, no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus status) {
		List<BeregningsgrunnlagAktivitetStatusDto> statuser = input.getBeregningsgrunnlag() == null
				? Collections.emptyList()
				: input.getBeregningsgrunnlag().getAktivitetStatuser();
		return statuser.stream().anyMatch(akt -> akt.getAktivitetStatus().equals(status));
	}

	void mapOppgittOpptjening(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjeningDto oppgittOpptjening) {
		oppgittOpptjening.getEgenNæring().stream()
				.filter(en -> en.getNyoppstartet() || en.getVarigEndring())
				.filter(en -> en.getBruttoInntekt() != null)
				.forEach(en -> inntektsgrunnlag.leggTilPeriodeinntekt(byggPeriodeinntektEgenNæring(en)));
	}

	private Periodeinntekt byggPeriodeinntektEgenNæring(OppgittEgenNæringDto en) {
		LocalDate datoForInntekt;
		if (en.getVarigEndring()) {
			datoForInntekt = en.getEndringDato();
		} else {
			datoForInntekt = en.getFraOgMed();
		}
		if (datoForInntekt == null) {
			throw new IllegalStateException("Søker har oppgitt varig endret eller nyoppstartet næring men har ikke oppgitt endringsdato eller oppstartsdato");
		}
		return Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
				.medMåned(datoForInntekt)
				.medInntekt(Beløp.safeVerdi(en.getBruttoInntekt()))
				.build();
	}

}
