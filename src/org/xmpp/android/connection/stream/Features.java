package org.xmpp.android.connection.stream;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.Stanza;
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

	public <T> T getFeature(Class<T> cls) {
		for (Stanza stanza : getSubStanzas()) {
			if (stanza.getClass().equals(cls)) {
				return (T) stanza;
			}
		}
		return null;
	}
}
