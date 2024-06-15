package com.application.reethau.com;

import com.google.gson.annotations.SerializedName;

public class ResponseRegistrasiRegId {

	@SerializedName("success")
	boolean success;

	@SerializedName("message")
	private String message;

	public boolean getSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

}
