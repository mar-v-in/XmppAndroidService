package org.xmpp.android.connection;


import android.util.Base64;
import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.TextStanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import java.util.*;

/**
 * Implements SASL negotiation as described in RFC 6120 Section 6.
 */
public class SaslNegotiation implements ConnectionFeature {
	public static final String SASL_PLAIN = "PLAIN";
	private XmppConnection connection;

	public SaslNegotiation(XmppConnection connection) {
		this.connection = connection;
	}

	@Override
	public boolean isSupported() {
		return connection.getStream().getFeatures().getFeature(Mechanisms.class) != null;
	}

	public static void register() {
		Auth.register();
		Mechanisms.register();
		Success.register();
		Failure.register();
	}

	@Override
	public void start() {
		if (!isSupported()) {
			throw new IllegalStateException("SASL is not supported by Stream, can't continue!");
		}
		if (connection.getJid().getPassword() == null) {
			throw new IllegalStateException("No password specified or already removed after login, can't continue!");
		}
		try {
			Mechanisms mechanisms = connection.getStream().getFeatures().getFeature(Mechanisms.class);
			if (Arrays.asList(mechanisms.getMechanisms()).contains(SASL_PLAIN)) {
				new Auth(SASL_PLAIN, getPlainResponse()).pushTag(connection);
			} else {
				// TODO Support other mechanisms than PLAIN
				throw new RuntimeException("SASL PLAIN not supported by remote end, can't continue!");
			}
			Stanza success = connection.readFullStanza();
			if (success instanceof Success) {
				connection.resetSocket(connection.getSocket());
			} else {
				throw new RuntimeException("Response is not success, but " + success);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not SASL", e);
		}
	}

	private String getPlainResponse() {
		return Base64.encodeToString(("\0" + connection.getJid().getUser() + "\0" + connection.getJid().getPassword()).getBytes(), 0);
	}

	/**
	 * SASL initiation stanza as described in RFC 6120 Section 6.4.2.
	 */
	public static class Auth extends XmppStanza {
		private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SASL, "auth");

		public static void register() {
			register(TYPE, Auth.class);
		}

		public Auth(String mechanism, String response) {
			super(TYPE, buildAttributes(mechanism), buildSubElements(response));
		}

		private static List<Stanza> buildSubElements(String response) {
			List<Stanza> elements = new ArrayList<Stanza>();
			elements.add(new TextStanza(response));
			return elements;
		}

		private static Map<String, String> buildAttributes(String mechanism) {
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("mechanism", mechanism);
			return attributes;
		}

		@Override
		public StanzaType getStanzaType() {
			return TYPE;
		}
	}

	/**
	 * SASL mechanisms feature as described in RFC 6120 Section 6.4.1.
	 */
	public static class Mechanisms extends XmppStanza {

		private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SASL, "mechanisms");

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

		public static class Mechanism extends XmppStanza {
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
	}

	/**
	 * SASL success stanza as described in RFC 6120 Section 6.4.6.
	 */
	public static class Success extends XmppStanza {
		private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SASL, "success");

		public Success(XmppStanza clone) {
			super(clone);
		}

		public static void register() {
			register(TYPE, Success.class);
		}

		@Override
		public StanzaType getStanzaType() {
			return TYPE;
		}
	}

	/**
	 * SASL failure stanza as described in RFC 6120 Section 6.4.5.
	 */
	public static class Failure extends XmppStanza {
		/**
		 * SASL failure conditions as described in RFC 6120 Section 6.5
		 */
		public enum FailureCondition {
			aborted,
			account_disabled,
			credentials_expired,
			encryption_required,
			incorrect_encoding,
			invalid_authzid,
			invalid_mechanism,
			malformed_request,
			mechanism_too_weak,
			not_authorized,
			temporary_auth_failure,
		}

		private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_SASL, "failure");

		public Failure(XmppStanza clone) {
			super(clone);
		}

		public static void register() {
			register(TYPE, Failure.class);
		}

		@Override
		public StanzaType getStanzaType() {
			return TYPE;
		}
	}
}
