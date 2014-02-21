package org.xmpp.android.connection.resource;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Bind extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_BIND, "bind");

	public Bind() {
		super(TYPE.getNamespace(), TYPE.getElement());
	}

	public Bind(XmppStanza clone) {
		super(clone);
	}

	public static void register() {
		register(TYPE, Bind.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}

	public String getJid() {
		return getSubText(XmppNamespaces.NAMESPACE_XMPP_BIND, "jid");
	}
}