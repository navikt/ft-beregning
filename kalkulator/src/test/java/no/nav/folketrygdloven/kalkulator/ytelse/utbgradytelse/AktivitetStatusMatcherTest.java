package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AktivitetStatusMatcherTest {

	@Test
	void skal_mappe_SN_IKKE_AKTIV_til_SN() {
		boolean matcher = AktivitetStatusMatcher.matcherStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE_IKKE_AKTIV);
		assertTrue(matcher, "SN_IKKE_AKTIV skulle matche selvstendig næringsdrivende");
	}

	@Test
	void skal_mappe_FL_IKKE_AKTIV_til_FL() {
		boolean matcher = AktivitetStatusMatcher.matcherStatus(AktivitetStatus.FRILANSER, UttakArbeidType.FRILANSER_IKKE_AKTIV);
		assertTrue(matcher, "FL_IKKE_AKTIV skulle matche frilanser");
	}

}