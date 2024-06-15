package com.application.reethau.com;

import java.io.Serializable;

public class version implements Serializable {

	private static final long serialVersionUID = 1L;

	String no, nama;

	public version(String no, String nama) {
		this.no = no;
		this.nama = nama;
	}

	public String getNo() {
		return this.no;
	}

	public String getNama() {
		return this.nama;
	}

}