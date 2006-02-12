/*
 * Half User Interface.
 * Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.mobizcorp.hui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

import de.mobizcorp.lib.Text;
import de.mobizcorp.lib.TextBuffer;
import de.mobizcorp.lib.TextParser;
import de.mobizcorp.qu8ax.TextLoader;

/**
 * HUI backend server.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class MiniServer extends Thread {

    private static final Text HTTP_CONTENT_LENGTH = Text
            .valueOf("Content-Length");

    private static final Text HTTP_GET = Text.constant(new byte[] { 'G', 'E',
            'T' });

    private static final Text HTTP_POST = Text.constant(new byte[] { 'P', 'O',
            'S', 'T' });

    private static final Text HTTP_REDIRECT1 = Text
            .valueOf("HTTP/1.0 302 Found\r\nLocation: ");

    private static final Text HTTP_REDIRECT2 = Text.constant(new byte[] { '\r',
            '\n', '\r', '\n' });

    private static final Text HTTP_RESPONSE_200, HTTP_RESPONSE_404;

    static {
        Iterator<Text> list = TextLoader.fromXML(MiniServer.class);
        HTTP_RESPONSE_200 = list.next();
        HTTP_RESPONSE_404 = list.next();
    }

    public static void main(String args[]) {
        try {
            final ActionHandler handler;
            if (args.length > 1) {
                handler = (ActionHandler) Class.forName(args[1]).newInstance();
            } else {
                handler = null;
            }
            final int port;
            if (args.length > 2) {
                port = Integer.parseInt(args[2]);
            } else {
                port = 4556;
            }
            new MiniServer(new HuiSource(args[0]), handler, new ServerSocket(
                    port)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Text urldecode(final Text value) {
        if (value.indexOf('%') == -1 && value.indexOf('+') == -1) {
            return value;
        }
        final int end = value.size();
        final TextBuffer buffer = new TextBuffer(end);
        for (int i = 0; i < end; i++) {
            byte b = value.getByte(i);
            if (b == '%') {
                try {
                    b = (byte) value.part(i + 1, 2).toInt(16);
                    i += 2;
                } catch (Exception e) {
                    // fall through on index or parse exception
                }
            } else if (b == '+') {
                b = ' ';
            }
            buffer.append(b);
        }
        return buffer.toText();
    }

    private boolean active = true;

    private final ServerSocket endpoint;

    private final HuiSource source;

    private final ActionHandler handler;

    public MiniServer(HuiSource source, ActionHandler handler,
            ServerSocket endpoint) {
        this.source = source;
        this.handler = handler;
        this.endpoint = endpoint;
        // setDaemon(true);
    }

    private void handle(Socket socket, BufferedInputStream in,
            MiniRequest request, TextBuffer buffer) throws IOException {
        Text url = request.getUrl();
        Text query = null;
        if (HTTP_GET.equals(request.getMethod())) {
            int mark = url.indexOf('?');
            if (mark != -1) {
                query = url.part(mark + 1, url.size() - mark - 1);
                url = url.part(0, mark);
            }
        } else if (HTTP_POST.equals(request.getMethod())) {
            try {
                int len = request.getHeader(HTTP_CONTENT_LENGTH).toInt(10);
                byte[] data = new byte[len];
                int n, off = 0;
                do {
                    n = in.read(data, off, len - off);
                    if (n > 0) {
                        off += n;
                    }
                } while (n != -1 && off < len);
                query = Text.constant(data); // mild cheat
            } catch (Exception e) {
                // ignore
            }
        } else {
            // unknown request method
            // TODO: complain to client
            return;
        }

        final OutputStream out = new BufferedOutputStream(socket
                .getOutputStream());
        try {
            int slash = url.lastIndexOf('/');
            if (url.lastIndexOf('.') != -1) {
                final String name;
                if (slash != -1) {
                    name = url.part(slash + 1, url.size() - slash - 1)
                            .toString();
                } else {
                    name = url.toString();
                }
                InputStream file = MiniServer.class.getResourceAsStream(name);
                if (file != null) {
                    try {
                        byte[] data = new byte[8192];
                        int n;
                        while ((n = file.read(data)) != -1) {
                            if (n > 0) {
                                out.write(data, 0, n);
                            }
                        }
                        return;
                    } finally {
                        file.close();
                    }
                } else {
                    HTTP_RESPONSE_404.writeTo(out);
                    return;
                }
            }
            HuiNode model = source.instance();
            if (slash != -1 && slash < url.size() - 1) {
                byte[] state = StateCodec.fromBase64(url.part(slash + 1,
                        url.size() - slash - 1).toBytes());
                model.readState(new ByteArrayInputStream(state));
            }
            if (query != null) {
                TextParser tp = new TextParser(query, Text.constant((byte) '&'));
                final Text equal = Text.constant((byte) '=');
                while (tp.hasNext()) {
                    Text post = tp.next();
                    int q = post.indexOf(equal);
                    if (q != -1) {
                        final Text id = urldecode(post.part(0, q));
                        Path<HuiNode> path = model.path(id);
                        if (path != null) {
                            final Text message = urldecode(post.part(q + 1,
                                    post.size() - q - 1));
                            path.getLast().post(message, handler, path);
                        }
                    }
                }
                HTTP_REDIRECT1.writeTo(out);
                model.writeState(out);
                HTTP_REDIRECT2.writeTo(out);
            } else {
                HTTP_RESPONSE_200.writeTo(out);
                model.renderPage(out);
            }
        } finally {
            out.close();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("accepting connections on " + endpoint);
            while (active) {
                Socket socket = endpoint.accept();
                try {
                    TextBuffer buffer = new TextBuffer();
                    BufferedInputStream in = new BufferedInputStream(socket
                            .getInputStream());
                    MiniRequest request = MiniRequest.from(in);
                    if (request != null) {
                        handle(socket, in, request, buffer);
                    }
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    socket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                endpoint.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
