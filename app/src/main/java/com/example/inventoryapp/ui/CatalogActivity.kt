package com.example.inventoryapp.ui

import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.inventoryapp.data.ItemContract.ItemEntry
import com.example.inventoryapp.adapter.ItemCursorAdapter
import com.example.inventoryapp.data.ItemDbHelper
import com.example.inventoryapp.R
import java.lang.IllegalArgumentException

class CatalogActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private var isFabClicked = false

    private lateinit var btnBasket: FloatingActionButton
    private lateinit var btnAddBasket: FloatingActionButton
    private lateinit var btnDeleteBasket: FloatingActionButton
    private lateinit var itemList: ListView
    private lateinit var itemCursorAdapter: ItemCursorAdapter

    private lateinit var mDbHelper: ItemDbHelper
    private val ITEM_LOADER = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        supportActionBar?.setTitle(R.string.inventory_management)

        btnBasket = findViewById(R.id.btnBasket)
        btnAddBasket = findViewById(R.id.btnAddBasket)
        btnDeleteBasket = findViewById(R.id.btnDeleteBasket)
        itemList = findViewById(R.id.itemList)

        mDbHelper = ItemDbHelper(this)

        LoaderManager.getInstance(this).initLoader(ITEM_LOADER, null, this)
        populateItemList()
        onClickFab()
    }

    private fun populateItemList() {
        itemCursorAdapter = ItemCursorAdapter(this, null)
        val emptyElement = findViewById<LinearLayout>(R.id.emptyView)

        itemList.apply {
            adapter = itemCursorAdapter
            emptyView = emptyElement
            setOnItemClickListener { parent, view, position, id ->
                val intent = Intent(this@CatalogActivity, EditorActivity::class.java)
                intent.data = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id)
                startActivity(intent)
            }
        }

        emptyElement.visibility = View.GONE
    }

    private fun onClickFab() {
        btnBasket.setOnClickListener {
            isFabClicked = !isFabClicked
            extendOrContractFabs()
            onClickAddBasket()
            onClickDeleteBasket()
        }
    }

    private fun extendOrContractFabs() {
        if (isFabClicked) {
            btnAddBasket.apply {
                visibility = View.VISIBLE
                isClickable = true
            }
            btnDeleteBasket.apply {
                visibility = View.VISIBLE
                isClickable = true
            }
            animateFabsAppearance(btnAddBasket)
            animateFabsAppearance(btnDeleteBasket)
        } else {
            btnAddBasket.apply {
                visibility = View.INVISIBLE
                isClickable = false
            }
            btnDeleteBasket.apply {
                visibility = View.INVISIBLE
                isClickable = false
            }
            animateFabsDisappearance(btnAddBasket)
            animateFabsDisappearance(btnDeleteBasket)
        }
    }

    private fun animateFabsAppearance(view: View) {
        view.animation = AnimationUtils.loadAnimation(this, R.anim.from_bottom_fade_in)
        view.animation.start()
    }

    private fun animateFabsDisappearance(view: View) {
        view.animation = AnimationUtils.loadAnimation(this, R.anim.to_bottom_fade_out)
        view.animation.start()
    }

    private fun onClickAddBasket() {
        btnAddBasket.setOnClickListener {
            isFabClicked = false
            extendOrContractFabs()
            val intent = Intent(this, EditorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onClickDeleteBasket() {
        btnDeleteBasket.setOnClickListener {
            contentResolver.delete(ItemEntry.CONTENT_URI, null, null)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(
            ItemEntry._ID,
            ItemEntry.COLUMN_ITEM_NAME,
            ItemEntry.COLUMN_ITEM_IMAGE,
            ItemEntry.COLUMN_SHELF_CODE,
            ItemEntry.COLUMN_ITEM_STATUS,
            ItemEntry.COLUMN_ITEM_PRICE,
            ItemEntry.COLUMN_ITEM_CURRENT_QUANTITY,
            ItemEntry.COLUMN_ITEM_MAX_CAPACITY
        )

        if (id == ITEM_LOADER)
            return CursorLoader(
                this,
                ItemEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
            )
        else throw IllegalArgumentException("Invalid loader id: $id")
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        itemCursorAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        itemCursorAdapter.swapCursor(null)
    }
}