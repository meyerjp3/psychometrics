/*
 * Copyright 2012 J. Patrick Meyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * The StopWatch class provide date and time functions
 * including an elapsed time function that provides
 * calculations to the milisecond. The starting time
 * of the elapsed time begins when the class is constructed.
 * Values are returned as Strings.
 *
 * @author J Patrick Meyer
 * @version 04-24-2008
 *
 */
public class StopWatch {

    String DATE_AND_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    String TIME_FORMAT = "HH:mm:ss";
    String ELAPSED_FORMAT = "dd:HH:mm:ss:SSS";//FIXME not returning number of days
    String DATE_FORMAT = "yyyy-MM-dd";

	long startTime = 0;

	public StopWatch(){
		startTime = System.currentTimeMillis();
	}

	/**
	 *
	 * @return the elapsed time in hours:minutes:seconds:miliseconds
	 *
	 */
	public String getElapsedTime(){
		long stopTime=System.currentTimeMillis();
		SimpleDateFormat timeFormat = new SimpleDateFormat(ELAPSED_FORMAT);
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		long elapsed = stopTime - startTime;
		long remaining=0L;

		long days = elapsed/(24 * 60 * 60 * 1000);
		remaining = elapsed-(days*(24 * 60 * 60 * 1000));
		long hours = remaining/(60 * 60 * 1000);
		remaining = remaining-(hours*(60 * 60 * 1000));
		long min = remaining/(60 * 1000);
		remaining = remaining-min*(60 * 1000);
		long sec = remaining/1000;
		long ms = remaining-(sec*1000);


//		String elapsedTime = timeFormat.format(new Date(elapsed));
//		String[] et = elapsedTime.split(":");
		String outString = "";
		if(days>0) outString += days + " days, ";
		if(days>0 || hours >0) outString += hours + " hrs, ";
		if(days>0 || hours>0 || min>0) outString += min + " mins, ";
		outString+=sec + " secs, " + ms + " msecs";
//		return et[0] + " days, " + et[1] + " hrs  " + et[2] + " mins  " + et[3] + "secs  " + et[4] + " msec";
		return outString;
	}

	/**
	 *
	 * @return current date and time in the format yyyy-MM-dd HH:mm:ss
	 *
	 */
	public String getDateAndTime(){
		SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String timeAndDate = dateAndTimeFormat.format(cal.getTime());
		return timeAndDate;
	}

	/**
	 *
	 * @return current system time in the format HH:mm:ss
	 */
	public String getTime(){
		SimpleDateFormat tf = new SimpleDateFormat(TIME_FORMAT);
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String time = tf.format(cal.getTime());
		return time;
	}

	/**
	 *
	 * @return current system date in the format yyyy-MM-dd
	 *
	 */
	public String getDate(){
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		String date = dateFormat.format(cal.getTime());
		return date;
	}

}
