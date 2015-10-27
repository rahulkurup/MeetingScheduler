package com.meeting;

import java.util.TimeZone;

/* UserData class for storing the details of people invited to meeting */
public class UserData {

	private String empId;
	private String userName;
	private TimeZone timeZone;
	
	public UserData(String empId, String userName, TimeZone timeZone) {
		super();
		this.empId = empId;
		this.userName = userName;
		this.timeZone = timeZone;
	}

	public String getEmpId() {
		return empId;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}	
}