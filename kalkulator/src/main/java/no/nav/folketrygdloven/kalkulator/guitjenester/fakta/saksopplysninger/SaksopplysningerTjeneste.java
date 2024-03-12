package no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger;

import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger.ArbeidMedLønnsendringTjeneste.lagArbeidsforholdMedLønnsendring;
import static no.nav.folketrygdloven.kalkulator.guitjenester.fakta.saksopplysninger.KortvarigArbeidsforholdTjeneste.lagKortvarigeArbeidsforhold;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta.Saksopplysninger;

public class SaksopplysningerTjeneste {

    private SaksopplysningerTjeneste() {
    }

    public static Saksopplysninger lagSaksopplysninger(BeregningsgrunnlagGUIInput input) {
        Saksopplysninger saksopplysninger = new Saksopplysninger();
        saksopplysninger.setLønnsendringSaksopplysning(LønnsendringSaksopplysningTjeneste.lagDto(input));
        saksopplysninger.setArbeidsforholdMedLønnsendring(lagArbeidsforholdMedLønnsendring(input));
        saksopplysninger.setKortvarigeArbeidsforhold(lagKortvarigeArbeidsforhold(input));
        return saksopplysninger;
    }

}
