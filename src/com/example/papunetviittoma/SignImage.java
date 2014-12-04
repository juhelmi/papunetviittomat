package com.example.papunetviittoma;

public class SignImage {
	private long id;
	private String searchname;
	private String href;
	private String title;
	private String shortTitle;	// title without " (Kuva: .*)"
	private String filename;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSearchname() {
		return this.searchname;
	}

	public void setSearchname(String name) {
		this.searchname = name;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String name) {
		this.filename = name;
	}

	public String getHref() {
		return this.href;
	}

	public void setHref(String name) {
		this.href = name;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String name) {
		this.title = name;
	}
	
	public String getShortTitle() {
		/*if (this.shortTitle == null || (this.shortTitle.isEmpty() && !this.title.isEmpty())) {
			// strip rest of after "(Kuva:"
			int pos = title.indexOf("(");
			if (pos > 0) {
				shortTitle = title.substring(0, pos-1);
			} else {
				shortTitle = title;
			}
		}*/
		return this.shortTitle;
	}

	public void setShortTitle(String name) {
		this.shortTitle = name;
	}
	
	@Override
	public String toString() {
		return Long.toString(id) +"; " + searchname + "; " + title + "; " + href + "; " + filename + "; " + shortTitle;
	}
}
