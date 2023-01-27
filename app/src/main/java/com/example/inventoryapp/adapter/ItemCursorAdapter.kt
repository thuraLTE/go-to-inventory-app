package com.example.inventoryapp.adapter

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.example.inventoryapp.R
import com.example.inventoryapp.data.ItemContract.ItemEntry
import com.example.inventoryapp.data.ItemContract.ItemEntry.Companion.formatPrice

class ItemCursorAdapter(mContext: Context, mCursor: Cursor?) :
    CursorAdapter(mContext, mCursor, false) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(parent?.context).inflate(R.layout.list_items, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        if (view != null && context != null) {

            val nameTv = view.findViewById<TextView>(R.id.name)
            val imageUrl = view.findViewById<ImageView>(R.id.imageUrl)
            val shelfCodeTv = view.findViewById<TextView>(R.id.shelfCode)
            val statusTv = view.findViewById<TextView>(R.id.status)
            val currentQuantityTv = view.findViewById<TextView>(R.id.currentQuantity)
            val maxQuantityTv = view.findViewById<TextView>(R.id.maxQuantity)
            val progressBar = view.findViewById<ProgressBar>(R.id.progress)
            val price = view.findViewById<TextView>(R.id.price)

            if (cursor != null) {
                with(cursor) {
                    nameTv.text = getStringOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_NAME))

                    imageUrl.setImageURI(Uri.parse(getStringOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE))))

                    shelfCodeTv.text = getStringOrNull(getColumnIndex(ItemEntry.COLUMN_SHELF_CODE))

                    val status = getIntOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_STATUS))
                    when (status) {
                        ItemEntry.STATUS_NORMAL -> {
                            statusTv.visibility = View.GONE
                        }
                        ItemEntry.STATUS_OUT_OF_STOCK -> {
                            statusTv.apply {
                                visibility = View.VISIBLE
                                text =
                                    context.getString(R.string.out_of_stock)
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        android.R.color.holo_red_dark
                                    )
                                )
                            }
                        }
                        ItemEntry.STATUS_ABOUT_TO_RUN_OUT -> {
                            statusTv.apply {
                                visibility = View.VISIBLE
                                text =
                                    context.getString(R.string.about_to_run_out)
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        android.R.color.holo_orange_dark
                                    )
                                )
                            }
                        }
                        ItemEntry.STATUS_ABOUT_TO_EXPIRE -> {
                            statusTv.apply {
                                visibility = View.VISIBLE
                                text =
                                    context.getString(R.string.about_to_expire)
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        android.R.color.holo_blue_dark
                                    )
                                )
                            }
                        }
                        ItemEntry.STATUS_EXPIRED -> {
                            statusTv.apply {
                                visibility = View.VISIBLE
                                text =
                                    context.getString(R.string.expired)
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        android.R.color.holo_purple
                                    )
                                )
                            }
                        }
                        ItemEntry.STATUS_FILLED -> {
                            statusTv.apply {
                                visibility = View.VISIBLE
                                text =
                                    context.getString(R.string.filled)
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        android.R.color.holo_green_dark
                                    )
                                )
                            }
                        }
                    }

                    val currentQuantity =
                        getIntOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_CURRENT_QUANTITY))
                    currentQuantityTv.text = currentQuantity.toString()

                    val maxQuantity =
                        getIntOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_MAX_CAPACITY))
                    maxQuantityTv.text = maxQuantity.toString()

                    progressBar.apply {
                        max = maxQuantity!!
                        progress = currentQuantity!!
                    }

                    price.text =
                        formatPrice(getDoubleOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE))!!)
                }
            }
        }
    }
}