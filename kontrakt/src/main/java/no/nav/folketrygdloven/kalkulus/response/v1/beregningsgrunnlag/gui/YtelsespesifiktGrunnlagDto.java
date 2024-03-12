package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.folketrygdloven.kalkulus.kodeverk.KodeKonstanter;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp.ForeldrepengerGrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn.FrisinnGrunnlagDto;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonTypeInfo(
        use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="ytelsetype")
@JsonSubTypes({
        @JsonSubTypes.Type(value= FrisinnGrunnlagDto.class, name= KodeKonstanter.YT_FRISINN),
        @JsonSubTypes.Type(value= OmsorgspengeGrunnlagDto.class, name= KodeKonstanter.YT_OMSORGSPENGER),
        @JsonSubTypes.Type(value= SvangerskapspengerGrunnlagDto.class, name= KodeKonstanter.YT_SVANGERSKAPSPENGER),
        @JsonSubTypes.Type(value= ForeldrepengerGrunnlagDto.class, name= KodeKonstanter.YT_FORELDREPENGER),
})
public abstract class YtelsespesifiktGrunnlagDto {

    public YtelsespesifiktGrunnlagDto() {
        // Jackson
    }
}

