package tkj.android.homecontrol.mythmote.db;

import android.database.Cursor;

/**
 * Provides functions to safely read values from a Cursor.
 */
public class CursorValueReader {

    private CursorValueReader() {

    }

    /**
     * Read a string value from cursor.
     * @param cursor Cursor to read value from.
     * @param columnKey Column name.
     * @param defaultValue Default value to return if column is not found in cursor.
     * @throws IllegalArgumentException when cursor is null.
     * @return Value.
     */
    public static String getString(Cursor cursor, String columnKey, String defaultValue)
            throws IllegalArgumentException {

        if(cursor == null) {
            throw new IllegalArgumentException();
        }

        int columnIndex = cursor.getColumnIndex(columnKey);

        if(columnIndex < 0 || columnIndex >= cursor.getColumnCount()) {
            return defaultValue;
        }

        return cursor.getString(columnIndex);
    }

    /**
     * Read a string value from cursor.
     * @param cursor Cursor to read value from.
     * @param columnKey Column name.
     * @param defaultValue Default value to return if column is not found in cursor.
     * @throws IllegalArgumentException when cursor is null.
     * @return Value.
     */
    public static int getInt(Cursor cursor, String columnKey, int defaultValue)
            throws IllegalArgumentException {

        if(cursor == null) {
            throw new IllegalArgumentException();
        }

        int columnIndex = cursor.getColumnIndex(columnKey);

        if(columnIndex < 0 || columnIndex >= cursor.getColumnCount()) {
            return defaultValue;
        }

        return cursor.getInt(columnIndex);
    }

}
