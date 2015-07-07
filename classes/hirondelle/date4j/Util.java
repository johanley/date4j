package hirondelle.date4j;

import java.lang.reflect.Array;
import java.util.logging.Logger;

final class Util  {

  static boolean textHasContent(String aText) {
    return (aText != null) && (aText.trim().length() > 0);
  }
  
  static String quote(Object aObject){
    return SINGLE_QUOTE + String.valueOf(aObject) + SINGLE_QUOTE; 
  }
  
  static String getArrayAsString(Object aArray){
    final String fSTART_CHAR = "[";
    final String fEND_CHAR = "]";
    final String fSEPARATOR = ", ";
    final String fNULL = "null";
    
    if ( aArray == null ) return fNULL;
    checkObjectIsArray(aArray);

    StringBuilder result = new StringBuilder( fSTART_CHAR );
    int length = Array.getLength(aArray);
    for ( int idx = 0 ; idx < length ; ++idx ) {
      Object item = Array.get(aArray, idx);
      if ( isNonNullArray(item) ){
        //recursive call!
        result.append( getArrayAsString(item) );
      }
      else{
        result.append( item );
      }
      if ( ! isLastItem(idx, length) ) {
        result.append(fSEPARATOR);
      }
    }
    result.append(fEND_CHAR);
    return result.toString();
  }

  static Logger getLogger(Class<?> aClass){
    return Logger.getLogger(aClass.getPackage().getName());  
  }
  
  // PRIVATE
  
  private static final String SINGLE_QUOTE = "'";
  
  private static boolean isNonNullArray(Object aItem){
    return aItem != null && aItem.getClass().isArray();
  }

  private static void checkObjectIsArray(Object aArray){
    if ( ! aArray.getClass().isArray() ) {
      throw new IllegalArgumentException("Object is not an array.");
    }
  }

  private static boolean isLastItem(int aIdx, int aLength){
    return (aIdx == aLength - 1);
  }
  
}
