package com.meeting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

public class Scheduler {
	
	private static Integer SLOT_DURATION = 15; // Our Minimum meeting duration is 15.

	// The central data structure which will store all the slot info for invited members */
	private final Map<Calendar, Set<UserData>> slotMap;
	
	private final Set<UserData> invitedMembers;

	
	// All Meeting Dates must be in format 23/12/15 09:15 (date/month/year hours_in_24_hour_format:minutes)
	private Calendar meetingStartTime;  // Start time of the Meeting
	private Calendar meetingEndTime;    // End time of the Meeting

	private int numberOfInvites;    // Number of people invited for Meeting
	private TimeZone timeZone;      // Time Zone of the Meeting
	private int durationInUnits ;   // Duration of meeting in units of SLOT_DURATION(15 Minutes)
	
	private Map<Calendar, Calendar> slotsfFound;
	private Map<Calendar, Map<Calendar, Set<UserData>>> bestPossibleSlots;
	
	
	public Scheduler() {
		super();
		meetingStartTime = Calendar.getInstance();
		meetingEndTime = Calendar.getInstance();
		slotMap = new LinkedHashMap<Calendar, Set<UserData>>();
		slotsfFound = new LinkedHashMap<Calendar, Calendar>();
		invitedMembers = new HashSet<UserData>();
		bestPossibleSlots = new LinkedHashMap<Calendar, Map<Calendar,Set<UserData>>>();
	}
	

	public Set<UserData> getInvitedMembers() {
		return invitedMembers;
	}


	public Calendar getMeetingStartTime() {
		return meetingStartTime;
	}



	public void setMeetingStartTime(Date meetingStartTime) {
		this.meetingStartTime.setTimeZone(this.getTimeZone());
		this.meetingStartTime.setTime(meetingStartTime);
	}



	public Calendar getMeetingEndTime() {
		return meetingEndTime;
	}



	public void setMeetingEndTime(Date meetingEndTime) {
		this.meetingEndTime.setTimeZone(this.getTimeZone());
		this.meetingEndTime.setTime(meetingEndTime);
	}



	public int getNumberOfInvites() {
		return numberOfInvites;
	}



	public void setNumberOfInvites(int numberOfInvites) {
		this.numberOfInvites = numberOfInvites;
	}



	public TimeZone getTimeZone() {
		return timeZone;
	}



	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}



	public int getDurationInUnits() {
		return durationInUnits;
	}



	public void setDurationInUnits(int durationInUnits) {
		this.durationInUnits = durationInUnits;
	}



	public Map<Calendar, Set<UserData>> getSlotmap() {
		return slotMap;
	}


	public Map<Calendar, Calendar> getSlotsfFound() {
		return slotsfFound;
	}


	public void setSlotsfFound(Map<Calendar, Calendar> slotsfFound) {
		this.slotsfFound = slotsfFound;
	}



	/** Find empty slots where everyone is free. Return false if no such slot found
	 * @return
	 */
	boolean findEmptySlots() {
		
		int count = 0;  // The slots where everyone is free.
		boolean slotFound = false;
		Calendar startTime = null;
		Calendar endTime = null;
		// Go through map and loop through slots.
		for(Entry<Calendar, Set<UserData>> slotEntry : getSlotmap().entrySet()) {
			if(startTime == null) {
				startTime = (Calendar) slotEntry.getKey().clone();
			}
			if(slotEntry.getValue().size() == 0)  { // The slot is free
				count++;
				if(count ==  getDurationInUnits()) {  // We found free slots for a particular duration.
					endTime = (Calendar) slotEntry.getKey().clone();
					endTime.add(Calendar.MINUTE, SLOT_DURATION); // we need to give the end time
					getSlotsfFound().put(startTime, endTime);  // Add the slot to result
					startTime = null;
					endTime = null;
					slotFound = true;   // We Found a slot.
					count = 0;
				}
			} else {
				startTime = null;
				count = 0;
			}
		}
		return slotFound;
	}


	/** The employee is busy from startDate to endDate. Add employee object into all slots in this range into value set
	 * @param user
	 * @param startDate
	 * @param endDate
	 */
	void addEmpIdtoSlot(UserData userData,
						Calendar startDate,
						Calendar endDate) {
		
		Calendar theDayCalender = startDate;
		
		/* Go through each 15 minutes increment from start to end and insert the emp ID into central map
		 * If start time is 23/12/15 9:00 and end time is 23/12/15 10.15
		 * Add Employee object to value list for keys 9:00, 9:15, 9:30, 9:45, 10:00 will be added to map
		 * 
		 */		
		while(!theDayCalender.after(endDate) 
				&& !theDayCalender.equals(endDate)){ //While the time is lower than end time
			if(!getSlotmap().containsKey(theDayCalender)) {
				// The employees this schedule is out of our meeting range. Not our concern.
			} else {
				//Add the empData to the map for this slot.
			    getSlotmap().get(theDayCalender).add(userData);
			}
		    theDayCalender.add(Calendar.MINUTE, SLOT_DURATION); // Increase the key by 15 minutes
		}
	}



	/** Populate the Center map for a meeting {Key: Calndar object with start time for slot, Value: Set of users busy during that time}
	 * @param meetingStartTime
	 * @param meetingEndTime
	 * @throws ParseException
	 */
	void populateMapWithDateKeys() 
		throws ParseException {
		
		Calendar theDayCalender = getMeetingStartTime();
		
		/* Go through each 15 minutes increment from start to end and insert the key into central map
		 * If start time is 23/12/15 9:00 and end time is 23/12/15 10.15
		 * Keys for time 9:00, 9:15, 9:30, 9:45, 10:00 will be added to map. Value will be a empty hashSet
		 * 
		 */		
		while( !theDayCalender.after(getMeetingEndTime()) 
				&& !theDayCalender.equals(getMeetingEndTime())){ //while the time is lower than end time
		    getSlotmap().put((Calendar)theDayCalender.clone(), new HashSet<UserData>());
		    theDayCalender.add(Calendar.MINUTE, SLOT_DURATION); // Increase the key by 15 minutes
		}
	}


	/** Find next slot in case.
	 * 
	 */
	public void findNextBestSlot() {		
		
		List <Entry<Calendar, Set<UserData>>> mapList = new LinkedList<Map.Entry<Calendar,Set<UserData>>>(getSlotmap().entrySet());
		int max_count = Integer.MIN_VALUE;  // The maximum number of people available in the slot
		boolean slotFound = false;
		boolean oneSlotFound = false;

		// Go through map and loop through slots.
		for(int i = 0; i <= mapList.size() - getDurationInUnits(); i++) {
			Set<UserData> uniqueEmp = new HashSet<UserData>(getInvitedMembers());   // Add all invited people to set first
			int j = 0;
			for(j = 0; j < getDurationInUnits(); j++) {
				uniqueEmp.removeAll(mapList.get(i + j).getValue());   //Remove all busy people from the set. (We want intersection of sets)
			}
			if (j == getDurationInUnits() && uniqueEmp.size() >= max_count) {   // We went through the interval of the required duration and found out people who are available for all small slots in the interval
				/* A slot found */												// Unique set is now an intesection of all sets in the given period, meaning it contains all people available for all slot in the duration
				max_count = uniqueEmp.size();
				Calendar startTime = (Calendar) mapList.get(i).getKey().clone();
				Calendar endTime =  (Calendar) mapList.get(i + j - 1).getKey().clone();
				endTime.add(Calendar.MINUTE, SLOT_DURATION);
				Set<UserData> resultList = new HashSet<UserData>(uniqueEmp);		// Add the list of user available		
				Map<Calendar, Set<UserData>> endDatetoUserListMap = new HashMap<Calendar, Set<UserData>>();
				endDatetoUserListMap.put(endTime, resultList);
				getBestPossibleSlots().put(startTime, endDatetoUserListMap);
				if(!slotFound) {
					slotFound = true;
				}
				uniqueEmp.clear();
			}
		}
		
		if(slotFound) {  // A slot found
			System.out.println("*********** Next best possible slots are as follows **************\n");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			for(Entry<Calendar, Map<Calendar, Set<UserData>>> data : getBestPossibleSlots().entrySet()) {
				Calendar startDate = data.getKey();
				Calendar endDate = null;
				for(Entry<Calendar, Set<UserData>> valueMap: data.getValue().entrySet()) {
					endDate = valueMap.getKey();  // only one element in map
				}

				Set<UserData> userSet = data.getValue().get(endDate);
				if(userSet.size() == max_count && userSet.size() != 0) {   // if the number of people who can attend is same the highest and not zero
					oneSlotFound = true;
					System.out.println(String.format("Start Time: %s End Time: %s",
		                               format.format(startDate.getTime()),
		                               format.format(endDate.getTime())));
					System.out.println(String.format("The number of people who can attend is: %s", userSet.size()));
					System.out.println("The candidates who can attend the slot are as follows:");
					
					for(UserData user : userSet) {
						System.out.println(String.format("UserName: %s  EmpId: %s", user.getUserName(), user.getEmpId()));
					}	
					System.out.println("\n");					
				}
			}
		}
		
		if( (!oneSlotFound) {
			System.out.println(String.format("No meeting can be scheduled." );
		}
	}


	public Map<Calendar, Map<Calendar, Set<UserData>>> getBestPossibleSlots() {
		return bestPossibleSlots;
	}


	public void setBestPossibleSlots(
			Map<Calendar, Map<Calendar, Set<UserData>>> bestPossibleSlots) {
		this.bestPossibleSlots = bestPossibleSlots;
	}
}