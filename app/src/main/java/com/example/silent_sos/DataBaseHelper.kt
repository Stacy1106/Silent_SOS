package com.example.silent_sos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHelper(context: Context) :
    SQLiteOpenHelper(context, "SilentSOS.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
    CREATE TABLE emergency_contacts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        phone TEXT NOT NULL,
        relationship TEXT
    )
    """.trimIndent()
        )

        db.execSQL(
            """
    CREATE TABLE sos_events (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        timestamp TEXT NOT NULL,
        status TEXT NOT NULL,
        latitude REAL,
        longitude REAL
    )
    """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS emergency_contacts")
        db.execSQL("DROP TABLE IF EXISTS sos_events")
        onCreate(db)
    }

    fun addContact(
        name: String,
        phone: String,
        relationship: String
    ): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("name", name)
            put("phone", phone)
            put("relationship", relationship)
        }

        val result = db.insert(
            "emergency_contacts",
            null,
            values
        )

        db.close()

        return result != -1L
    }
    fun addSosEvent(
        timestamp: String,
        status: String,
        latitude: Double?,
        longitude: Double?
    ): Boolean {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("timestamp", timestamp)
            put("status", status)

            if (latitude != null) {
                put("latitude", latitude)
            }

            if (longitude != null) {
                put("longitude", longitude)
            }
        }

        val result = db.insert(
            "sos_events",
            null,
            values
        )

        db.close()

        return result != -1L
    }

    fun getContacts(): ArrayList<Contact> {

        val contacts = ArrayList<Contact>()

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM emergency_contacts",
            null
        )

        while (cursor.moveToNext()) {

            val id = cursor.getInt(
                cursor.getColumnIndexOrThrow("id")
            )

            val name = cursor.getString(
                cursor.getColumnIndexOrThrow("name")
            )

            val phone = cursor.getString(
                cursor.getColumnIndexOrThrow("phone")
            )

            val relationship = cursor.getString(
                cursor.getColumnIndexOrThrow("relationship")
            )

            contacts.add(
                Contact(
                    id,
                    name,
                    phone,
                    relationship
                )
            )
        }

        cursor.close()
        db.close()

        return contacts
    }

    fun deleteContact(id: Int): Boolean {

        val db = writableDatabase

        val result = db.delete(
            "emergency_contacts",
            "id = ?",
            arrayOf(id.toString())
        )

        db.close()

        return result > 0
    }
}

data class Contact(
    val id: Int,
    val name: String,
    val phone: String,
    val relationship: String
)