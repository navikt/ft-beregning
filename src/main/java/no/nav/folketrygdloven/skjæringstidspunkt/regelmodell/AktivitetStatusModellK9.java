package no.nav.folketrygdloven.skjæringstidspunkt.regelmodell;

import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class AktivitetStatusModellK9 extends AktivitetStatusModell {

	private MidlertidigInaktivType midlertidigInaktivType;

	public AktivitetStatusModellK9() {
		super();
	}

	public AktivitetStatusModellK9(MidlertidigInaktivType midlertidigInaktivType,
	                                    AktivitetStatusModell aktivitetStatusModell) {
		super(aktivitetStatusModell);
		this.midlertidigInaktivType = midlertidigInaktivType;

	}

	public MidlertidigInaktivType getMidlertidigInaktivType() {
		return midlertidigInaktivType;
	}

	@Override
	public LocalDate getBeregningstidspunkt() {
		LocalDate sisteAktivitetsdato = sisteAktivitetsdato();
		return sisteAktivitetsdato.isBefore(skjæringstidspunktForOpptjening) ? sisteAktivitetsdato : skjæringstidspunktForOpptjening.minusDays(1);
	}

}
