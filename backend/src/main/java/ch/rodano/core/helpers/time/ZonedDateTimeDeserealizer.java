package ch.rodano.core.helpers.time;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ZonedDateTimeDeserealizer extends JsonDeserializer<ZonedDateTime> {
	//The DateTimeFormatter to use
	private final DateTimeFormatter dtf;

	/**
	 * Constructor
	 * Instantiate a new Deserealizer
	 *
	 * @param dtf The datetime formatter
	 */
	public ZonedDateTimeDeserealizer(final DateTimeFormatter dtf) {
		if(dtf == null) {
			throw new IllegalArgumentException("Date time formatter is null");
		}
		this.dtf = dtf;
	}

	@Override
	public ZonedDateTime deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
		return ZonedDateTime.parse(jsonParser.getText(), dtf);
	}
}
