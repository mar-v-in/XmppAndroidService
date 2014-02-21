package org.xmpp.android.shared.stanzas;

import org.xmpp.android.shared.XmppNamespaces;

import java.util.HashMap;
import java.util.Map;

public class MessageStanza extends BaseStanza {
	public static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_CLIENT, "message");

	public MessageStanza(XmppStanza stanza) {
		super(stanza);
		assert TYPE.equals(stanza.getStanzaType());
	}

	public MessageStanza(String from, String to) {
		super(new XmppStanza(TYPE, buildAttributes(from, to)));
	}

	private static Map<String, String> buildAttributes(String from, String to) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("from", from);
		attributes.put("to", to);
		return attributes;
	}

	public String getFrom() {
		return stanza.getAttribute("from");
	}

	public String getTo() {
		return stanza.getAttribute("to");
	}
}
