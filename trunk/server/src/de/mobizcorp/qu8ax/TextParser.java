package de.mobizcorp.qu8ax;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TextParser implements Iterator<Text> {

    private int index = -1;

    private final Text value;

    private final Text separator;

    public TextParser(Text value, Text separator) {
        this.value = value;
        this.separator = separator;
    }

    public boolean hasNext() {
        return index < value.size();
    }

    public Text next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int start = index + 1;
        outer: while (++index < value.size()) {
            byte b = value.getByte(index);
            if (b < 0xC0) {
                int scan = separator.size();
                match: while (--scan >= 0) {
                    if (b == separator.getByte(scan)) {
                        if (b > 0x7F) {
                            int i = 0;
                            do {
                                b = value.getByte(index + --i);
                                if (b != separator.getByte(--scan)) {
                                    continue match;
                                }
                            } while ((b & 0xC0) == 0x80);
                            return value.part(start, index + i - start);
                        }
                        break outer;
                    }
                }
            }
        }
        return value.part(start, index - start);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
