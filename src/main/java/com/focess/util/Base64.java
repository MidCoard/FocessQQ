package com.focess.util;

import java.nio.charset.StandardCharsets;

public class Base64 {

    public static String base64Encode(byte[] bytes) {
        return new String(java.util.Base64.getEncoder().encode(bytes),StandardCharsets.ISO_8859_1);
    }

    public static byte[] base64Decode(String value) {
        return java.util.Base64.getDecoder().decode(value);
    }
}
