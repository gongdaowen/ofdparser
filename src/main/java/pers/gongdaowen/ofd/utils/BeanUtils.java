package pers.gongdaowen.ofd.utils;

import java.util.Collection;
import java.util.Map;

public class BeanUtils {

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).isEmpty();
        }
        if (obj instanceof Collection<?>) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        if (obj instanceof Object[]) {
            return ((Object[]) obj).length == 0;
        }
        return false;
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static String emptyFilter(String str, String... defs) {
        if (isNotEmpty(str) || defs.length == 0) {
            return str;
        }
        for (String def : defs) {
            if (isNotEmpty(def)) {
                return def;
            }
        }
        return str;
    }
}
