package com.example.minikers_receiver;

import android.app.ActionBar;
import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Query;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

class UsageRepository {

    private AllDao mAllDao;
    private final String TAG = "UsageRepository";

    // Note that in order to unit test the UsageRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    UsageRepository(Application application) {
        UsageRoomDatabase db = UsageRoomDatabase.getDatabase(application);
        mAllDao = db.allDao();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Usage>> getAllRecords() {
        return mAllDao.getAllRecords();
    }



    //Maybe don't deal with LocalTime inside repository, do it in SingleDayData or UsageListAdapter
    LiveData<List<String>> getUsageStartTimesForDay(String day) {
        return mAllDao.getUsageStartTimesFromDay(day);
    }

    LiveData<List<String>> getUsageEndTimesFromDay(String day){
        return mAllDao.getUsageEndTimesFromDay(day);
    }

    LiveData<List<String>> getUsageTypeList(String day) {
        return mAllDao.getUsageTypesFromDay(day);
    }

    LiveData<List<Usage>> getDaysRecords(String date) {
        return mAllDao.getRecordsFromDay(date);
    }


    List<String> getUsageStartTimesForDayAsNormalList(String date){
        return mAllDao.getUsageStartTimesFromDayAsOrdinaryList(date);
    }

    List<String> getUsageEndTimesForDayAsNormalList(String date){
        return mAllDao.getUsageEndTimesFromDayAsOrdinaryList(date);
    }

    List<String> getUsageTypesFromDayAsOrdinaryList(String day){
        return mAllDao.getUsageTypesFromDayAsOrdinaryList(day);
    }


    List<Usage> getRecordsFromDayAsOrdinaryList(String day){
        return mAllDao.getRecordsFromDayAsOrdinaryList(day);
    }

    void checkpoint(SupportSQLiteQuery supportSQLiteQuery){
        mAllDao.checkpoint(supportSQLiteQuery);
    }


    List<Usage> getDeviceUsagesAsOrdinaryList(String device){
        return mAllDao.getDeviceUsagesAsOrdinaryList(device);
    }

    LiveData<List<Usage>> getDeviceUsages(String device){
        return mAllDao.getDeviceUsages(device);
    }

    List<Usage> getDeviceUsagesForDayAsOrdinaryList(String device, String day){
        return mAllDao.getDeviceUsagesForDayAsOrdinaryList(device, day);
    }


    LiveData<List<Usage>> getDeviceUsagesForDay(String device, String day){
        return mAllDao.getDeviceUsagesForDay(device, day);
    }

    List<Usage> getUsagesOfMacAddress(String macAddress){
        return mAllDao.getUsagesOfMacAddress(macAddress);
    }

    List<Usage> getUsagesOfMacAddressForDay(String macAddress, String day){
        return mAllDao.getUsagesOfMacAddressForDay(macAddress, day);
    }

    List<String> getUsageStartTimesFromDayForMacAddress(String macAddress, String day){
        return mAllDao.getUsageStartTimesFromDayForMacAddress(macAddress, day);
    }

    List<String> getUsageEndTimesFromDayForMacAddress(String macAddress, String day){
        return mAllDao.getUsageEndTimesFromDayForMacAddress(macAddress, day);
    }

    List<String> getUsageTypesFromDayForMacAddress(String macAddress, String day){
        return mAllDao.getUsageTypesFromDayForMacAddress(macAddress, day);
    }

    List<Double> getVoltagesFromDayForMacAddress(String macAddress, String day){
        return mAllDao.getVoltagesFromDayForMacAddress(macAddress, day);
    }

    List<String> getAllMacAddresses(){
        return mAllDao.getAllMacAddresses();
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    // (i.e. use the ExecutorService (thread pool) in UsageRoomDatabase)

    void insert(Usage usage) {
        UsageRoomDatabase.databaseWriteExecutor.execute( () -> {

            mAllDao.insert(usage);
        });
    }
}