package no.nav.folketrygdloven.kalkulator.modell.diff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BasisKodeverdi;
import no.nav.folketrygdloven.kalkulus.kodeverk.KodeKonstanter;

/**
 * Hjelpemetoder for å raskere sette sammen en IndexKey fra flere deler.
 * Må være String, CharSequence, Number eller implementere IndexKey for hver key part.
 */
final class IndexKeyComposer {

    private IndexKeyComposer() {
        // hidden
    }

    /** Hjelpe metode for å effektivt generere keys. */
    static String createKey(Object... keyParts) {
        var sb = new StringBuilder(keyParts.length * 10);
        var max = keyParts.length;
        for (var i = 0; i < max; i++) {
            var part = toString(keyParts[i], i);
            sb.append(part);
            if (i < (max - 1)) {
                sb.append("::");
            }
        }
        return sb.toString();

    }

    private static String toString(Object obj, int i) {
        if (obj == null) {
            return KodeKonstanter.UDEFINERT;
        }
        var objClass = obj.getClass();
        if (String.class.isAssignableFrom(objClass)) {
            return (String) obj;
        } else if (IndexKey.class.isAssignableFrom(objClass)) {
            return ((IndexKey) obj).getIndexKey();
        } else if (Intervall.class.isAssignableFrom(objClass)) {
            var periode = (Intervall) obj;
            return "[" + periode.getFomDato().format(DateTimeFormatter.ISO_DATE) + //$NON-NLS-1$
                "," + periode.getTomDato().format(DateTimeFormatter.ISO_DATE) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (LocalDate.class.isAssignableFrom(objClass)) {
            var dt = (LocalDate) obj;
            return dt.format(DateTimeFormatter.ISO_DATE);
        } else if (LocalDateTime.class.isAssignableFrom(objClass)) {
            var ldt = (LocalDateTime) obj;
            return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else if (BasisKodeverdi.class.isAssignableFrom(objClass)) {
            var bkv = (BasisKodeverdi) obj;
            return bkv.getIndexKey();
        } else {
            throw new IllegalArgumentException("Støtter ikke å lage IndexKey for " + objClass.getName() + "[index=" + i + "], " + obj);
        }
    }
}
