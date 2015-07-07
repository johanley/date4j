package hirondelle.date4j;

import hirondelle.date4j.DateTime.DayOverflow;
import hirondelle.date4j.DateTime.Unit;

/**
 Helper class for adding intervals of time. 
 The mental model of this class is similar to that of a car's odometer, except
 in reverse. 
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
    fDayOverflow = aMonthOverflow;
  }
  
  DateTime plus(int aYear, int aMonth, int aDay, int aHour, int aMinute, int aSecond){
    return plusOrMinus(PLUS, aYear, aMonth, aDay, aHour, aMinute, aSecond);
  }
  
  DateTime minus(int aYear, int aMonth, int aDay, int aHour, int aMinute, int aSecond){
    return plusOrMinus(MINUS, aYear, aMonth, aDay, aHour, aMinute, aSecond);
  }
  
  // PRIVATE 
  private final DateTime fFrom;
  private boolean fIsPlus;
  private DateTime.DayOverflow fDayOverflow;
  
  private int fYearIncr;
  private int fMonthIncr;
  private int  fDayIncr;
  private int fHourIncr;
  private int fMinuteIncr;
  private int fSecondIncr;

  private Integer fYear;
  private Integer fMonth;
  private Integer fDay;
  private Integer fHour;
  private Integer fMinute;
  private Integer fSecond;

  private static final int MIN = 0;
  private static final int MAX = 9999;
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
  
  private DateTime plusOrMinus(boolean aIsPlus, Integer aYear, Integer aMonth, Integer aDay, Integer aHour, Integer aMinute, Integer aSecond){
    fIsPlus = aIsPlus;
    fYearIncr = aYear;
    fMonthIncr = aMonth;
    fDayIncr = aDay;
    fHourIncr = aHour;
    fMinuteIncr = aMinute;
    fSecondIncr = aSecond;
    
    checkRange(fYearIncr, "Year");
    checkRange(fMonthIncr, "Month");
    checkRange(fDayIncr, "Day");
    checkRange(fHourIncr, "Hour");
    checkRange(fMinuteIncr, "Minute");
    checkRange(fSecondIncr, "Second");
    
    changeYear();
    changeMonth();
    handleMonthOverflow();
    changeDay();
    changeHour();
    changeMinute();
    changeSecond();
    
    return new DateTime(fYear, fMonth, fDay, fHour, fMinute, fSecond, fFrom.getNanoseconds());
  }

  private void checkRange(Integer aValue, String aName) {
    if ( aValue <  MIN || aValue > MAX ) { 
      throw new IllegalArgumentException(aName + " is not in the range " + MIN + ".." + MAX); 
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
      count++;
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
