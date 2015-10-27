package com.meeting;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TimeZone;


/*********************************** Notes *********************************************
 *  * @author rkurup
 *  
 * This program helps the user to schedule meetings of required duration with including people from various parts of world (with different TimeZone)
 * The program suggests the available slots where all the users can participate. If there are no such slots available, program will suggest all the slots where maximum members can participate.
 * 
 *********************How to provide input******************************
 * Input must be provided as the content of a text file.
 * 1 : Starting date and time [All Meeting Dates must be in format 23/12/15 09:15 (date/month/year hours_in_24_hour_format:minutes]
 * 2 : End date and time for meeting
 * 3 : Number of participants for meeting
 * 4 : Time Zone for Meeting
 * 5 : Required duration of meeting (in SLOT_DURATION unit)
 * 
 * After all the meeting deatils, input for user and the slots the users are busy must be specified for all the user invited(Number of participants for meeting). Each user has two lines. First line for
 * the employee details like empid, name, timezone etc. Second line will have slots for which user is busy.(comma separated). If user doesnot have buy slot, second line must be "Free"
 * 6 : empId userName timeZone
 * 7 : Starting date and time-End date and time for which user is busy, Starting date and time-End date and time for which user is busy,  (comma separated time)
 *  [All Meeting Dates must be in format 23/12/15 09:15 (date/month/year hours_in_24_hour_format:minutes]
 *  Example:      2 Rohith Asia/Calcutta
 *                26/12/15 09:45-26/12/15 21:00,27/12/15 19:45-27/12/15 22:00
 ****Important:   If the user is not busy for any duration give the line as "Free"
 *
 *
 ****************** Assumptions ***********************
 * The program assumes that the smallest unit of time is SLOT_DURATION which is 15 Minutes in this case. Thus the minimum duration of a meeting has to be 15 minutes.
 * The duration of the meeting has to be a multiple of SLOT_DURATION. ie, 15 mins, 30 mins, 45 mins, 1 hour...4 hours, 4.15 hours, 5 hours etc
 *  Time start time and end time of the meeting suggested by the tool must have minutes in following values(00 minutes, 15 minutes, 30 minutes, 45 minutes). ie Tool wouldnot schedule the meeting from 3:20 to 4:50
 *  Tool will schedule from either 3:30 to 4:00 or from 3.15 to 3:45
 *  
 *  ****************** Scalability ***********************
 *  The program is highly scalable.
 *  There is no limit in the number of participants for meeting
 *  There is no limit in duration of meeting.
 *  The program can be easily modified to change the minimum SLOT_DURATION by chnaging just a static variable. We can modify the varaible to alow the minumum duartion to even one minute (Space complexity goes high)
 *  This will result is enabling user to schedule meeting of any duration. 1 min, 12 mins or 23 mins etc..Meeting start/end time restrictions can be chnaged by just adding more content to a LL
 * 
 *  
 *  
 *  ************Important feature*******************
 *  The program takes care of all DST (day light saving time combinations while converting betwen timezones)
 *  Th meeting duration can be anything. it can span across days, months or even year :)
 *  Meeting can start in one day and end on anther day
 *  Meeting can start on one month and end on next month
 *  Meeting can start in one year and end in next year. (In case someone wants to atand meetings on new years eve :) )
 *  
 *   ***************** OutPut **********************
 *   Tool will first suggest all the slots in the meeting duration where everyone can participate
 *   
 *   ***********Note: To avoid too many slots being suggested and make result unclear , Slot once suggested would not be included in another suggestion again. ie, lets say there is a free interval from 10 to 12 when we look for a 1 hour meeting. Tool may suggest 10-11 and 11-12 slots.
 *   The slots 10.15 to 11.15 or 10.30 to 11.30 are also free and can be used for meeting. But tool would not suggest it. once tool sugest 10-11 and 11-12. user can clearly figure out the other options. We can easily change it by using an extra loop in program.
 *   
 *   Unlike the above scenario, tool will suggest all the possible slots where max members can participate, if there is no possible slot where all members can participate.
 *   If there is no slot wehere everyone can participate, the tool will suggest all the slots where maximum members can participate along with who all can attend.
 *   ie if we have a time slot where max members can participate for a one hour meeting from 10-12. tool will suggest all combinations like
 *   10-11, 10:15-11:15, 10:30-11:30, 10:45-11:45, 11-12  (this is diffrent from the logic we follow in above case for slots where everyone is free. Same logic can be used above but makes output confusing)
 * 
 *   ********************Test Cases**********************
 *   Test cases & results are in the Test Cases/input folder
 */


public class ProgramMain {
	
	// All Meeting Dates must be in format 23/12/15 09:15 (date/month/year hours_in_24_hour_format:minutes)
	private final static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy HH:mm");
	
	public static void main(String[] args) {

		final String filePath = "C:/input/input.txt";  /*Note: Edit the input file path for running your custom inputs */

		/* Read the File data */
		final File file = new File(filePath);		
		Scanner sc = null;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println("File not Found. Use Valid file path");
			e.printStackTrace();
			System.exit(0);
		}
		
		String startTime = sc.nextLine();   //Start Time for meeting
		String endTime = sc.nextLine();		//End Time for meeting

		final Scheduler scheduler = new Scheduler(); // Create a schedule object
		int numberOfInvites = sc.nextInt(); /* Number of people invited for Meeting */
		
		scheduler.setNumberOfInvites(numberOfInvites);
		sc.nextLine();
		
		String timeZoneString = sc.nextLine();  /* Time Zone for the meeting */
		scheduler.setTimeZone(TimeZone.getTimeZone(timeZoneString));
		
		int durationInUnits = sc.nextInt();			//Duration of meeting
		scheduler.setDurationInUnits(durationInUnits);
		sc.nextLine();
		
		try {
			scheduler.setMeetingStartTime(formatter.parse(startTime));
			scheduler.setMeetingEndTime(formatter.parse(endTime));

			/*once you have start date and end date. Put those date into central map as keys.*/
			scheduler.populateMapWithDateKeys();    //Populate the map with keys (starting time of all slot between starting and end time)
		} catch (ParseException e) {
			System.out.println("Date provided is not in specified format. /n All Meeting Dates must be in format like: 23/10/10 9:00");
			e.printStackTrace();
			System.exit(0);	
		}
		
		/* For each of the person invited. Read his data and populate our center Map */
		for(int i = 0; i < scheduler.getNumberOfInvites(); i++) {		
			
			/* User data will be in the following format for each employee
			 * empId userName timeZone
			 * 23/10/10 9:00-23/10/10 13:30,24/10/10 9:00 24/10/10 10:30
			 * 
			 */
			String[] empDetails = sc.nextLine().split(" "); //Read first line for all Employee basic details
			String empId = empDetails[0];
			String userName = empDetails[1];
			String userTimeZoneString = empDetails[2];
			TimeZone userTimeZone = TimeZone.getTimeZone(userTimeZoneString);
			final UserData user = new UserData(empId, userName, userTimeZone);   ///Create emp object
			
			scheduler.getInvitedMembers().add(user);   // Add user details to Set of invited members

			String busySlot = sc.nextLine(); /* Get all booked slot for this user */
			
			if(busySlot.equals("Free")) {
				continue; // The employee don't have any busy slots and free. Lrts go to next employee
			}
			
			String[] busySlots = busySlot.split(","); /* Get all booked slot for this user */
			// Go through each slot and add Employee id in center map 
			for(String slot : busySlots) {
				
				String[] slotData = slot.split("-");
				Calendar startDate = null;
				Calendar endDate = null;
				try {
					startDate = getCalenderWithTimeZone(formatter.parse(slotData[0]), userTimeZone, scheduler.getTimeZone());   //Conversation of timeZone happens here
					endDate = getCalenderWithTimeZone(formatter.parse(slotData[1]), userTimeZone, scheduler.getTimeZone());
				} catch (ParseException e) {
					System.out.println("Date provided is not in specified format/n All Meeting Dates must be in format like: 23/10/10 9:00");
					e.printStackTrace();
				}				
				/* Add the Employee id of this employee for all the slots in given period */
				scheduler.addEmpIdtoSlot(user, 
						                 startDate,
						                 endDate);
			}
		}
		
		
		System.out.println("\n");
		
		if(scheduler.findEmptySlots()) { // We found some free slots for our meeting :)
			System.out.println("**********The possible slots are as follows*********************");
			for(Entry<Calendar, Calendar> slot : scheduler.getSlotsfFound().entrySet()) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				System.out.println(String.format("Start Time: %s End Time: %s",
									             format.format(slot.getKey().getTime()),
									             format.format(slot.getValue().getTime())));

			}

							
		} else {   // lets find the slot where most number of people can attend
			System.out.println("Sorry!! There was no slot found for the meeting where everyone can participate.");
			System.out.println("Don't worry!! We will find the slot where maximum members can participate.\n");	
			scheduler.findNextBestSlot();   // Fnd next best slots
		}
	}
	

	/** Convert a date from one TimeZone to another. Consider DST as well
	 * @param date
	 * @param fromTimeZone
	 * @param toTimeZone
	 * @return
	 */
	public static Calendar getCalenderWithTimeZone(Date date, TimeZone fromTimeZone,TimeZone toTimeZone) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);
		if (fromTimeZone.inDaylightTime(calendar.getTime())) {
			calendar.add(Calendar.MILLISECOND, calendar.getTimeZone().getDSTSavings() * -1);
		}
		calendar.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
		if (toTimeZone.inDaylightTime(calendar.getTime())) {
			calendar.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
		}
		return calendar;
	}
}