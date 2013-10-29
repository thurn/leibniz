package ca.thurn.leibniz;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;

// We do not need to close the database, refer to stackoverflow.com/questions/4547461.
@SuppressWarnings("resource")
public class Leibniz extends ContentProvider implements ChildEventListener {

  private static final int ENTITIES = 1;
  private static final int ENTITY_ID = 2;  
  private static final String AUTHORITY = "ca.thurn.leibniz";
  private static final String BASE_PATH = "entities";
  public static final Uri CONTENT_URI =
      Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
  public static final String CONTENT_TYPE =
      ContentResolver.CURSOR_DIR_BASE_TYPE + "/entities";
  public static final String CONTENT_ITEM_TYPE = 
      ContentResolver.CURSOR_ITEM_BASE_TYPE + "/entity";
  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, ENTITIES);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTITY_ID);
  }
  
  public static abstract class SnapshotConverter {
    public abstract ContentValues toContentValues(DataSnapshot snapshot);
  }

  private final Firebase mFirebase;
  private final SnapshotConverter mConverter;
  private EntityDatabaseHelper mDatabase;
  private Runnable mCancellationCallback;
  
  public Leibniz(Firebase firebase, SnapshotConverter converter) {
    mFirebase = firebase;
    mConverter = converter;
    mFirebase.addChildEventListener(this);
  }

  @Override
  public void onCancelled() {
    mCancellationCallback.run();
  }
  
  /**
   * Adds a callback to be invoked when Firebase calls onCancelled().
   * 
   * @param callback The callback
   */
  public void setCancellationCallback(Runnable callback) {
    mCancellationCallback = callback;
  }

  @Override
  public void onChildAdded(DataSnapshot snapshot, String prevChild) {
    SQLiteDatabase db = mDatabase.getWritableDatabase();
    db.insert(EntityTable.TABLE_NAME, null, mConverter.toContentValues(snapshot));
  }

  @Override
  public void onChildChanged(DataSnapshot snapshot, String prevChild) {
    ContentValues contentValues = mConverter.toContentValues(snapshot);
    SQLiteDatabase db = mDatabase.getWritableDatabase();
    String id = contentValues.getAsString("id");
    db.update(EntityTable.TABLE_NAME, contentValues, EntityTable.COLUMN_ID + " = " + id, null);
  }

  @Override
  public void onChildMoved(DataSnapshot snapshot, String prevChild) {
    // TODO: Implement support for priority
  }

  @Override
  public void onChildRemoved(DataSnapshot snapshot) {
    ContentValues contentValues = mConverter.toContentValues(snapshot);
    SQLiteDatabase db = mDatabase.getWritableDatabase();
    String id = contentValues.getAsString("id");
    db.delete(EntityTable.TABLE_NAME, EntityTable.COLUMN_ID + " = " + id, null);
  }

  @Override
  public String getType(Uri uri) {
    return null; // no MIME type
  }

  @Override
  public boolean onCreate() {
    mDatabase = new EntityDatabaseHelper(getContext());
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
    
    if (!EntityTable.COLUMNS.containsAll(new HashSet<String>(Arrays.asList(projection)))) {
      throw new IllegalArgumentException("Unknown columns in query() call");
    }

    queryBuilder.setTables(EntityTable.TABLE_NAME);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case ENTITIES:
      break;
    case ENTITY_ID:
      queryBuilder.appendWhere(EntityTable.COLUMN_ID + "="
          + uri.getLastPathSegment());
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = mDatabase.getWritableDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection,
        selectionArgs, null, null, sortOrder);

    cursor.setNotificationUri(getContext().getContentResolver(), uri);
    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new IllegalArgumentException("This ContentProvider is read-only.");
  }
  
  @Override
  public Uri insert(Uri uri, ContentValues values) {
    throw new IllegalArgumentException("This ContentProvider is read-only.");
  }
  
  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    throw new IllegalArgumentException("This ContentProvider is read-only.");
  }
  
}
