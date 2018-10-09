package com.erupt.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Page {

	private int pageNumber;

	private int pageSize;

	private long total;

	private Object example;

	private String sort;

	private List<Object> list;

	private LinkedHashMap<String, String> columns;

	private Set<GroupHeader> groupHeaders;

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public Object getExample() {
		return example;
	}

	public void setExample(Object example) {
		this.example = example;
	}

	public String getSort() {
		if (null == sort || "".equals(sort)) {
			return "id";
		}
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public List<Object> getList() {
		return list;
	}

	public void setList(List<Object> list) {
		this.list = list;
	}

	public LinkedHashMap<String, String> getColumns() {
		return columns;
	}

	public void addColumn(String key, String value) {
		if (null == columns) {
			columns = new LinkedHashMap<String, String>();
		}
		columns.put(key, value);
	}

	public Set<GroupHeader> getGroupHeaders() {
		return groupHeaders;
	}

	public void setGroupHeaders(Set<GroupHeader> groupHeaders) {
		this.groupHeaders = groupHeaders;
	}

	public void addGroupHeader(String startColumnName, Integer numberOfColumns, String titleText) {

		if (null == this.groupHeaders) {
			this.groupHeaders = new HashSet<GroupHeader>();
		}
		GroupHeader groupHeader = new GroupHeader();
		groupHeader.setNumberOfColumns(numberOfColumns);
		groupHeader.setStartColumnName(startColumnName);
		groupHeader.setTitleText(titleText);
		this.groupHeaders.add(groupHeader);
	}

	public class GroupHeader {
		private String startColumnName;
		private Integer numberOfColumns;
		private String titleText;

		public String getStartColumnName() {
			return startColumnName;
		}

		public void setStartColumnName(String startColumnName) {
			this.startColumnName = startColumnName;
		}

		public Integer getNumberOfColumns() {
			return numberOfColumns;
		}

		public void setNumberOfColumns(Integer numberOfColumns) {
			this.numberOfColumns = numberOfColumns;
		}

		public String getTitleText() {
			return titleText;
		}

		public void setTitleText(String titleText) {
			this.titleText = titleText;
		}
	}
}
