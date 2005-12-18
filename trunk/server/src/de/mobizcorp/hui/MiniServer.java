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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import de.mobizcorp.qu8ax.Text;
import de.mobizcorp.qu8ax.TextBuffer;
import de.mobizcorp.qu8ax.TextParser;

/**
 * HUI backend server.
 * 
 * @author Copyright(C) 2005 Klaus Rennecke, all rights reserved.
 */
public class MiniServer extends Thread {

    private static final Text HTTP_CONTENT_LENGTH = Text.constant((byte) 'C',
            (byte) 'o', (byte) 'n', (byte) 't', (byte) 'e', (byte) 'n',
            (byte) 't', (byte) '-', (byte) 'L', (byte) 'e', (byte) 'n',
            (byte) 'g', (byte) 't', (byte) 'h');

    private static final Text HTTP_GET = Text.constant((byte) 'G', (byte) 'E',
            (byte) 'T');

    private static final Text HTTP_POST = Text.constant((byte) 'P', (byte) 'O',
            (byte) 'S', (byte) 'T');

    private static final Text HTTP_REDIRECT1 = Text.constant((byte) 'H',
            (byte) 'T', (byte) 'T', (byte) 'P', (byte) '/', (byte) '1',
            (byte) '.', (byte) '0', (byte) ' ', (byte) '3', (byte) '0',
            (byte) '2', (byte) ' ', (byte) 'F', (byte) 'o', (byte) 'u',
            (byte) 'n', (byte) 'd', (byte) '\r', (byte) '\n', (byte) 'L',
            (byte) 'o', (byte) 'c', (byte) 'a', (byte) 't', (byte) 'i',
            (byte) 'o', (byte) 'n', (byte) ':', (byte) ' ');

    private static final Text HTTP_REDIRECT2 = Text.constant((byte) '\r',
            (byte) '\n', (byte) '\r', (byte) '\n');

    private static final Text HTTP_RESPONSE = Text.constant((byte) 'H',
            (byte) 'T', (byte) 'T', (byte) 'P', (byte) '/', (byte) '1',
            (byte) '.', (byte) '0', (byte) ' ', (byte) '2', (byte) '0',
            (byte) '0', (byte) ' ', (byte) 'O', (byte) 'K', (byte) '\r',
            (byte) '\n', (byte) 'C', (byte) 'o', (byte) 'n', (byte) 't',
            (byte) 'e', (byte) 'n', (byte) 't', (byte) '-', (byte) 'T',
            (byte) 'y', (byte) 'p', (byte) 'e', (byte) ':', (byte) ' ',
            (byte) 't', (byte) 'e', (byte) 'x', (byte) 't', (byte) '/',
            (byte) 'h', (byte) 't', (byte) 'm', (byte) 'l', (byte) ';',
            (byte) 'c', (byte) 'h', (byte) 'a', (byte) 'r', (byte) 's',
            (byte) 'e', (byte) 't', (byte) '=', (byte) 'U', (byte) 'T',
            (byte) 'F', (byte) '-', (byte) '8', (byte) '\r', (byte) '\n',
            (byte) '\r', (byte) '\n');

    public static void main(String args[]) {
        try {
            final FileInputStream in = new FileInputStream(args[0]);
            try {
                final ActionHandler handler;
                if (args.length > 1) {
                    handler = (ActionHandler) Class.forName(args[1])
                            .newInstance();
                } else {
                    handler = null;
                }
                final int port;
                if (args.length > 2) {
                    port = Integer.parseInt(args[2]);
                } else {
                    port = 4556;
                }
                new MiniServer(HuiBuilder.build(in), handler, new ServerSocket(
                        port)).start();
            } finally {
                in.close();
            }
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

    private final HuiNode model;

    private final ActionHandler handler;

    public MiniServer(HuiNode model, ActionHandler handler, ServerSocket endpoint) {
        this.model = model;
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
            if (slash != -1 && slash < url.size() - 1) {
                byte[] state = StateCodec.fromBase64(url.part(slash + 1,
                        url.size() - slash - 1).toBytes());
                model.loadState(new ByteArrayInputStream(state));
            }
            if (query != null) {
                TextParser tp = new TextParser(query, Text.constant((byte) '&'));
                final Text equal = Text.constant((byte) '=');
                while (tp.hasNext()) {
                    Text post = tp.next();
                    int q = post.indexOf(equal);
                    if (q != -1) {
                        HuiNode node = model.find(urldecode(post.part(0, q)));
                        if (node != null) {
                            node.post(urldecode(post.part(q + 1, post.size()
                                    - q - 1)), handler, model);
                        }
                    }
                }
                HTTP_REDIRECT1.writeTo(out);
                model.getState().writeTo(out);
                HTTP_REDIRECT2.writeTo(out);
            } else {
                HTTP_RESPONSE.writeTo(out);
                model.renderTree(out);
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
                } catch (SocketException e) {
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
