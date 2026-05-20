CachePurge (Shizuku edition)


CachePurge is a super lightweight, lightning-fast android utility built specifically for personal user optimization of your device. Unlike dozens of legacy "cache cleaner" apps which use slow, sluggish Accessibility Services to visually slam clicky clicky-through unnatural menus, this app strategically integrates directly with the Shizuku API to execute pure silent background operations with native system-level ADB permissions.
Uses a scanner to find out what apps you've installed to know how much cache you're consuming, then, with one single click, easily toll away to get rid of all the useless temporary files you don't need! 

Highlights
Powerful Shizuku Integration: Circumvents the restrictions of Android's sandboxing fiercely. It accesses a modern Shizuku UserService to run native Java/AIDL code with powerful shell status, completely deterring the need for a rooted device!

Intelligent Footprint Sorting: Grouping up the exact cache storage size of every installed application via querying Android's internal StorageStatsManager. Sorted/enumerated your app repository from biggest cache consumer to smallest!

Focused & Discrete Auto-cleaning: Individual checkboxes show up next to each application to selectively choose which app data you want to clear! Use the handy-dandy global 'Select All' toggle switch to wipe-and-clean your entire cache database at once!

Dynamic Search Filter: Did you know this app was made with a collapsable, real-time search bar? Its instant speed instantly finds and filters your list based on the letter character(s) you type, as it happens, by the application name or package label id.

Dynamic Refresh Mechanics: A manual refresh button makes sure you're ready for a real-time scan of newly installed application precursors and recalculates your cache sizes when you remove any foreground temporary files with a single click!

No Recurring Bloat or Annoying Ads: Made entirely through field-coding, visual components; no distracting cumbersome hunkering external libraries that "track app metrics".



How it works, under-the-hood style
Authenticator: Upon start, the app contacts the file system-bound Shizuku background daemon. If user consent is given, it builds a unique custom privileged IPC (Inter-Process Communication) conduit via AIDL.
Analysis: The privileged background service performs a non-UI blocking calculation of app dimensions to not cause any scrolling lags or UI hanging hazards.
The Cleansing Feat: Instead of a destructive wipe of data (which would have gotten you logged out of what you just cleaned), the app intelligently takes advantage of Android's native freeStorageAndNotify API. It requests a high-storage capacity quota directly to your presently selected app's package lifetime, engaging Android to empty its runtime's cache temporary folders immediately!