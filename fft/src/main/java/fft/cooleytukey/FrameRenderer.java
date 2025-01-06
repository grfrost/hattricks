package fft.cooleytukey;


import java.util.function.Consumer;


public class FrameRenderer {
    StringBuilder stringBuilder = new StringBuilder();

    static final char chessKingUnicode = 0x2654;

    public FrameRenderer rgb(int r, int g, int b) {
        return str("2;" + r + ";" + g + ";" + b);
    }

    public FrameRenderer esc() {
        return str("\u001b[");
    }

    public FrameRenderer consume(Consumer<FrameRenderer> consumer) {
        consumer.accept(this);
        return this;
    }

    public FrameRenderer escXrgb(String x, int r, int g, int b, Consumer<FrameRenderer> consumer) {
        return esc().str(x).rgb(r, g, b).ch('m').consume(consumer).esc().str(x).ch('0').ch('m');
    }

    public FrameRenderer bg(int r, int g, int b, Consumer<FrameRenderer> consumer) {
        return escXrgb("48;", r, g, b, consumer);
    }

    public FrameRenderer bgnf(float r, float g, float b, Consumer<FrameRenderer> consumer) {
        return escXrgb("48;", (int)(256*r), (int)(256*g), (int)(256*b), consumer);
    }

    public FrameRenderer fg(int r, int g, int b, Consumer<FrameRenderer> consumer) {
        return escXrgb("38;", r, g, b, consumer);
    }
    public FrameRenderer fgnf(float r, float g, float b, Consumer<FrameRenderer> consumer) {
        return escXrgb("38;", (int)(256*r), (int)(256*g), (int)(256*b), consumer);
    }

    public FrameRenderer greenForeground(Consumer<FrameRenderer> consumer) {
        return fg(0, 250, 0, consumer);
    }

    public FrameRenderer greenOnGrey(Consumer<FrameRenderer> consumer) {
        return greenForeground(fg -> fg.bg(148, 148, 148, consumer));
    }

    public FrameRenderer ch(char ch) {
        stringBuilder.append(ch);
        return this;
    }

    public FrameRenderer ch(int ch) {
        return ch((char) ch);
    }

    public FrameRenderer chln(char ch) {
        return ch(ch).nl();
    }

    public FrameRenderer str(String s) {
        stringBuilder.append(s);
        return this;
    }
    public FrameRenderer strf(String fmt, Object... args) {
        stringBuilder.append(String.format(fmt, args));
        return this;
    }

    int len(String escapedString) {
        int length = 0;
        boolean escaped = false;
        for (var c : escapedString.toCharArray()) {
            if (c == '\u001b') {
                escaped = true;
            } else if (escaped) {
                if (c == 'm') {
                    escaped = false;
                }
            } else {
                length++;
            }
        }
        return length;
    }

    public FrameRenderer pad(String s, int len) {
        stringBuilder.append(s);
        int length = len(s);
        for (int i = length; i < len; i++) {
            space();
        }
        return this;
    }

    public FrameRenderer nl() {
        return ch('\n');
    }




    public FrameRenderer space() {
        return ch(' ');
    }

    public FrameRenderer space(int n) {
        while (n-- > 0) {
            space();
        }
        return this;
    }

    public FrameRenderer either(boolean test, Consumer<FrameRenderer> yes, Consumer<FrameRenderer> no) {
        if (test) {
            yes.accept(this);
        } else {
            no.accept(this);
        }
        return this;
    }






    public FrameRenderer intf(String format, int value) {
        return str(String.format(format, value));
    }

    public FrameRenderer bar() {
        return ch('|');
    }

    public FrameRenderer strln(String s) {
        return str(s).nl();
    }
}
