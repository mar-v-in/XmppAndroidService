package org.xmpp.android.connection.session;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Session extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SESSION, "session");

	public Session() {
		super(TYPE.getNamespace(), TYPE.getElement());
	}

	public Session(XmppStanza clone) {
		super(clone);
	}

	public static void register() {
		register(TYPE, Session.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}