package org.xmpp.android.connection.stream;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Features extends XmppStanza {

	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_STREAMS, "features");

	public Features(XmppStanza clone) {
		super(clone);
	}

	public static void register() {
		register(TYPE, Features.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
