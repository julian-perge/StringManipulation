package osmedile.intellij.stringmanip.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;

public class ReplacedLibrariesTest {

	@Test
	public void rfc3986EncodesEverythingExceptUnreservedCharacters() {
		assertEquals("aZ9-._~%20%2B%2F%3F%26%3D%23%25%C3%A9", EncodingUtils.encodeUrlRFC3986("aZ9-._~ +/?&=#%" + (char) 0xE9));
	}

	@Test
	public void rfc3986DecodeKeepsPlusAndLiteralNonAscii() {
		assertEquals("+ A" + (char) 0xE9, EncodingUtils.decodeUrlRFC3986("+%20%41" + (char) 0xE9));
		assertThrows(IllegalArgumentException.class, () -> EncodingUtils.decodeUrlRFC3986("%4"));
		assertThrows(IllegalArgumentException.class, () -> EncodingUtils.decodeUrlRFC3986("%zz"));
	}

	@Test
	public void minifyJsonKeepsOrderAndNumbers() {
		assertEquals("{\"b\":null,\"a\":[1,2.0,1.50,1e3,-0,-0.0,1e400,12345678901234567890,\"</a>\"]}",
				JsonUtils.minify("{ b: null, 'a': [1, 2.0, 1.50, 1e3, -0, -0.0, 1e400, 12345678901234567890, \"</a>\"] }"));
	}

	@Test
	public void parseJsonKeepsAllDigitsOfDecimals() {
		assertEquals("[0.1234567890123456789012345, 1E+400]",
				JsonUtils.parse("[0.1234567890123456789012345, 1e400]").toString());
	}

	@Test
	public void jsonThatWouldLoseTextIsRejected() {
		for (String json : new String[]{"{\"a\":1,\"a\":2}", "{\"a\":1} trailing", "[1,]"}) {
			assertThrows(json, IllegalArgumentException.class, () -> JsonUtils.minify(json));
			assertThrows(json, IllegalArgumentException.class, () -> JsonUtils.parse(json));
		}
	}

	@Test
	public void deepCloneCopiesNestedCollections() {
		java.util.List<java.util.Map<String, java.util.List<String>>> original = new java.util.ArrayList<>();
		original.add(new java.util.LinkedHashMap<>(java.util.Map.of("k", new java.util.ArrayList<>(java.util.List.of("v")))));

		java.util.List<java.util.Map<String, java.util.List<String>>> clone = Cloner.deepClone(original);

		assertEquals(original, clone);
		assertNotSame(original.get(0).get("k"), clone.get(0).get("k"));
	}
}
