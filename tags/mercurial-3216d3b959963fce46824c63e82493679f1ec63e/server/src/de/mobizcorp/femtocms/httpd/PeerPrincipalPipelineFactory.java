package de.mobizcorp.femtocms.httpd;

import java.io.IOException;
import java.net.Socket;

import simple.http.Pipeline;
import simple.http.PipelineFactory;

/**
 * @author Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.
 */
public class PeerPrincipalPipelineFactory implements PipelineFactory {

	public Pipeline getInstance(Socket sock) throws IOException {
		return new PeerPrincipalPipeline(sock);
	}

}
