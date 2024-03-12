package no.nav.folketrygdloven.kalkulus.mappers;


import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.READER_JSON;
import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.WRITER_JSON;
import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.validateResult;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.beregning.v1.AktivitetGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.AndelGraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulus.beregning.v1.GraderingDto;
import no.nav.folketrygdloven.kalkulus.beregning.v1.RefusjonskravDatoDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulus.felles.v1.AktørIdPersonident;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Organisasjon;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;
import no.nav.folketrygdloven.kalkulus.iay.IayProsent;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.ArbeidDto;
import no.nav.folketrygdloven.kalkulus.iay.arbeid.v1.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntekterDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.InntektsmeldingerDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingDto;
import no.nav.folketrygdloven.kalkulus.iay.inntekt.v1.UtbetalingsPostDto;
import no.nav.folketrygdloven.kalkulus.iay.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.AnvistAndel;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelseDto;
import no.nav.folketrygdloven.kalkulus.iay.ytelse.v1.YtelserDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.opptjening.v1.OpptjeningPeriodeDto;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregnForRequest;
import no.nav.folketrygdloven.kalkulus.request.v1.BeregnListeRequest;

public class KalkulatorMapperTest {

    private final InternArbeidsforholdRefDto ref = new InternArbeidsforholdRefDto(UUID.randomUUID().toString());
    private final Periode periode = new Periode(LocalDate.now(), LocalDate.now().plusMonths(2));
    private final Organisasjon organisasjon = new Organisasjon("974652269");
    private final Beløp beløpDto = Beløp.fra(BigDecimal.TEN);

    @Test
    void skal_generere_og_validere_roundtrip_kalkulator_input_json() throws Exception {

        KalkulatorInputDto grunnlag = byggKalkulatorInput();

        String json = WRITER_JSON.writeValueAsString(grunnlag);

        KalkulatorInputDto roundTripped = READER_JSON.forType(KalkulatorInputDto.class).readValue(json);

        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getIayGrunnlag()).isNotNull();
        validateResult(roundTripped);
    }


    @Test
    public void skal_generere_og_validere_roundtrip_av_start_beregning_request() throws Exception {
        //arrange
        Saksnummer saksnummer = new Saksnummer("1234");
        AktørIdPersonident dummy = AktørIdPersonident.dummy();
        KalkulatorInputDto kalkulatorInputDto = byggKalkulatorInput();

        var enUuid = UUID.randomUUID();
        BeregnListeRequest spesifikasjon = new BeregnListeRequest(
                saksnummer, UUID.randomUUID(), dummy, FagsakYtelseType.PLEIEPENGER_SYKT_BARN,
                BeregningSteg.FASTSETT_STP_BER,
                List.of(new BeregnForRequest(enUuid, List.of(UUID.randomUUID()), kalkulatorInputDto, null)));

        String json = WRITER_JSON.writeValueAsString(spesifikasjon);

        BeregnListeRequest roundTripped = READER_JSON.forType(BeregnListeRequest.class).readValue(json);

        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getAktør()).isEqualTo(dummy);
        assertThat(roundTripped.getSaksnummer()).isEqualTo(saksnummer);
        assertThat(roundTripped.getBeregnForListe().getFirst().getEksternReferanse()).isEqualTo(enUuid);
        validateResult(roundTripped);
    }

    private KalkulatorInputDto byggKalkulatorInput() {
        GraderingDto graderingDto = new GraderingDto(periode, Aktivitetsgrad.fra(100));
        AndelGraderingDto andelGraderingDto = new AndelGraderingDto(AktivitetStatus.ARBEIDSTAKER, organisasjon, null, List.of(graderingDto));
        AktivitetGraderingDto aktivitetGraderingDto = new AktivitetGraderingDto(List.of(andelGraderingDto));

        InntektArbeidYtelseGrunnlagDto iayGrunnlag = byggIAY();
        OpptjeningAktiviteterDto opptjeningAktiviteter = new OpptjeningAktiviteterDto(List.of(new OpptjeningPeriodeDto(OpptjeningAktivitetType.ARBEID, periode, organisasjon, null)));
        LocalDate skjæringstidspunkt = periode.getFom();

        KalkulatorInputDto kalkulatorInputDto = new KalkulatorInputDto(iayGrunnlag, opptjeningAktiviteter, skjæringstidspunkt);
        kalkulatorInputDto.medYtelsespesifiktGrunnlag(new ForeldrepengerGrunnlag(BigDecimal.valueOf(100), false, aktivitetGraderingDto, Collections.emptyList()));
        kalkulatorInputDto.medRefusjonskravDatoer(List.of(new RefusjonskravDatoDto(organisasjon, periode.getFom(), periode.getFom().minusMonths(1), true)));
        kalkulatorInputDto.medRefusjonskravDatoer(List.of(new RefusjonskravDatoDto(organisasjon, periode.getFom(), periode.getFom().minusMonths(1), true)));

        return kalkulatorInputDto;
    }

    private InntektArbeidYtelseGrunnlagDto byggIAY() {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = new InntektArbeidYtelseGrunnlagDto();
        iayGrunnlag.medArbeidDto(new ArbeidDto(List.of(new YrkesaktivitetDto(organisasjon, ref, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, List.of(new AktivitetsAvtaleDto(periode, null, IayProsent.fra(100)), new AktivitetsAvtaleDto(periode, null, null))))));
        iayGrunnlag.medYtelserDto(new YtelserDto(byggYtelseDto()));
        iayGrunnlag.medInntekterDto(new InntekterDto(List.of(new UtbetalingDto(InntektskildeType.INNTEKT_BEREGNING, List.of(new UtbetalingsPostDto(periode, InntektspostType.LØNN, Beløp.fra(1000)))))));
        iayGrunnlag.medInntektsmeldingerDto(new InntektsmeldingerDto(List.of(new InntektsmeldingDto(organisasjon, Beløp.fra(100), List.of(), List.of(), null, null, null, null, null))));
        return iayGrunnlag;
    }

    private List<YtelseDto> byggYtelseDto() {
        YtelseAnvistDto ytelseAnvistDto = new YtelseAnvistDto(periode, beløpDto, beløpDto, IayProsent.fra(10), List.of(new AnvistAndel(new Organisasjon("974652269"),
                new InternArbeidsforholdRefDto("r8j3wr8w3"),
                beløpDto,
                IayProsent.fra(100),
                IayProsent.fra(100),
                Inntektskategori.ARBEIDSTAKER)));
        return List.of(new YtelseDto(beløpDto, Set.of(ytelseAnvistDto), YtelseType.FORELDREPENGER, periode, null));
    }
}
