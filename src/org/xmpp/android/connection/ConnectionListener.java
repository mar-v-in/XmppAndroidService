package org.xmpp.android.connection;

import org.xmpp.android.shared.stanzas.Stanza;

public interface ConnectionListener {
	void stanzaRead(Stanza stanza);
}
