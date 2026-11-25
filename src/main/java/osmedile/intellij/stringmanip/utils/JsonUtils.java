package osmedile.intellij.stringmanip.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

public class JsonUtils {
	/**
	 * Keeps the key order. Accepts unquoted keys and single quotes. Fails on duplicate keys and on text after
	 * the first value, because both would otherwise silently remove a part of the user's selection.
	 */
	private static final JsonMapper MAPPER = JsonMapper.builder()
			.enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
			.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
			.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
			.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
			.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
			.build();

	/**
	 * Returns maps, lists and plain values. Decimals become {@link java.math.BigDecimal} to keep all their digits.
	 */
	public static Object parse(String json) {
		try {
			return MAPPER.reader()
					.with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
					.forType(Object.class)
					.readValue(json);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public static String minify(String json) {
		// The full parse rejects bad input. The token copy below does not check for text after the value.
		parse(json);
		StringWriter result = new StringWriter();
		try (JsonParser parser = MAPPER.createParser(json);
			 JsonGenerator generator = MAPPER.createGenerator(result)) {
			for (JsonToken token = parser.nextToken(); token != null; token = parser.nextToken()) {
				if (token.isNumeric()) {
					// Copying the text keeps numbers such as -0, 1.50 and 1e3 as the user wrote them.
					generator.writeNumber(parser.getText());
				} else {
					generator.copyCurrentEvent(parser);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return result.toString();
	}
}
