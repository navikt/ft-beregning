package no.nav.folketrygdloven.kalkulus.migrering;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovStatus;

public class AvklaringsbehovMigreringDto extends BaseMigreringDto {

	@Valid
	@NotNull
	private AvklaringsbehovDefinisjon definisjon;

	@Valid
	@NotNull
	private AvklaringsbehovStatus status;

	@Valid
	@Pattern(regexp = "^[\\p{N}\\p{L}\\p{M}\\p{Z}\\p{Cf}\\p{P}\\p{Sc}\\p{Sk}\n\t+=]*$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
	private String begrunnelse;

	@Valid
	@NotNull
	private Boolean erTrukket;

	@Valid
	@Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}§]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
	private String vurdertAv;

	@Valid
	private LocalDateTime vurdertTidspunkt;

    public AvklaringsbehovMigreringDto() {
    }

	public AvklaringsbehovMigreringDto(AvklaringsbehovDefinisjon definisjon,
	                                   AvklaringsbehovStatus status,
	                                   String begrunnelse,
	                                   Boolean erTrukket,
	                                   String vurdertAv,
	                                   LocalDateTime vurdertTidspunkt) {
		this.definisjon = definisjon;
		this.status = status;
		this.begrunnelse = begrunnelse;
		this.erTrukket = erTrukket;
		this.vurdertAv = vurdertAv;
		this.vurdertTidspunkt = vurdertTidspunkt;
	}

	public AvklaringsbehovDefinisjon getDefinisjon() {
		return definisjon;
	}

	public AvklaringsbehovStatus getStatus() {
		return status;
	}

	public String getBegrunnelse() {
		return begrunnelse;
	}

	public Boolean getErTrukket() {
		return erTrukket;
	}

	public String getVurdertAv() {
		return vurdertAv;
	}

	public LocalDateTime getVurdertTidspunkt() {
		return vurdertTidspunkt;
	}
}
