package org.xmpp.android.shared.stanzas;

import org.xmpp.android.shared.XmppNamespaces;

public class ErrorStanza extends BaseStanza {
	public static final XmppStanza.StanzaType TYPE = new XmppStanza.StanzaType(XmppNamespaces.NAMESPACE_JABBER_CLIENT, "error");

	public ErrorStanza(XmppStanza stanza) {
		super(stanza);
	}

	public ErrorCondition getErrorCondition() {
		if (stanza.getSubStanzas().size() == 1) {
			return ErrorCondition.valueOf(stanza.getSubStanzas().get(0).getStanzaType().getElement());
		}
		return null;
	}

	/**
	 * Error condition as described in RFC 6120 Section 8.3.3.
	 */
	public enum ErrorCondition {
		bad_request,
		conflict,
		feature_not_implemented,
		forbidden,
		gone,
		internal_server_error,
		item_not_found,
		jid_malformed,
		not_acceptable,
		not_allowed,
		not_authorized,
		policy_violation,
		recipient_unavailable,
		redirect,
		registration_required,
		remote_server_not_found,
		remote_server_timeout,
		resource_constraint,
		service_unavailable,
		subscription_required,
		undefined_condition,
		unexpected_request
	}
}
