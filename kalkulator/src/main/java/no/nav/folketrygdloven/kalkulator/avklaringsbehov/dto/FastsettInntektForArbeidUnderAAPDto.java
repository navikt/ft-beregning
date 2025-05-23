package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

public class FastsettInntektForArbeidUnderAAPDto {

	private Integer fastsattPrMnd;

	public FastsettInntektForArbeidUnderAAPDto(Integer fastsattPrMnd) { // NOSONAR
		this.fastsattPrMnd = fastsattPrMnd;
	}

	public Integer getFastsattPrMnd() {
		return fastsattPrMnd;
	}

	public void setFastsattPrMnd(Integer fastsattPrMnd) {
		this.fastsattPrMnd = fastsattPrMnd;
	}
}
