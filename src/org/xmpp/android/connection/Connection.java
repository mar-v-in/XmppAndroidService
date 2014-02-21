package org.xmpp.android.connection;

import org.xmpp.android.connection.stream.Stream;
import org.xmpp.android.shared.Jid;

public interface Connection {
	void close();

	void flush();

	Jid getJid();

	Stream getStream();

	void send(String string);
}
