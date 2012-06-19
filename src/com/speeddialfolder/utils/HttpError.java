package com.speeddialfolder.utils;


@SuppressWarnings("serial")
public class HttpError extends RuntimeException {
	protected int code;

	public int getErrorCode() {
		return code;
	}

	static HttpError create(int code) {
		switch (code) {
		case 304:
			return new E304();
		case 400:
			return new E400();
		case 401:
			return new E401();
		case 402:
			return new E402();
		case 403:
			return new E403();
		case 404:
			return new E404();
		case 405:
			return new E405();
		case 500:
			return new E500();
		case 501:
			return new E501();
		case 502:
			return new E502();
		case 503:
			return new E503();
		case 504:
			return new E504();
		case 505:
			return new E505();
		default:
			return new HttpError(code);
		}
	}

	public HttpError() {
	}

	public HttpError(int errorCode) {
		this.code = errorCode;
	}

	public HttpError(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public HttpError(String msg) {
		super(msg);
	}

	public HttpError(Throwable throwable) {
		super(throwable);
	}

	public static class E304 extends HttpError {
		{
			code = 304;
		}
	}

	public static class E400 extends HttpError {
		{
			code = 400;
		}
	}

	public static class E401 extends HttpError {
		{
			code = 401;
		}
	}

	public static class E402 extends HttpError {
		{
			code = 402;
		}
	}

	public static class E403 extends HttpError {
		{
			code = 403;
		};
	}

	public static class E404 extends HttpError {
		{
			code = 404;
		}
	}

	public static class E405 extends HttpError {
		{
			code = 405;
		}
	}

	public static class E500 extends HttpError {
		{
			code = 500;
		}
	}

	public static class E501 extends HttpError {
		{
			code = 501;
		}
	}

	public static class E502 extends HttpError {
		{
			code = 502;
		}
	}

	public static class E503 extends HttpError {
		{
			code = 503;
		}
	}

	public static class E504 extends HttpError {
		{
			code = 504;
		}
	}

	public static class E505 extends HttpError {
		{
			code = 505;
		}
	}
}
