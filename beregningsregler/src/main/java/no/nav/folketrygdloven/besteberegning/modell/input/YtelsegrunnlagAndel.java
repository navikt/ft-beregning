package no.nav.folketrygdloven.besteberegning.modell.input;

import java.math.BigDecimal;

public class YtelsegrunnlagAndel {
	private YtelseAktivitetType aktivitet;
	private BigDecimal dagsats;


	public YtelsegrunnlagAndel(YtelseAktivitetType aktivitet) {
		this.aktivitet = aktivitet;
	}

	public YtelsegrunnlagAndel(YtelseAktivitetType aktivitet, BigDecimal dagsats) {
		this.aktivitet = aktivitet;
		this.dagsats = dagsats;
	}


	public YtelseAktivitetType getAktivitet() {
		return aktivitet;
	}

	public BigDecimal getDagsats() {
		return dagsats;
	}

	@Override
	public String toString() {
		return "YtelsegrunnlagAndel{" +
				"aktivitet=" + aktivitet +
				", dagsats=" + dagsats +
				'}';
	}
}
