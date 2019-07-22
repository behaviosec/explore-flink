package org.sense.flink.pojo;

import java.io.Serializable;
import java.util.Date;

public class ValenciaPollution implements Serializable {
	private static final long serialVersionUID = -469085368038671321L;
	private Integer id;
	private String street;
	private AirPollution parameters;
	private Date update;
	private String uri;
	private Point coordinates;

	public ValenciaPollution() {
	}

	public ValenciaPollution(String street, String parameters, String uri, Point coordinates) {
		this.id = 0;
		this.street = street;
		this.parameters = AirPollution.extract(uri);
		this.update = new Date();
		this.uri = uri;
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return "ValenciaPollution [id=" + id + ", coordinates=" + coordinates + ", street=" + street + ", parameters="
				+ parameters + ", update=" + update + ", uri=" + uri + "]";
	}
}
