package org.xmpp.android.shared.stanzas;

import org.xmpp.android.shared.Jid;
import org.xmpp.android.shared.XmppNamespaces;

import java.util.HashMap;
import java.util.Map;

public class PresenceStanza extends BaseStanza {
	public static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_CLIENT, "presence");

	public PresenceStanza(XmppStanza stanza) {
		super(stanza);
	}

	public PresenceStanza() {
		super(new XmppStanza(TYPE));
	}

	public PresenceStanza(String from, String to) {
		super(new XmppStanza(TYPE, buildAttributes(from, to)));
	}

	private static Map<String, String> buildAttributes(String from, String to) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("from", from);
		attributes.put("to", to);
		return attributes;
	}

	public Jid getFrom() {
		return Jid.of(stanza.getAttribute("from"));
	}

	public Type getPresenceType() {
		return Type.of(stanza.getAttribute("type"));
	}

	public int getPriority() {
		try {
			return Integer.parseInt(stanza.getSubText(XmppNamespaces.NAMESPACE_JABBER_CLIENT, "priority"));
		} catch (NumberFormatException ignored) {
			return -1;
		}
	}

	public Show getShow() {
		return Show.of(stanza.getSubText(XmppNamespaces.NAMESPACE_JABBER_CLIENT, "show"));
	}

	public String getStatus() {
		return stanza.getSubText(XmppNamespaces.NAMESPACE_JABBER_CLIENT, "status");
	}

	public Jid getTo() {
		return Jid.of(stanza.getAttribute("to"));
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Presence {");
		if (getFrom() != null) {
			sb.append(" from=").append(getFrom());
		}
		if (getTo() != null) {
			sb.append(" to=").append(getTo());
		}
		if (getShow() != null) {
			sb.append(" show=").append(getShow());
		}
		if (getPresenceType() != null) {
			sb.append(" type=").append(getPresenceType());
		}
		if (getPriority() != -1) {
			sb.append(" priority=").append(getPriority());
		}
		if (getStatus() != null) {
			sb.append(" status=\"").append(getStatus()).append("\"");
		}
		return sb.append(" }").toString();
	}

	public enum Show {
		away, chat, dnd, xa;

		public static Show of(String s) {
			if (s == null) {
				return null;
			}
			return valueOf(s);
		}
	}

	public enum Type {
		error, probe, subscribe, subscribed, unavailable, unsubscribe, unsubscribed;

		public static Type of(String s) {
			if (s == null) {
				return null;
			}
			return valueOf(s);
		}
	}
}
