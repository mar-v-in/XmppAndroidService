package org.xmpp.android.shared.stanzas;

import org.xmpp.android.shared.Jid;

/**
 * Is a basic common stanza as described in RFC 6120 Section 8.1
 */
public abstract class CommonStanza extends BaseStanza {
	public CommonStanza(XmppStanza stanza) {
		super(stanza);
	}

	public String getType() {
		return stanza.getAttribute("type");
	}

	public Jid getTo() {
		return Jid.of(stanza.getAttribute("to"));
	}

	public Jid getFrom() {
		return Jid.of(stanza.getAttribute("from"));
	}

	public String getId() {
		return stanza.getAttribute("id");
	}
}
