package org.xmpp.android.connection.stream;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Error extends XmppStanza {

	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_STREAMS, "error");

	public Error(XmppStanza clone) {
		super(clone);
	}

	public static void register() {
		register(TYPE, Error.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
