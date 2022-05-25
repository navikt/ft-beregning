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


	@Override
	public LocalDate getBeregningstidspunkt() {
		if (MidlertidigInaktivType.B.equals(midlertidigInaktivType)) {
			return getSkjæringstidspunktForBeregning();
		}
		return getSkjæringstidspunktForBeregning().minusDays(1);
	}

	public MidlertidigInaktivType getMidlertidigInaktivType() {
		return midlertidigInaktivType;
	}
}
