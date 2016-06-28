package cm.aptoide.pt.model.v7.timeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Created by marcelobenites on 6/23/16.
 */
@EqualsAndHashCode
public class Publisher {

	@Getter private final String name;
	@Getter private final String logoUrl;

	@JsonCreator public Publisher(@JsonProperty("name") String name, @JsonProperty("logo") String logoUrl) {
		this.name = name;
		this.logoUrl = logoUrl;
	}
}