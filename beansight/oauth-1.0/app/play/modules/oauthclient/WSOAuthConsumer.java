package play.modules.oauthclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpRequest;
import play.libs.WS.WSRequest;

public class WSOAuthConsumer extends AbstractOAuthConsumer {

	public WSOAuthConsumer(String consumerKey, String consumerSecret) {
		super(consumerKey, consumerSecret);
	}

	@Override
	protected HttpRequest wrap(Object request) {
		if (!(request instanceof WSRequest)) {
			throw new IllegalArgumentException("WSOAuthConsumer expects requests of type play.libs.WS.WSRequest");
		}
		return new WSRequestAdapter((WSRequest)request);
	}

	public WSRequest sign(WSRequest request, String method) throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException {
		WSRequestAdapter req = (WSRequestAdapter)wrap(request);
		req.setMethod(method);
		sign(req);
		return request;
	}

	public class WSRequestAdapter implements HttpRequest {

		private WSRequest request;

		public WSRequestAdapter(WSRequest request) {
			this.request = request;
		}

		public Map<String, String> getAllHeaders() {
			return request.headers;
		}

		public String getContentType() {
			return request.mimeType;
		}

		public String getHeader(String name) {
			return request.headers.get(name);
		}

		public InputStream getMessagePayload() throws IOException {
			return null;
		}

		private String method = "GET";

		public String getMethod() {
			return this.method;
		}

		private void setMethod(String method) {
			this.method = method;
		}

		public String getRequestUrl() {
			return request.url;
		}

		public void setHeader(String name, String value) {
			request.setHeader(name, value);
		}

		public void setRequestUrl(String url) {
			request.url = url;
		}

	}

}
