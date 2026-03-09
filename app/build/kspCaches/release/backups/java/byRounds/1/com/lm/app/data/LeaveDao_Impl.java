package com.lm.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LeaveDao_Impl implements LeaveDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LeaveBalance> __insertionAdapterOfLeaveBalance;

  private final EntityInsertionAdapter<LeaveEntry> __insertionAdapterOfLeaveEntry;

  private final DateConverters __dateConverters = new DateConverters();

  private final EntityDeletionOrUpdateAdapter<LeaveEntry> __deletionAdapterOfLeaveEntry;

  private final EntityDeletionOrUpdateAdapter<LeaveEntry> __updateAdapterOfLeaveEntry;

  public LeaveDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLeaveBalance = new EntityInsertionAdapter<LeaveBalance>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `leave_balances` (`kgid`,`clYear`,`clAnnualLimit`,`clRemaining`,`elManualBalance`,`elBalance`,`hplBalance`,`cclUsed`,`maternityUsedCount`,`paternityUsedCount`,`mclUsedThisMonth`,`mclLastUsedMonth`,`mclLastUsedYear`,`lastResetYear`,`lastCreditDate`,`lastElHplCreditDate`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LeaveBalance entity) {
        statement.bindString(1, entity.getKgid());
        statement.bindLong(2, entity.getClYear());
        statement.bindLong(3, entity.getClAnnualLimit());
        statement.bindDouble(4, entity.getClRemaining());
        statement.bindDouble(5, entity.getElManualBalance());
        statement.bindDouble(6, entity.getElBalance());
        statement.bindDouble(7, entity.getHplBalance());
        statement.bindDouble(8, entity.getCclUsed());
        statement.bindLong(9, entity.getMaternityUsedCount());
        statement.bindLong(10, entity.getPaternityUsedCount());
        statement.bindLong(11, entity.getMclUsedThisMonth());
        statement.bindLong(12, entity.getMclLastUsedMonth());
        statement.bindLong(13, entity.getMclLastUsedYear());
        statement.bindLong(14, entity.getLastResetYear());
        statement.bindString(15, entity.getLastCreditDate());
        statement.bindString(16, entity.getLastElHplCreditDate());
      }
    };
    this.__insertionAdapterOfLeaveEntry = new EntityInsertionAdapter<LeaveEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `leave_entries` (`id`,`kgid`,`dateFrom`,`dateTo`,`totalDays`,`leaveType`,`remark`,`createdAt`,`modifiedAt`,`year`,`month`,`isHalfDay`,`isMcl`,`elEntryType`,`hasMedicalCertificate`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LeaveEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getKgid());
        final Long _tmp = __dateConverters.dateToTimestamp(entity.getDateFrom());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, _tmp);
        }
        final Long _tmp_1 = __dateConverters.dateToTimestamp(entity.getDateTo());
        if (_tmp_1 == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, _tmp_1);
        }
        statement.bindDouble(5, entity.getTotalDays());
        statement.bindString(6, entity.getLeaveType());
        if (entity.getRemark() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRemark());
        }
        final Long _tmp_2 = __dateConverters.dateToTimestamp(entity.getCreatedAt());
        if (_tmp_2 == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, _tmp_2);
        }
        final Long _tmp_3 = __dateConverters.dateToTimestamp(entity.getModifiedAt());
        if (_tmp_3 == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, _tmp_3);
        }
        statement.bindLong(10, entity.getYear());
        statement.bindLong(11, entity.getMonth());
        final int _tmp_4 = entity.isHalfDay() ? 1 : 0;
        statement.bindLong(12, _tmp_4);
        final int _tmp_5 = entity.isMcl() ? 1 : 0;
        statement.bindLong(13, _tmp_5);
        statement.bindString(14, entity.getElEntryType());
        final int _tmp_6 = entity.getHasMedicalCertificate() ? 1 : 0;
        statement.bindLong(15, _tmp_6);
      }
    };
    this.__deletionAdapterOfLeaveEntry = new EntityDeletionOrUpdateAdapter<LeaveEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `leave_entries` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LeaveEntry entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfLeaveEntry = new EntityDeletionOrUpdateAdapter<LeaveEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `leave_entries` SET `id` = ?,`kgid` = ?,`dateFrom` = ?,`dateTo` = ?,`totalDays` = ?,`leaveType` = ?,`remark` = ?,`createdAt` = ?,`modifiedAt` = ?,`year` = ?,`month` = ?,`isHalfDay` = ?,`isMcl` = ?,`elEntryType` = ?,`hasMedicalCertificate` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LeaveEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getKgid());
        final Long _tmp = __dateConverters.dateToTimestamp(entity.getDateFrom());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, _tmp);
        }
        final Long _tmp_1 = __dateConverters.dateToTimestamp(entity.getDateTo());
        if (_tmp_1 == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, _tmp_1);
        }
        statement.bindDouble(5, entity.getTotalDays());
        statement.bindString(6, entity.getLeaveType());
        if (entity.getRemark() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getRemark());
        }
        final Long _tmp_2 = __dateConverters.dateToTimestamp(entity.getCreatedAt());
        if (_tmp_2 == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, _tmp_2);
        }
        final Long _tmp_3 = __dateConverters.dateToTimestamp(entity.getModifiedAt());
        if (_tmp_3 == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, _tmp_3);
        }
        statement.bindLong(10, entity.getYear());
        statement.bindLong(11, entity.getMonth());
        final int _tmp_4 = entity.isHalfDay() ? 1 : 0;
        statement.bindLong(12, _tmp_4);
        final int _tmp_5 = entity.isMcl() ? 1 : 0;
        statement.bindLong(13, _tmp_5);
        statement.bindString(14, entity.getElEntryType());
        final int _tmp_6 = entity.getHasMedicalCertificate() ? 1 : 0;
        statement.bindLong(15, _tmp_6);
        statement.bindLong(16, entity.getId());
      }
    };
  }

  @Override
  public Object insertBalance(final LeaveBalance balance,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLeaveBalance.insert(balance);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertEntry(final LeaveEntry entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLeaveEntry.insert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEntry(final LeaveEntry entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfLeaveEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateEntry(final LeaveEntry entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLeaveEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object saveEntryWithBalance(final LeaveBalance balance, final LeaveEntry entry,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> LeaveDao.DefaultImpls.saveEntryWithBalance(LeaveDao_Impl.this, balance, entry, __cont), $completion);
  }

  @Override
  public Object deleteEntryWithBalance(final LeaveBalance balance, final LeaveEntry entry,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> LeaveDao.DefaultImpls.deleteEntryWithBalance(LeaveDao_Impl.this, balance, entry, __cont), $completion);
  }

  @Override
  public Object getBalance(final String kgid,
      final Continuation<? super LeaveBalance> $completion) {
    final String _sql = "SELECT * FROM leave_balances WHERE kgid = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, kgid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LeaveBalance>() {
      @Override
      @Nullable
      public LeaveBalance call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKgid = CursorUtil.getColumnIndexOrThrow(_cursor, "kgid");
          final int _cursorIndexOfClYear = CursorUtil.getColumnIndexOrThrow(_cursor, "clYear");
          final int _cursorIndexOfClAnnualLimit = CursorUtil.getColumnIndexOrThrow(_cursor, "clAnnualLimit");
          final int _cursorIndexOfClRemaining = CursorUtil.getColumnIndexOrThrow(_cursor, "clRemaining");
          final int _cursorIndexOfElManualBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "elManualBalance");
          final int _cursorIndexOfElBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "elBalance");
          final int _cursorIndexOfHplBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "hplBalance");
          final int _cursorIndexOfCclUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "cclUsed");
          final int _cursorIndexOfMaternityUsedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "maternityUsedCount");
          final int _cursorIndexOfPaternityUsedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "paternityUsedCount");
          final int _cursorIndexOfMclUsedThisMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "mclUsedThisMonth");
          final int _cursorIndexOfMclLastUsedMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "mclLastUsedMonth");
          final int _cursorIndexOfMclLastUsedYear = CursorUtil.getColumnIndexOrThrow(_cursor, "mclLastUsedYear");
          final int _cursorIndexOfLastResetYear = CursorUtil.getColumnIndexOrThrow(_cursor, "lastResetYear");
          final int _cursorIndexOfLastCreditDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCreditDate");
          final int _cursorIndexOfLastElHplCreditDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastElHplCreditDate");
          final LeaveBalance _result;
          if (_cursor.moveToFirst()) {
            final String _tmpKgid;
            _tmpKgid = _cursor.getString(_cursorIndexOfKgid);
            final int _tmpClYear;
            _tmpClYear = _cursor.getInt(_cursorIndexOfClYear);
            final int _tmpClAnnualLimit;
            _tmpClAnnualLimit = _cursor.getInt(_cursorIndexOfClAnnualLimit);
            final double _tmpClRemaining;
            _tmpClRemaining = _cursor.getDouble(_cursorIndexOfClRemaining);
            final double _tmpElManualBalance;
            _tmpElManualBalance = _cursor.getDouble(_cursorIndexOfElManualBalance);
            final double _tmpElBalance;
            _tmpElBalance = _cursor.getDouble(_cursorIndexOfElBalance);
            final double _tmpHplBalance;
            _tmpHplBalance = _cursor.getDouble(_cursorIndexOfHplBalance);
            final double _tmpCclUsed;
            _tmpCclUsed = _cursor.getDouble(_cursorIndexOfCclUsed);
            final int _tmpMaternityUsedCount;
            _tmpMaternityUsedCount = _cursor.getInt(_cursorIndexOfMaternityUsedCount);
            final int _tmpPaternityUsedCount;
            _tmpPaternityUsedCount = _cursor.getInt(_cursorIndexOfPaternityUsedCount);
            final int _tmpMclUsedThisMonth;
            _tmpMclUsedThisMonth = _cursor.getInt(_cursorIndexOfMclUsedThisMonth);
            final int _tmpMclLastUsedMonth;
            _tmpMclLastUsedMonth = _cursor.getInt(_cursorIndexOfMclLastUsedMonth);
            final int _tmpMclLastUsedYear;
            _tmpMclLastUsedYear = _cursor.getInt(_cursorIndexOfMclLastUsedYear);
            final int _tmpLastResetYear;
            _tmpLastResetYear = _cursor.getInt(_cursorIndexOfLastResetYear);
            final String _tmpLastCreditDate;
            _tmpLastCreditDate = _cursor.getString(_cursorIndexOfLastCreditDate);
            final String _tmpLastElHplCreditDate;
            _tmpLastElHplCreditDate = _cursor.getString(_cursorIndexOfLastElHplCreditDate);
            _result = new LeaveBalance(_tmpKgid,_tmpClYear,_tmpClAnnualLimit,_tmpClRemaining,_tmpElManualBalance,_tmpElBalance,_tmpHplBalance,_tmpCclUsed,_tmpMaternityUsedCount,_tmpPaternityUsedCount,_tmpMclUsedThisMonth,_tmpMclLastUsedMonth,_tmpMclLastUsedYear,_tmpLastResetYear,_tmpLastCreditDate,_tmpLastElHplCreditDate);
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
  public Flow<List<LeaveEntry>> getAllEntries(final String kgid) {
    final String _sql = "SELECT * FROM leave_entries WHERE kgid = ? ORDER BY dateFrom DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, kgid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"leave_entries"}, new Callable<List<LeaveEntry>>() {
      @Override
      @NonNull
      public List<LeaveEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKgid = CursorUtil.getColumnIndexOrThrow(_cursor, "kgid");
          final int _cursorIndexOfDateFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "dateFrom");
          final int _cursorIndexOfDateTo = CursorUtil.getColumnIndexOrThrow(_cursor, "dateTo");
          final int _cursorIndexOfTotalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDays");
          final int _cursorIndexOfLeaveType = CursorUtil.getColumnIndexOrThrow(_cursor, "leaveType");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "modifiedAt");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfIsHalfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "isHalfDay");
          final int _cursorIndexOfIsMcl = CursorUtil.getColumnIndexOrThrow(_cursor, "isMcl");
          final int _cursorIndexOfElEntryType = CursorUtil.getColumnIndexOrThrow(_cursor, "elEntryType");
          final int _cursorIndexOfHasMedicalCertificate = CursorUtil.getColumnIndexOrThrow(_cursor, "hasMedicalCertificate");
          final List<LeaveEntry> _result = new ArrayList<LeaveEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LeaveEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKgid;
            _tmpKgid = _cursor.getString(_cursorIndexOfKgid);
            final Date _tmpDateFrom;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfDateFrom)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfDateFrom);
            }
            _tmpDateFrom = __dateConverters.fromTimestamp(_tmp);
            final Date _tmpDateTo;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDateTo)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfDateTo);
            }
            _tmpDateTo = __dateConverters.fromTimestamp(_tmp_1);
            final double _tmpTotalDays;
            _tmpTotalDays = _cursor.getDouble(_cursorIndexOfTotalDays);
            final String _tmpLeaveType;
            _tmpLeaveType = _cursor.getString(_cursorIndexOfLeaveType);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Date _tmpCreatedAt;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_3 = __dateConverters.fromTimestamp(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_3;
            }
            final Date _tmpModifiedAt;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfModifiedAt)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfModifiedAt);
            }
            final Date _tmp_5 = __dateConverters.fromTimestamp(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpModifiedAt = _tmp_5;
            }
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final boolean _tmpIsHalfDay;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsHalfDay);
            _tmpIsHalfDay = _tmp_6 != 0;
            final boolean _tmpIsMcl;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsMcl);
            _tmpIsMcl = _tmp_7 != 0;
            final String _tmpElEntryType;
            _tmpElEntryType = _cursor.getString(_cursorIndexOfElEntryType);
            final boolean _tmpHasMedicalCertificate;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfHasMedicalCertificate);
            _tmpHasMedicalCertificate = _tmp_8 != 0;
            _item = new LeaveEntry(_tmpId,_tmpKgid,_tmpDateFrom,_tmpDateTo,_tmpTotalDays,_tmpLeaveType,_tmpRemark,_tmpCreatedAt,_tmpModifiedAt,_tmpYear,_tmpMonth,_tmpIsHalfDay,_tmpIsMcl,_tmpElEntryType,_tmpHasMedicalCertificate);
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
  public Object getWoEntries(final String kgid, final int year, final int month,
      final Continuation<? super List<LeaveEntry>> $completion) {
    final String _sql = "SELECT * FROM leave_entries WHERE kgid = ? AND leaveType = 'WO' AND year = ? AND month = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, kgid);
    _argIndex = 2;
    _statement.bindLong(_argIndex, year);
    _argIndex = 3;
    _statement.bindLong(_argIndex, month);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LeaveEntry>>() {
      @Override
      @NonNull
      public List<LeaveEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKgid = CursorUtil.getColumnIndexOrThrow(_cursor, "kgid");
          final int _cursorIndexOfDateFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "dateFrom");
          final int _cursorIndexOfDateTo = CursorUtil.getColumnIndexOrThrow(_cursor, "dateTo");
          final int _cursorIndexOfTotalDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDays");
          final int _cursorIndexOfLeaveType = CursorUtil.getColumnIndexOrThrow(_cursor, "leaveType");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "modifiedAt");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "month");
          final int _cursorIndexOfIsHalfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "isHalfDay");
          final int _cursorIndexOfIsMcl = CursorUtil.getColumnIndexOrThrow(_cursor, "isMcl");
          final int _cursorIndexOfElEntryType = CursorUtil.getColumnIndexOrThrow(_cursor, "elEntryType");
          final int _cursorIndexOfHasMedicalCertificate = CursorUtil.getColumnIndexOrThrow(_cursor, "hasMedicalCertificate");
          final List<LeaveEntry> _result = new ArrayList<LeaveEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LeaveEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpKgid;
            _tmpKgid = _cursor.getString(_cursorIndexOfKgid);
            final Date _tmpDateFrom;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfDateFrom)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfDateFrom);
            }
            _tmpDateFrom = __dateConverters.fromTimestamp(_tmp);
            final Date _tmpDateTo;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDateTo)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfDateTo);
            }
            _tmpDateTo = __dateConverters.fromTimestamp(_tmp_1);
            final double _tmpTotalDays;
            _tmpTotalDays = _cursor.getDouble(_cursorIndexOfTotalDays);
            final String _tmpLeaveType;
            _tmpLeaveType = _cursor.getString(_cursorIndexOfLeaveType);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Date _tmpCreatedAt;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_3 = __dateConverters.fromTimestamp(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_3;
            }
            final Date _tmpModifiedAt;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfModifiedAt)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfModifiedAt);
            }
            final Date _tmp_5 = __dateConverters.fromTimestamp(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpModifiedAt = _tmp_5;
            }
            final int _tmpYear;
            _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            final int _tmpMonth;
            _tmpMonth = _cursor.getInt(_cursorIndexOfMonth);
            final boolean _tmpIsHalfDay;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsHalfDay);
            _tmpIsHalfDay = _tmp_6 != 0;
            final boolean _tmpIsMcl;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsMcl);
            _tmpIsMcl = _tmp_7 != 0;
            final String _tmpElEntryType;
            _tmpElEntryType = _cursor.getString(_cursorIndexOfElEntryType);
            final boolean _tmpHasMedicalCertificate;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfHasMedicalCertificate);
            _tmpHasMedicalCertificate = _tmp_8 != 0;
            _item = new LeaveEntry(_tmpId,_tmpKgid,_tmpDateFrom,_tmpDateTo,_tmpTotalDays,_tmpLeaveType,_tmpRemark,_tmpCreatedAt,_tmpModifiedAt,_tmpYear,_tmpMonth,_tmpIsHalfDay,_tmpIsMcl,_tmpElEntryType,_tmpHasMedicalCertificate);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
