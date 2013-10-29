package ca.thurn.leibniz;

import java.util.HashSet;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;

public class EntityTable {
  public static final String TABLE_NAME = "entity";
  public static final String COLUMN_ID = "id";
  public static final String COLUMN_LAST_MODIFIED = "last_modified";
  public static final String COLUMN_VALUE = "value";
  public static final Set<String> COLUMNS = new HashSet<String>();
  static {
    COLUMNS.add(COLUMN_ID);
    COLUMNS.add(COLUMN_LAST_MODIFIED);
    COLUMNS.add(COLUMN_VALUE);
  }
  
  public static void onCreate(SQLiteDatabase database) {
    database.execSQL("create table " 
        + TABLE_NAME
        + "(" 
        + COLUMN_ID + " text primary key," 
        + COLUMN_LAST_MODIFIED + " integer not null, " 
        + COLUMN_VALUE + " text not null" 
        + ");");
  }
  
  public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    throw new UnsupportedOperationException("Database upgrading not implemented");
  }
}
