package com.habs.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HabitDao_Impl implements HabitDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HabitEntity> __insertionAdapterOfHabitEntity;

  private final EntityDeletionOrUpdateAdapter<HabitEntity> __deletionAdapterOfHabitEntity;

  private final EntityDeletionOrUpdateAdapter<HabitEntity> __updateAdapterOfHabitEntity;

  public HabitDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHabitEntity = new EntityInsertionAdapter<HabitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `habits` (`id`,`name`,`icon`,`colorHex`,`frequency`,`reminderTimeMinutes`,`calendarSynced`,`calendarEventId`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIcon());
        statement.bindString(4, entity.getColorHex());
        statement.bindString(5, entity.getFrequency());
        if (entity.getReminderTimeMinutes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getReminderTimeMinutes());
        }
        final int _tmp = entity.getCalendarSynced() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getCalendarEventId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCalendarEventId());
        }
        statement.bindLong(9, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfHabitEntity = new EntityDeletionOrUpdateAdapter<HabitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `habits` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfHabitEntity = new EntityDeletionOrUpdateAdapter<HabitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `habits` SET `id` = ?,`name` = ?,`icon` = ?,`colorHex` = ?,`frequency` = ?,`reminderTimeMinutes` = ?,`calendarSynced` = ?,`calendarEventId` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HabitEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIcon());
        statement.bindString(4, entity.getColorHex());
        statement.bindString(5, entity.getFrequency());
        if (entity.getReminderTimeMinutes() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getReminderTimeMinutes());
        }
        final int _tmp = entity.getCalendarSynced() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getCalendarEventId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCalendarEventId());
        }
        statement.bindLong(9, entity.getCreatedAt());
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insertHabit(final HabitEntity habit, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfHabitEntity.insertAndReturnId(habit);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteHabit(final HabitEntity habit, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfHabitEntity.handle(habit);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateHabit(final HabitEntity habit, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfHabitEntity.handle(habit);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<HabitEntity>> getAllHabits() {
    final String _sql = "SELECT * FROM habits ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"habits"}, new Callable<List<HabitEntity>>() {
      @Override
      @NonNull
      public List<HabitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "icon");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfReminderTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTimeMinutes");
          final int _cursorIndexOfCalendarSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarSynced");
          final int _cursorIndexOfCalendarEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarEventId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<HabitEntity> _result = new ArrayList<HabitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HabitEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIcon;
            _tmpIcon = _cursor.getString(_cursorIndexOfIcon);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final Integer _tmpReminderTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfReminderTimeMinutes)) {
              _tmpReminderTimeMinutes = null;
            } else {
              _tmpReminderTimeMinutes = _cursor.getInt(_cursorIndexOfReminderTimeMinutes);
            }
            final boolean _tmpCalendarSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCalendarSynced);
            _tmpCalendarSynced = _tmp != 0;
            final String _tmpCalendarEventId;
            if (_cursor.isNull(_cursorIndexOfCalendarEventId)) {
              _tmpCalendarEventId = null;
            } else {
              _tmpCalendarEventId = _cursor.getString(_cursorIndexOfCalendarEventId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new HabitEntity(_tmpId,_tmpName,_tmpIcon,_tmpColorHex,_tmpFrequency,_tmpReminderTimeMinutes,_tmpCalendarSynced,_tmpCalendarEventId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getHabitById(final long id, final Continuation<? super HabitEntity> $completion) {
    final String _sql = "SELECT * FROM habits WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<HabitEntity>() {
      @Override
      @Nullable
      public HabitEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "icon");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "frequency");
          final int _cursorIndexOfReminderTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTimeMinutes");
          final int _cursorIndexOfCalendarSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarSynced");
          final int _cursorIndexOfCalendarEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarEventId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final HabitEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIcon;
            _tmpIcon = _cursor.getString(_cursorIndexOfIcon);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            final String _tmpFrequency;
            _tmpFrequency = _cursor.getString(_cursorIndexOfFrequency);
            final Integer _tmpReminderTimeMinutes;
            if (_cursor.isNull(_cursorIndexOfReminderTimeMinutes)) {
              _tmpReminderTimeMinutes = null;
            } else {
              _tmpReminderTimeMinutes = _cursor.getInt(_cursorIndexOfReminderTimeMinutes);
            }
            final boolean _tmpCalendarSynced;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCalendarSynced);
            _tmpCalendarSynced = _tmp != 0;
            final String _tmpCalendarEventId;
            if (_cursor.isNull(_cursorIndexOfCalendarEventId)) {
              _tmpCalendarEventId = null;
            } else {
              _tmpCalendarEventId = _cursor.getString(_cursorIndexOfCalendarEventId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new HabitEntity(_tmpId,_tmpName,_tmpIcon,_tmpColorHex,_tmpFrequency,_tmpReminderTimeMinutes,_tmpCalendarSynced,_tmpCalendarEventId,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
