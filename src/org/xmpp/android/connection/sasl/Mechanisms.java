package org.xmpp.android.connection.sasl;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.util.List;

public class Mechanisms extends XmppStanza {
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SASL,"mechanisms");

	public Mechanisms(XmppStanza clone) {
		super(clone);
	}

	public String[] getMechanisms() {
		List<Stanza> subElements = getSubStanzas();
		String[] ms = new String[subElements.size()];
		for (int i = 0; i < subElements.size(); i++) {
			ms[i] = ((Mechanism) subElements.get(i)).getMechanism();
		}
		return ms;
	}

	public static void register() {
		register(TYPE, Mechanisms.class);
		Mechanism.register();
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}
}
