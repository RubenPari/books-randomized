package dev.rubenpari.backend.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO mapping the language resource from the external book database API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalLanguage {
    private String iso639Code;
    private String englishName;

    public String getIso639Code() {
        return iso639Code;
    }

    public void setIso639Code(String iso639Code) {
        this.iso639Code = iso639Code;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }
}
