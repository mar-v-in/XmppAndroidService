package org.xmpp.android.connection.tls;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class StartTls extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_TLS, "starttls");

	public StartTls() {
		super(TYPE.getNamespace(), TYPE.getElement());
	}

	public StartTls(XmppStanza clone) {
		super(clone);
		TYPE.equals(clone.getStanzaType());
	}

	public static void register() {
		register(TYPE, StartTls.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
