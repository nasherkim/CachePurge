package com.example.cachecleaner;

import java.util.List;
import com.example.cachecleaner.AppCacheInfo;

interface ICacheService {
    List<AppCacheInfo> getCacheSizes(in List<String> packageNames);
    void purgeSelectedCaches(in List<String> packageNames);
    void destroy();
}