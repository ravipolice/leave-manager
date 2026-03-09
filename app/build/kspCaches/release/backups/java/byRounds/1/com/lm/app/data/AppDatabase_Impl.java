package com.lm.app.data;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile LeaveDao _leaveDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `leave_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `kgid` TEXT NOT NULL, `dateFrom` INTEGER, `dateTo` INTEGER, `totalDays` REAL NOT NULL, `leaveType` TEXT NOT NULL, `remark` TEXT, `createdAt` INTEGER NOT NULL, `modifiedAt` INTEGER NOT NULL, `year` INTEGER NOT NULL, `month` INTEGER NOT NULL, `isHalfDay` INTEGER NOT NULL, `isMcl` INTEGER NOT NULL, `elEntryType` TEXT NOT NULL, `hasMedicalCertificate` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `leave_balances` (`kgid` TEXT NOT NULL, `clYear` INTEGER NOT NULL, `clAnnualLimit` INTEGER NOT NULL, `clRemaining` REAL NOT NULL, `elManualBalance` REAL NOT NULL, `elBalance` REAL NOT NULL, `hplBalance` REAL NOT NULL, `cclUsed` REAL NOT NULL, `maternityUsedCount` INTEGER NOT NULL, `paternityUsedCount` INTEGER NOT NULL, `mclUsedThisMonth` INTEGER NOT NULL, `mclLastUsedMonth` INTEGER NOT NULL, `mclLastUsedYear` INTEGER NOT NULL, `lastResetYear` INTEGER NOT NULL, `lastCreditDate` TEXT NOT NULL, `lastElHplCreditDate` TEXT NOT NULL, PRIMARY KEY(`kgid`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6431474ee63a06c5d8275c3a327e7e65')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `leave_entries`");
        db.execSQL("DROP TABLE IF EXISTS `leave_balances`");
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
        final HashMap<String, TableInfo.Column> _columnsLeaveEntries = new HashMap<String, TableInfo.Column>(15);
        _columnsLeaveEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("kgid", new TableInfo.Column("kgid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("dateFrom", new TableInfo.Column("dateFrom", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("dateTo", new TableInfo.Column("dateTo", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("totalDays", new TableInfo.Column("totalDays", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("leaveType", new TableInfo.Column("leaveType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("remark", new TableInfo.Column("remark", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("modifiedAt", new TableInfo.Column("modifiedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("month", new TableInfo.Column("month", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("isHalfDay", new TableInfo.Column("isHalfDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("isMcl", new TableInfo.Column("isMcl", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("elEntryType", new TableInfo.Column("elEntryType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveEntries.put("hasMedicalCertificate", new TableInfo.Column("hasMedicalCertificate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLeaveEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLeaveEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLeaveEntries = new TableInfo("leave_entries", _columnsLeaveEntries, _foreignKeysLeaveEntries, _indicesLeaveEntries);
        final TableInfo _existingLeaveEntries = TableInfo.read(db, "leave_entries");
        if (!_infoLeaveEntries.equals(_existingLeaveEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "leave_entries(com.lm.app.data.LeaveEntry).\n"
                  + " Expected:\n" + _infoLeaveEntries + "\n"
                  + " Found:\n" + _existingLeaveEntries);
        }
        final HashMap<String, TableInfo.Column> _columnsLeaveBalances = new HashMap<String, TableInfo.Column>(16);
        _columnsLeaveBalances.put("kgid", new TableInfo.Column("kgid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("clYear", new TableInfo.Column("clYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("clAnnualLimit", new TableInfo.Column("clAnnualLimit", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("clRemaining", new TableInfo.Column("clRemaining", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("elManualBalance", new TableInfo.Column("elManualBalance", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("elBalance", new TableInfo.Column("elBalance", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("hplBalance", new TableInfo.Column("hplBalance", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("cclUsed", new TableInfo.Column("cclUsed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("maternityUsedCount", new TableInfo.Column("maternityUsedCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("paternityUsedCount", new TableInfo.Column("paternityUsedCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("mclUsedThisMonth", new TableInfo.Column("mclUsedThisMonth", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("mclLastUsedMonth", new TableInfo.Column("mclLastUsedMonth", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("mclLastUsedYear", new TableInfo.Column("mclLastUsedYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("lastResetYear", new TableInfo.Column("lastResetYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("lastCreditDate", new TableInfo.Column("lastCreditDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaveBalances.put("lastElHplCreditDate", new TableInfo.Column("lastElHplCreditDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLeaveBalances = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLeaveBalances = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLeaveBalances = new TableInfo("leave_balances", _columnsLeaveBalances, _foreignKeysLeaveBalances, _indicesLeaveBalances);
        final TableInfo _existingLeaveBalances = TableInfo.read(db, "leave_balances");
        if (!_infoLeaveBalances.equals(_existingLeaveBalances)) {
          return new RoomOpenHelper.ValidationResult(false, "leave_balances(com.lm.app.data.LeaveBalance).\n"
                  + " Expected:\n" + _infoLeaveBalances + "\n"
                  + " Found:\n" + _existingLeaveBalances);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6431474ee63a06c5d8275c3a327e7e65", "21bf9a25505f9efd3e3569a8097d5ce2");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "leave_entries","leave_balances");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `leave_entries`");
      _db.execSQL("DELETE FROM `leave_balances`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
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
    _typeConvertersMap.put(LeaveDao.class, LeaveDao_Impl.getRequiredConverters());
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
  public LeaveDao leaveDao() {
    if (_leaveDao != null) {
      return _leaveDao;
    } else {
      synchronized(this) {
        if(_leaveDao == null) {
          _leaveDao = new LeaveDao_Impl(this);
        }
        return _leaveDao;
      }
    }
  }
}
