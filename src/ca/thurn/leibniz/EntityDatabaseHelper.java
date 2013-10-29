package ca.thurn.leibniz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EntityDatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "entities.db";
  private static final int DATABASE_VERSION = 1;
  
  public EntityDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    EntityTable.onCreate(database); 
  }

  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    EntityTable.onUpgrade(database, oldVersion, newVersion);
  }

}
