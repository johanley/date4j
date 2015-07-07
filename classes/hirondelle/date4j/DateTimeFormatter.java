package hirondelle.date4j;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Formats a {@link DateTime}, and implements {@link DateTime#format(String)}.
 
 <P>This class defines a mini-language for defining how a {@link DateTime} is formatted.
 See {@link DateTime#format(String)} for details regarding the formatting mini-language.

 <P>The DateFormatSymbols class might be used to grab the locale-specific text, but the arrays it 
 returns are wonky and weird, so I have avoided it.
*/
final class DateTimeFormatter {

  /**
   Constructor used for patterns that represent date-time elements using only numbers, and no localizable text. 
   @param aFormat uses the syntax described by {@link DateTime#format(String)}.
   */
  DateTimeFormatter(String aFormat){
    fFormat = aFormat;
    fLocale = null;
    fCustomLocalization = null;
    validateState();
  }

  /**
   Constructor used for patterns that represent date-time elements using not only numbers, but text as well. 
   The text needs to be localizable.
   @param aFormat uses the syntax described by {@link DateTime#format(String)}.
   @param aLocale used to generate text for Month, Weekday, and AM-PM indicator; required only by patterns which return localized 
   text, instead of numeric forms for date-time elements.
   */
  DateTimeFormatter(String aFormat, Locale aLocale){
    fFormat = aFormat;
    fLocale = aLocale;
    fCustomLocalization = null;
    validateState();
  }

  /**
   Constructor used for patterns that represent using not only numbers, but customized text as well. 
   
   <P>This constructor exists mostly since SimpleDateFormat doesn't support all locales, and it has a 
   policy of N letters for text, where N != 3. 

   @param aFormat must match the syntax described by {@link DateTime#format(String)}.
   @param aMonths contains text for all 12 months, starting with January; size must be 12. 
   @param aWeekdays contains text for all 7 weekdays, starting with Sunday; size must be 7. 
   @param aAmPmIndicators contains text for A.M and P.M. indicators (in that order); size must be 2. 
   */
  DateTimeFormatter(String aFormat, List<String> aMonths, List<String> aWeekdays, List<String> aAmPmIndicators){
    fFormat = aFormat;
    fLocale = null;
    fCustomLocalization = new CustomLocalization(aMonths, aWeekdays, aAmPmIndicators);
    validateState();
  }
  
  /**  Format a {@link DateTime}.  */
  String format(DateTime aDateTime){
    fEscapedRanges = new ArrayList<EscapedRange>();
    fInterpretedRanges = new ArrayList<InterpretedRange>();
    findEscapedRanges();
    interpretInput(aDateTime);
    return produceFinalOutput();
  }
  
  // PRIVATE 
  private final String fFormat;
  private final Locale fLocale;
  private Collection<InterpretedRange> fInterpretedRanges;
  private Collection<EscapedRange> fEscapedRanges;
  
   /** 
    Table mapping a Locale to the names of the months.
    Initially empty, populated only when a specific Locale is needed for presenting such text. 
    Used for MMMM and MMM tokens.  
  */
  private final Map<Locale, List<String>> fMonths = new LinkedHashMap<Locale, List<String>>();
   
   /** 
   Table mapping a Locale to the names of the weekdays.
   Initially empty, populated only when a specific Locale is needed for presenting such text. 
   Used for WWWW and WWW tokens.  
  */
  private final Map<Locale, List<String>> fWeekdays = new LinkedHashMap<Locale, List<String>>();
   
   /**
    Table mapping a Locale to the text used to indicate a.m. and p.m. 
    Initially empty, populated only when a specific Locale is needed for presenting such text. 
    Used for the 'a' token.  
  */
  private final Map<Locale, List<String>> fAmPm = new LinkedHashMap<Locale, List<String>>();
  
  private final CustomLocalization fCustomLocalization;
  
  private final class CustomLocalization{
    CustomLocalization(List<String> aMonths, List<String> aWeekdays, List<String> aAmPm){
      if(aMonths.size() != 12){
        throw new IllegalArgumentException("Your List of custom months must have size 12, but its size is " + aMonths.size());
      }
      if(aWeekdays.size() != 7){
        throw new IllegalArgumentException("Your List of custom weekdays must have size 7, but its size is " + aWeekdays.size());
      }
      if(aAmPm.size() != 2){
        throw new IllegalArgumentException("Your List of custom a.m./p.m. indicators must have size 2, but its size is " + aAmPm.size());
      }
      Months = aMonths;
      Weekdays = aWeekdays;
      AmPmIndicators = aAmPm;
    }
    List<String> Months;
    List<String> Weekdays;
    List<String> AmPmIndicators;
  }

  /** A section of fFormat containing a token that must be interpreted. */ 
  private static final class InterpretedRange {
    int Start; 
    int End;
    String Text;
    @Override public String toString(){ return "Start:" + Start + " End:" + End + " '" + Text + "'";};
  }
  
  /** A section of fFormat bounded by a pair of escape characters; such ranges contain uninterpreted text. */
  private static final class EscapedRange {
    int Start;
    int End;
  }

  /** Special character used to escape the interpretation of parts of fFormat. */
  private static final String ESCAPE_CHAR = "|";
  private static final Pattern ESCAPED_RANGE = Pattern.compile("\\|[^\\|]*\\|");
  
  /* Here, 'token' means an item in the mini-language, having special meaning (defined below). */ 
  
  //all date-related tokens are in upper case
  private static final String YYYY = "YYYY";
  private static final String YY = "YY";
  private static final String M = "M";
  private static final String MM = "MM";
  private static final String MMM = "MMM";
  private static final String MMMM = "MMMM";
  private static final String D = "D";
  private static final String DD = "DD";
  private static final String WWW = "WWW";
  private static final String WWWW = "WWWW";
  
  //all time-related tokens are in lower case
  private static final String hh = "hh";
  private static final String h = "h";
  private static final String m = "m";
  private static final String mm = "mm";
  private static final String s = "s";
  private static final String ss = "ss";
  
  /**
   The 12-hour clock style.
    
   12:00 am is midnight, 12:30am is 30 minutes past midnight, 12:00 pm is 12 noon.
   This item is almost always used with 'a' to indicate am/pm.  
  */
  private static final String h12 = "h12";
  
  /** As {@link #h12}, but with leading zero. */ 
  private static final String hh12 = "hh12";

  private static final int AM = 0; //a.m. comes first in lists used by this class 
  private static final int PM = 1;
  
  /**
   A.M./P.M. text is sensitive to Locale, in the same way that names of months and weekdays are 
   sensitive to Locale.  
  */
  private static final String a = "a"; 
  
  private static final Pattern FRACTIONALS = Pattern.compile("f{1,9}");
  
  private static final String EMPTY_STRING = "";
  
  /** 
   The order of these items is significant, and is critical for how fFormat is interpreted.
   The 'longer' tokens must come first, in any group of related tokens. 
  */
  private static final List<String> TOKENS = new ArrayList<String>();
  static {
    TOKENS.add(YYYY);
    TOKENS.add(YY);
    TOKENS.add(MMMM);
    TOKENS.add(MMM);
    TOKENS.add(MM);
    TOKENS.add(M);
    TOKENS.add(DD);
    TOKENS.add(D);
    TOKENS.add(WWWW);
    TOKENS.add(WWW);
    TOKENS.add(hh12);
    TOKENS.add(h12);
    TOKENS.add(hh);
    TOKENS.add(h);
    TOKENS.add(mm);
    TOKENS.add(m);
    TOKENS.add(ss);
    TOKENS.add(s);
    TOKENS.add(a);
    //should these be constants too?
    TOKENS.add("fffffffff");
    TOKENS.add("ffffffff");
    TOKENS.add("fffffff");
    TOKENS.add("ffffff");
    TOKENS.add("fffff");
    TOKENS.add("ffff");
    TOKENS.add("fff");
    TOKENS.add("ff");
    TOKENS.add("f");
  }

  /** Escaped ranges are bounded by a PAIR of {@link #ESCAPE_CHAR} characters. */
  private void findEscapedRanges(){
    Matcher matcher = ESCAPED_RANGE.matcher(fFormat);
    while (matcher.find()){
      EscapedRange escapedRange = new EscapedRange();
      escapedRange.Start = matcher.start(); //first pipe
      escapedRange.End = matcher.end() - 1; //second pipe
      fEscapedRanges.add(escapedRange);
    }
  }
  
  /** Return true only if the start of the interpreted range is in an escaped range. */ 
  private boolean isInEscapedRange(InterpretedRange aInterpretedRange){
    boolean result = false; //innocent till shown guilty
    for(EscapedRange escapedRange : fEscapedRanges){
      //checking only the start is sufficient, because the tokens never contain the escape char
      if(escapedRange.Start <= aInterpretedRange.Start && aInterpretedRange.Start <= escapedRange.End ){
        result = true;
        break;
      }
    }
    return result;
  }
  
  /** 
   Scan fFormat for all tokens, in a specific order, and interpret them with the given DateTime. 
   The interpreted tokens are saved for output later. 
  */
  private void interpretInput(DateTime aDateTime){
    String format = fFormat;
    for(String token : TOKENS){
      Pattern pattern = Pattern.compile(token);
      Matcher matcher = pattern.matcher(format);
      while(matcher.find()){
        InterpretedRange interpretedRange = new InterpretedRange();
        interpretedRange.Start = matcher.start();
        interpretedRange.End = matcher.end() - 1;
        if(! isInEscapedRange(interpretedRange)){
          interpretedRange.Text = interpretThe(matcher.group(), aDateTime);
          fInterpretedRanges.add(interpretedRange);
        }
      }
      format = format.replace(token, withCharDenotingAlreadyInterpreted(token));
    }
  }
  
  /** 
   Return a temp placeholder string used to identify sections of fFormat that have already been interpreted.
   The returned string is a list of "@" characters, whose length is the same as aToken. 
  */
  private String withCharDenotingAlreadyInterpreted(String aToken){
    StringBuilder result = new StringBuilder();
    for(int idx = 1; idx <= aToken.length(); ++idx){
      //any character that isn't interpreted, or a special regex char, will do here
      //the fact that it's interpreted at location x is stored elsewhere;
      //this is meant only to prevent multiple interpretations of the same text
      result.append("@");
    }
    return result.toString();
  }

  /** Render the final output returned to the caller. */
  private String produceFinalOutput(){
    StringBuilder result = new StringBuilder();
    int idx = 0;
    while ( idx < fFormat.length() ) {
      String letter = nextLetter(idx);
      InterpretedRange interpretation = getInterpretation(idx);
      if  (interpretation != null){
        result.append(interpretation.Text);
        idx = interpretation.End;
      }
      else {
        if(!ESCAPE_CHAR.equals(letter)){
          result.append(letter);
        }
      }
      ++idx;
    }
    return result.toString();
  }

  private InterpretedRange getInterpretation(int aIdx){
    InterpretedRange result = null;
    for(InterpretedRange interpretedRange : fInterpretedRanges){
      if(interpretedRange.Start == aIdx ){
        result = interpretedRange;
      }
    }
    return result;
  }

  private String nextLetter(int aIdx){
    return fFormat.substring(aIdx, aIdx+1); 
  }
  
  private String  interpretThe(String aCurrentToken, DateTime aDateTime){
    String result = EMPTY_STRING;
    if(YYYY.equals(aCurrentToken)) {
      result =  valueStr(aDateTime.getYear());
    }
    else if (YY.equals(aCurrentToken)){
      result = noCentury(valueStr(aDateTime.getYear()));
    }
    else if (MMMM.equals(aCurrentToken)){
      int month = aDateTime.getMonth();
      result = fullMonth(month);
    }
    else if (MMM.equals(aCurrentToken)){
      int month = aDateTime.getMonth();
      result = firstThreeChars(fullMonth(month));
    }
    else if (MM.equals(aCurrentToken)){
      result = addLeadingZero(valueStr(aDateTime.getMonth()));
    }
    else if (M.equals(aCurrentToken)){
      result = valueStr(aDateTime.getMonth());
    }
    else if(DD.equals(aCurrentToken)){
      result = addLeadingZero(valueStr(aDateTime.getDay()));
    }
    else if(D.equals(aCurrentToken)){
      result = valueStr(aDateTime.getDay());
    }
    else if(WWWW.equals(aCurrentToken)){
      int weekday = aDateTime.getWeekDay();
      result = fullWeekday(weekday);
    }
    else if(WWW.equals(aCurrentToken)){
      int weekday = aDateTime.getWeekDay();
      result = firstThreeChars(fullWeekday(weekday));
    }
    else if(hh.equals(aCurrentToken)){
      result = addLeadingZero(valueStr(aDateTime.getHour()));
    }
    else if(h.equals(aCurrentToken)){
      result = valueStr(aDateTime.getHour());
    }
    else if (h12.equals(aCurrentToken)){
      result = valueStr(twelveHourStyle(aDateTime.getHour()));
    }
    else if (hh12.equals(aCurrentToken)){
      result = addLeadingZero(valueStr(twelveHourStyle(aDateTime.getHour())));
    }
    else if (a.equals(aCurrentToken)){
      int hour = aDateTime.getHour();
      result = amPmIndicator(hour);
    }
    else if(mm.equals(aCurrentToken)){
      result = addLeadingZero(valueStr(aDateTime.getMinute()));
    }
    else if(m.equals(aCurrentToken)){
      result = valueStr(aDateTime.getMinute());
    }
    else if(ss.equals(aCurrentToken)){
      result = addLeadingZero(valueStr(aDateTime.getSecond()));
    }
    else if(s.equals(aCurrentToken)){
      result = valueStr(aDateTime.getSecond());
    }
    else if(aCurrentToken.startsWith("f")){
      Matcher matcher = FRACTIONALS.matcher(aCurrentToken);
      if ( matcher.matches() ) {
        String nanos = nanosWithLeadingZeroes(aDateTime.getNanoseconds());
        int numDecimalsToShow = aCurrentToken.length();
        result = firstNChars(nanos, numDecimalsToShow);
      }
      else {
        throw new IllegalArgumentException("Unknown token in date formatting pattern: " + aCurrentToken);
      }
    }
    else {
      throw new IllegalArgumentException("Unknown token in date formatting pattern: " + aCurrentToken);
    }
    
    return result;
  }
  
  private String valueStr(Object aItem){
    String result = EMPTY_STRING;
    if(aItem != null){
      result = String.valueOf(aItem);
    }
    return result;
  }

  private String noCentury(String aItem){
    String result = EMPTY_STRING;
    if(Util.textHasContent(aItem)){
      result = aItem.substring(2);
    }
    return result;
  }
  
  private String nanosWithLeadingZeroes(Integer aNanos){
    String result = valueStr(aNanos);
    while(result.length() < 9){
      result = "0" + result;
    }
    return result;
  }
  
  
  /** Pad 0..9 with a leading zero. */
  private String addLeadingZero(String aTimePart){
    String result = aTimePart;
    if(Util.textHasContent(aTimePart) && aTimePart.length() ==1){
      result = "0" + result;
    }
    return result;
  }
  
  private String firstThreeChars(String aText){
    String result = aText;
    if(Util.textHasContent(aText) && aText.length()>=3){
      result = aText.substring(0,3);
    }
    return result;
  }
  
  private String fullMonth(Integer aMonth){
    String result = "";
    if(aMonth != null){
      if(fCustomLocalization != null){
        result = lookupCustomMonthFor(aMonth);
      }
      else if (fLocale != null){
        result = lookupMonthFor(aMonth);
      }
      else {
        throw new IllegalArgumentException("Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat)) ;
      }
    }
    return result;
  }

  private String lookupCustomMonthFor(Integer aMonth){
    return fCustomLocalization.Months.get(aMonth-1);
  }
  
  private String lookupMonthFor(Integer aMonth){
    String result = EMPTY_STRING;
    if (! fMonths.containsKey(fLocale) ){
      List<String> months = new ArrayList<String>();
      SimpleDateFormat format = new SimpleDateFormat("MMMM", fLocale);
      for(int idx = Calendar.JANUARY; idx <= Calendar.DECEMBER; ++idx){
        Calendar firstDayOfMonth = new GregorianCalendar();
        firstDayOfMonth.set(Calendar.YEAR, 2000);
        firstDayOfMonth.set(Calendar.MONTH, idx);
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 15);
        String monthText = format.format(firstDayOfMonth.getTime());
        months.add(monthText);
      }
      fMonths.put(fLocale, months);
    }
    result = fMonths.get(fLocale).get(aMonth-1); //list is 0-based
    return result;
  }
  
  private String fullWeekday(Integer aWeekday){
    String result = "";
    if(aWeekday != null){
      if (fCustomLocalization != null){
        result = lookupCustomWeekdayFor(aWeekday);
      }
      else if(fLocale != null ){
        result = lookupWeekdayFor(aWeekday);
      }
      else {
        throw new IllegalArgumentException("Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat)) ;
      }
    }
    return result;
  }

  private String lookupCustomWeekdayFor(Integer aWeekday){
    return fCustomLocalization.Weekdays.get(aWeekday-1);
  }
  
  private String lookupWeekdayFor(Integer aWeekday){
    String result = EMPTY_STRING;
    if (! fWeekdays.containsKey(fLocale) ){
      List<String> weekdays = new ArrayList<String>();
      SimpleDateFormat format = new SimpleDateFormat("EEEE", fLocale);
      //Feb 8, 2009..Feb 14, 2009 runs Sun..Sat
      for(int idx = 8; idx <= 14; ++idx){
        Calendar firstDayOfWeek = new GregorianCalendar();
        firstDayOfWeek.set(Calendar.YEAR, 2009);
        firstDayOfWeek.set(Calendar.MONTH, 1); //month is 0-based
        firstDayOfWeek.set(Calendar.DAY_OF_MONTH, idx);
        String weekdayText = format.format(firstDayOfWeek.getTime());
        weekdays.add(weekdayText);
      }
      fWeekdays.put(fLocale, weekdays);
    }
    result = fWeekdays.get(fLocale).get(aWeekday-1); //list is 0-based
    return result;
  }

  private String firstNChars(String aText, int aN){
    String result = aText;
    if(Util.textHasContent(aText) && aText.length()>=aN){
      result = aText.substring(0,aN);
    }
    return result;
  }

  /**  Coerce the hour to match the number used in the 12-hour style. */
  private Integer twelveHourStyle(Integer aHour){
    Integer result = aHour;
    if(aHour != null){
      if (aHour == 0) {
        result = 12; //eg 12:30 am
      }
      else if (aHour > 12){
        result = aHour - 12; //eg 14:00 -> 2:00
      }
    }
    return result;
  }
  
  private String amPmIndicator(Integer aHour){
    String result = "";
    if(aHour != null){
      if(fCustomLocalization != null){
        result = lookupCustomAmPmFor(aHour);
      }
      else if (fLocale != null) {
        result = lookupAmPmFor(aHour);
      }
      else {
        throw new IllegalArgumentException(
          "Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat)
        ) ;
      }
    }
    return result;
  }
    
  private String lookupCustomAmPmFor(Integer aHour){
    String result = EMPTY_STRING;
    if(aHour < 12 ){
      result = fCustomLocalization.AmPmIndicators.get(AM);
    }
    else {
      result = fCustomLocalization.AmPmIndicators.get(PM);
    }
    return result;
  }
    
  private String lookupAmPmFor(Integer aHour){
    String result = EMPTY_STRING;
    if (! fAmPm.containsKey(fLocale) ){
      List<String> indicators = new ArrayList<String>();
      indicators.add(getAmPmTextFor(6));
      indicators.add(getAmPmTextFor(18));
      fAmPm.put(fLocale, indicators);
    }
    if (aHour < 12 ){
      result = fAmPm.get(fLocale).get(AM); 
    }
    else {
      result = fAmPm.get(fLocale).get(PM); 
    }
    return result;
  }
  
  private String getAmPmTextFor(Integer aHour){
    SimpleDateFormat format = new SimpleDateFormat("a", fLocale);
    Calendar someDay = new GregorianCalendar();
    someDay.set(Calendar.YEAR, 2000);
    someDay.set(Calendar.MONTH, 6);
    someDay.set(Calendar.DAY_OF_MONTH, 15);
    someDay.set(Calendar.HOUR_OF_DAY, aHour);
    return format.format(someDay.getTime());
  }
  
  private void validateState(){
    if(! Util.textHasContent(fFormat)){  
      throw new IllegalArgumentException("DateTime format has no content.");
    }
  }
}
