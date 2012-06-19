package com.speeddialfolder.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.ByteArrayBuffer;

public class HttpUtils {
	private static final DefaultHttpClient httpClient = createMultiThreadedHttpClient(10, 3000);

	public static byte[] get(String url) throws IOException {
		HttpContext localContext = new BasicHttpContext();
		HttpUriRequest request = new HttpGet(url);

		HttpResponse response = httpClient.execute(request, localContext);
		if (response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
			return getResponseContent(response);
		} else {
			throw HttpError.create(response.getStatusLine().getStatusCode());
		}
	}

	public static byte[] post(String url)
			throws IOException {
		HttpContext localContext = new BasicHttpContext();
		HttpPost request = new HttpPost(url);
		HttpResponse response = httpClient.execute(request, localContext);
		if (response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null)
			return getResponseContent(response);
		return null;
	}

	private static DefaultHttpClient createMultiThreadedHttpClient(int maxConnections, int timeout) {
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, maxConnections);
		ConnManagerParams.setTimeout(params, timeout);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
		return httpClient;
	}

	private static byte[] getResponseContent(HttpResponse response) throws IOException {
		InputStream instream = response.getEntity().getContent();

		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		if (contentEncoding != null && contentEncoding.getValue().equals("gzip")) {
			instream = new GZIPInputStream(instream);
		}

		int i = (int) response.getEntity().getContentLength();
		if (i < 0) {
			i = 4096;
		}

		ByteArrayBuffer buffer = new ByteArrayBuffer(i);
		try {
			byte[] tmp = new byte[4096];
			int l;
			while ((l = instream.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		} finally {
			instream.close();
		}
		return buffer.toByteArray();
	}

}
