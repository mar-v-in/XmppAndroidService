package org.xmpp.android.util;

import org.xmpp.android.connection.Connection;
import org.xmpp.android.connection.stream.Stream;
import org.xmpp.android.shared.Jid;

public class SysoutConnection implements Connection {
	@Override
	public Jid getJid() {
		return null; //TODO: Implement
	}

	@Override
	public Stream getStream() {
		return null; //TODO: Implement
	}

	@Override
	public void close() {
		//TODO: Implement
	}

	@Override
	public void flush() {
		System.out.println();
	}

	@Override
	public void send(String string) {
		System.out.print(string);
	}
}
