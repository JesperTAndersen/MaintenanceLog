package app.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RandomUserDTO
{
    @JsonProperty("name")
    private Name name;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("login")
    private Login login;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Name(String first, String last)
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Login(String password)
    {
    }
}
