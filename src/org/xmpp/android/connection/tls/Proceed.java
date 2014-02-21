package org.xmpp.android.connection.tls;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Proceed extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_TLS, "proceed");

	public Proceed() {
		super(TYPE.getNamespace(), TYPE.getElement());
	}

	public Proceed(XmppStanza clone) {
		super(clone);
		TYPE.equals(clone.getStanzaType());
	}

	public static void register() {
		register(TYPE, Proceed.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
