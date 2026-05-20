package com.example.cachecleaner;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.Process;
import android.os.storage.StorageManager;
import android.app.usage.StorageStatsManager;
import android.app.usage.StorageStats;
import android.content.Context;
import android.content.pm.IPackageManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CacheService extends ICacheService.Stub {

    public CacheService() {}

    @Override
    public List<AppCacheInfo> getCacheSizes(List<String> packageNames) throws RemoteException {
        List<AppCacheInfo> results = new ArrayList<>();
        if (packageNames == null || packageNames.isEmpty()) return results;

        try {
            IBinder binder = ServiceManager.getService(Context.STORAGE_STATS_SERVICE);
            StorageStatsManager statsManager = StorageStatsManager.Stub.asInterface(binder);
            UUID storageUuid = StorageManager.UUID_DEFAULT;
            UserHandle currentUser = Process.myUserHandle();

            for (String pkg : packageNames) {
                try {
                    StorageStats stats = statsManager.queryStatsForPackage(storageUuid, pkg, currentUser);
                    AppCacheInfo info = new AppCacheInfo();
                    info.packageName = pkg;
                    info.cacheSizeBytes = stats.getCacheBytes();
                    results.add(info);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
        return results;
    }

    @Override
    public void purgeSelectedCaches(List<String> packageNames) throws RemoteException {
        if (packageNames == null || packageNames.isEmpty()) return;
        try {
            IBinder binder = ServiceManager.getService("package");
            IPackageManager pm = IPackageManager.Stub.asInterface(binder);
            if (pm != null) {
                for (String pkg : packageNames) {
                    // Requests Android to clear 50GB for this specific package to purge its cache instantly
                    pm.freeStorageAndNotify(pkg, 53687091200L, 0);
                }
            }
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void destroy() { System.exit(0); }
}