package no.nav.folketrygdloven.kalkulus.felles.v1;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record Saksnummer(@JsonValue
                    @Valid
                    @NotNull
                    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
                    String verdi) {

    @JsonCreator
    public static Saksnummer fra(String saksnummer) {
        return saksnummer != null ? new Saksnummer(saksnummer) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Saksnummer ob && Objects.equals(this.verdi(), ob.verdi());

    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }
}
