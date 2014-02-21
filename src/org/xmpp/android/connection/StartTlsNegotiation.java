package org.xmpp.android.connection;

import org.xmpp.android.shared.XmppNamespaces;
import org.xmpp.android.shared.stanzas.Stanza;
import org.xmpp.android.shared.stanzas.XmppStanza;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Implements STARTTLS negotiation as described in RFC 6120 Section 5.
 */
public class StartTlsNegotiation implements ConnectionFeature {
	private XmppConnection connection;

	public StartTlsNegotiation(XmppConnection connection) {
		this.connection = connection;
	}

	@Override
	public boolean isSupported() {
		return connection.getStream().getFeatures().getFeature(StartTls.class) != null;
	}

	public static void register() {
		StartTls.register();
		Proceed.register();
		Failure.register();
	}

	@Override
	public void start() {
		if (!isSupported()) {
			throw new IllegalStateException("STARTTLS is not supported by Stream, cannot continue!");
		}
		try {
			new StartTls().pushTag(connection);
			Stanza stanza = connection.readFullStanza();
			if (stanza instanceof Proceed) {
				SSLContext tls = setupTlsContext();
				connection.resetSocket(tls.getSocketFactory()
						.createSocket(connection.getSocket(), connection.getSocket().getInetAddress().getHostName(), connection.getSocket().getPort(), true));
			} else {
				throw new RuntimeException("Response is not proceed, but " + stanza.getStanzaType().getElement());
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not STARTTLS", e);
		}
	}

	private SSLContext setupTlsContext()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(null, null);
		KeyManager[] kms = kmf.getKeyManagers();

		SSLContext tls = SSLContext.getInstance("TLS");
		// TODO FIXME !!! This is not a SECURE socket !!!
		tls.init(kms, new javax.net.ssl.TrustManager[]{new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				//TODO: Implement
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				//TODO: Implement
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0]; //TODO: Implement
			}
		}}, new SecureRandom());
		return tls;
	}

	/**
	 * STARTTLS proceed stanza as described in RFC 6120 Section 5.4.2.3.
	 */
	public static class Proceed extends XmppStanza {
		private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_TLS, "proceed");

		public Proceed(XmppStanza clone) {
			super(clone);
			TYPE.equals(clone.getStanzaType());
		}

		public static void register() {
			register(TYPE, Proceed.class);
		}

		@Override
		public StanzaType getStanzaType() {
			return TYPE;
		}
	}

	/**
	 * STARTTLS failure stanza as described in RFC 6120 Section 5.4.2.2.
	 */
	public static class Failure extends XmppStanza {
		private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_TLS, "failure");

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

	/**
	 * STARTTLS feature as described in RFC 6120 Section 5.4.1. and STARTTLS command stanza as described in RFC 6120 Section 6.4.2.1.
	 */
	public static class StartTls extends XmppStanza {
		private static final StanzaType TYPE = new StanzaType(XmppNamespaces.NAMESPACE_XMPP_TLS, "starttls");

		public StartTls() {
			super(TYPE.getNamespace(), TYPE.getElement());
		}

		public StartTls(XmppStanza clone) {
			super(clone);
			TYPE.equals(clone.getStanzaType());
		}

		public static void register() {
			register(TYPE, StartTls.class);
		}

		@Override
		public StanzaType getStanzaType() {
			return TYPE;
		}
	}
}
