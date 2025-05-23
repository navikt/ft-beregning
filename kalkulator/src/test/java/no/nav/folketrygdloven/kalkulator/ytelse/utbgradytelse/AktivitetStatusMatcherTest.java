package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

class AktivitetStatusMatcherTest {

	@Test
	void skal_mappe_SN_IKKE_AKTIV_til_SN() {
		var matcher = AktivitetStatusMatcher.matcherStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE_IKKE_AKTIV);
		assertTrue(matcher, "SN_IKKE_AKTIV skulle matche selvstendig næringsdrivende");
	}

	@Test
	void skal_mappe_FL_IKKE_AKTIV_til_FL() {
		var matcher = AktivitetStatusMatcher.matcherStatus(AktivitetStatus.FRILANSER, UttakArbeidType.FRILANSER_IKKE_AKTIV);
		assertTrue(matcher, "FL_IKKE_AKTIV skulle matche frilanser");
	}

}
