package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@ToString
public class CosmosResponse {
    Map<String, Object> dps;
}
