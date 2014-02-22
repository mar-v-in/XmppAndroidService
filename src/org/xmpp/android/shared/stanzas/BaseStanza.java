package org.xmpp.android.shared.stanzas;

public class BaseStanza implements Stanza {
	protected final XmppStanza stanza;

	public BaseStanza(XmppStanza stanza) {
		this.stanza = stanza;
	}

	@Override
	public XmppStanza asXmppStanza() {
		return stanza;
	}

	@Override
	public StanzaType getStanzaType() {
		return stanza.getStanzaType();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"stanza=" + stanza +
				'}';
	}
}
