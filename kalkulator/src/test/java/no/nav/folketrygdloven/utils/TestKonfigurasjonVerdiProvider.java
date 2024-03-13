package no.nav.folketrygdloven.utils;

import java.util.Properties;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdiProvider;

public final class TestKonfigurasjonVerdiProvider implements KonfigurasjonVerdiProvider {

    private final Properties verdier = new Properties(20);

    @Override
    public String get(String key) {
        return verdier.getProperty(key);
    }

    void put(String key, String value) {
        verdier.setProperty(key, value);
    }

    void remove(String key) {
        verdier.remove(key);
    }

    void clear() {
        verdier.clear();
    }
}
