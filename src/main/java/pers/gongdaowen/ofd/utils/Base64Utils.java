package pers.gongdaowen.ofd.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Base64Utils {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final Base64Delegate delegate = new JdkBase64Delegate();

    public static byte[] encode(byte[] src) {
        return delegate.encode(src);
    }

    public static byte[] decode(byte[] src) throws IOException {
        return delegate.decode(src);
    }

    public static String encodeToString(byte[] src) {
        if (src == null) {
            return null;
        }
        if (src.length == 0) {
            return "";
        }

        if (delegate != null) {
            // Full encoder available
            return new String(delegate.encode(src), DEFAULT_CHARSET);
        }
        else {
            // JAXB fallback for String case
            return DatatypeConverter.printBase64Binary(src);
        }
    }

    public static byte[] decodeFromString(String src) throws IOException {
        if (src == null) {
            return null;
        }
        if (src.length() == 0) {
            return new byte[0];
        }

        if (delegate != null) {
            // Full encoder available
            return delegate.decode(src.getBytes(DEFAULT_CHARSET));
        }
        else {
            // JAXB fallback for String case
            return DatatypeConverter.parseBase64Binary(src);
        }
    }

    interface Base64Delegate {

        byte[] encode(byte[] src);

        byte[] decode(byte[] src) throws IOException;
    }


    static class JdkBase64Delegate implements Base64Delegate {

        @Override
        public byte[] encode(byte[] src) {
            if (src == null || src.length == 0) {
                return src;
            }
            return new BASE64Encoder().encode(src).getBytes(DEFAULT_CHARSET);
        }

        @Override
        public byte[] decode(byte[] src) throws IOException {
            if (src == null || src.length == 0) {
                return src;
            }
            return new BASE64Decoder().decodeBuffer(new String(src, DEFAULT_CHARSET));
        }

    }
}
