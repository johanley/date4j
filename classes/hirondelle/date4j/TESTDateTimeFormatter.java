package hirondelle.date4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

/** JUnit tests. */
public final class TESTDateTimeFormatter   extends TestCase   {

  /** Run the test cases.  */
  public static void main(String args[]) {
    String[] testCaseName = { TESTDateTimeFormatter.class.getName() };
    junit.textui.TestRunner.main(testCaseName);
  }

  public TESTDateTimeFormatter(String aName) {
    super(aName);
  }

   // TEST CASES //

  public void testSpeed(){
      //.046
      testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MM-DD h12:mm:ss", "2009-10-28 1:59:01");
  }
  
  public void testSpeedJDK() throws ParseException {    
    String dateTime =  "2009-10-28 01:59:01";
    //.063 - slower than my classes - at least in this simple test
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Date result = format.parse(dateTime);
     String dateResurrected = format.format(result);
  }
  
  public void testDate(){
    testDate(SUCCESS, "2009-10-28", "YYYY-MM-DD", "2009-10-28");
    testDate(SUCCESS, "2009-01-28", "YYYY-MM-DD", "2009-01-28");
    testDate(SUCCESS, "2009-12-01", "YYYY-MM-DD", "2009-12-01");
    testDate(SUCCESS, "2009-01-01", "YYYY-MM-DD", "2009-01-01");
    
    testDate(SUCCESS, "2009-10-28", "YYYY-M-D", "2009-10-28");
    testDate(SUCCESS, "2009-10-03", "YYYY-M-D", "2009-10-3");
    testDate(SUCCESS, "2009-05-28", "YYYY-M-D", "2009-5-28");
    
    testDate(SUCCESS, "2009-10-28", "YYYY-MM-D", "2009-10-28");
    testDate(SUCCESS, "2009-10-03", "YYYY-MM-D", "2009-10-3");
    testDate(SUCCESS, "2009-05-28", "YYYY-MM-D", "2009-05-28");
    
    testDate(SUCCESS, "2009-10-28", "YYYY-M-DD", "2009-10-28");
    testDate(SUCCESS, "2009-10-03", "YYYY-M-DD", "2009-10-03");
    testDate(SUCCESS, "2009-05-28", "YYYY-M-DD", "2009-5-28");
    
    testDate(SUCCESS, "2009-10-28", "YY-M-DD", "09-10-28");
    testDate(SUCCESS, "2009-10-03", "YY-M-DD", "09-10-03");
    testDate(SUCCESS, "2099-05-28", "YY-M-DD", "99-5-28");

    testDate(SUCCESS, "2099-05-28", "YYYYMMDD", "20990528");
    testDate(SUCCESS, "2099-11-28", "WWW MMM DD, YYYY", Locale.CANADA, "Sat Nov 28, 2099");
    testDate(SUCCESS, "2099-11-28", "WWW-DD|th|", Locale.CANADA, "Sat-28th");

    testDate(SUCCESS, "2009-12-31", "MM-DD-YYYY", "12-31-2009");
    
    testDate(SUCCESS, "2009-12-31", "***YYYY-MM-DD", "***2009-12-31");
    testDate(SUCCESS, "2009-12-31", "***YYYY-MM-DD*", "***2009-12-31*");
    testDate(SUCCESS, "2009-12-31", "*  YYYY-MM-DD *", "*  2009-12-31 *");
    
    testDate(SUCCESS, "2009-12-31", "WWWW, MMM D, YYYY", Locale.CANADA, "Thursday, Dec 31, 2009");
    testDate(SUCCESS, "2009-12-31", "WWW, MMM D, YYYY", Locale.CANADA, "Thu, Dec 31, 2009");
    
    testDate(SUCCESS, "01:59:59", "hh:mm:ss", "01:59:59");
    testDate(SUCCESS, "01:59:59", "h:mm:ss", "1:59:59");
    testDate(SUCCESS, "01:59:59", "hh:m:ss", "01:59:59");
    testDate(SUCCESS, "01:01:59", "hh:m:ss", "01:1:59");
    testDate(SUCCESS, "01:59:59", "hh:mm:s", "01:59:59");
    testDate(SUCCESS, "01:59:01", "hh:mm:s", "01:59:1");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss", "01:59:01");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.f", "01:59:01.1");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.ff", "01:59:01.12");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.fff", "01:59:01.123");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.ffff", "01:59:01.1234");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.fffff", "01:59:01.12345");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.ffffff", "01:59:01.123456");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.fffffff", "01:59:01.1234567");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.ffffffff", "01:59:01.12345678");
    testDate(SUCCESS, "01:59:01.123456789", "hh:mm:ss.fffffffff", "01:59:01.123456789");
    
    testDate(SUCCESS, "01:59:01", "hh", "01");
    testDate(SUCCESS, "01:59:01", "mm", "59");
    testDate(SUCCESS, "01:59:01", "ss", "01");
    testDate(SUCCESS, "01:59:01", "hh ", "01 ");
    testDate(SUCCESS, "01:59:01", "hh:mm", "01:59");
    testDate(SUCCESS, "01:59:01", "h:mm", "1:59");
    testDate(SUCCESS, "01:59:01", "mm:ss", "59:01");
    testDate(SUCCESS, "01:59:01", "mm:s", "59:1");
    
    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MM-DD hh:mm:ss", "2009-10-28 01:59:01");
    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYYMMDDThh:mm:ss", "20091028T01:59:01");
    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MMM-DD hh:mm:ss", Locale.CANADA, "2009-Oct-28 01:59:01");
    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MMM-DD", Locale.CANADA, "2009-Oct-28");
    testDate(SUCCESS, "2009-10-28 01:59:01", ":hh:mm:ss:", ":01:59:01:");
    testDate(SUCCESS, "2009-04-28 13:59:01", "DD MMM, YYYY hh:mm:ss", Locale.CANADA_FRENCH, "28 avr, 2009 13:59:01");


    testDate(SUCCESS, "01:59:01.01", "hh:mm:ss.ff", "01:59:01.01");
    testDate(SUCCESS, "01:59:01.01", "hh:mm:ss.fff", "01:59:01.010");
    testDate(SUCCESS, "01:59:01.01", "hh:mm:ss.ffff", "01:59:01.0100");
    testDate(SUCCESS, "01:59:01.01", "hh:mm:ss.fffff", "01:59:01.01000");
    testDate(SUCCESS, "01:59:01.01", "hh:mm:ss.ffffff", "01:59:01.010000");
    testDate(SUCCESS, "01:59:01.01", "hh:mm:ss.fffffff", "01:59:01.0100000");
    testDate(SUCCESS, "01:59:01.01", "hh:mm:ss.ffffffff", "01:59:01.01000000");
    testDate(SUCCESS, "01:59:01.01", "hh:mm:ss.fffffffff", "01:59:01.010000000");
    
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss", "01:59:01");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.f", "01:59:01.0");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.ff", "01:59:01.00");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.fff", "01:59:01.000");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.ffff", "01:59:01.0000");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.fffff", "01:59:01.00000");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.ffffff", "01:59:01.000000");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.fffffff", "01:59:01.0000000");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.ffffffff", "01:59:01.00000000");
    testDate(SUCCESS, "01:59:01.000000001", "hh:mm:ss.fffffffff", "01:59:01.000000001");
    
    testDate(SUCCESS, "01:59:01.000000000", "hh:mm:ss.fffffffff", "01:59:01.000000000");

    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MM-DD h12:mm:ss", "2009-10-28 1:59:01");
    testDate(SUCCESS, "2009-10-28 13:59:01", "YYYY-MM-DD h12:mm:ss", "2009-10-28 1:59:01");

    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MM-DD hh12:mm:ss", "2009-10-28 01:59:01");
    testDate(SUCCESS, "2009-10-28 12:59:01", "YYYY-MM-DD hh12:mm:ss", "2009-10-28 12:59:01");
    testDate(SUCCESS, "2009-10-28 13:59:01", "YYYY-MM-DD hh12:mm:ss", "2009-10-28 01:59:01");
    testDate(SUCCESS, "2009-10-28 23:59:01", "YYYY-MM-DD hh12:mm:ss", "2009-10-28 11:59:01");
    
    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MM-DD h12:mm:ss a", Locale.CANADA, "2009-10-28 1:59:01 AM");
    testDate(SUCCESS, "2009-10-28 13:59:01", "YYYY-MM-DD h12:mm:ss a", Locale.CANADA, "2009-10-28 1:59:01 PM");
    
    //WRONG : 
     //this fails - tokens cannot appear next to each other:
    //testDate(SUCCESS, "2009-10-28 13:59:01", "YYYY-MM-DD h12:mm:ssa", Locale.CANADA, "2009-10-28 1:59:01PM");
    //the workaround for the above is to use the escape character, with nothing in between:
    testDate(SUCCESS, "2009-10-28 13:59:01", "YYYY-MM-DD h12:mm:ss||a", Locale.CANADA, "2009-10-28 1:59:01PM");
  }
  
  public void testEscapeChar(){
    testDate(SUCCESS, "2009-10-28 01:59:01", "|Date:|YYYY-MM-DD |Time:|hh12:mm:ss", "Date:2009-10-28 Time:01:59:01");
    testDate(SUCCESS, "15:59:59", "h12| o'clock| a", Locale.CANADA, "3 o'clock PM");
    //inside escaped regions, the tokens are uninterpreted :
    testDate(SUCCESS, "2009-10-28 01:59:01", "|Date(YYYY-MM-DD):|YYYY-MM-DD |Timehh12:mm:ss:|hh12:mm:ss", "Date(YYYY-MM-DD):2009-10-28 Timehh12:mm:ss:01:59:01");
  }

  public void testCustomFormats() {
    List<String> months = Arrays.asList("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D");
    List<String> weekdays = Arrays.asList("sunday", "monday", "tuesday", "humpday", "thursday", "friday", "saturday");
    List<String> amPm = Arrays.asList("am", "pm");
    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MM-DD hh:mm:ss a", months, weekdays, amPm, "2009-10-28 01:59:01 am");
    testDate(SUCCESS, "2009-10-28 16:59:01", "YYYY-MM-DD h12:mm:ss a", months, weekdays, amPm, "2009-10-28 4:59:01 pm");
    
    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MMMM-DD hh:mm:ss a", months, weekdays, amPm, "2009-O-28 01:59:01 am");
    testDate(SUCCESS, "2009-10-28 01:59:01", "YYYY-MM-DD WWWW hh:mm:ss a", months, weekdays, amPm, "2009-10-28 humpday 01:59:01 am");
  }
  
  // PRIVATE
  
  private static final boolean SUCCESS = true;
  
  private void testDate(boolean aSuccess, String aDate , String aFormat, String aExpectedResult){
    DateTimeParser parser = new DateTimeParser();
    DateTime dateTime = parser.parse(aDate);
    DateTimeFormatter formatter = new DateTimeFormatter(aFormat);
    String result = formatter.format(dateTime);
    if(aSuccess){
      if(! result.equals(aExpectedResult)){
        throw new AssertionError("Expected:" + aExpectedResult + ", but result was:" + result);
      }
    }
    else {
      assertFalse(result.equals(aExpectedResult));
    }
  }
  
  private void testDate(boolean aSuccess, String aDate , String aFormat, Locale aLocale, String aExpectedResult){
    DateTimeParser parser = new DateTimeParser();
    DateTime dateTime = parser.parse(aDate);
    DateTimeFormatter formatter = new DateTimeFormatter(aFormat, aLocale);
    String result = formatter.format(dateTime);
    if(aSuccess){
      if(! result.equals(aExpectedResult)){
        throw new AssertionError("Expected:" + aExpectedResult + ", but result was:" + result);
      }
    }
    else {
      assertFalse(result.equals(aExpectedResult));
    }
  }
  
  private void testDate(boolean aSuccess, String aDate , String aFormat, List<String> aMonths, List<String> aWeekdays, List<String> aAmPm,  String aExpectedResult){
    DateTimeParser parser = new DateTimeParser();
    DateTime dateTime = parser.parse(aDate);
    DateTimeFormatter formatter = new DateTimeFormatter(aFormat, aMonths, aWeekdays, aAmPm);
    String result = formatter.format(dateTime);
    if(aSuccess){
      if(! result.equals(aExpectedResult)){
        throw new AssertionError("Expected:" + aExpectedResult + ", but result was:" + result);
      }
    }
    else {
      assertFalse(result.equals(aExpectedResult));
    }
  }
}
