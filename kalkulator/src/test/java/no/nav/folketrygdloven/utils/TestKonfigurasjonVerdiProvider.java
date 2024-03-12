package no.nav.folketrygdloven.utils;

import java.util.Properties;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdiProvider;

public final class TestKonfigurasjonVerdiProvider implements KonfigurasjonVerdiProvider {

    private final Properties verdier = new Properties(20);

    @Override
    public String get(String key) {
        return verdier.getProperty(key);
    }

    public void put(String key, String value) {
        verdier.setProperty(key, value);
    }

    public void remove(String key) {
        verdier.remove(key);
    }

    public void clear() {
        verdier.clear();
    }
}
