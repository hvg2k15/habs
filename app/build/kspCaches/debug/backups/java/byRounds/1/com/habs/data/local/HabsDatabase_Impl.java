package com.habs.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HabsDatabase_Impl extends HabsDatabase {
  private volatile HabitDao _habitDao;

  private volatile CompletionDao _completionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `habits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `frequency` TEXT NOT NULL, `reminderTimeMinutes` INTEGER, `calendarSynced` INTEGER NOT NULL, `calendarEventId` TEXT, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `completions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `completedAt` INTEGER NOT NULL, `dateKey` TEXT NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_completions_habitId` ON `completions` (`habitId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_completions_dateKey` ON `completions` (`dateKey`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f8704bba1e46285095e3a26707c0bece')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `habits`");
        db.execSQL("DROP TABLE IF EXISTS `completions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsHabits = new HashMap<String, TableInfo.Column>(9);
        _columnsHabits.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("icon", new TableInfo.Column("icon", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("colorHex", new TableInfo.Column("colorHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("frequency", new TableInfo.Column("frequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("reminderTimeMinutes", new TableInfo.Column("reminderTimeMinutes", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("calendarSynced", new TableInfo.Column("calendarSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("calendarEventId", new TableInfo.Column("calendarEventId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHabits.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHabits = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHabits = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHabits = new TableInfo("habits", _columnsHabits, _foreignKeysHabits, _indicesHabits);
        final TableInfo _existingHabits = TableInfo.read(db, "habits");
        if (!_infoHabits.equals(_existingHabits)) {
          return new RoomOpenHelper.ValidationResult(false, "habits(com.habs.data.local.HabitEntity).\n"
                  + " Expected:\n" + _infoHabits + "\n"
                  + " Found:\n" + _existingHabits);
        }
        final HashMap<String, TableInfo.Column> _columnsCompletions = new HashMap<String, TableInfo.Column>(4);
        _columnsCompletions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCompletions.put("habitId", new TableInfo.Column("habitId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCompletions.put("completedAt", new TableInfo.Column("completedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCompletions.put("dateKey", new TableInfo.Column("dateKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCompletions = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysCompletions.add(new TableInfo.ForeignKey("habits", "CASCADE", "NO ACTION", Arrays.asList("habitId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesCompletions = new HashSet<TableInfo.Index>(2);
        _indicesCompletions.add(new TableInfo.Index("index_completions_habitId", false, Arrays.asList("habitId"), Arrays.asList("ASC")));
        _indicesCompletions.add(new TableInfo.Index("index_completions_dateKey", false, Arrays.asList("dateKey"), Arrays.asList("ASC")));
        final TableInfo _infoCompletions = new TableInfo("completions", _columnsCompletions, _foreignKeysCompletions, _indicesCompletions);
        final TableInfo _existingCompletions = TableInfo.read(db, "completions");
        if (!_infoCompletions.equals(_existingCompletions)) {
          return new RoomOpenHelper.ValidationResult(false, "completions(com.habs.data.local.CompletionEntity).\n"
                  + " Expected:\n" + _infoCompletions + "\n"
                  + " Found:\n" + _existingCompletions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "f8704bba1e46285095e3a26707c0bece", "1f0dccea026ab1087219a855fafb6632");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "habits","completions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `habits`");
      _db.execSQL("DELETE FROM `completions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(HabitDao.class, HabitDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CompletionDao.class, CompletionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public HabitDao habitDao() {
    if (_habitDao != null) {
      return _habitDao;
    } else {
      synchronized(this) {
        if(_habitDao == null) {
          _habitDao = new HabitDao_Impl(this);
        }
        return _habitDao;
      }
    }
  }

  @Override
  public CompletionDao completionDao() {
    if (_completionDao != null) {
      return _completionDao;
    } else {
      synchronized(this) {
        if(_completionDao == null) {
          _completionDao = new CompletionDao_Impl(this);
        }
        return _completionDao;
      }
    }
  }
}
