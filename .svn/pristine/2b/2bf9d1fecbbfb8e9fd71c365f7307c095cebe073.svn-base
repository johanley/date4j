package hirondelle.date4j;

import hirondelle.date4j.DateTime.DayOverflow;
import hirondelle.date4j.DateTime.Unit;

/**
 Helper class for adding intervals of time. 
 The mental model of this class is similar to that of a car's odometer.
*/
final class DateTimeInterval {

  /**  Constructor.  */
  DateTimeInterval(DateTime aFrom, DayOverflow aMonthOverflow){
    fFrom = aFrom;
    checkUnits();
    fYear = fFrom.getYear() == null ? 1 : fFrom.getYear();
    fMonth = fFrom.getMonth() == null ? 1 : fFrom.getMonth();
    fDay = fFrom.getDay() == null ? 1 : fFrom.getDay();
    fHour = fFrom.getHour() == null ? 0 : fFrom.getHour();
    fMinute = fFrom.getMinute() == null ? 0 : fFrom.getMinute();
    fSecond = fFrom.getSecond() == null ? 0 : fFrom.getSecond();
    fNanosecond = fFrom.getNanoseconds() == null ? 0 : fFrom.getNanoseconds();
    fDayOverflow = aMonthOverflow;
  }
  
  DateTime plus(int aYear, int aMonth, int aDay, int aHour, int aMinute, int aSecond, int aNanosecond){
    return plusOrMinus(PLUS, aYear, aMonth, aDay, aHour, aMinute, aSecond, aNanosecond);
  }
  
  DateTime minus(int aYear, int aMonth, int aDay, int aHour, int aMinute, int aSecond, int aNanosecond){
    return plusOrMinus(MINUS, aYear, aMonth, aDay, aHour, aMinute, aSecond, aNanosecond);
  }
  
  // PRIVATE 
  
  //the base date to which the interval is calculated
  private final DateTime fFrom;
  
  private boolean fIsPlus;
  private DateTime.DayOverflow fDayOverflow;
  
  //the various increments
  private int fYearIncr;
  private int fMonthIncr;
  private int  fDayIncr;
  private int fHourIncr;
  private int fMinuteIncr;
  private int fSecondIncr;
  private int fNanosecondIncr;

  //work area for the final result - starts off with values from base date fFrom
  private Integer fYear;
  private Integer fMonth;
  private Integer fDay;
  private Integer fHour;
  private Integer fMinute;
  private Integer fSecond;
  private Integer fNanosecond;

  private static final int MIN = 0;
  private static final int MAX = 9999;
  private static final int MIN_NANOS = 0;
  private static final int MAX_NANOS = 999999999;
  private static final boolean PLUS = true;
  private static final boolean MINUS = false;

  private void checkUnits(){
    boolean success = false;
    if(fFrom.unitsAllPresent(Unit.YEAR, Unit.MONTH, Unit.DAY, Unit.HOUR, Unit.MINUTE, Unit.SECOND) ){
      success = true;
    }
    else if( fFrom.unitsAllPresent(Unit.YEAR, Unit.MONTH, Unit.DAY) &&  fFrom.unitsAllAbsent(Unit.HOUR, Unit.MINUTE, Unit.SECOND) ){
      success = true;
    }
    else if ( fFrom.unitsAllAbsent(Unit.YEAR, Unit.MONTH, Unit.DAY) && fFrom.unitsAllPresent(Unit.HOUR, Unit.MINUTE, Unit.SECOND) ){
      success = true;
    }
    else {
      success = false;
    }
    if(! success ){
      throw new IllegalArgumentException("For interval calculations, DateTime must have year-month-day, or hour-minute-second, or both.");
    }
  }
  
  private DateTime plusOrMinus(boolean aIsPlus, Integer aYear, Integer aMonth, Integer aDay, Integer aHour, Integer aMinute, Integer aSecond, Integer aNanosecond){
    fIsPlus = aIsPlus;
    fYearIncr = aYear;
    fMonthIncr = aMonth;
    fDayIncr = aDay;
    fHourIncr = aHour;
    fMinuteIncr = aMinute;
    fSecondIncr = aSecond;
    fNanosecondIncr = aNanosecond;
    
    checkRange(fYearIncr, "Year");
    checkRange(fMonthIncr, "Month");
    checkRange(fDayIncr, "Day");
    checkRange(fHourIncr, "Hour");
    checkRange(fMinuteIncr, "Minute");
    checkRange(fSecondIncr, "Second");
    checkRangeNanos(fNanosecondIncr);
    
    changeYear();
    changeMonth();
    handleMonthOverflow();
    changeDay();
    changeHour();
    changeMinute();
    changeSecond();
    changeNanosecond();
    
    return new DateTime(fYear, fMonth, fDay, fHour, fMinute, fSecond, fNanosecond);
  }

  private void checkRange(Integer aValue, String aName) {
    if ( aValue <  MIN || aValue > MAX ) { 
      throw new IllegalArgumentException(aName + " is not in the range " + MIN + ".." + MAX); 
    }
  }
  
  private void checkRangeNanos(Integer aValue) {
    if ( aValue <  MIN_NANOS || aValue > MAX_NANOS ) { 
      throw new IllegalArgumentException("Nanosecond interval is not in the range " + MIN_NANOS + ".." + MAX_NANOS); 
    }
  }
  
  private void changeYear(){
    if(fIsPlus){
      fYear = fYear + fYearIncr;
    }
    else {
      fYear = fFrom.getYear() - fYearIncr;
    }
    //the DateTime ctor will check the range of the year 
  }
  
  private void changeMonth(){
    int count = 0;
    while (count < fMonthIncr){
      stepMonth();
      count++;
    }
  }

  private void  changeDay(){
    int count = 0;
    while (count < fDayIncr){
      stepDay();
      count++;
    }
  }

  private  void changeHour(){
    int count = 0;
    while (count < fHourIncr){
      stepHour();
      count++;
    }
  }

  private void changeMinute(){
    int count = 0;
    while (count < fMinuteIncr){
      stepMinute();
      count++;
    }
  }
  
  private void changeSecond(){
    int count = 0;
    while (count < fSecondIncr){
      stepSecond();
      count++;
    }
  }

  /** 
   Nanos are different from other items. They don't cycle one step at a time.
   They are just added. If they under/over flow, then extra math is performed.
   They don't over/under by more than 1 second, since the size of the increment is limited.
  */ 
  private void changeNanosecond(){
    if (fIsPlus){
      fNanosecond = fNanosecond + fNanosecondIncr;      
    }
    else {
      fNanosecond = fNanosecond - fNanosecondIncr;      
    }
    if(fNanosecond > MAX_NANOS){
      stepSecond();
      fNanosecond = fNanosecond - MAX_NANOS - 1;
    }
    else if (fNanosecond < MIN_NANOS){
      stepSecond();
      fNanosecond =  MAX_NANOS + fNanosecond + 1;
    }
  }
  
  private void stepYear() {
    if(fIsPlus) {
      fYear = fYear + 1;
    }
    else {
      fYear = fYear - 1;
    }
  }
  
  private void stepMonth() {
    if(fIsPlus){
      fMonth = fMonth + 1;
    }
    else {
      fMonth = fMonth - 1;
    }
    if(fMonth > 12) { 
      fMonth = 1;
      stepYear();
    }
    else if(fMonth < 1){
      fMonth = 12;
      stepYear();
    }
  }

  private void stepDay() {
    if(fIsPlus){
      fDay = fDay + 1;
    }
    else {
      fDay = fDay - 1;
    }
    if(fDay > numDaysInMonth()){
      fDay = 1;
      stepMonth();
    }
    else if (fDay < 1){
      fDay = numDaysInPreviousMonth();
      stepMonth();
    }
  }
  
  private int numDaysInMonth(){
    return DateTime.getNumDaysInMonth(fYear, fMonth);
  }
  
  private int numDaysInPreviousMonth(){
    int result = 0;
    if(fMonth > 1) {
      result = DateTime.getNumDaysInMonth(fYear, fMonth - 1);
    }
    else {
      result = DateTime.getNumDaysInMonth(fYear - 1 , 12);
    }
    return result;
  }
  
  private void stepHour() {
    if(fIsPlus){
      fHour = fHour + 1;      
    }
    else {
      fHour = fHour - 1;      
    }
    if(fHour > 23){
      fHour = 0;
      stepDay();
    }
    else if (fHour < 0){
      fHour = 23;
      stepDay();
    }
  }
  
  private void stepMinute() {
    if(fIsPlus){
      fMinute = fMinute + 1;      
    }
    else {
      fMinute = fMinute - 1;      
    }
    if(fMinute > 59){
      fMinute = 0;
      stepHour();
    }
    else if (fMinute < 0){
      fMinute = 59;
      stepHour();
    }
  }
  
  private void stepSecond() {
    if(fIsPlus){
      fSecond = fSecond + 1;        
    }
    else {
      fSecond = fSecond - 1;        
    }
    if (fSecond > 59){
      fSecond = 0;
      stepMinute();
    }
    else if (fSecond < 0){
      fSecond = 59;
      stepMinute();
    }
  }
  
  private void handleMonthOverflow(){
    int daysInMonth = numDaysInMonth();
    if( fDay > daysInMonth ){
      if(DayOverflow.Abort == fDayOverflow) {
        throw new RuntimeException(
          "Day Overflow: Year:" + fYear + " Month:" + fMonth + " has " + daysInMonth + " days, but day has value:" + fDay + 
          " To avoid these exceptions, please specify a different DayOverflow policy."
        );
      }
      else if (DayOverflow.FirstDay == fDayOverflow) {
        fDay = 1;
        stepMonth();
      }
      else if (DayOverflow.LastDay == fDayOverflow) {
        fDay = daysInMonth;
      }
      else if (DayOverflow.Spillover == fDayOverflow) {
        int overflowAmount = fDay - daysInMonth;
        fDay = overflowAmount;
        stepMonth();
      }
    }
  }

}
