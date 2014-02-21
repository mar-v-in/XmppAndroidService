package org.xmpp.android.shared;

import android.os.Parcel;
import android.os.Parcelable;

public final class Jid implements Parcelable {
	private final String user;
	private final String server;
	private final String password;
	private final String resource;

	public static JidBuilder builderOf(String jid) {
		JidBuilder builder = builder();
		if (jid != null && jid.contains("@")) {
			String notResource = jid;
			if (jid.contains("/")) {
				String[] split = jid.split("/");
				builder.resource(split[1]);
				notResource = split[0];
			}
			String[] split = notResource.split("@");
			builder.user(split[0]);
			builder.server(split[1]);
		}
		return builder;
	}

	public static Jid of(String jid) {
		return builderOf(jid).build();
	}

	@Override
	public int describeContents() {
		return 0; //TODO: Implement
	}

	@Override
	public String toString() {
		if (resource != null) {
			return user + "@" + server + "/" + resource;
		}
		return withoutResource();
	}

	public String withoutResource() {
		return user + "@" + server;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(user);
		dest.writeString(server);
		dest.writeString(resource);
	}

	
	Jid(final String user, final String server, final String password, final String resource) {
		this.user = user;
		this.server = server;
		this.password = password;
		this.resource = resource;
	}

	
	public static class JidBuilder {
		private String user;
		private String server;
		private String password;
		private String resource;

		
		JidBuilder() {
		}

		
		public JidBuilder user(final String user) {
			this.user = user;
			return this;
		}

		
		public JidBuilder server(final String server) {
			this.server = server;
			return this;
		}

		
		public JidBuilder password(final String password) {
			this.password = password;
			return this;
		}

		
		public JidBuilder resource(final String resource) {
			this.resource = resource;
			return this;
		}

		
		public Jid build() {
			return new Jid(user, server, password, resource);
		}

		@java.lang.Override
		public java.lang.String toString() {
			return "Jid.JidBuilder(user=" + this.user + ", server=" + this.server + ", password=" + this.password + ", resource=" + this.resource + ")";
		}
	}

	public static JidBuilder builder() {
		return new JidBuilder();
	}

	public String getUser() {
		return this.user;
	}

	public String getServer() {
		return this.server;
	}

	public String getPassword() {
		return this.password;
	}

	public String getResource() {
		return this.resource;
	}

	@java.lang.Override
	public boolean equals(final java.lang.Object o) {
		if (o == this) return true;
		if (!(o instanceof Jid)) return false;
		final Jid other = (Jid)o;
		final java.lang.Object this$user = this.getUser();
		final java.lang.Object other$user = other.getUser();
		if (this$user == null ? other$user != null : !this$user.equals(other$user)) return false;
		final java.lang.Object this$server = this.getServer();
		final java.lang.Object other$server = other.getServer();
		if (this$server == null ? other$server != null : !this$server.equals(other$server)) return false;
		final java.lang.Object this$password = this.getPassword();
		final java.lang.Object other$password = other.getPassword();
		if (this$password == null ? other$password != null : !this$password.equals(other$password)) return false;
		final java.lang.Object this$resource = this.getResource();
		final java.lang.Object other$resource = other.getResource();
		if (this$resource == null ? other$resource != null : !this$resource.equals(other$resource)) return false;
		return true;
	}

	@java.lang.Override
	public int hashCode() {
		final int PRIME = 277;
		int result = 1;
		final java.lang.Object $user = this.getUser();
		result = result * PRIME + ($user == null ? 0 : $user.hashCode());
		final java.lang.Object $server = this.getServer();
		result = result * PRIME + ($server == null ? 0 : $server.hashCode());
		final java.lang.Object $password = this.getPassword();
		result = result * PRIME + ($password == null ? 0 : $password.hashCode());
		final java.lang.Object $resource = this.getResource();
		result = result * PRIME + ($resource == null ? 0 : $resource.hashCode());
		return result;
	}
}
