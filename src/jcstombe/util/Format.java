package jcstombe.util;

/**
 * @author Josh Stomberg <jcstombe@mtu.edu>
 * Last Modified: May 07, 2018
 */
public class Format {

    private enum Flag {
        NONE('\0'),
        LEFT_JUSTIFY('-'),
        PLUS('+'),
        LEADING_SPACE(' '),
        ZERO_PAD('0');

        private static final int CLEAR_FLAGS = ((1 << Flag.values().length) - 1);
        private char c;

        Flag(char c) {
            this.c = c;
        }

        int mask() {
            return (1 << (ordinal() - 1));
        }

        String flag() {
            return ("" + c);
        }

        int and(int flags) {
            int result = flags;
            if (c == '+' || c == ' ') {
                result &= (PLUS.mask() | LEADING_SPACE.mask()) ^ CLEAR_FLAGS;
                result |= mask();
            } else if (c == '-' || c == '0') {
                result &= (LEFT_JUSTIFY.mask() | ZERO_PAD.mask()) ^ CLEAR_FLAGS;
                result |= mask();
            }
            return result;
        }

        static String string(int flags) {
            String lj = (flags & LEFT_JUSTIFY.mask()) > 0 ? LEFT_JUSTIFY.flag() : "";
            String p = (flags & PLUS.mask()) > 0 ? PLUS.flag() : "";
            String ls = (flags & LEADING_SPACE.mask()) > 0 ? LEADING_SPACE.flag() : "";
            String zp = (flags & ZERO_PAD.mask()) > 0 ? ZERO_PAD.flag() : "";
            return (lj + p + ls + zp);
        }
    }

    public static Format floatingPoint() {
        return new Format(true);
    }

    public static Format integer() {
        return new Format(false);
    }

    private boolean floatingPoint;
    private int width;
    private int precision;
    private int flags;

    private Format(boolean isFloatingPoint) {
        floatingPoint = isFloatingPoint;
        width = -1;
        precision = -1;
        flags = 0;
    }

    public boolean isFloatingPoint() {
        return floatingPoint;
    }

    public Format width(int w) {
        if (w > -1) {
            width = w;
        }
        return this;
    }

    public Format precision(int p) {
        if (p > -1 && floatingPoint) {
            precision = p;
        }
        return this;
    }

    public Format flags(Flag... myFlags) {
        for (Flag f : myFlags) {
            if (width == -1 && (f == Flag.ZERO_PAD || f == Flag.LEFT_JUSTIFY)) continue;
            flags = f.and(flags);
        }
        return this;
    }

    public String formatString() {
        StringBuilder str = new StringBuilder("%");
        str.append(Flag.string(flags));
        if (width != -1) {
            str.append(width);
        }
        if (precision != -1) {
            str.append(".").append(precision);
        }
        str.append(floatingPoint ? 'f' : 'd');
        return str.toString();
    }
}
