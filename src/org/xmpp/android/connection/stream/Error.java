package org.xmpp.android.connection.stream;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.XmppStanza;

public class Error extends XmppStanza {

	/**
	 * XMPP Stream error conditions as defined in RFC 6120 Section 4.9.3.
	 */
	public enum ErrorCondition {
		bad_format,
		bad_namespace_prefix,
		conflict,
		connection_timeout,
		host_gone,
		host_unknown,
		improper_addressing,
		internal_server_error,
		invalid_form,
		invalid_namespace,
		invalid_xml,
		not_authorized,
		not_well_formed,
		policy_violation,
		remote_connection_failed,
		reset,
		resource_constraint,
		restricted_xml,
		see_other_host,
		system_shutdown,
		undefined_condition,
		unsupported_encoding,
		unsupported_feature,
		unsupported_stanza_type,
		unsupported_version,
	}

	private static final String TAG = "XMPP/Error";
	private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_JABBER_STREAMS, "error");

	public Error(XmppStanza clone) {
		super(clone);
	}

	public static void register() {
		register(TYPE, Error.class);
	}

	@Override
	public StanzaType getStanzaType() {
		return TYPE;
	}

	public ErrorCondition getErrorCondition() {
		if (getSubStanzas().isEmpty()) {
			return null;
		}
		return ErrorCondition.valueOf(getSubStanzas().get(0).getStanzaType().getElement().replace('-', '_'));
	}
}
