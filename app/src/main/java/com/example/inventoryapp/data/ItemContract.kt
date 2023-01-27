package com.example.inventoryapp.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns
import java.text.NumberFormat
import java.util.*

object ItemContract {

    private const val SCHEMA = "content://"
    const val CONTENT_AUTHORITY = "com.example.inventoryapp"
    const val PATH_ITEMS = "items"
    private val BASE_CONTENT_URI = Uri.parse("$SCHEMA$CONTENT_AUTHORITY")

    class ItemEntry: BaseColumns {

        companion object {
            const val TABLE_NAME = "items"

            const val _ID = BaseColumns._ID
            const val COLUMN_ITEM_NAME = "name"
            const val COLUMN_ITEM_IMAGE = "image_url"
            const val COLUMN_SHELF_CODE = "shelf_code"
            const val COLUMN_ITEM_STATUS = "status"
            const val COLUMN_ITEM_MAX_CAPACITY = "max_quantity"
            const val COLUMN_ITEM_CURRENT_QUANTITY = "current_quantity"
            const val COLUMN_ITEM_PRICE = "price"

            const val STATUS_NORMAL = 0
            const val STATUS_OUT_OF_STOCK = 1
            const val STATUS_ABOUT_TO_RUN_OUT = 2
            const val STATUS_ABOUT_TO_EXPIRE = 3
            const val STATUS_EXPIRED = 4
            const val STATUS_FILLED = 5

            val CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS)

            const val CONTENT_LIST_TYPE =
                "${ContentResolver.CURSOR_DIR_BASE_TYPE}/${CONTENT_AUTHORITY}/${PATH_ITEMS}"
            const val CONTENT_ITEM_TYPE =
                "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/${CONTENT_AUTHORITY}/${PATH_ITEMS}"

            fun formatPrice(number: Double): String {
                return NumberFormat.getCurrencyInstance(Locale.US).format(number)
            }
        }
    }
}