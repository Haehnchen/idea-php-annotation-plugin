package de.espend.idea.php.annotation.util;


public class WorkaroundUtil {

    /**
     * TODO: eap cleanup
     * check for field name inside class
     */
    public static boolean isClassFieldName(String className, String fieldName) {
        try {
            Class c = Class.forName(className);
            c.getDeclaredField(fieldName).get(c.getDeclaredField(fieldName));

        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoSuchFieldException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        }

        return true;
    }

}
