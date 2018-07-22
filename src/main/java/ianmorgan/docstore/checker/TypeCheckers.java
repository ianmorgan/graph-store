package ianmorgan.docstore.checker;


import java.math.BigDecimal;
import java.util.*;

/**
 * A set of default validators for automated type checking. It is relatively strict and won't allow
 * the say conversion of number string to strings
 * <p>
 * This JSON is considered valid:
 * <p>
 * { "StringField" : "This is a String", }
 * { "IntegerField" :10 }
 * <p>
 * and this is not:
 * <p>
 * { "IntegerField" : "10" }
 */
public class TypeCheckers {

    public static boolean check(Object value, Class type) {
        if (type.equals(String.class)) {
            return value instanceof String;
        }
        if (type.equals(Boolean.class)) {
            return value instanceof Boolean;
        }
        if (type.equals(Long.class)) {
            return value instanceof Integer || value instanceof Long;
        }
        if (type.equals(Integer.class)) {
            return value instanceof Integer;
        }
        if (type.equals(Double.class)) {
            return value instanceof Double || value instanceof Float || value instanceof BigDecimal ;
        }
        if (type.equals(UUID.class)) {
            try {
                UUID.fromString((String) value);
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        }

        return false;
    }



}
