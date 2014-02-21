package org.xmpp.android.connection.sasl;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Success extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SASL,"success");
	public Success(XmppStanza clone) {
		super(clone);
	}

	public static void register() {
		register(TYPE, Success.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
