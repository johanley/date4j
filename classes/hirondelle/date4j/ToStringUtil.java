package hirondelle.date4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Implements the <tt>toString</tt> method for some common cases.

 <P>This class is intended only for cases where <tt>toString</tt> is used in
 an informal manner (usually for logging and stack traces). It is especially
 suited for <tt>public</tt> classes which model domain objects.

 Here is an example of a return value of the {@link #getText} method :
 <PRE>
hirondelle.web4j.model.MyUser {
LoginName: Bob
LoginPassword: ****
EmailAddress: bob@blah.com
StarRating: 1
FavoriteTheory: Quantum Chromodynamics
SendCard: true
Age: 42
DesiredSalary: 42000
BirthDate: Sat Feb 26 13:45:43 EST 2005
}
 </PRE>
 (Previous versions of this classes used indentation within the braces. That has 
 been removed, since it displays poorly when nesting occurs.)

 <P>Here are two more examples, using classes taken from the JDK :
 <PRE>
java.util.StringTokenizer {
nextElement: This
hasMoreElements: true
countTokens: 3
nextToken: is
hasMoreTokens: true
}

java.util.ArrayList {
size: 3
toArray: [blah, blah, blah]
isEmpty: false
}
 </PRE>

 There are two use cases for this class. The typical use case is :
 <PRE>
  public String toString() {
    return ToStringUtil.getText(this);
  }
 </PRE>
 
 <span class="highlight">However, there is a case where this typical style can
 fail catastrophically</span> : when two objects reference each other, and each 
 has <tt>toString</tt> implemented as above, then the program will loop 
 indefinitely!

 <P>As a remedy for this problem, the following variation is provided :
 <PRE>
  public String toString() {
    return ToStringUtil.getTextAvoidCyclicRefs(this, Product.class, "getId");
  }
 </PRE>
 Here, the usual behavior is overridden for any method 
 which returns a <tt>Product</tt> : instead of calling <tt>Product.toString</tt>, 
 the return value of <tt>Product.getId()</tt> is used to textually represent 
 the object.
*/
final class ToStringUtil {

  /**
   Return an informal textual description of an object. 
   <P>It is highly recommened that the caller <em>not</em> rely on details 
   of the returned <tt>String</tt>. See class description for examples of return 
   values.
  
   <P><span class="highlight">WARNING</span>: If two classes have cyclic references 
   (that is, each has a reference to the other), then infinite looping will result 
   if <em>both</em> call this method! To avoid this problem, use <tt>getText</tt>
   for one of the classes, and {@link #getTextAvoidCyclicRefs} for the other class.
  
   <P>The only items which contribute to the result are the class name, and all
   no-argument <tt>public</tt> methods which return a value. As well, methods
   defined by the <tt>Object</tt> class, and factory methods which return an 
   <tt>Object</tt> of the native class ("<tt>getInstance</tt>" methods) do not contribute.
    
   <P>Items are converted to a <tt>String</tt> simply by calling their 
   <tt>toString method</tt>, with these exceptions : 
   <ul>
   <li>{@link Util#getArrayAsString(Object)} is used for arrays
   <li>a method whose name contain the text <tt>"password"</tt> (not case-sensitive) have 
   their return values hard-coded to <tt>"****"</tt>. 
   </ul>
  
   <P>If the method name follows the pattern <tt>getXXX</tt>, then the word 'get'
   is removed from the presented result.
  
   @param aObject the object for which a <tt>toString</tt> result is required.
  */
  static String getText(Object aObject) {
    return getTextAvoidCyclicRefs(aObject, null, null);
  }

  /**
   As in {@link #getText}, but, for return values which are instances of 
   <tt>aSpecialClass</tt>, then call <tt>aMethodName</tt> instead of <tt>toString</tt>.
  
   <P> If <tt>aSpecialClass</tt> and <tt>aMethodName</tt> are <tt>null</tt>, then the 
   behavior is exactly the same as calling {@link #getText}.
  */
  static String getTextAvoidCyclicRefs(Object aObject, Class aSpecialClass, String aMethodName) {
    StringBuilder result = new StringBuilder();
    addStartLine(aObject, result);

    Method[] methods =  aObject.getClass().getDeclaredMethods();
    for(Method method: methods){
      if ( isContributingMethod(method, aObject.getClass()) ){
        addLineForGetXXXMethod(aObject, method, result, aSpecialClass, aMethodName);
      }
    }

    addEndLine(result);
    return result.toString();
  }
  
  // PRIVATE //

  /*
   Names of methods in the <tt>Object</tt> class which are ignored.
  */
  private static final String fGET_CLASS = "getClass";
  private static final String fCLONE = "clone";
  private static final String fHASH_CODE = "hashCode";
  private static final String fTO_STRING = "toString";
  
  private static final String fGET = "get";
  private static final Object[] fNO_ARGS = new Object[0];
  private static final Class[] fNO_PARAMS = new Class[0];
  /*
   Previous versions of this class indented the data within a block. 
   That style breaks when one object references another. The indentation
   has been removed, but this variable has been retained, since others might 
   prefer the indentation anyway.
  */
  private static final String fINDENT = "";
  private static final String fAVOID_CIRCULAR_REFERENCES = "[circular reference]";
  private static final Logger fLogger = Util.getLogger(ToStringUtil.class);
  private static final String NEW_LINE = System.getProperty("line.separator");

  private static Pattern PASSWORD_PATTERN = Pattern.compile("password", Pattern.CASE_INSENSITIVE);
  private static String HIDDEN_PASSWORD_VALUE = "****";

  //prevent construction by the caller
  private ToStringUtil() {
    //empty
  }
  
  private static void addStartLine(Object aObject, StringBuilder aResult){
    aResult.append( aObject.getClass().getName() );
    aResult.append(" {");
    aResult.append(NEW_LINE);
  }

  private static void addEndLine(StringBuilder aResult){
    aResult.append("}");
    aResult.append(NEW_LINE);
  }

  /**
   Return <tt>true</tt> only if <tt>aMethod</tt> is public, takes no args, 
   returns a value whose class is not the native class, is not a method of 
   <tt>Object</tt>.
  */
  private static boolean isContributingMethod(Method aMethod, Class aNativeClass){
    boolean isPublic = Modifier.isPublic( aMethod.getModifiers() );
    boolean hasNoArguments = aMethod.getParameterTypes().length == 0;
    boolean hasReturnValue = aMethod.getReturnType() != Void.TYPE;
    boolean returnsNativeObject = aMethod.getReturnType() == aNativeClass;
    boolean isMethodOfObjectClass = 
      aMethod.getName().equals(fCLONE) || 
      aMethod.getName().equals(fGET_CLASS) || 
      aMethod.getName().equals(fHASH_CODE) || 
      aMethod.getName().equals(fTO_STRING)
   ;
    return 
      isPublic && 
      hasNoArguments && 
      hasReturnValue && 
      ! isMethodOfObjectClass && 
      ! returnsNativeObject;
  }

  private static void addLineForGetXXXMethod(
    Object aObject,
    Method aMethod,
    StringBuilder aResult,
    Class aCircularRefClass, 
    String aCircularRefMethodName
  ){
    aResult.append(fINDENT);
    aResult.append( getMethodNameMinusGet(aMethod) );
    aResult.append(": ");
    Object returnValue = getMethodReturnValue(aObject, aMethod);
    if ( returnValue != null && returnValue.getClass().isArray() ) {
      aResult.append( Util.getArrayAsString(returnValue) );
    }
    else {
      if (aCircularRefClass == null) {
        aResult.append( returnValue );
      }
      else {
        if (aCircularRefClass == returnValue.getClass()) {
          Method method = getMethodFromName(aCircularRefClass, aCircularRefMethodName);
          if ( isContributingMethod(method, aCircularRefClass)){
            returnValue = getMethodReturnValue(returnValue, method);
            aResult.append( returnValue );
          }
          else {
            aResult.append(fAVOID_CIRCULAR_REFERENCES);
          }
        }
      }
    }
    aResult.append( NEW_LINE );
  }

  private static String getMethodNameMinusGet(Method aMethod){
    String result = aMethod.getName();
    if (result.startsWith(fGET) ) {
      result = result.substring(fGET.length());
    }
    return result;
  }

  /** Return value is possibly-null.  */
  private static Object getMethodReturnValue(Object aObject, Method aMethod){
    Object result = null;
    try {
      result = aMethod.invoke(aObject, fNO_ARGS);
    }
    catch (IllegalAccessException ex){
      vomit(aObject, aMethod);
    }
    catch (InvocationTargetException ex){
      vomit(aObject, aMethod);
    }
    result = dontShowPasswords(result, aMethod);
    return result;
  }
  
  private static Method getMethodFromName(Class aSpecialClass, String aMethodName){
    Method result = null;
    try {
      result = aSpecialClass.getMethod(aMethodName, fNO_PARAMS);
    }
    catch ( NoSuchMethodException ex){
      vomit(aSpecialClass, aMethodName);
    }
    return result;
  }
  

  private static void vomit(Object aObject, Method aMethod){
    fLogger.severe(
      "Cannot get return value using reflection. Class: " +
      aObject.getClass().getName() +
      " Method: " +
      aMethod.getName()
    );
  }
  
  private static void vomit(Class aSpecialClass, String aMethodName){
    fLogger.severe(
      "Reflection fails to get no-arg method named: " +
      Util.quote(aMethodName) +
      " for class: " +
      aSpecialClass.getName()
    );
  }
  
  private static Object dontShowPasswords(Object aReturnValue, Method aMethod){
    Object result = aReturnValue;
    Matcher matcher = PASSWORD_PATTERN.matcher(aMethod.getName());
    if ( matcher.find()) {
      result = HIDDEN_PASSWORD_VALUE;
    }
    return result;
  }
  
  /*
   Two informal classes with cyclic references, used for testing. 
  */
  private static final class Ping {
    public void setPong(Pong aPong){fPong = aPong; }
    public Pong getPong(){ return fPong; }
    public Integer getId() { return new Integer(123); }
    public String getUserPassword(){ return "blah"; }
    public String toString() {
      return getText(this);
    }
    private Pong fPong;
  }
  private static final class Pong {
    public void setPing(Ping aPing){ fPing = aPing; }
    public Ping getPing() { return fPing; }
    public String toString() {
      return getTextAvoidCyclicRefs(this, Ping.class, "getId");
      //to see the infinite looping, use this instead :
      //return getText(this);
    }
    private Ping fPing;
  }
  
  /**
   Informal test harness.
  */
  public static void main (String... args) {
    List<String> list = new ArrayList<String>();
    list.add("blah");
    list.add("blah");
    list.add("blah");
    System.out.println( ToStringUtil.getText(list) );
    
    StringTokenizer parser = new StringTokenizer("This is the end.");
    System.out.println( ToStringUtil.getText(parser) );
    
    Ping ping = new Ping();
    Pong pong = new Pong();
    ping.setPong(pong);
    pong.setPing(ping);
    System.out.println( ping );
    System.out.println( pong );
  }
}
