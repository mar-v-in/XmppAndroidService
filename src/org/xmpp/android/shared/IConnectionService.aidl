package org.xmpp.android.shared;

import org.xmpp.android.shared.stanzas.XmppStanza;

interface IConnectionService {
	void send(in XmppStanza xmppStanza);
}
