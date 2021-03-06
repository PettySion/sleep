package com.szip.smartdream.Util;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DateUtil {

    public static ArrayList<String> getMonthList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(String.format("%02d", i));
        }
        return list;
    }

    public static ArrayList<String> getDayList(int year, int month) {
        int day = 0;
        ArrayList<String> list = new ArrayList<>();
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                day = 30;
                break;
            case 2:
                day = year % 4 == 0 ? 29 : 28;
            default:
                break;
        }

        for (int i = 1; i <= day; i++) {
            list.add(String.format("%02d", i));
        }
        return list;


    }

    public static ArrayList<String> getYearList() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= Integer.valueOf(getCurrentYear()) - 1930; i++) {
            list.add(String.format("%4d", i + 1930));
        }
        return list;
    }

    public static ArrayList<String> getStature() {
        ArrayList<String> list1 = new ArrayList<>();

        for (int i = 0; i < 130; i++) {
            list1.add(String.format("%d", i + 100));
        }

        return list1;
    }

    public static ArrayList<List<String>> getStatureWithBritish() {
        ArrayList<List<String>> lists = new ArrayList<>();
        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            list1.add(String.format("%d", i + 3));
        }
        for (int i = 0; i < 12; i++) {
            list2.add(String.format("%02d", i));
        }

        lists.add(list1);
        lists.add(list2);
        return lists;
    }

    public static ArrayList<String> getWeight() {

        ArrayList<String> list1 = new ArrayList<>();


        for (int i = 0; i < 180; i++) {
            list1.add(String.format("%d", i + 20));
        }
        return list1;
    }

    public static ArrayList<String> getWeightWithBritish() {

        ArrayList<String> list2 = new ArrayList<>();

        for (int i = 0; i < 400; i++) {
            list2.add(String.format("%d", 44 + i));
        }

        return list2;
    }

    public static String getCurrentYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date date = new Date();
        return sdf.format(date);
    }

    //??????????????????????????????Date??????
    public static Date parse(String strDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(strDate);
    }

    //???????????????????????????
    public static int getAge(Date birthDay) throws Exception {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);

        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;
            } else {
                age--;
            }
        }
        return age;
    }

    /**
     * ???????????????????????????
     */
    public static int getStringToDate(String TimeData) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        int gmt[] = getGMT();

        if (TimeData.equals("today")) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR,gmt[0]);
            calendar.add(Calendar.MINUTE,gmt[1]);
            return (int) (calendar.getTimeInMillis() / 60 / 60 / 24 / 1000);
        } else {
            try {
                date = dateFormat.parse(TimeData + " 00:00:00");
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR,gmt[0]);
            calendar.add(Calendar.MINUTE,gmt[1]);
            return (int) (calendar.getTimeInMillis() / 60 / 60 / 24 / 1000);
        }
    }

    /**
     * ???????????????????????????
     */
    public static int getDateLongToInt(long TimeData) {

        int gmt[] = getGMT();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeData*1000);
        if (calendar.get(Calendar.HOUR_OF_DAY)<5){//?????????????????????????????????5?????????????????????????????????
            calendar.add(Calendar.DAY_OF_YEAR,-1);
            calendar.add(Calendar.HOUR,gmt[0]);
            calendar.add(Calendar.MINUTE,gmt[1]);
        }else {//?????????????????????????????????5???????????????????????????
            calendar.add(Calendar.HOUR,gmt[0]);
            calendar.add(Calendar.MINUTE,gmt[1]);
        }
        return (int) (calendar.getTimeInMillis() / 60 / 60 / 24 / 1000);
    }

    /**
     * ???????????????????????????
     *
     * @param milSecond
     * @return
     */
    public static String getDateToString(int milSecond) {
        long time = ((long) milSecond) * 60 * 60 * 24 * 1000;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    /**
     * ???????????????????????????(MM/DD??????)
     *
     * @param milSecond
     * @return
     */
    public static String getDateToStringWithoutYear(int milSecond) {
        long time = ((long) milSecond) * 60 * 60 * 24 * 1000;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd");
        return format.format(date);
    }


    /**
     * ???????????????
     *
     * @param milSecond ??????????????????
     * @return ????????????????????????
     */
    public static int getWeek(int milSecond) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {

            c.setTime(format.parse(getDateToString(milSecond)));

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return c.get(Calendar.DAY_OF_WEEK)-1;
    }

    /**
     * ???????????????
     *
     * @param milSecond ??????????????????
     * @return ????????????????????????
     */
    public static int[] getMonth(int milSecond) {

        int date[] = new int[2];
        int day = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {

            c.setTime(format.parse(getDateToString(milSecond)));

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        switch (c.get(Calendar.MONTH)) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
            case 9:
            case 11:
                day = 31;
                break;
            case 3:
            case 5:
            case 8:
            case 10:
                day = 30;
                break;
            case 1:
                day = c.get(Calendar.YEAR) % 4 == 0 ? 29 : 28;
            default:
                break;
        }

        if (c.get(Calendar.DAY_OF_MONTH) == 1) {
            date[0] = 0;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 2) {
            date[0] = 1;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 3) {
            date[0] = 2;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 4) {
            date[0] = 3;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 5) {
            date[0] = 4;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 6) {
            date[0] = 5;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 7) {
            date[0] = 6;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 8) {
            date[0] = 7;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 9) {
            date[0] = 8;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 10) {
            date[0] = 9;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 11) {
            date[0] = 10;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 12) {
            date[0] = 11;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 13) {
            date[0] = 12;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 14) {
            date[0] = 13;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 15) {
            date[0] = 14;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 16) {
            date[0] = 15;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 17) {
            date[0] = 16;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 18) {
            date[0] = 17;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 19) {
            date[0] = 18;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 20) {
            date[0] = 19;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 21) {
            date[0] = 20;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 22) {
            date[0] = 21;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 23) {
            date[0] = 22;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 24) {
            date[0] = 23;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 25) {
            date[0] = 24;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 26) {
            date[0] = 25;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 27) {
            date[0] = 26;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 28) {
            date[0] = 27;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 29) {
            date[0] = 28;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 30) {
            date[0] = 29;
            date[1] = day;
            return date;
        }
        if (c.get(Calendar.DAY_OF_MONTH) == 31) {
            date[0] = 30;
            date[1] = day;
            return date;
        }
        return date;
    }

    /**
     *???????????????????????????
     * @param date          ???????????????
     * @param flag          ture+,false-
     * @return
     */
    public static int getMonthAgo(int date,boolean flag) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(getDateToString(date)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (flag)
            calendar.add(Calendar.MONTH, +1);
        else
            calendar.add(Calendar.MONTH, -1);
        return (int)(calendar.getTime().getTime()/60/60/24/1000);
    }

    /**
     * ??????????????????
     * */
    public static String getTimeNow(){
        Calendar calendar = Calendar.getInstance();

        //??????????????????
        //??????
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //??????
        int minute = calendar.get(Calendar.MINUTE);

        String time = String.format("%02d:%02d",hour,minute);

        return time;
    }

    /**
     * ??????????????????
     * */
    public static int getTimeNowForMinute(){
        Calendar calendar = Calendar.getInstance();

        //??????????????????
        //??????
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //??????
        int minute = calendar.get(Calendar.MINUTE);

        int time = hour*60+minute;

        return time;
    }

    /**
     * ?????????????????????
     * */
    public static Spannable initText(String text,boolean flag){
        if (flag){
            Spannable span = new SpannableString(text);
            int i = text.indexOf('h');
            if (i>=0){
                span.setSpan(new RelativeSizeSpan(1.5f), 0, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            i = text.indexOf("min");
            if (i>=0){
                span.setSpan(new RelativeSizeSpan(1.5f), i-2, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return span;
        }else {
            Spannable span = new SpannableString(text);
            span.setSpan(new RelativeSizeSpan(1.5f), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return span;
        }
    }

    /**
     * ?????????????????????????????????????????????
     * */
    public static int getUpdataSub(int updataTime,long nowTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nowTime);
        calendar.set(Calendar.HOUR_OF_DAY,8);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        long time = calendar.getTimeInMillis();

        return (int)(time/1000/60/60/24)-updataTime;
    }

    /**
     * ??????GMT??????
     * */
//    public static int[]getGMT(){
//        TimeZone timeZone = TimeZone.getDefault();
//        String str = timeZone.getDisplayName(false,TimeZone.SHORT);zai
//        int gmt[] = new int[2];
//        int i = str.indexOf("+");
//        if (i>0){
//            gmt[0] = Integer.valueOf(str.substring(i+1,i+3));
//            gmt[1] = Integer.valueOf(str.substring(i+4,i+6));
//        }else{
//            i = str.indexOf("-");
//            gmt[0] = Integer.valueOf(str.substring(i+1,i+3))*(-1);
//            gmt[1] = Integer.valueOf(str.substring(i+4,i+6))*(-1);
//        }
//        return gmt;
//    }

    public static int[]getGMT() {
        TimeZone tz = TimeZone.getDefault();
        int offsetMinutes = tz.getOffset(System.currentTimeMillis()) / 60000;
        int[] gmt = new int[2];
        gmt[0] = offsetMinutes / 60;
        gmt[1] = offsetMinutes % 60;
        return gmt;
    }

    public static String getGMTWithString(){
        TimeZone tz = TimeZone.getDefault();
        int offsetMinutes = tz.getOffset(System.currentTimeMillis()) / 60000;
        return String.format("%d",offsetMinutes);
    }

    /**
     * ???????????????????????????
     */
    public static long getStringToDateMillis() {
        Calendar calendar = Calendar.getInstance();

        return  calendar.getTimeInMillis() / 1000;
    }

    /**
     * ?????????????????????
     * @param day ??????
     * @return ???????????????0??????24???????????????
     * */
    public static long[] getTimeScopeForDay(int day){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        long time[] = new long[2];
        String dateStr = DateUtil.getDateToString(day);
        try {
            date = dateFormat.parse(dateStr+" 00:00:00");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        time[0] = date.getTime()/1000;
        try {
            date = dateFormat.parse(dateStr+" 23:59:59");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        time[1] = date.getTime()/1000;

        return time;
    }

    /**
     * ???????????????????????????????????????????????????
     * @param second ?????????
     * @return ???????????????0??????24???????????????
     * */
    public static long[] getTimeScopeForDay(long second){
        long time[] = new long[2];
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(second*1000);

        if (calendar.get(Calendar.HOUR_OF_DAY)<5){//???????????????5????????????????????????????????????
            calendar.add(Calendar.DAY_OF_YEAR,-1);
            calendar.set(Calendar.HOUR_OF_DAY,5);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            time[0] = calendar.getTimeInMillis()/1000;
            time[1] = time[0]+24*60*60-1;
        }else {//?????????????????????????????????????????????????????????
            calendar.set(Calendar.HOUR_OF_DAY,5);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            calendar.set(Calendar.MILLISECOND,0);
            time[0] = calendar.getTimeInMillis()/1000;
            time[1] = time[0]+24*60*60-1;
        }
        return time;
    }

    /**
     * ???????????????????????????????????????
     * @param day ??????
     * @return ????????????
     * */
    public static boolean isToday(int day){
        return day==getStringToDate("today");
    }

    /**
     * ???????????????????????????????????????
     *
     * @param milSecond
     * @return
     */
    public static String getDateToString(long milSecond) {
        long time = milSecond * 1000;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    /**
     * ????????????0????????????????????????????????????
     * */
    public static int getMinueOfDay(long milSecond){
        long time = milSecond * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        long time1 = calendar.getTimeInMillis();
        return (int)(time-time1)/1000/60;
    }
}

