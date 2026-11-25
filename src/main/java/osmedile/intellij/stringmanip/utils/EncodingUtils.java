package osmedile.intellij.stringmanip.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class EncodingUtils {
    public static String encodeUrl(String selectedText) {
        try {
            return URLEncoder.encode(selectedText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public static String decodeUrl(String selectedText) {
        try {
            return URLDecoder.decode(selectedText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeUrlRFC3986(String selectedText) {
        StringBuilder result = new StringBuilder();
        for (byte b : selectedText.getBytes(StandardCharsets.UTF_8)) {
            if (isUnreservedRFC3986(b)) {
                result.append((char) b);
            } else {
                result.append('%').append(HexFormat.of().withUpperCase().toHexDigits(b));
            }
        }
        return result.toString();
    }

    private static boolean isUnreservedRFC3986(byte b) {
        return (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9')
                || b == '-' || b == '.' || b == '_' || b == '~';
    }

    /**
     * Unlike {@link URLDecoder}, this keeps '+' as it is.
     */
    public static String decodeUrlRFC3986(String selectedText) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int i = 0;
        while (i < selectedText.length()) {
            if (selectedText.charAt(i) == '%') {
                if (i + 2 >= selectedText.length()
                        || !HexFormat.isHexDigit(selectedText.charAt(i + 1))
                        || !HexFormat.isHexDigit(selectedText.charAt(i + 2))) {
                    throw new IllegalArgumentException("Invalid encoded sequence \"" + selectedText.substring(i) + "\"");
                }
                bytes.write(HexFormat.fromHexDigits(selectedText, i + 1, i + 3));
                i += 3;
            } else {
                int end = selectedText.indexOf('%', i);
                if (end < 0) {
                    end = selectedText.length();
                }
                bytes.writeBytes(selectedText.substring(i, end).getBytes(StandardCharsets.UTF_8));
                i = end;
            }
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

}
