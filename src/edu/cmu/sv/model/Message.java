package edu.cmu.sv.model;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 7053552466316534001L;
	private String email;
	private String text;
	private String dateTime;
	public String getEmail() {
		return email;
	}
	public Message setEmail(String email) {
		this.email = email;
		return this;
	}
	public String getText() {
		return text;
	}
	public Message setText(String text) {
		this.text = text;
		return this;
	}
	public String getDateTime() {
		return dateTime;
	}
	public Message setDateTime(String dateTime) {
		this.dateTime = dateTime.split("\\.")[0];
		return this;
	}
}
