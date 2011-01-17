package play.modules.oauthclient;

public interface ICredentials {

	public void setToken(String token);

	public String getToken();

	public void setSecret(String secret);

	public String getSecret();

}
