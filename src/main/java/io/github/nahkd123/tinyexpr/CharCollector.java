package io.github.nahkd123.tinyexpr;

class CharCollector {
    private char[] buf;
    private int len = 0;

    public CharCollector(int initialSize) {
        buf = new char[initialSize];
    }

    public void clear() {
        len = 0;
    }

    public void push(char ch) {
        if (len >= buf.length) {
            char[] b2 = new char[buf.length * 2];
            System.arraycopy(buf, 0, b2, 0, len);
            buf = b2;
        }

        buf[len++] = ch;
    }

    public int length() {
        return len;
    }

    @Override
    public String toString() {
        return String.copyValueOf(buf, 0, len);
    }
}
