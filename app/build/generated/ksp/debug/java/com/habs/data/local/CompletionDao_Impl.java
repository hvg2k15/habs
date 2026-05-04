package com.habs.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class CompletionDao_Impl implements CompletionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CompletionEntity> __insertionAdapterOfCompletionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCompletion;

  public CompletionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCompletionEntity = new EntityInsertionAdapter<CompletionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `completions` (`id`,`habitId`,`completedAt`,`dateKey`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CompletionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHabitId());
        statement.bindLong(3, entity.getCompletedAt());
        statement.bindString(4, entity.getDateKey());
      }
    };
    this.__preparedStmtOfDeleteCompletion = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM completions WHERE habitId = ? AND dateKey = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCompletion(final CompletionEntity completion,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCompletionEntity.insert(completion);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCompletion(final long habitId, final String dateKey,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCompletion.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, habitId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, dateKey);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCompletion.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CompletionEntity>> getCompletionsForDate(final String dateKey) {
    final String _sql = "SELECT * FROM completions WHERE dateKey = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, dateKey);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"completions"}, new Callable<List<CompletionEntity>>() {
      @Override
      @NonNull
      public List<CompletionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfDateKey = CursorUtil.getColumnIndexOrThrow(_cursor, "dateKey");
          final List<CompletionEntity> _result = new ArrayList<CompletionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final long _tmpCompletedAt;
            _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            final String _tmpDateKey;
            _tmpDateKey = _cursor.getString(_cursorIndexOfDateKey);
            _item = new CompletionEntity(_tmpId,_tmpHabitId,_tmpCompletedAt,_tmpDateKey);
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
  public Object getCompletion(final long habitId, final String dateKey,
      final Continuation<? super CompletionEntity> $completion) {
    final String _sql = "SELECT * FROM completions WHERE habitId = ? AND dateKey = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    _argIndex = 2;
    _statement.bindString(_argIndex, dateKey);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CompletionEntity>() {
      @Override
      @Nullable
      public CompletionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfDateKey = CursorUtil.getColumnIndexOrThrow(_cursor, "dateKey");
          final CompletionEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final long _tmpCompletedAt;
            _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            final String _tmpDateKey;
            _tmpDateKey = _cursor.getString(_cursorIndexOfDateKey);
            _result = new CompletionEntity(_tmpId,_tmpHabitId,_tmpCompletedAt,_tmpDateKey);
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

  @Override
  public Object getCompletionsForHabit(final long habitId,
      final Continuation<? super List<CompletionEntity>> $completion) {
    final String _sql = "SELECT * FROM completions WHERE habitId = ? ORDER BY completedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, habitId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CompletionEntity>>() {
      @Override
      @NonNull
      public List<CompletionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfDateKey = CursorUtil.getColumnIndexOrThrow(_cursor, "dateKey");
          final List<CompletionEntity> _result = new ArrayList<CompletionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final long _tmpCompletedAt;
            _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            final String _tmpDateKey;
            _tmpDateKey = _cursor.getString(_cursorIndexOfDateKey);
            _item = new CompletionEntity(_tmpId,_tmpHabitId,_tmpCompletedAt,_tmpDateKey);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CompletionEntity>> getCompletionsInRange(final String fromKey,
      final String toKey) {
    final String _sql = "SELECT * FROM completions WHERE dateKey BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fromKey);
    _argIndex = 2;
    _statement.bindString(_argIndex, toKey);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"completions"}, new Callable<List<CompletionEntity>>() {
      @Override
      @NonNull
      public List<CompletionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHabitId = CursorUtil.getColumnIndexOrThrow(_cursor, "habitId");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfDateKey = CursorUtil.getColumnIndexOrThrow(_cursor, "dateKey");
          final List<CompletionEntity> _result = new ArrayList<CompletionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CompletionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHabitId;
            _tmpHabitId = _cursor.getLong(_cursorIndexOfHabitId);
            final long _tmpCompletedAt;
            _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            final String _tmpDateKey;
            _tmpDateKey = _cursor.getString(_cursorIndexOfDateKey);
            _item = new CompletionEntity(_tmpId,_tmpHabitId,_tmpCompletedAt,_tmpDateKey);
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
  public Object countCompletionsForDate(final String dateKey,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM completions WHERE dateKey = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, dateKey);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
