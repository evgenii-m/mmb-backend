package ru.pushkin.mma.deezer.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data<T> {

    private List<T> data;
    private Integer total;
    private String next;
    private String checksum;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

	@Override
	public String toString() {
		return "Data{" +
				"data=" + data +
				", total=" + total +
				", next='" + next + '\'' +
				", checksum='" + checksum + '\'' +
				'}';
	}
}
