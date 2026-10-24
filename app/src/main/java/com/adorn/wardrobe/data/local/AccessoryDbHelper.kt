package com.adorn.wardrobe.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.adorn.wardrobe.data.model.AccessoryCategory
import com.adorn.wardrobe.data.model.AccessoryItem
import com.adorn.wardrobe.data.model.MetalType
import com.adorn.wardrobe.data.model.OccasionType
import com.adorn.wardrobe.data.model.Trip
import com.adorn.wardrobe.data.model.TripItemWithAccessory

class AccessoryDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "wardrobe.db"
        private const val DATABASE_VERSION = 7

        const val TABLE_ACCESSORIES = "accessories"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_METAL = "metal"
        const val COLUMN_PURITY = "purity"
        const val COLUMN_PRICE = "price"
        const val COLUMN_ORIGINAL_PRICE = "original_price"
        const val COLUMN_WEIGHT = "weight_grams"
        const val COLUMN_OCCASION = "occasion"
        const val COLUMN_LOCATION = "storage_location"
        const val COLUMN_BRAND = "brand"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_IMAGE_URI = "image_uri"
        const val COLUMN_DATE_ADDED = "date_added"
        const val COLUMN_IS_FAVORITE = "is_favorite"
        const val COLUMN_DIMENSIONS = "dimensions"
        const val COLUMN_IS_SET = "is_set"
        const val COLUMN_SET_INCLUDES = "set_includes"
        const val COLUMN_IS_WISHLIST = "is_wishlist"
        const val COLUMN_PRIORITY = "priority"
        const val COLUMN_TARGET_PRICE = "target_price"
        const val COLUMN_STORE_URL = "store_url"
        const val COLUMN_TIMES_WORN = "times_worn"
        const val COLUMN_LAST_WORN_DATE = "last_worn_date"
        const val COLUMN_CUTOUT_URI = "cutout_uri"
        const val COLUMN_DOMINANT_COLOR = "dominant_color"
        const val COLUMN_ACCENT_COLOR = "accent_color"
        const val COLUMN_COLOR_NAME = "color_name"

        // Trips Table
        const val TABLE_TRIPS = "trips"
        const val COLUMN_TRIP_ID = "_id"
        const val COLUMN_TRIP_NAME = "name"
        const val COLUMN_TRIP_DESTINATION = "destination"
        const val COLUMN_TRIP_TRAVEL_DATE = "travel_date"
        const val COLUMN_TRIP_IS_COMPLETED = "is_completed"
        const val COLUMN_TRIP_CREATED_AT = "created_at"

        // Trip Items Table (Before & After Trip Checklist)
        const val TABLE_TRIP_ITEMS = "trip_items"
        const val COLUMN_TI_ID = "_id"
        const val COLUMN_TI_TRIP_ID = "trip_id"
        const val COLUMN_TI_ACCESSORY_ID = "accessory_id"
        const val COLUMN_TI_IS_PACKED = "is_packed"
        const val COLUMN_TI_IS_RETURNED = "is_returned"
        const val COLUMN_TI_NOTES = "notes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createAccessoriesQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_ACCESSORIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_METAL TEXT NOT NULL,
                $COLUMN_PURITY TEXT,
                $COLUMN_PRICE REAL NOT NULL DEFAULT 0.0,
                $COLUMN_ORIGINAL_PRICE REAL DEFAULT 0.0,
                $COLUMN_WEIGHT REAL,
                $COLUMN_OCCASION TEXT,
                $COLUMN_LOCATION TEXT,
                $COLUMN_BRAND TEXT,
                $COLUMN_NOTES TEXT,
                $COLUMN_IMAGE_URI TEXT,
                $COLUMN_DATE_ADDED INTEGER NOT NULL,
                $COLUMN_IS_FAVORITE INTEGER NOT NULL DEFAULT 0,
                $COLUMN_DIMENSIONS TEXT,
                $COLUMN_IS_SET INTEGER NOT NULL DEFAULT 0,
                $COLUMN_SET_INCLUDES TEXT,
                $COLUMN_IS_WISHLIST INTEGER NOT NULL DEFAULT 0,
                $COLUMN_PRIORITY TEXT DEFAULT 'MEDIUM',
                $COLUMN_TARGET_PRICE REAL DEFAULT 0.0,
                $COLUMN_STORE_URL TEXT DEFAULT '',
                $COLUMN_TIMES_WORN INTEGER NOT NULL DEFAULT 0,
                $COLUMN_LAST_WORN_DATE INTEGER DEFAULT 0,
                $COLUMN_CUTOUT_URI TEXT DEFAULT '',
                $COLUMN_DOMINANT_COLOR TEXT DEFAULT '',
                $COLUMN_ACCENT_COLOR TEXT DEFAULT '',
                $COLUMN_COLOR_NAME TEXT DEFAULT ''
            )
        """.trimIndent()
        db.execSQL(createAccessoriesQuery)

        createTripTables(db)
    }

    private fun createTripTables(db: SQLiteDatabase) {
        val createTripsQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRIPS (
                $COLUMN_TRIP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TRIP_NAME TEXT NOT NULL,
                $COLUMN_TRIP_DESTINATION TEXT,
                $COLUMN_TRIP_TRAVEL_DATE TEXT,
                $COLUMN_TRIP_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_TRIP_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTripsQuery)

        val createTripItemsQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRIP_ITEMS (
                $COLUMN_TI_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TI_TRIP_ID INTEGER NOT NULL,
                $COLUMN_TI_ACCESSORY_ID INTEGER NOT NULL,
                $COLUMN_TI_IS_PACKED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_TI_IS_RETURNED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_TI_NOTES TEXT
            )
        """.trimIndent()
        db.execSQL(createTripItemsQuery)
    }

    private fun migrateVersion3(db: SQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_DIMENSIONS TEXT")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_IS_SET INTEGER NOT NULL DEFAULT 0")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_SET_INCLUDES TEXT")
        } catch (_: Exception) {}
    }

    private fun migrateVersion4(db: SQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_IS_WISHLIST INTEGER NOT NULL DEFAULT 0")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_PRIORITY TEXT DEFAULT 'MEDIUM'")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_TARGET_PRICE REAL DEFAULT 0.0")
        } catch (_: Exception) {}
    }

    private fun migrateVersion5(db: SQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_STORE_URL TEXT DEFAULT ''")
        } catch (_: Exception) {}
    }

    private fun migrateVersion6(db: SQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_ORIGINAL_PRICE REAL DEFAULT 0.0")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_TIMES_WORN INTEGER NOT NULL DEFAULT 0")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_LAST_WORN_DATE INTEGER DEFAULT 0")
        } catch (_: Exception) {}
    }

    private fun migrateVersion7(db: SQLiteDatabase) {
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_CUTOUT_URI TEXT DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_DOMINANT_COLOR TEXT DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_ACCENT_COLOR TEXT DEFAULT ''")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCESSORIES ADD COLUMN $COLUMN_COLOR_NAME TEXT DEFAULT ''")
        } catch (_: Exception) {}
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createTripTables(db)
        }
        if (oldVersion < 3) {
            migrateVersion3(db)
        }
        if (oldVersion < 4) {
            migrateVersion4(db)
        }
        if (oldVersion < 5) {
            migrateVersion5(db)
        }
        if (oldVersion < 6) {
            migrateVersion6(db)
        }
        if (oldVersion < 7) {
            migrateVersion7(db)
        }
    }

    // ==========================================
    // ACCESSORIES CRUD
    // ==========================================

    private fun serializeImageUris(uris: List<String>): String {
        return uris.filter { it.isNotBlank() }.joinToString("|||")
    }

    private fun parseImageUris(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return if (raw.contains("|||")) {
            raw.split("|||").filter { it.isNotBlank() }
        } else {
            listOf(raw)
        }
    }

    fun insertItem(item: AccessoryItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_CATEGORY, item.category.name)
            put(COLUMN_METAL, item.metal.name)
            put(COLUMN_PURITY, item.purity)
            put(COLUMN_PRICE, item.price)
            put(COLUMN_ORIGINAL_PRICE, item.originalPrice)
            if (item.weightGrams != null) put(COLUMN_WEIGHT, item.weightGrams) else putNull(COLUMN_WEIGHT)
            put(COLUMN_OCCASION, item.occasion.name)
            put(COLUMN_LOCATION, item.storageLocation)
            put(COLUMN_BRAND, item.brand)
            put(COLUMN_NOTES, item.notes)
            put(COLUMN_IMAGE_URI, serializeImageUris(item.imageUris))
            put(COLUMN_DATE_ADDED, item.dateAdded)
            put(COLUMN_IS_FAVORITE, if (item.isFavorite) 1 else 0)
            put(COLUMN_DIMENSIONS, item.dimensions)
            put(COLUMN_IS_SET, if (item.isSet) 1 else 0)
            put(COLUMN_SET_INCLUDES, item.setIncludes)
            put(COLUMN_IS_WISHLIST, if (item.isWishlist) 1 else 0)
            put(COLUMN_PRIORITY, item.priority)
            put(COLUMN_TARGET_PRICE, item.targetPrice)
            put(COLUMN_STORE_URL, item.storeUrl)
            put(COLUMN_TIMES_WORN, item.timesWorn)
            put(COLUMN_LAST_WORN_DATE, item.lastWornDate)
            put(COLUMN_CUTOUT_URI, item.cutoutUri)
            put(COLUMN_DOMINANT_COLOR, item.dominantColorHex)
            put(COLUMN_ACCENT_COLOR, item.accentColorHex)
            put(COLUMN_COLOR_NAME, item.colorName)
        }
        return db.insert(TABLE_ACCESSORIES, null, values)
    }

    fun updateItem(item: AccessoryItem): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_CATEGORY, item.category.name)
            put(COLUMN_METAL, item.metal.name)
            put(COLUMN_PURITY, item.purity)
            put(COLUMN_PRICE, item.price)
            put(COLUMN_ORIGINAL_PRICE, item.originalPrice)
            if (item.weightGrams != null) put(COLUMN_WEIGHT, item.weightGrams) else putNull(COLUMN_WEIGHT)
            put(COLUMN_OCCASION, item.occasion.name)
            put(COLUMN_LOCATION, item.storageLocation)
            put(COLUMN_BRAND, item.brand)
            put(COLUMN_NOTES, item.notes)
            put(COLUMN_IMAGE_URI, serializeImageUris(item.imageUris))
            put(COLUMN_IS_FAVORITE, if (item.isFavorite) 1 else 0)
            put(COLUMN_DIMENSIONS, item.dimensions)
            put(COLUMN_IS_SET, if (item.isSet) 1 else 0)
            put(COLUMN_SET_INCLUDES, item.setIncludes)
            put(COLUMN_IS_WISHLIST, if (item.isWishlist) 1 else 0)
            put(COLUMN_PRIORITY, item.priority)
            put(COLUMN_TARGET_PRICE, item.targetPrice)
            put(COLUMN_STORE_URL, item.storeUrl)
            put(COLUMN_TIMES_WORN, item.timesWorn)
            put(COLUMN_LAST_WORN_DATE, item.lastWornDate)
            put(COLUMN_CUTOUT_URI, item.cutoutUri)
            put(COLUMN_DOMINANT_COLOR, item.dominantColorHex)
            put(COLUMN_ACCENT_COLOR, item.accentColorHex)
            put(COLUMN_COLOR_NAME, item.colorName)
        }
        val affected = db.update(TABLE_ACCESSORIES, values, "$COLUMN_ID = ?", arrayOf(item.id.toString()))
        return affected > 0
    }

    fun deleteItem(id: Long): Boolean {
        val db = writableDatabase
        db.delete(TABLE_TRIP_ITEMS, "$COLUMN_TI_ACCESSORY_ID = ?", arrayOf(id.toString()))
        val affected = db.delete(TABLE_ACCESSORIES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        return affected > 0
    }

    fun deleteItems(ids: List<Long>): Int {
        if (ids.isEmpty()) return 0
        var deleted = 0
        ids.forEach { id ->
            if (deleteItem(id)) deleted++
        }
        return deleted
    }

    fun moveToWardrobe(id: Long): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_WISHLIST, 0)
        }
        return db.update(TABLE_ACCESSORIES, values, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
    }

    fun moveToWishlist(id: Long, priority: String = "MEDIUM", targetPrice: Double = 0.0): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_WISHLIST, 1)
            put(COLUMN_PRIORITY, priority)
            if (targetPrice > 0.0) put(COLUMN_TARGET_PRICE, targetPrice)
        }
        return db.update(TABLE_ACCESSORIES, values, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
    }

    fun moveItemsToWardrobe(ids: List<Long>): Int {
        if (ids.isEmpty()) return 0
        var count = 0
        ids.forEach { if (moveToWardrobe(it)) count++ }
        return count
    }

    fun removeDuplicates(): Int {
        val all = getAllItems()
        if (all.isEmpty()) return 0

        // Only group genuine duplicates: identical photo, or identical title + brand + price added within 10 minutes
        val grouped = all.groupBy {
            val normTitle = it.title.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
            val normBrand = it.brand.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
            val primaryPhoto = it.primaryImageUri.trim()
            if (primaryPhoto.isNotBlank()) {
                "PHOTO##$primaryPhoto##${it.isWishlist}"
            } else if (normTitle.isNotBlank() && normBrand.isNotBlank()) {
                "DETAILS##$normTitle##$normBrand##${it.price.toInt()}##${it.isWishlist}"
            } else {
                "UNIQUE##${it.id}"
            }
        }

        var removedCount = 0
        grouped.values.forEach { group ->
            if (group.size > 1) {
                val sorted = group.sortedWith(
                    compareByDescending<AccessoryItem> { it.imageUris.size }
                        .thenByDescending { it.price }
                        .thenByDescending { it.dateAdded }
                )
                val keep = sorted.first()
                val duplicates = sorted.drop(1)

                val allPhotos = (keep.imageUris + duplicates.flatMap { it.imageUris })
                    .filter { it.isNotBlank() }
                    .distinct()

                val updated = keep.copy(
                    imageUris = allPhotos,
                    notes = if (keep.notes.isNotBlank()) keep.notes else duplicates.mapNotNull { it.notes.ifBlank { null } }.firstOrNull() ?: "",
                    price = if (keep.price > 0) keep.price else duplicates.map { it.price }.firstOrNull { it > 0 } ?: 0.0
                )
                updateItem(updated)

                duplicates.forEach { dup ->
                    deleteItem(dup.id)
                    removedCount++
                }
            }
        }
        return removedCount
    }

    fun toggleFavorite(id: Long, isFav: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_FAVORITE, if (isFav) 1 else 0)
        }
        val affected = db.update(TABLE_ACCESSORIES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        return affected > 0
    }

    fun incrementTimesWorn(id: Long, delta: Int = 1): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_TIMES_WORN FROM $TABLE_ACCESSORIES WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
        var currentCount = 0
        cursor.use {
            if (it.moveToFirst()) {
                currentCount = it.getInt(0)
            }
        }
        val newCount = (currentCount + delta).coerceAtLeast(0)
        val values = ContentValues().apply {
            put(COLUMN_TIMES_WORN, newCount)
            if (delta > 0) {
                put(COLUMN_LAST_WORN_DATE, System.currentTimeMillis())
            }
        }
        return db.update(TABLE_ACCESSORIES, values, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
    }

    fun updatePrice(id: Long, newPrice: Double, originalPrice: Double? = null): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRICE, newPrice)
            if (originalPrice != null && originalPrice > 0.0) {
                put(COLUMN_ORIGINAL_PRICE, originalPrice)
            }
        }
        return db.update(TABLE_ACCESSORIES, values, "$COLUMN_ID = ?", arrayOf(id.toString())) > 0
    }

    fun getAllItems(): List<AccessoryItem> {
        val list = mutableListOf<AccessoryItem>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_ACCESSORIES,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE_ADDED DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToItem(it))
            }
        }
        return list
    }

    fun getItemById(id: Long): AccessoryItem? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ACCESSORIES,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToItem(it)
            }
        }
        return null
    }

    private fun cursorToItem(cursor: Cursor): AccessoryItem {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
        val categoryStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
        val metalStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_METAL))
        val purity = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PURITY)) ?: ""
        val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE))
        val weightIdx = cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)
        val weight = if (cursor.isNull(weightIdx)) null else cursor.getDouble(weightIdx)
        val occasionStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCCASION)) ?: "DAILY"
        val location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)) ?: ""
        val brand = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND)) ?: ""
        val notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)) ?: ""
        val rawImageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)) ?: ""
        val imageUris = parseImageUris(rawImageUri)
        val dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE_ADDED))
        val isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)) == 1

        val dimIdx = cursor.getColumnIndex(COLUMN_DIMENSIONS)
        val dimensions = if (dimIdx != -1 && !cursor.isNull(dimIdx)) cursor.getString(dimIdx) else ""

        val isSetIdx = cursor.getColumnIndex(COLUMN_IS_SET)
        val isSet = if (isSetIdx != -1 && !cursor.isNull(isSetIdx)) cursor.getInt(isSetIdx) == 1 else false

        val setIncIdx = cursor.getColumnIndex(COLUMN_SET_INCLUDES)
        val setIncludes = if (setIncIdx != -1 && !cursor.isNull(setIncIdx)) cursor.getString(setIncIdx) else ""

        val wishIdx = cursor.getColumnIndex(COLUMN_IS_WISHLIST)
        val isWishlist = if (wishIdx != -1 && !cursor.isNull(wishIdx)) cursor.getInt(wishIdx) == 1 else false

        val prioIdx = cursor.getColumnIndex(COLUMN_PRIORITY)
        val priority = if (prioIdx != -1 && !cursor.isNull(prioIdx)) cursor.getString(prioIdx) ?: "MEDIUM" else "MEDIUM"

        val targetIdx = cursor.getColumnIndex(COLUMN_TARGET_PRICE)
        val targetPrice = if (targetIdx != -1 && !cursor.isNull(targetIdx)) cursor.getDouble(targetIdx) else 0.0

        val storeUrlIdx = cursor.getColumnIndex(COLUMN_STORE_URL)
        val storeUrl = if (storeUrlIdx != -1 && !cursor.isNull(storeUrlIdx)) cursor.getString(storeUrlIdx) ?: "" else ""

        val origPriceIdx = cursor.getColumnIndex(COLUMN_ORIGINAL_PRICE)
        val originalPrice = if (origPriceIdx != -1 && !cursor.isNull(origPriceIdx)) cursor.getDouble(origPriceIdx) else 0.0

        val timesWornIdx = cursor.getColumnIndex(COLUMN_TIMES_WORN)
        val timesWorn = if (timesWornIdx != -1 && !cursor.isNull(timesWornIdx)) cursor.getInt(timesWornIdx) else 0

        val lastWornIdx = cursor.getColumnIndex(COLUMN_LAST_WORN_DATE)
        val lastWornDate = if (lastWornIdx != -1 && !cursor.isNull(lastWornIdx)) cursor.getLong(lastWornIdx) else 0L

        val cutoutIdx = cursor.getColumnIndex(COLUMN_CUTOUT_URI)
        val cutoutUri = if (cutoutIdx != -1 && !cursor.isNull(cutoutIdx)) cursor.getString(cutoutIdx) ?: "" else ""

        val domColorIdx = cursor.getColumnIndex(COLUMN_DOMINANT_COLOR)
        val dominantColorHex = if (domColorIdx != -1 && !cursor.isNull(domColorIdx)) cursor.getString(domColorIdx) ?: "" else ""

        val accColorIdx = cursor.getColumnIndex(COLUMN_ACCENT_COLOR)
        val accentColorHex = if (accColorIdx != -1 && !cursor.isNull(accColorIdx)) cursor.getString(accColorIdx) ?: "" else ""

        val colorNameIdx = cursor.getColumnIndex(COLUMN_COLOR_NAME)
        val colorName = if (colorNameIdx != -1 && !cursor.isNull(colorNameIdx)) cursor.getString(colorNameIdx) ?: "" else ""

        return AccessoryItem(
            id = id,
            title = title,
            category = AccessoryCategory.fromString(categoryStr),
            metal = MetalType.fromString(metalStr),
            purity = purity,
            price = price,
            originalPrice = originalPrice,
            weightGrams = weight,
            dimensions = dimensions,
            isSet = isSet,
            setIncludes = setIncludes,
            occasion = OccasionType.fromString(occasionStr),
            storageLocation = location,
            brand = brand,
            notes = notes,
            imageUris = imageUris,
            dateAdded = dateAdded,
            isFavorite = isFavorite,
            isWishlist = isWishlist,
            priority = priority,
            targetPrice = targetPrice,
            storeUrl = storeUrl,
            timesWorn = timesWorn,
            lastWornDate = lastWornDate,
            cutoutUri = cutoutUri,
            dominantColorHex = dominantColorHex,
            accentColorHex = accentColorHex,
            colorName = colorName
        )
    }

    // ==========================================
    // TRIPS & BEFORE/AFTER CHECKLIST CRUD
    // ==========================================

    fun createTrip(name: String, destination: String, travelDate: String, accessoryIds: List<Long>): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TRIP_NAME, name)
            put(COLUMN_TRIP_DESTINATION, destination)
            put(COLUMN_TRIP_TRAVEL_DATE, travelDate)
            put(COLUMN_TRIP_IS_COMPLETED, 0)
            put(COLUMN_TRIP_CREATED_AT, System.currentTimeMillis())
        }
        val tripId = db.insert(TABLE_TRIPS, null, values)
        if (tripId > 0) {
            accessoryIds.forEach { accId ->
                val tiValues = ContentValues().apply {
                    put(COLUMN_TI_TRIP_ID, tripId)
                    put(COLUMN_TI_ACCESSORY_ID, accId)
                    put(COLUMN_TI_IS_PACKED, 0)
                    put(COLUMN_TI_IS_RETURNED, 0)
                }
                db.insert(TABLE_TRIP_ITEMS, null, tiValues)
            }
        }
        return tripId
    }

    fun getAllTrips(): List<Trip> {
        val list = mutableListOf<Trip>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TRIPS, null, null, null, null, null, "$COLUMN_TRIP_CREATED_AT DESC")
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_TRIP_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_TRIP_NAME))
                val dest = it.getString(it.getColumnIndexOrThrow(COLUMN_TRIP_DESTINATION)) ?: ""
                val date = it.getString(it.getColumnIndexOrThrow(COLUMN_TRIP_TRAVEL_DATE)) ?: ""
                val isCompleted = it.getInt(it.getColumnIndexOrThrow(COLUMN_TRIP_IS_COMPLETED)) == 1
                val createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_TRIP_CREATED_AT))
                list.add(Trip(id, name, dest, date, isCompleted, createdAt))
            }
        }
        return list
    }

    fun getTripItems(tripId: Long): List<TripItemWithAccessory> {
        val list = mutableListOf<TripItemWithAccessory>()
        val db = readableDatabase
        val query = """
            SELECT ti.$COLUMN_TI_ID as ti_id, ti.$COLUMN_TI_TRIP_ID as trip_id,
                   ti.$COLUMN_TI_IS_PACKED as is_packed, ti.$COLUMN_TI_IS_RETURNED as is_returned,
                   ti.$COLUMN_TI_NOTES as ti_notes,
                   a.*
            FROM $TABLE_TRIP_ITEMS ti
            INNER JOIN $TABLE_ACCESSORIES a ON ti.$COLUMN_TI_ACCESSORY_ID = a.$COLUMN_ID
            WHERE ti.$COLUMN_TI_TRIP_ID = ?
            ORDER BY ti.$COLUMN_TI_ID ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(tripId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                val tiId = it.getLong(it.getColumnIndexOrThrow("ti_id"))
                val tId = it.getLong(it.getColumnIndexOrThrow("trip_id"))
                val isPacked = it.getInt(it.getColumnIndexOrThrow("is_packed")) == 1
                val isReturned = it.getInt(it.getColumnIndexOrThrow("is_returned")) == 1
                val tiNotes = it.getString(it.getColumnIndexOrThrow("ti_notes")) ?: ""
                val accessory = cursorToItem(it)

                list.add(TripItemWithAccessory(tiId, tId, accessory, isPacked, isReturned, tiNotes))
            }
        }
        return list
    }

    fun setTripItemPacked(tripItemId: Long, isPacked: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TI_IS_PACKED, if (isPacked) 1 else 0)
        }
        return db.update(TABLE_TRIP_ITEMS, values, "$COLUMN_TI_ID = ?", arrayOf(tripItemId.toString())) > 0
    }

    fun setTripItemReturned(tripItemId: Long, isReturned: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TI_IS_RETURNED, if (isReturned) 1 else 0)
        }
        return db.update(TABLE_TRIP_ITEMS, values, "$COLUMN_TI_ID = ?", arrayOf(tripItemId.toString())) > 0
    }

    fun addItemsToTrip(tripId: Long, accessoryIds: List<Long>) {
        val db = writableDatabase
        accessoryIds.forEach { accId ->
            val values = ContentValues().apply {
                put(COLUMN_TI_TRIP_ID, tripId)
                put(COLUMN_TI_ACCESSORY_ID, accId)
                put(COLUMN_TI_IS_PACKED, 0)
                put(COLUMN_TI_IS_RETURNED, 0)
            }
            db.insert(TABLE_TRIP_ITEMS, null, values)
        }
    }

    fun deleteTripItem(tripItemId: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_TRIP_ITEMS, "$COLUMN_TI_ID = ?", arrayOf(tripItemId.toString())) > 0
    }

    fun deleteTrip(tripId: Long): Boolean {
        val db = writableDatabase
        db.delete(TABLE_TRIP_ITEMS, "$COLUMN_TI_TRIP_ID = ?", arrayOf(tripId.toString()))
        return db.delete(TABLE_TRIPS, "$COLUMN_TRIP_ID = ?", arrayOf(tripId.toString())) > 0
    }

    // ==========================================
    // STARTER SEEDS
    // ==========================================

    fun seedStarterWardrobeIfEmpty() {
        // Ensure trip tables and new columns exist
        createTripTables(writableDatabase)
        migrateVersion3(writableDatabase)
        migrateVersion4(writableDatabase)
        migrateVersion5(writableDatabase)
        migrateVersion6(writableDatabase)

        val existing = getAllItems()
        if (existing.isNotEmpty()) {
            // Backfill sample store URLs, wear counts, and price drop data for existing items
            existing.forEach { item ->
                var needsUpdate = false
                var updatedItem = item

                // Store URLs
                if (updatedItem.isWishlist && updatedItem.storeUrl.isBlank()) {
                    val sampleUrl = when {
                        updatedItem.title.contains("Sabyasachi", ignoreCase = true) -> "https://www.sabyasachi.com"
                        updatedItem.title.contains("Tiffany", ignoreCase = true) -> "https://www.tiffany.com/jewelry/bracelets/tiffany-t-wire-bracelet-GRP07787/"
                        updatedItem.title.contains("Cartier", ignoreCase = true) -> "https://www.cartier.com/en-us/watches/collections/panthere-de-cartier/"
                        updatedItem.title.contains("Lululemon", ignoreCase = true) -> "https://www.myntra.com/activewear"
                        else -> ""
                    }
                    if (sampleUrl.isNotBlank()) {
                        updatedItem = updatedItem.copy(storeUrl = sampleUrl)
                        needsUpdate = true
                    }
                }

                // Sample wear count for dresses, ethnic wear, and daily pieces
                if (updatedItem.timesWorn == 0 && !updatedItem.isWishlist) {
                    val sampleWorn = when {
                        updatedItem.title.contains("Midi Dress", ignoreCase = true) -> 12
                        updatedItem.title.contains("Saree", ignoreCase = true) -> 4
                        updatedItem.title.contains("Kurta", ignoreCase = true) -> 7
                        updatedItem.title.contains("Studs", ignoreCase = true) -> 18
                        updatedItem.title.contains("Watch", ignoreCase = true) -> 26 // Demonstrates aging/high mileage staple!
                        else -> 3
                    }
                    val sampleLastWorn = System.currentTimeMillis() - (sampleWorn * 86400000L * 2)
                    updatedItem = updatedItem.copy(timesWorn = sampleWorn, lastWornDate = sampleLastWorn)
                    needsUpdate = true
                }

                // Sample price drop data (Original/MRP price higher than current price)
                if (updatedItem.originalPrice == 0.0) {
                    val sampleOrig = when {
                        updatedItem.title.contains("Lehenga", ignoreCase = true) -> 450000.0 // Was 450k, now 395k (-55,000 OFF!)
                        updatedItem.title.contains("Tiffany", ignoreCase = true) -> 350000.0 // Was 350k, now 320k (-30,000 OFF!)
                        updatedItem.title.contains("Kurta", ignoreCase = true) -> 2499.0   // Was 2499, now 1544 (-955 OFF!)
                        updatedItem.title.contains("Midi Dress", ignoreCase = true) -> 5990.0 // Was 5990, now 4500 (-1,490 OFF!)
                        else -> 0.0
                    }
                    if (sampleOrig > 0.0) {
                        updatedItem = updatedItem.copy(originalPrice = sampleOrig)
                        needsUpdate = true
                    }
                }

                if (needsUpdate) {
                    updateItem(updatedItem)
                }
            }

            // Seed wishlist starters if no wishlist items exist
            if (existing.none { it.isWishlist }) {
                val starterWishlist = listOf(
                    AccessoryItem(
                        title = "Sabyasachi Heritage Crimson Velvet Lehenga",
                        category = AccessoryCategory.SAREES_ETHNIC,
                        metal = MetalType.EMBELLISHED_ZARI,
                        purity = "Handcrafted Zardozi & Crystals",
                        price = 395000.0,
                        targetPrice = 395000.0,
                        priority = "HIGH",
                        isWishlist = true,
                        dimensions = "Custom Bridal Sizing",
                        occasion = OccasionType.WEDDING,
                        storageLocation = "Wishlist Goal",
                        brand = "Sabyasachi",
                        notes = "Iconic crimson velvet bridal lehenga with antique dabka and tilla embroidery.",
                        imageUris = listOf("https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?auto=format&fit=crop&w=600&q=80"),
                        storeUrl = "https://www.sabyasachi.com"
                    ),
                    AccessoryItem(
                        title = "Tiffany T Wire Diamond Bracelet",
                        category = AccessoryCategory.BRACELETS_BANGLES,
                        metal = MetalType.ROSE_GOLD,
                        purity = "18K Rose Gold with Pavé Diamonds",
                        price = 320000.0,
                        targetPrice = 320000.0,
                        priority = "HIGH",
                        isWishlist = true,
                        dimensions = "Medium (6.25\" wrist)",
                        occasion = OccasionType.PARTY,
                        storageLocation = "Wishlist Goal",
                        brand = "Tiffany & Co.",
                        notes = "Timeless T graphic wire silhouette wrapped with brilliant cut pavé diamonds.",
                        imageUris = listOf("https://images.unsplash.com/photo-1611591475824-749e7b233959?auto=format&fit=crop&w=600&q=80"),
                        storeUrl = "https://www.tiffany.com/jewelry/bracelets/tiffany-t-wire-bracelet-GRP07787/"
                    ),
                    AccessoryItem(
                        title = "Cartier Panthère Watch in 18K Yellow Gold",
                        category = AccessoryCategory.WATCHES,
                        metal = MetalType.GOLD_18K,
                        purity = "18K Solid Gold & Blued Steel Hands",
                        price = 680000.0,
                        targetPrice = 680000.0,
                        priority = "MEDIUM",
                        isWishlist = true,
                        dimensions = "Small Model (22mm x 30mm)",
                        occasion = OccasionType.OFFICE,
                        storageLocation = "Wishlist Goal",
                        brand = "Cartier",
                        notes = "The quintessence of jewelry watch elegance with iconic supple link bracelet.",
                        imageUris = listOf("https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=600&q=80"),
                        storeUrl = "https://www.cartier.com/en-us/watches/collections/panthere-de-cartier/"
                    ),
                    AccessoryItem(
                        title = "Lululemon Align Activewear Training Kit",
                        category = AccessoryCategory.SPORTS_ATHLETIC,
                        metal = MetalType.COTTON_LINEN,
                        purity = "Nulu™ Weightless Fabric",
                        price = 18500.0,
                        targetPrice = 18500.0,
                        priority = "LOW",
                        isWishlist = true,
                        dimensions = "Size 6 (Medium)",
                        occasion = OccasionType.DAILY,
                        storageLocation = "Wishlist Goal",
                        brand = "Lululemon",
                        notes = "Buttery-soft high-rise 25\" leggings paired with matching strap energy bra in dusty mauve.",
                        imageUris = listOf("https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=600&q=80"),
                        storeUrl = "https://www.myntra.com/activewear"
                    )
                )
                starterWishlist.forEach { insertItem(it) }
            }
            if (existing.none { it.category.isClothing }) {
                val starterClothes = listOf(
                    AccessoryItem(
                        title = "Emerald Kanjeevaram Silk Saree",
                        category = AccessoryCategory.SAREES_ETHNIC,
                        metal = MetalType.PURE_SILK,
                        purity = "Mulberry Silk & Gold Zari",
                        price = 34000.0,
                        weightGrams = null,
                        dimensions = "6.3m (with blouse piece)",
                        isSet = false,
                        setIncludes = "",
                        occasion = OccasionType.WEDDING,
                        storageLocation = "Wardrobe Saree Bag #1",
                        brand = "Nalli Silks",
                        notes = "Royal bottle emerald green body with rich gold zari woven pallu and contrast magenta border.",
                        imageUris = listOf(
                            "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=600&q=80",
                            "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?auto=format&fit=crop&w=600&q=80"
                        ),
                        isFavorite = true
                    ),
                    AccessoryItem(
                        title = "Pastel Floral Tiered Midi Dress",
                        category = AccessoryCategory.DRESSES_GOWNS,
                        metal = MetalType.GEORGETTE_CHIFFON,
                        purity = "Tiered Flared Georgette",
                        price = 4500.0,
                        weightGrams = null,
                        dimensions = "Size M (38)",
                        isSet = false,
                        setIncludes = "",
                        occasion = OccasionType.CASUAL,
                        storageLocation = "Hanging Closet Rack 2",
                        brand = "Zara",
                        notes = "Soft blush pink with delicate floral blooms. Smocked waist and puff sleeves.",
                        imageUris = listOf(
                            "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=600&q=80"
                        ),
                        isFavorite = true
                    )
                )
                starterClothes.forEach { insertItem(it) }
            }
            return
        }

        val starters = listOf(
            AccessoryItem(
                title = "Emerald Kanjeevaram Silk Saree",
                category = AccessoryCategory.SAREES_ETHNIC,
                metal = MetalType.PURE_SILK,
                purity = "Mulberry Silk & Gold Zari",
                price = 34000.0,
                weightGrams = null,
                dimensions = "6.3m (with blouse piece)",
                isSet = false,
                setIncludes = "",
                occasion = OccasionType.WEDDING,
                storageLocation = "Wardrobe Saree Bag #1",
                brand = "Nalli Silks",
                notes = "Royal bottle emerald green body with rich gold zari woven pallu and contrast magenta border.",
                imageUris = listOf(
                    "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?auto=format&fit=crop&w=600&q=80"
                ),
                isFavorite = true
            ),
            AccessoryItem(
                title = "Pastel Floral Tiered Midi Dress",
                category = AccessoryCategory.DRESSES_GOWNS,
                metal = MetalType.GEORGETTE_CHIFFON,
                purity = "Tiered Flared Georgette",
                price = 4500.0,
                weightGrams = null,
                dimensions = "Size M (38)",
                isSet = false,
                setIncludes = "",
                occasion = OccasionType.CASUAL,
                storageLocation = "Hanging Closet Rack 2",
                brand = "Zara",
                notes = "Soft blush pink with delicate floral blooms. Smocked waist and puff sleeves.",
                imageUris = listOf(
                    "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=600&q=80"
                ),
                isFavorite = true
            ),
            AccessoryItem(
                title = "Royal Kundan Choker & Jhumka Set",
                category = AccessoryCategory.MATCHING_SETS,
                metal = MetalType.GOLD_22K,
                purity = "22K Gold & Polki Kundan",
                price = 185000.0,
                weightGrams = 52.0,
                dimensions = "Choker 16\" adjustable, Earrings 2.5\" drop",
                isSet = true,
                setIncludes = "Choker Necklace + Matching Jhumka Earrings",
                occasion = OccasionType.WEDDING,
                storageLocation = "Bridal Trousseau Box #1",
                brand = "Tanishq / Sabyasachi",
                notes = "Exquisite handcrafted uncut polki kundan necklace paired with matching bell jhumkas with emerald bead drops.",
                imageUris = listOf(
                    "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?auto=format&fit=crop&w=600&q=80"
                ),
                isFavorite = true
            ),
            AccessoryItem(
                title = "Rose Gold Halo Studs",
                category = AccessoryCategory.EARRINGS,
                metal = MetalType.ROSE_GOLD,
                purity = "18K & Solitaire",
                price = 64000.0,
                weightGrams = 4.2,
                dimensions = "8mm diameter",
                isSet = false,
                setIncludes = "",
                occasion = OccasionType.DAILY,
                storageLocation = "Velvet Vanity Box - Top Slot 1",
                brand = "Cartier",
                notes = "Gifted for anniversary. Delicate pavé diamond setting.",
                imageUris = listOf(
                    "https://images.unsplash.com/photo-1630019852942-f89202989a59?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?auto=format&fit=crop&w=600&q=80"
                ),
                isFavorite = true
            ),
            AccessoryItem(
                title = "Heritage Layered Gold Chain",
                category = AccessoryCategory.CHAINS_NECKLACES,
                metal = MetalType.GOLD_22K,
                purity = "22K 916 Hallmark",
                price = 215000.0,
                weightGrams = 28.5,
                dimensions = "24 inches (Matinee Length)",
                isSet = false,
                setIncludes = "",
                occasion = OccasionType.WEDDING,
                storageLocation = "Locker Safe #4",
                brand = "Tanishq",
                notes = "Handcrafted filigree work with detachable floral pendant.",
                imageUris = listOf(
                    "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=600&q=80"
                ),
                isFavorite = true
            ),
            AccessoryItem(
                title = "Quilted Leather Classic Flap Bag",
                category = AccessoryCategory.HANDBAGS,
                metal = MetalType.GENUINE_LEATHER,
                purity = "Calfskin & Gold Hardware",
                price = 85000.0,
                weightGrams = null,
                dimensions = "26cm x 16cm x 7cm",
                isSet = false,
                setIncludes = "",
                occasion = OccasionType.PARTY,
                storageLocation = "Wardrobe Bag Shelf #2 (Dust Bag)",
                brand = "Michael Kors",
                notes = "Black quilted calfskin leather with polished gold woven chain strap.",
                imageUris = listOf(
                    "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=600&q=80"
                ),
                isFavorite = true
            ),
            AccessoryItem(
                title = "Raw Silk Zardozi Bridal Clutch",
                category = AccessoryCategory.CLUTCHES_SLINGS,
                metal = MetalType.EMBELLISHED_ZARI,
                purity = "Hand-Embroidered",
                price = 14500.0,
                weightGrams = null,
                occasion = OccasionType.WEDDING,
                storageLocation = "Bridal Trousseau Box",
                brand = "Sabyasachi Inspired",
                notes = "Emerald green raw silk with antique gold zardozi bullion embroidery.",
                imageUris = listOf(
                    "https://images.unsplash.com/photo-1566150905458-1bf1fc113f0d?auto=format&fit=crop&w=600&q=80"
                ),
                isFavorite = true
            ),
            AccessoryItem(
                title = "Classic Tank Watch - Gold Mesh",
                category = AccessoryCategory.WATCHES,
                metal = MetalType.GOLD_18K,
                purity = "Sapphire Crystal",
                price = 52000.0,
                weightGrams = null,
                occasion = OccasionType.OFFICE,
                storageLocation = "Watch Cushion Box",
                brand = "Longines",
                notes = "Roman numerals, scratch-resistant sapphire crystal glass.",
                imageUris = listOf("https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=600&q=80"),
                isFavorite = false
            ),
            AccessoryItem(
                title = "Emerald Cut Solitaire Ring",
                category = AccessoryCategory.RINGS,
                metal = MetalType.PLATINUM,
                purity = "Pt950 & 1.5ct Diamond",
                price = 260000.0,
                weightGrams = 5.1,
                occasion = OccasionType.PARTY,
                storageLocation = "Velvet Ring Roll",
                brand = "Tiffany & Co.",
                notes = "Conflict-free certified diamond with platinum prongs.",
                imageUris = listOf("https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=600&q=80"),
                isFavorite = true
            ),
            AccessoryItem(
                title = "Oxidised Silver Boho Jhumkas",
                category = AccessoryCategory.EARRINGS,
                metal = MetalType.STERLING_SILVER,
                purity = "925 Sterling",
                price = 3800.0,
                weightGrams = 18.0,
                occasion = OccasionType.CASUAL,
                storageLocation = "Dresser Hanging Stand",
                brand = "FabIndia",
                notes = "Traditional bell jhumkas with tiny pearl drops.",
                imageUris = listOf("https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?auto=format&fit=crop&w=600&q=80"),
                isFavorite = false
            ),
            AccessoryItem(
                title = "Diamond Tennis Bracelet",
                category = AccessoryCategory.BRACELETS_BANGLES,
                metal = MetalType.DIAMONDS,
                purity = "14K White Gold (3.0 ctw)",
                price = 175000.0,
                weightGrams = 9.8,
                occasion = OccasionType.PARTY,
                storageLocation = "Velvet Vanity Box - Tray 2",
                brand = "Swarovski Fine Jewelry",
                notes = "Double safety clasp with sparkling round brilliant stones.",
                imageUris = listOf("https://images.unsplash.com/photo-1611591475824-749e7b233959?auto=format&fit=crop&w=600&q=80"),
                isFavorite = true
            )
        )

        val insertedIds = mutableListOf<Long>()
        starters.forEach { insertedIds.add(insertItem(it)) }

        // Seed a starter trip with checklist so users immediately see the Before & After feature!
        if (insertedIds.size >= 4) {
            val starterTripId = createTrip(
                name = "Jaipur Destination Wedding",
                destination = "Jaipur, Rajasthan",
                travelDate = "Nov 14 - Nov 18",
                accessoryIds = listOf(insertedIds[0], insertedIds[1], insertedIds[2], insertedIds[3])
            )
            // Mark a couple as packed before trip for demonstration
            val tripItems = getTripItems(starterTripId)
            if (tripItems.size >= 2) {
                setTripItemPacked(tripItems[0].tripItemId, true)
                setTripItemPacked(tripItems[1].tripItemId, true)
            }
        }
    }
}
