package org.xmpp.android.shared.stanzas;

import java.util.HashMap;
import java.util.Map;

public class TextStanza extends BaseStanza {
	public static final StanzaType TYPE = new StanzaType(null, "!text");

	public TextStanza(String text) {
		super(new TextXmppStanza(text));
	}

	public TextStanza(XmppStanza stanza) {
		super(new TextXmppStanza(stanza));
	}

	private static Map<String, String> buildAttributes(String text) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("text", text);
		return attributes;
	}

	public String getText() {
		return stanza.getAttribute("text");
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}

	private static class TextXmppStanza extends XmppStanza {

		protected TextXmppStanza(XmppStanza clone) {
			super(clone);
		}

		protected TextXmppStanza(String text) {
			super(TYPE, buildAttributes(text));
		}

		@Override
		public String buildNoChildTag() {
			return getAttribute("text");
		}
	}
}
