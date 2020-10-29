package pers.gongdaowen.ofd.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public static <T> T[] convertArray(Object[] objs, Class<T> clz) {
        List<Object> list = new ArrayList<>();
        for (Object obj : objs) {
            if (obj == null) {
                list.add(null);
            } else if (String.class.equals(clz)) {
                list.add(obj.toString());
            } else if (Integer.class.equals(clz)) {
                list.add(Integer.parseInt(obj.toString()));
            } else if (Double.class.equals(clz)) {
                list.add(Double.parseDouble(obj.toString()));
            } else if (Float.class.equals(clz)) {
                list.add(Float.parseFloat(obj.toString()));
            } else if (Boolean.class.equals(clz)) {
                list.add(Boolean.parseBoolean(obj.toString()));
            } else if (Byte.class.equals(clz)) {
                list.add(Byte.parseByte(obj.toString()));
            } else if (Short.class.equals(clz)) {
                list.add(Short.parseShort(obj.toString()));
            } else if (Long.class.equals(clz)) {
                list.add(Long.parseLong(obj.toString()));
            } else if (Character.class.equals(clz)) {
                list.add(obj.toString().length() == 1 ? obj.toString().charAt(0) : (char) obj);
            }
        }
        return (T[]) list.toArray();
    }

    /**
     * 数字转大写
     */
    public static class NumberToCN {
        /**
         * 汉语中数字大写
         */
        private static final String[] CN_UPPER_NUMBER = { "零", "壹", "贰", "叁",
                "肆", "伍", "陆", "柒", "捌", "玖" };
        /**
         * 汉语中货币单位大写，这样的设计类似于占位符
         */
        private static final String[] CN_UPPER_MONEY_UNIT = { "分", "角", "元",
                "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆",
                "拾", "佰", "仟" };
        /**
         * 特殊字符：整
         */
        private static final String CN_FULL = "整";
        /**
         * 特殊字符：负
         */
        private static final String CN_NEGATIVE = "负";
        /**
         * 金额的精度，默认值为2
         */
        private static final int MONEY_PRECISION = 2;
        /**
         * 特殊字符：零元整
         */
        private static final String CN_ZERO_FULL = "零元" + CN_FULL;

        /**
         * 把输入的金额转换为汉语中人民币的大写
         *
         * @param numberOfMoney
         *            输入的金额
         * @return 对应的汉语大写
         */
        public static String from(BigDecimal numberOfMoney) {
            StringBuffer sb = new StringBuffer();
            // -1, 0, or 1 as the value of this BigDecimal is negative, zero, or
            // positive.
            int signum = numberOfMoney.signum();
            // 零元整的情况
            if (signum == 0) {
                return CN_ZERO_FULL;
            }
            // 这里会进行金额的四舍五入
            long number = numberOfMoney.movePointRight(MONEY_PRECISION)
                    .setScale(0, 4).abs().longValue();
            // 得到小数点后两位值
            long scale = number % 100;
            int numUnit = 0;
            int numIndex = 0;
            boolean getZero = false;
            // 判断最后两位数，一共有四中情况：00 = 0, 01 = 1, 10, 11
            if (!(scale > 0)) {
                numIndex = 2;
                number = number / 100;
                getZero = true;
            }
            if ((scale > 0) && (!((scale % 10) > 0))) {
                numIndex = 1;
                number = number / 10;
                getZero = true;
            }
            int zeroSize = 0;
            while (true) {
                if (number <= 0) {
                    break;
                }
                // 每次获取到最后一个数
                numUnit = (int) (number % 10);
                if (numUnit > 0) {
                    if ((numIndex == 9) && (zeroSize >= 3)) {
                        sb.insert(0, CN_UPPER_MONEY_UNIT[6]);
                    }
                    if ((numIndex == 13) && (zeroSize >= 3)) {
                        sb.insert(0, CN_UPPER_MONEY_UNIT[10]);
                    }
                    sb.insert(0, CN_UPPER_MONEY_UNIT[numIndex]);
                    sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                    getZero = false;
                    zeroSize = 0;
                } else {
                    ++zeroSize;
                    if (!(getZero)) {
                        sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                    }
                    if (numIndex == 2) {
                        if (number > 0) {
                            sb.insert(0, CN_UPPER_MONEY_UNIT[numIndex]);
                        }
                    } else if ((((numIndex - 2) % 4) == 0)
                            && ((number % 1000) > 0)) {
                        sb.insert(0, CN_UPPER_MONEY_UNIT[numIndex]);
                    }
                    getZero = true;
                }
                // 让number每次都去掉最后一个数
                number = number / 10;
                ++numIndex;
            }
            // 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
            if (signum == -1) {
                sb.insert(0, CN_NEGATIVE);
            }
            // 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
            if (!(scale > 0)) {
                sb.append(CN_FULL);
            }
            return sb.toString();
        }
    }
}
