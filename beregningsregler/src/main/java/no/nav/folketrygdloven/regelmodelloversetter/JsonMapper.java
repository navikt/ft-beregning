package no.nav.folketrygdloven.regelmodelloversetter;

import no.nav.fpsak.nare.json.JsonOutput;
import no.nav.fpsak.nare.json.NareJsonException;

public class JsonMapper {

    private JsonMapper() {
        // skjul public constructor
    }

    public static String asJson(Object object) {
        try {
            return JsonOutput.asJson(object);
        } catch (NareJsonException e) {
            throw new BeregningsregelException("Kunne ikke serialisere regelinput for beregningsgrunnlag.");
        }
    }

}
