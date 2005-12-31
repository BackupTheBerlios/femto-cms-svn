package de.mobizcorp.qu8ax;

import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class Writer extends FilterOutputStream implements Handler {

    private static final Text XML_DECL, XML_CDATA1, XML_CDATA2, XML_XMLNS_,
            XML_COMMENT1, XML_COMMENT2;

    static {
        Iterator<Text> list = TextLoader.fromXML(Writer.class);
        XML_DECL = list.next();
        XML_CDATA1 = list.next();
        XML_CDATA2 = Text.constant(new byte[] { ']', ']', '>' });
        XML_XMLNS_ = list.next();
        XML_COMMENT1 = list.next();
        XML_COMMENT2 = list.next();
    }

    private NamePool<Text> lNames;

    private NamePool<NamePair> qNames;

    private final Stack<Text> stack = new Stack<Text>();

    private boolean started;

    private final HashMap<Text, Text> uriMap = new HashMap<Text, Text>();

    public Writer(OutputStream out) {
        super(out);
    }

    private void flushNamespaces() {
        int scan = stack.size();
        while (--scan >= 0) {
            if (stack.get(scan) == null) {
                break;
            }
        }
        stack.setSize(scan + 1);
    }

    public void handleAddAttribute(final int name, final Text value)
            throws IOException {
        write(' ');
        writeName(name);
        write('=');
        write('"');
        writeText(value);
        write('"');
    }

    public void handleCharacterData(final boolean parsed, final Text value)
            throws IOException {
        writeClose();
        if (parsed) {
            writeText(value);
        } else {
            XML_CDATA1.writeTo(this);
            value.writeTo(this);
            XML_CDATA2.writeTo(this);
        }
    }

    public void handleCloseDocument() throws IOException {
        writeClose();
    }

    public void handleCloseElement(final int name) throws IOException {
        writeClose();
        write('<');
        write('/');
        writeName(name);
        write('>');
        stack.pop(); // pop null marker
        flushNamespaces();
    }

    public void handleInstruction(final Text target, final Text value)
            throws IOException {
        writeClose();
        write('<');
        write('?');
        target.writeTo(this);
        if (value != null && value.size() > 0) {
            write(' ');
            value.writeTo(this);
        }
        write('?');
        write('>');
    }

    public void handleOpenDocument(NamePool<Text> lNames,
            NamePool<NamePair> qNames) throws IOException {
        this.lNames = lNames;
        this.qNames = qNames;
        XML_DECL.writeTo(this);
    }

    public void handleOpenElement(final int name) throws IOException {
        writeNamespaces();
        write('>');
        started = false;
        stack.push(null);
    }

    public void handleStartElement(final int name) throws IOException {
        writeClose();
        write('<');
        writeName(name);
        started = true;
    }

    public void handleWhitespace(final boolean comment, final Text value)
            throws IOException {
        writeClose();
        if (comment) {
            XML_COMMENT1.writeTo(this);
            value.writeTo(this);
            XML_COMMENT2.writeTo(this);
        } else {
            value.writeTo(this);
        }
    }

    private Text prefixFor(final int uriCode) {
        final Text uri = lNames.extern(uriCode);
        if (uri == null || uri.size() == 0) {
            return Text.EMPTY;
        }
        Text prefix = uriMap.get(uri);
        if (prefix == null) {
            prefix = new TextBuffer().append((byte) 'p').append(
                    Text.valueOf(uriMap.size(), Text.MAX_RADIX)).toText();
            uriMap.put(uri, prefix);
        } else {
            // check if the uri is still in scope
            int scan = stack.size();
            while (--scan >= 0) {
                final Text old = stack.get(scan);
                if (old != null && old.equals(uri)) {
                    // already in scope
                    return prefix;
                }
            }
        }
        stack.push(uri);
        return prefix;
    }

    private void writeClose() throws IOException {
        if (started) {
            writeNamespaces();
            write('/');
            write('>');
            flushNamespaces();
            started = false;
        }
    }

    private void writeName(final int name) throws IOException {
        NamePair pair = qNames.extern(name);
        final Text prefix = prefixFor(pair.getA());
        if (prefix.size() > 0) {
            prefix.writeTo(this);
            write(':');
        }
        lNames.extern(pair.getB()).writeTo(this);
    }

    private void writeNamespaces() throws IOException {
        int scan = stack.size();
        while (--scan >= 0) {
            final Text uri = stack.get(scan);
            if (uri == null) {
                break;
            } else {
                XML_XMLNS_.writeTo(this);
                uriMap.get(uri).writeTo(this);
                write('=');
                write('"');
                uri.writeTo(this);
                write('"');
            }
        }
    }

    public void writeText(final Text text) throws IOException {
        if (text == null) {
            return;
        }
        final int end = text.size();
        for (int i = 0; i < end; i++) {
            final byte b = text.getByte(i);
            if (b == 34) {
                Resolver.ENT_QUOT.writeTo(this);
            } else if (b == 38) {
                Resolver.ENT_AMP.writeTo(this);
            } else if (b == 60) {
                Resolver.ENT_LT.writeTo(this);
            } else if (b == 62) {
                Resolver.ENT_GT.writeTo(this);
            } else {
                write(b);
            }
        }
    }

    public static void main(String args[]) {
        try {
            final int end = args == null ? 0 : args.length;
            for (int i = 0; i < end; i++) {
                final Writer writer = new Writer(System.out);
                Parser parser = new Parser(Resolver.INSTANCE, writer);
                FileInputStream in = new FileInputStream(args[i]);
                try {
                    parser.parse(in);
                    writer.flush();
                } finally {
                    in.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
