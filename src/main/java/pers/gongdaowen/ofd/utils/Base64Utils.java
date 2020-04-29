package pers.gongdaowen.ofd.utils;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.Base64;

public class Base64Utils {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private static final Base64Delegate delegate = new JdkBase64Delegate();

    public static byte[] encode(byte[] src) {
        return delegate.encode(src);
    }

    public static byte[] decode(byte[] src) {
        return delegate.decode(src);
    }

    public static byte[] encodeUrlSafe(byte[] src) {
        return delegate.encodeUrlSafe(src);
    }

    public static byte[] decodeUrlSafe(byte[] src) {
        return delegate.decodeUrlSafe(src);
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

    public static byte[] decodeFromString(String src) {
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

    public static String encodeToUrlSafeString(byte[] src) {
        return new String(delegate.encodeUrlSafe(src), DEFAULT_CHARSET);
    }

    public static byte[] decodeFromUrlSafeString(String src) {
        return delegate.decodeUrlSafe(src.getBytes(DEFAULT_CHARSET));
    }


    interface Base64Delegate {

        byte[] encode(byte[] src);

        byte[] decode(byte[] src);

        byte[] encodeUrlSafe(byte[] src);

        byte[] decodeUrlSafe(byte[] src);
    }


    static class JdkBase64Delegate implements Base64Delegate {

        @Override
        public byte[] encode(byte[] src) {
            if (src == null || src.length == 0) {
                return src;
            }
            return Base64.getEncoder().encode(src);
        }

        @Override
        public byte[] decode(byte[] src) {
            if (src == null || src.length == 0) {
                return src;
            }
            return Base64.getDecoder().decode(src);
        }

        @Override
        public byte[] encodeUrlSafe(byte[] src) {
            if (src == null || src.length == 0) {
                return src;
            }
            return Base64.getUrlEncoder().encode(src);
        }

        @Override
        public byte[] decodeUrlSafe(byte[] src) {
            if (src == null || src.length == 0) {
                return src;
            }
            return Base64.getUrlDecoder().decode(src);
        }

    }
}
