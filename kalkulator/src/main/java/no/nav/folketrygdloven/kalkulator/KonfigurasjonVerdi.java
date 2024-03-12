package no.nav.folketrygdloven.kalkulator;

import java.util.Optional;

public final class KonfigurasjonVerdi {

    private static KonfigurasjonVerdi INSTANCE;

    private final KonfigurasjonVerdiProvider provider;

    private KonfigurasjonVerdi(KonfigurasjonVerdiProvider provider) {
        this.provider = provider;
    }

    public static synchronized void configure(KonfigurasjonVerdiProvider provider) {
        var inst = INSTANCE;
        inst = new KonfigurasjonVerdi(provider);
        INSTANCE = inst;
    }

    public static synchronized void clear() {
        var inst = INSTANCE;
        inst = null;
        INSTANCE = inst;
    }

    public static synchronized KonfigurasjonVerdi instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new KonfigurasjonVerdi(new DummyKonfigurasjonVerdiProvider());
            INSTANCE = inst;
        }
        return inst;
    }

    public boolean get(String key, boolean defaultValue) {
        return Optional.ofNullable(provider.get(key)).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    private static class DummyKonfigurasjonVerdiProvider implements KonfigurasjonVerdiProvider {
        @Override
        public String get(String key) {
            return null;
        }
    }
}
