package com.example.inventoryapp.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.widget.Toast
import com.example.inventoryapp.R
import com.example.inventoryapp.data.ItemContract.ItemEntry
import kotlin.IllegalArgumentException

class ItemProvider : ContentProvider() {

    private lateinit var mItemDbHelper: ItemDbHelper

    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private val ITEMS = 100
    private val ITEM_ID = 101

    private fun addContentUri() {
        sUriMatcher.apply {
            addURI(
                ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS
            )
            addURI(
                ItemContract.CONTENT_AUTHORITY, "${ItemContract.PATH_ITEMS}/#", ITEM_ID
            )
        }

    }

    override fun onCreate(): Boolean {
        mItemDbHelper = ItemDbHelper(context!!)
        addContentUri()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        var cursor: Cursor? = null
        val db = mItemDbHelper.readableDatabase

        when (sUriMatcher.match(uri)) {
            ITEMS -> {
                cursor = db.query(
                    ItemEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
                )
            }
            ITEM_ID -> {
                val mSelection = "${ItemEntry._ID}=?"
                val mSelectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                cursor = db.query(
                    ItemEntry.TABLE_NAME,
                    projection,
                    mSelection,
                    mSelectionArgs,
                    null,
                    null,
                    sortOrder
                )
            }
            else -> throw IllegalArgumentException("Cannot query unknown Uri: $uri")
        }
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String {
        return when (sUriMatcher.match(uri)) {
            ITEMS -> ItemEntry.CONTENT_LIST_TYPE
            ITEM_ID -> ItemEntry.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
    }

    private fun insertItem(uri: Uri, values: ContentValues?): Uri? {
        val db = mItemDbHelper.writableDatabase

        if (values != null) {
            val name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME)
            val imageUrl = values.getAsString(ItemEntry.COLUMN_ITEM_IMAGE)
            val shelfCode = values.getAsString(ItemEntry.COLUMN_SHELF_CODE)
            val currentQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_CURRENT_QUANTITY)
            val maxQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_MAX_CAPACITY)
            val price = values.getAsDouble(ItemEntry.COLUMN_ITEM_PRICE)
            val status = values.getAsInteger(ItemEntry.COLUMN_ITEM_STATUS)

            when {
                (name == null) -> throw IllegalArgumentException("Item requires a name")
                (imageUrl == null) -> throw IllegalArgumentException("Item requires an image url")
                (shelfCode == null) -> throw IllegalArgumentException("Item requires a shelf code")
                (maxQuantity == null || maxQuantity < 1 || maxQuantity < currentQuantity) ->
                    throw IllegalArgumentException("Max Quantity requires valid number")
                (currentQuantity == null || currentQuantity < 0 || currentQuantity > maxQuantity) ->
                    throw IllegalArgumentException("Current Quantity requires valid number")
                (price == null || price <= 0) ->
                    throw IllegalArgumentException("Price requires valid number")
                (status == null || status < 0 || status > 5) ->
                    throw IllegalArgumentException("Status requires valid status")
                else -> {
                    val newRowId = db.insert(
                        ItemEntry.TABLE_NAME,
                        null,
                        values
                    )
                    if (newRowId < 0L)
                        Toast.makeText(context, R.string.error_saving_item, Toast.LENGTH_SHORT)
                            .show()
                    else {
                        Toast.makeText(context, R.string.item_saved, Toast.LENGTH_SHORT).show()
                        context?.contentResolver?.notifyChange(uri, null)
                        return ContentUris.withAppendedId(uri, newRowId)
                    }
                }
            }
        }
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (sUriMatcher.match(uri)) {
            ITEMS -> insertItem(uri, values)
            else -> throw IllegalArgumentException("Insertion is not supported for Uri: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = mItemDbHelper.writableDatabase

        val rowsDeleted = when (sUriMatcher.match(uri)) {
            ITEMS -> db.delete(ItemEntry.TABLE_NAME, null, null)
            ITEM_ID -> {
                val mSelection = "${ItemEntry._ID}=?"
                val mSelectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                db.delete(
                    ItemEntry.TABLE_NAME,
                    mSelection,
                    mSelectionArgs
                )
            }
            else -> throw IllegalArgumentException("Deletion is not supported for Uri: $uri")
        }

        if (rowsDeleted < 1)
            Toast.makeText(context, R.string.deletion_failed, Toast.LENGTH_SHORT).show()
        else {
            Toast.makeText(context, R.string.deletion_success, Toast.LENGTH_SHORT).show()
            context?.contentResolver?.notifyChange(uri, null)
            return rowsDeleted
        }
        return 0
    }

    private fun updateItem(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = mItemDbHelper.writableDatabase

        if (values != null) {
            val name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME)
            val imageUrl = values.getAsString(ItemEntry.COLUMN_ITEM_IMAGE)
            val shelfCode = values.getAsString(ItemEntry.COLUMN_SHELF_CODE)
            val currentQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_CURRENT_QUANTITY)
            val maxQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_MAX_CAPACITY)
            val price = values.getAsDouble(ItemEntry.COLUMN_ITEM_PRICE)
            val status = values.getAsInteger(ItemEntry.COLUMN_ITEM_STATUS)

            when {
                (name == null) -> throw IllegalArgumentException("Item requires a name")
                (imageUrl == null) -> throw IllegalArgumentException("Item requires an image url")
                (shelfCode == null) -> throw IllegalArgumentException("Item requires a shelf code")
                (maxQuantity == null || maxQuantity < 1 || maxQuantity < currentQuantity) ->
                    throw IllegalArgumentException("Max Quantity requires valid number")
                (currentQuantity == null || currentQuantity < 0 || currentQuantity > maxQuantity) ->
                    throw IllegalArgumentException("Current Quantity requires valid number")
                (price == null || price <= 0) ->
                    throw IllegalArgumentException("Price requires valid number")
                (status == null || status < 0 || status > 5) ->
                    throw IllegalArgumentException("Status requires valid status")
                else -> {
                    if (values.size() > 0) {
                        val rowsUpdated = db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs)
                        if (rowsUpdated > 0) {
                            Toast.makeText(context, R.string.update_success, Toast.LENGTH_SHORT).show()
                            context?.contentResolver?.notifyChange(uri, null)
                            return rowsUpdated
                        } else
                            Toast.makeText(context, R.string.update_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return when (sUriMatcher.match(uri)) {
            ITEMS -> updateItem(uri, values, selection, selectionArgs)
            ITEM_ID -> {
                val mSelection = "${ItemEntry._ID}=?"
                val mSelectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                updateItem(uri, values, mSelection, mSelectionArgs)
            }
            else -> throw IllegalArgumentException("Update is not supported for Uri: $uri")
        }
    }
}