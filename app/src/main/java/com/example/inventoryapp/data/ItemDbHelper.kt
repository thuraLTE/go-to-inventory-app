package com.example.inventoryapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.inventoryapp.data.ItemContract.ItemEntry

class ItemDbHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "warehouse.db"
        const val DATABASE_VERSION = 1
    }

    private val SQL_CREATE_ENTRIES = "CREATE TABLE ${ItemEntry.TABLE_NAME}" +
            "(${ItemEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "${ItemEntry.COLUMN_ITEM_NAME} TEXT NOT NULL, " +
            "${ItemEntry.COLUMN_ITEM_IMAGE} TEXT NOT NULL, " +
            "${ItemEntry.COLUMN_ITEM_PRICE} DECIMAL(2,2) NOT NULL, " +
            "${ItemEntry.COLUMN_SHELF_CODE} TEXT NOT NULL, " +
            "${ItemEntry.COLUMN_ITEM_STATUS} INTEGER NOT NULL, " +
            "${ItemEntry.COLUMN_ITEM_MAX_CAPACITY} INTEGER NOT NULL, " +
            "${ItemEntry.COLUMN_ITEM_CURRENT_QUANTITY} INTEGER NOT NULL);"
    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ItemEntry.TABLE_NAME};"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
}