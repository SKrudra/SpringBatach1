package com.example.batch.beans;

public class Website {

	private Long id;
	private String url;

	public Website() {
	}

	public Website(Long id, String url) {
		super();
		this.id = id;
		this.url = url;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Website [id=" + id + ", url=" + url + "]";
	}

}
