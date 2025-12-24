package net.uniquecomputer.houseguru

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS_TABLE)

        val adminValues = ContentValues().apply {
            put(COLUMN_NAME, "Admin")
            put(COLUMN_PHONE, DEFAULT_ADMIN_PHONE)
            put(COLUMN_EMAIL, "admin@houseguru.com")
            put(COLUMN_PASSWORD, DEFAULT_ADMIN_PASSWORD)
            put(COLUMN_ROLE, "admin")
        }
        db.insert(TABLE_USERS, null, adminValues)

        db.execSQL(CREATE_SERVICES_TABLE)
        db.execSQL(CREATE_ADDRESSES_TABLE)

        db.execSQL(CREATE_CARS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SERVICES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ADDRESSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun insertUser(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: String = "user"
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PHONE, phone)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_ROLE, role)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun isPhoneRegistered(phone: String): Boolean {
        val db = readableDatabase
        var cursor: Cursor? = null
        return try {
            cursor = db.query(
                TABLE_USERS,
                arrayOf(COLUMN_USER_ID),
                "$COLUMN_PHONE = ?",
                arrayOf(phone),
                null,
                null,
                null
            )
            cursor.count > 0
        } finally {
            cursor?.close()
        }
    }

    fun getUserByPhoneAndPassword(phone: String, password: String): User? {
        val db = readableDatabase
        var cursor: Cursor? = null
        return try {
            cursor = db.query(
                TABLE_USERS,
                arrayOf(
                    COLUMN_USER_ID,
                    COLUMN_NAME,
                    COLUMN_PHONE,
                    COLUMN_EMAIL,
                    COLUMN_PASSWORD,
                    COLUMN_ROLE
                ),
                "$COLUMN_PHONE = ? AND $COLUMN_PASSWORD = ?",
                arrayOf(phone, password),
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)) ?: ""
                val phoneDb = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)) ?: ""
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)) ?: ""
                val passwordDb = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)) ?: ""
                val role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)) ?: "user"

                User(
                    id = id,
                    name = name,
                    phone = phoneDb,
                    email = email,
                    password = passwordDb,
                    role = role
                )
            } else null
        } finally {
            cursor?.close()
        }
    }

    fun updateUserProfile(
        userId: Int,
        name: String,
        phone: String,
        email: String
    ): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PHONE, phone)
            put(COLUMN_EMAIL, email)
        }
        return db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        )
    }

    fun getWalletBalance(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_WALLET_BALANCE),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        cursor.use { c ->
            return if (c.moveToFirst()) {
                c.getInt(c.getColumnIndexOrThrow(COLUMN_WALLET_BALANCE))
            } else 0
        }
    }

    fun updateWalletBalance(userId: Int, newBalance: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WALLET_BALANCE, newBalance)
        }

        db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString())
        )
    }

    data class CarInfo(
        val plate: String?,
        val type: String?,
        val color: String?
    )

    fun getCarInfoForUser(userId: Int): CarInfo? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CARS,
            arrayOf(COLUMN_CAR_PLATE, COLUMN_CAR_TYPE, COLUMN_CAR_COLOR),
            "$COLUMN_CAR_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null,
            "1"
        )

        cursor.use { c ->
            return if (c.moveToFirst()) {
                val plate = c.getString(c.getColumnIndexOrThrow(COLUMN_CAR_PLATE))
                val type = c.getString(c.getColumnIndexOrThrow(COLUMN_CAR_TYPE))
                val color = c.getString(c.getColumnIndexOrThrow(COLUMN_CAR_COLOR))
                CarInfo(plate, type, color)
            } else null
        }
    }

    fun updateCarInfoForUser(userId: Int, plate: String, type: String, color: String) {
        val db = writableDatabase

        val existsCursor = db.query(
            TABLE_CARS,
            arrayOf(COLUMN_CAR_ID),
            "$COLUMN_CAR_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null,
            "1"
        )

        val exists = existsCursor.use { it.moveToFirst() }

        val values = ContentValues().apply {
            put(COLUMN_CAR_USER_ID, userId)
            put(COLUMN_CAR_PLATE, plate)
            put(COLUMN_CAR_TYPE, type)
            put(COLUMN_CAR_COLOR, color)
        }

        if (exists) {
            db.update(
                TABLE_CARS,
                values,
                "$COLUMN_CAR_USER_ID = ?",
                arrayOf(userId.toString())
            )
        } else {
            db.insert(TABLE_CARS, null, values)
        }
    }

    fun insertService(
        userId: Int,
        title: String,
        description: String,
        date: String,
        time: String,
        price: String,
        imageRes: Int,
        status: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SERVICE_USER_ID, userId)
            put(COLUMN_SERVICE_TITLE, title)
            put(COLUMN_SERVICE_DESC, description)
            put(COLUMN_SERVICE_DATE, date)
            put(COLUMN_SERVICE_TIME, time)
            put(COLUMN_SERVICE_PRICE, price)
            put(COLUMN_SERVICE_IMAGE_RES, imageRes)
            put(COLUMN_SERVICE_STATUS, status)
        }
        return db.insert(TABLE_SERVICES, null, values)
    }

    fun insertSampleCompletedHistoryOnce(userId: Int) {
        val db = writableDatabase

        data class Sample(
            val title: String,
            val date: String,
            val time: String,
            val price: String,
            val imageRes: Int
        )

        val samples = listOf(
            Sample("Discount Cleaning", "Thu, 23 Oct", "04:00 PM", "RM39", R.drawable.max_discont_cleaning),
            Sample("Car Waxing", "Mon, 04 Aug", "02:15 PM", "RM49", R.drawable.max_car_waxing),
            Sample("Premium Cleaning", "Wed, 11 Jun", "03:30 PM", "RM89", R.drawable.max_premium_cleaning),
            Sample("Car Disinfection", "Tue, 06 May", "05:10 PM", "RM29", R.drawable.max_car_disinfection)
        )

        for (s in samples) {
            val cursor = db.query(
                TABLE_SERVICES,
                arrayOf(COLUMN_SERVICE_ID),
                "$COLUMN_SERVICE_USER_ID=? AND $COLUMN_SERVICE_TITLE=? AND $COLUMN_SERVICE_DATE=? AND $COLUMN_SERVICE_TIME=?",
                arrayOf(userId.toString(), s.title, s.date, s.time),
                null,
                null,
                null
            )
            val exists = cursor.use { it.moveToFirst() }
            if (!exists) {
                insertService(
                    userId = userId,
                    title = s.title,
                    description = "",
                    date = s.date,
                    time = s.time,
                    price = s.price,
                    imageRes = s.imageRes,
                    status = "completed"
                )
            }
        }
    }

    fun getServicesForUser(userId: Int): List<ServiceItem> {
        val result = mutableListOf<ServiceItem>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_SERVICES,
            arrayOf(
                COLUMN_SERVICE_ID,
                COLUMN_SERVICE_USER_ID,
                COLUMN_SERVICE_TITLE,
                COLUMN_SERVICE_DESC,
                COLUMN_SERVICE_DATE,
                COLUMN_SERVICE_TIME,
                COLUMN_SERVICE_PRICE,
                COLUMN_SERVICE_IMAGE_RES,
                COLUMN_SERVICE_STATUS
            ),
            "$COLUMN_SERVICE_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_SERVICE_ID DESC"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_ID))
                    val uid = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_USER_ID))
                    val title = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_TITLE)) ?: ""
                    val desc = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_DESC)) ?: ""
                    val date = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_DATE)) ?: ""
                    val time = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_TIME)) ?: ""
                    val price = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_PRICE)) ?: ""
                    val imageRes = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_IMAGE_RES))
                    val status = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_STATUS)) ?: ""

                    result.add(
                        ServiceItem(
                            id = id,
                            userId = uid,
                            title = title,
                            description = desc,
                            date = date,
                            time = time,
                            price = price,
                            imageRes = imageRes,
                            status = status
                        )
                    )
                } while (c.moveToNext())
            }
        }
        return result
    }

    fun getServiceById(serviceId: Int): ServiceItem? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SERVICES,
            arrayOf(
                COLUMN_SERVICE_ID,
                COLUMN_SERVICE_USER_ID,
                COLUMN_SERVICE_TITLE,
                COLUMN_SERVICE_DESC,
                COLUMN_SERVICE_DATE,
                COLUMN_SERVICE_TIME,
                COLUMN_SERVICE_PRICE,
                COLUMN_SERVICE_IMAGE_RES,
                COLUMN_SERVICE_STATUS
            ),
            "$COLUMN_SERVICE_ID = ?",
            arrayOf(serviceId.toString()),
            null,
            null,
            null,
            "1"
        )

        cursor.use { c ->
            return if (c.moveToFirst()) {
                ServiceItem(
                    id = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_ID)),
                    userId = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_USER_ID)),
                    title = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_TITLE)) ?: "",
                    description = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_DESC)) ?: "",
                    date = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_DATE)) ?: "",
                    time = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_TIME)) ?: "",
                    price = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_PRICE)) ?: "",
                    imageRes = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_IMAGE_RES)),
                    status = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_STATUS)) ?: ""
                )
            } else null
        }
    }

    fun getAllServices(): List<ServiceItem> {
        val result = mutableListOf<ServiceItem>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_SERVICES,
            arrayOf(
                COLUMN_SERVICE_ID,
                COLUMN_SERVICE_USER_ID,
                COLUMN_SERVICE_TITLE,
                COLUMN_SERVICE_DESC,
                COLUMN_SERVICE_DATE,
                COLUMN_SERVICE_TIME,
                COLUMN_SERVICE_PRICE,
                COLUMN_SERVICE_IMAGE_RES,
                COLUMN_SERVICE_STATUS
            ),
            null,
            null,
            null,
            null,
            "$COLUMN_SERVICE_ID DESC"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_ID))
                    val uid = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_USER_ID))
                    val title = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_TITLE)) ?: ""
                    val desc = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_DESC)) ?: ""
                    val date = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_DATE)) ?: ""
                    val time = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_TIME)) ?: ""
                    val price = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_PRICE)) ?: ""
                    val imageRes = c.getInt(c.getColumnIndexOrThrow(COLUMN_SERVICE_IMAGE_RES))
                    val status = c.getString(c.getColumnIndexOrThrow(COLUMN_SERVICE_STATUS)) ?: ""

                    result.add(
                        ServiceItem(
                            id = id,
                            userId = uid,
                            title = title,
                            description = desc,
                            date = date,
                            time = time,
                            price = price,
                            imageRes = imageRes,
                            status = status
                        )
                    )
                } while (c.moveToNext())
            }
        }
        return result
    }

    fun getUserById(userId: Int): User? {
        val db = readableDatabase
        var cursor: Cursor? = null
        return try {
            cursor = db.query(
                TABLE_USERS,
                arrayOf(
                    COLUMN_USER_ID,
                    COLUMN_NAME,
                    COLUMN_PHONE,
                    COLUMN_EMAIL,
                    COLUMN_PASSWORD,
                    COLUMN_ROLE
                ),
                "$COLUMN_USER_ID = ?",
                arrayOf(userId.toString()),
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)) ?: ""
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)) ?: ""
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)) ?: ""
                val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)) ?: ""
                val role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)) ?: "user"

                User(
                    id = id,
                    name = name,
                    phone = phone,
                    email = email,
                    password = password,
                    role = role
                )
            } else null
        } finally {
            cursor?.close()
        }
    }

    fun updateServiceDateTime(serviceId: Int, newDate: String, newTime: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SERVICE_DATE, newDate)
            put(COLUMN_SERVICE_TIME, newTime)
        }
        return db.update(
            TABLE_SERVICES,
            values,
            "$COLUMN_SERVICE_ID = ?",
            arrayOf(serviceId.toString())
        )
    }

    fun deleteService(serviceId: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_SERVICES,
            "$COLUMN_SERVICE_ID = ?",
            arrayOf(serviceId.toString())
        )
    }

    data class AddressItem(
        val id: Int,
        val userId: Int,
        val label: String,
        val phone: String,
        val detail: String,
        val isDefault: Boolean
    )

    fun ensureFixedAddressesOnce(userId: Int) {
        val db = writableDatabase

        data class FixedAddr(val label: String, val phone: String, val detail: String)

        val fixed = listOf(
            FixedAddr(
                "Xiamen University Malaysia",
                "(+60) 178751741",
                "Jalan Sunsuria, Bandar Sunsuria"
            ),
            FixedAddr(
                "Bell Suites",
                "(+60) 159518223",
                "Bell Suites, Kota Warisan"
            )
        )

        for (a in fixed) {
            val cursor = db.query(
                TABLE_ADDRESSES,
                arrayOf(COLUMN_ADDRESS_ID),
                "$COLUMN_ADDRESS_USER_ID=? AND $COLUMN_ADDRESS_LABEL=? AND $COLUMN_ADDRESS_DETAIL=? AND $COLUMN_ADDRESS_IS_DEFAULT=1",
                arrayOf(userId.toString(), a.label, a.detail),
                null, null, null,
                "1"
            )
            val exists = cursor.use { it.moveToFirst() }

            if (!exists) {
                insertAddressForUser(
                    userId = userId,
                    label = a.label,
                    phone = a.phone,
                    detail = a.detail,
                    isDefault = true
                )
            }
        }
    }

    fun getDefaultAddressesForUser(userId: Int, limit: Int = 2): List<AddressItem> {
        val list = mutableListOf<AddressItem>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_ADDRESSES,
            arrayOf(
                COLUMN_ADDRESS_ID,
                COLUMN_ADDRESS_USER_ID,
                COLUMN_ADDRESS_LABEL,
                COLUMN_ADDRESS_PHONE,
                COLUMN_ADDRESS_DETAIL,
                COLUMN_ADDRESS_IS_DEFAULT
            ),
            "$COLUMN_ADDRESS_USER_ID = ? AND $COLUMN_ADDRESS_IS_DEFAULT = 1",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_ADDRESS_ID ASC",
            limit.toString()
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    AddressItem(
                        id = c.getInt(c.getColumnIndexOrThrow(COLUMN_ADDRESS_ID)),
                        userId = c.getInt(c.getColumnIndexOrThrow(COLUMN_ADDRESS_USER_ID)),
                        label = c.getString(c.getColumnIndexOrThrow(COLUMN_ADDRESS_LABEL)) ?: "",
                        phone = c.getString(c.getColumnIndexOrThrow(COLUMN_ADDRESS_PHONE)) ?: "",
                        detail = c.getString(c.getColumnIndexOrThrow(COLUMN_ADDRESS_DETAIL)) ?: "",
                        isDefault = true
                    )
                )
            }
        }
        return list
    }



    fun insertAddressForUser(
        userId: Int,
        label: String,
        phone: String,
        detail: String,
        isDefault: Boolean
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ADDRESS_USER_ID, userId)
            put(COLUMN_ADDRESS_LABEL, label)
            put(COLUMN_ADDRESS_PHONE, phone)
            put(COLUMN_ADDRESS_DETAIL, detail)
            put(COLUMN_ADDRESS_IS_DEFAULT, if (isDefault) 1 else 0)
        }
        return db.insert(TABLE_ADDRESSES, null, values)
    }

    fun getCustomAddressForUser(userId: Int): AddressItem? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ADDRESSES,
            arrayOf(
                COLUMN_ADDRESS_ID,
                COLUMN_ADDRESS_USER_ID,
                COLUMN_ADDRESS_LABEL,
                COLUMN_ADDRESS_PHONE,
                COLUMN_ADDRESS_DETAIL,
                COLUMN_ADDRESS_IS_DEFAULT
            ),
            "$COLUMN_ADDRESS_USER_ID = ? AND $COLUMN_ADDRESS_IS_DEFAULT = 0",
            arrayOf(userId.toString()),
            null,
            null,
            null,
            "1"
        )

        cursor.use { c ->
            return if (c.moveToFirst()) {
                val id = c.getInt(c.getColumnIndexOrThrow(COLUMN_ADDRESS_ID))
                val uid = c.getInt(c.getColumnIndexOrThrow(COLUMN_ADDRESS_USER_ID))
                val label = c.getString(c.getColumnIndexOrThrow(COLUMN_ADDRESS_LABEL)) ?: ""
                val phone = c.getString(c.getColumnIndexOrThrow(COLUMN_ADDRESS_PHONE)) ?: ""
                val detail = c.getString(c.getColumnIndexOrThrow(COLUMN_ADDRESS_DETAIL)) ?: ""
                val isDefault = c.getInt(c.getColumnIndexOrThrow(COLUMN_ADDRESS_IS_DEFAULT)) == 1

                AddressItem(
                    id = id,
                    userId = uid,
                    label = label,
                    phone = phone,
                    detail = detail,
                    isDefault = isDefault
                )
            } else null
        }
    }

    fun upsertCustomAddressForUser(userId: Int, label: String, phone: String, detail: String) {
        val db = writableDatabase

        val existing = getCustomAddressForUser(userId)
        if (existing == null) {
            insertAddressForUser(
                userId = userId,
                label = label,
                phone = phone,
                detail = detail,
                isDefault = false
            )
        } else {
            val values = ContentValues().apply {
                put(COLUMN_ADDRESS_LABEL, label)
                put(COLUMN_ADDRESS_PHONE, phone)
                put(COLUMN_ADDRESS_DETAIL, detail)
            }
            db.update(
                TABLE_ADDRESSES,
                values,
                "$COLUMN_ADDRESS_ID = ?",
                arrayOf(existing.id.toString())
            )
        }
    }

    fun deleteCustomAddressForUser(userId: Int) {
        val db = writableDatabase
        db.delete(
            TABLE_ADDRESSES,
            "$COLUMN_ADDRESS_USER_ID = ? AND $COLUMN_ADDRESS_IS_DEFAULT = 0",
            arrayOf(userId.toString())
        )
    }

    companion object {
        private const val DATABASE_NAME = "my_app.db"

        private const val DATABASE_VERSION = 4

        // Users Table
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_ROLE = "role"
        const val COLUMN_WALLET_BALANCE = "wallet_balance"

        private const val DEFAULT_ADMIN_PHONE = "123456789"
        private const val DEFAULT_ADMIN_PASSWORD = "admin123"

        // Cars Table
        const val TABLE_CARS = "cars"
        const val COLUMN_CAR_ID = "car_id"
        const val COLUMN_CAR_USER_ID = "user_id"
        const val COLUMN_CAR_PLATE = "plate"
        const val COLUMN_CAR_TYPE = "type"
        const val COLUMN_CAR_COLOR = "color"

        // Services Table
        const val TABLE_SERVICES = "services"
        const val COLUMN_SERVICE_ID = "service_id"
        const val COLUMN_SERVICE_USER_ID = "user_id"
        const val COLUMN_SERVICE_TITLE = "title"
        const val COLUMN_SERVICE_DESC = "description"
        const val COLUMN_SERVICE_DATE = "service_date"
        const val COLUMN_SERVICE_TIME = "service_time"
        const val COLUMN_SERVICE_PRICE = "price"
        const val COLUMN_SERVICE_IMAGE_RES = "image_res"
        const val COLUMN_SERVICE_STATUS = "status"

        // Addresses Table
        const val TABLE_ADDRESSES = "addresses"
        const val COLUMN_ADDRESS_ID = "address_d"
        const val COLUMN_ADDRESS_USER_ID = "user_id"
        const val COLUMN_ADDRESS_LABEL = "label"
        const val COLUMN_ADDRESS_PHONE = "phone"
        const val COLUMN_ADDRESS_DETAIL = "detail"
        const val COLUMN_ADDRESS_IS_DEFAULT = "is_default"

        private const val CREATE_USERS_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_PHONE TEXT UNIQUE,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_ROLE TEXT,
                $COLUMN_WALLET_BALANCE INTEGER DEFAULT 0
            )
        """

        private const val CREATE_CARS_TABLE = """
            CREATE TABLE $TABLE_CARS (
                $COLUMN_CAR_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CAR_USER_ID INTEGER UNIQUE,
                $COLUMN_CAR_PLATE TEXT,
                $COLUMN_CAR_TYPE TEXT,
                $COLUMN_CAR_COLOR TEXT,
                FOREIGN KEY($COLUMN_CAR_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """

        private const val CREATE_SERVICES_TABLE = """
            CREATE TABLE $TABLE_SERVICES (
                $COLUMN_SERVICE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SERVICE_USER_ID INTEGER,
                $COLUMN_SERVICE_TITLE TEXT,
                $COLUMN_SERVICE_DESC TEXT,
                $COLUMN_SERVICE_DATE TEXT,
                $COLUMN_SERVICE_TIME TEXT,
                $COLUMN_SERVICE_PRICE TEXT,
                $COLUMN_SERVICE_IMAGE_RES INTEGER,
                $COLUMN_SERVICE_STATUS TEXT,
                FOREIGN KEY($COLUMN_SERVICE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """

        private const val CREATE_ADDRESSES_TABLE = """
            CREATE TABLE $TABLE_ADDRESSES (
                $COLUMN_ADDRESS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ADDRESS_USER_ID INTEGER,
                $COLUMN_ADDRESS_LABEL TEXT,
                $COLUMN_ADDRESS_PHONE TEXT,
                $COLUMN_ADDRESS_DETAIL TEXT,
                $COLUMN_ADDRESS_IS_DEFAULT INTEGER DEFAULT 0,
                FOREIGN KEY($COLUMN_ADDRESS_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """
    }
}

data class User(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val role: String
)

data class ServiceItem(
    val id: Int,
    val userId: Int,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val price: String,
    val imageRes: Int,
    val status: String
)
