package org.ijsberg.iglu.server.connection.socket.module.dummy;

import java.io.IOException;
import java.net.Socket;

import org.ijsberg.iglu.server.connection.ConnectionFactory;
import org.ijsberg.iglu.server.connection.socket.ByteStreamReadingConnection;

/**
 */
public class ConnectionFactoryDummy implements ConnectionFactory
{
	private int nrofTimesInvoked;

	public int getNrofTimesInvoked()
	{
		return nrofTimesInvoked;
	}

	public ByteStreamReadingConnection createConnection(Socket socket) throws IOException
	{
		nrofTimesInvoked++;
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
