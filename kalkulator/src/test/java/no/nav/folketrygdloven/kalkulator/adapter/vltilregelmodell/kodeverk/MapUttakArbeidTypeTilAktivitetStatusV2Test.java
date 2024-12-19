package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;

import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapUttakArbeidTypeTilAktivitetStatusV2Test {

	@Test
	public void skal_mappe_SN_IKKE_AKTIV_til_SN() {
		var aktivitet = new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.SN_IKKE_AKTIV);
		var status = MapUttakArbeidTypeTilAktivitetStatusV2.mapAktivitetStatus(aktivitet);
		assertEquals(AktivitetStatusV2.SN, status);
	}

	@Test
	public void skal_mappe_FL_IKKE_AKTIV_til_FL() {
		var aktivitet = new AktivitetDto(null, InternArbeidsforholdRefDto.nullRef(), UttakArbeidType.FL_IKKE_AKTIV);
		var status = MapUttakArbeidTypeTilAktivitetStatusV2.mapAktivitetStatus(aktivitet);
		assertEquals(AktivitetStatusV2.FL, status);
	}

}