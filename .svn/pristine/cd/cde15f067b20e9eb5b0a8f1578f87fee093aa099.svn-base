package hirondelle.date4j;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Run all JUnit tests. */
public final class TESTAll {

  public static void main(String args[]) {
    String[] testCaseName = { TESTAll.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
 }
  
  public static Test suite ( ) {
    TestSuite suite= new TestSuite("All JUnit Tests");

    suite.addTest(new TestSuite(TESTDateTime.class));
    suite.addTest(new TestSuite(TESTDateTimeFormatter.class));
    suite.addTest(new TestSuite(TESTDateTimeInterval.class));
    
    return suite;
  }
}
