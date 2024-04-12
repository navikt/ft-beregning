package no.nav.folketrygdloven.kalkulus.mappers;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonMapperUtil {

    public static final ObjectWriter WRITER_JSON = JsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    public static final ObjectReader READER_JSON = JsonMapper.getMapper().reader();
}
