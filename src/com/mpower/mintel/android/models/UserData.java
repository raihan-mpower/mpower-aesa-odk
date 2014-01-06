package com.mpower.mintel.android.models;

public class UserData {
	private String username = "";
	private String password = "";
	
	public void resetAll() {
		username = "";
		password = "";
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
