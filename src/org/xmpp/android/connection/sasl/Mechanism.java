package org.xmpp.android.connection.sasl;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.TextStanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Mechanism extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SASL, "mechanism");

	public Mechanism(XmppStanza clone) {
		super(clone);
	}

	public String getMechanism() {
		return ((TextStanza) getSubStanzas().get(0)).getText();
	}

	public static void register() {
		register(TYPE, Mechanism.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
