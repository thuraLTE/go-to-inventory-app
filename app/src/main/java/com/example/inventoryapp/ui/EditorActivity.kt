package com.example.inventoryapp.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.inventoryapp.data.ItemContract.ItemEntry
import com.example.inventoryapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditorActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val LOG_TAG = EditorActivity::class.java.name

    private lateinit var mSpinner: Spinner
    private var mStatus = ItemEntry.STATUS_NORMAL

    private var mCurrentUri: Uri? = null
    private var mItemHasChanged = false
    private val CURRENT_ITEM_LOADER = 101

    private lateinit var mEditName: TextInputEditText
    private lateinit var mEditImageUrl: TextInputEditText
    private lateinit var mEditShelfCode: TextInputEditText
    private lateinit var mEditCurrent: TextInputEditText
    private lateinit var mEditMax: TextInputEditText
    private lateinit var mEditPrice: TextInputEditText

    private lateinit var btnImageUpload: AppCompatButton
    private lateinit var btnAffirm: AppCompatButton
    private lateinit var btnDelete: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mSpinner = findViewById(R.id.spinnerStatus)
        mEditName = findViewById(R.id.editName)
        mEditImageUrl = findViewById(R.id.editImageUrl)
        mEditShelfCode = findViewById(R.id.editShelfCode)
        mEditCurrent = findViewById(R.id.editCurrent)
        mEditMax = findViewById(R.id.editMax)
        mEditPrice = findViewById(R.id.editPrice)
        btnImageUpload = findViewById(R.id.btnImageUpload)
        btnAffirm = findViewById(R.id.btnAffirm)
        btnDelete = findViewById(R.id.btnDelete)

        getDataFromPreviousScreen()
        setUpSpinner()
        setUpOnTouchListener()
        onClickAffirm()
        onClickDelete()
    }

    private fun getDataFromPreviousScreen() {
        mCurrentUri = intent?.data
        Log.d(LOG_TAG, "Current Uri: $mCurrentUri")
        if (mCurrentUri != null) {
            supportActionBar?.setTitle(R.string.update_item)
            btnAffirm.text = getString(R.string.update)
            LoaderManager.getInstance(this).initLoader(CURRENT_ITEM_LOADER, null, this)
        } else {
            supportActionBar?.setTitle(R.string.insert_item)
            btnAffirm.text = getString(R.string.insert)
            btnDelete.apply {
                visibility = View.GONE
                isClickable = false
            }
        }
    }

    private fun setUpSpinner() {
        val statusSpinnerAdapter = ArrayAdapter.createFromResource(
            this, R.array.array_status_options, R.layout.spinner_item
        )
        mSpinner.adapter = statusSpinnerAdapter

        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selection = parent?.getItemAtPosition(position) as String
                mStatus = when (selection) {
                    getString(R.string.out_of_stock) -> ItemEntry.STATUS_OUT_OF_STOCK
                    getString(R.string.about_to_run_out) -> ItemEntry.STATUS_ABOUT_TO_RUN_OUT
                    getString(R.string.about_to_expire) -> ItemEntry.STATUS_ABOUT_TO_EXPIRE
                    getString(R.string.expired) -> ItemEntry.STATUS_EXPIRED
                    getString(R.string.filled) -> ItemEntry.STATUS_FILLED
                    else -> ItemEntry.STATUS_NORMAL
                }
                Log.d(LOG_TAG, "Spinner selected entry: $selection")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                mStatus = ItemEntry.STATUS_NORMAL
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpOnTouchListener() {
        val onTouchListener = View.OnTouchListener { _, _ ->
            mItemHasChanged = true
            false
        }

        mEditName.setOnTouchListener(onTouchListener)
        mEditImageUrl.setOnTouchListener(onTouchListener)
        mEditShelfCode.setOnTouchListener(onTouchListener)
        mEditCurrent.setOnTouchListener(onTouchListener)
        mEditMax.setOnTouchListener(onTouchListener)
        mEditPrice.setOnTouchListener(onTouchListener)
        mSpinner.setOnTouchListener(onTouchListener)
    }

    private fun onClickAffirm() {
        val nameField = findViewById<TextInputLayout>(R.id.nameField)
        val imageField = findViewById<TextInputLayout>(R.id.imageUrlField)
        val shelfCodeField = findViewById<TextInputLayout>(R.id.shelfCodeField)
        val currentField = findViewById<TextInputLayout>(R.id.currentField)
        val maxField = findViewById<TextInputLayout>(R.id.maxField)
        val priceField = findViewById<TextInputLayout>(R.id.priceField)

        val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                // Persist uri permission
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageField.editText?.setText(uri.toString())
            }
        }
        btnImageUpload.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        btnAffirm.setOnClickListener {
            val name = mEditName.text?.toString()?.trim()
            val imageUrl = mEditImageUrl.text?.toString()?.trim()
            val shelfCode = mEditShelfCode.text?.toString()?.trim()
            val currentQuantity = mEditCurrent.text?.toString()?.trim()
            val maxQuantity = mEditMax.text?.toString()?.trim()
            val price = mEditPrice.text?.toString()?.trim()

            Log.d(
                LOG_TAG,
                "\nName: $name\nImageUrl: $imageUrl\nShelfCode: $shelfCode\nCurrent: $currentQuantity\nMax: $maxQuantity\nPrice: $price\nStatus: $mStatus"
            )
            when {
                TextUtils.isEmpty(name) -> {
                    showErrorTextField(
                        nameField,
                        R.string.name
                    )
                    removeErrorTextField(imageField)
                    removeErrorTextField(shelfCodeField)
                    removeErrorTextField(currentField)
                    removeErrorTextField(maxField)
                    removeErrorTextField(priceField)
                }
                TextUtils.isEmpty(imageUrl) -> {
                    showErrorTextField(
                        imageField,
                        R.string.image_url
                    )
                    removeErrorTextField(nameField)
                    removeErrorTextField(shelfCodeField)
                    removeErrorTextField(currentField)
                    removeErrorTextField(maxField)
                    removeErrorTextField(priceField)
                }
                TextUtils.isEmpty(shelfCode) -> {
                    showErrorTextField(
                        shelfCodeField,
                        R.string.shelf_code
                    )
                    removeErrorTextField(nameField)
                    removeErrorTextField(imageField)
                    removeErrorTextField(currentField)
                    removeErrorTextField(maxField)
                    removeErrorTextField(priceField)
                }
                TextUtils.isEmpty(currentQuantity) -> {
                    showErrorTextField(
                        currentField,
                        R.string.current_quantity
                    )
                    removeErrorTextField(nameField)
                    removeErrorTextField(imageField)
                    removeErrorTextField(shelfCodeField)
                    removeErrorTextField(maxField)
                    removeErrorTextField(priceField)
                }
                TextUtils.isEmpty(maxQuantity) -> {
                    showErrorTextField(
                        maxField,
                        R.string.max_quantity
                    )
                    removeErrorTextField(nameField)
                    removeErrorTextField(imageField)
                    removeErrorTextField(shelfCodeField)
                    removeErrorTextField(currentField)
                    removeErrorTextField(priceField)
                }
                (mEditCurrent.text.toString().toInt() > mEditMax.text.toString().toInt()) -> {
                    currentField.apply {
                        isErrorEnabled = true
                        error = getString(R.string.quantity_error)
                    }
                    removeErrorTextField(nameField)
                    removeErrorTextField(imageField)
                    removeErrorTextField(shelfCodeField)
                    removeErrorTextField(maxField)
                    removeErrorTextField(priceField)
                }
                TextUtils.isEmpty(price) -> {
                    showErrorTextField(
                        priceField,
                        R.string.price
                    )
                    removeErrorTextField(nameField)
                    removeErrorTextField(imageField)
                    removeErrorTextField(shelfCodeField)
                    removeErrorTextField(currentField)
                    removeErrorTextField(maxField)
                }
                else -> {
                    removeErrorTextField(nameField)
                    removeErrorTextField(imageField)
                    removeErrorTextField(shelfCodeField)
                    removeErrorTextField(currentField)
                    removeErrorTextField(maxField)
                    removeErrorTextField(priceField)

                    val values = ContentValues()
                    values.apply {
                        put(ItemEntry.COLUMN_ITEM_NAME, name)
                        put(ItemEntry.COLUMN_ITEM_IMAGE, imageUrl)
                        put(ItemEntry.COLUMN_SHELF_CODE, shelfCode)
                        put(ItemEntry.COLUMN_ITEM_CURRENT_QUANTITY, currentQuantity!!.toInt())
                        put(ItemEntry.COLUMN_ITEM_MAX_CAPACITY, maxQuantity!!.toInt())
                        put(ItemEntry.COLUMN_ITEM_PRICE, price!!.toDouble())
                        put(ItemEntry.COLUMN_ITEM_STATUS, mStatus)
                    }
                    if (mCurrentUri != null)
                        contentResolver.update(mCurrentUri!!, values, null, null)
                    else
                        contentResolver.insert(ItemEntry.CONTENT_URI, values)
                    finish()
                }
            }
        }
    }

    private fun showErrorTextField(view: TextInputLayout, stringResourceId: Int) {
        view.apply {
            isErrorEnabled = true
            error = getString(R.string.field_cannot_be_empty, getString(stringResourceId))
        }
    }

    private fun removeErrorTextField(view: TextInputLayout) {
        view.apply {
            isErrorEnabled = false
            error = null
        }
    }

    private fun showDeleteConfirmDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(R.string.delete_dialog_title)
            .setMessage(R.string.delete_dialog_message)
            .setIcon(R.drawable.ic_warning)
            .setCancelable(false)
            .setPositiveButton(R.string.delete_item) { _, _ ->
                contentResolver.delete(mCurrentUri!!, null, null)
                finish()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun onClickDelete() {
        if (mCurrentUri != null) {
            btnDelete.setOnClickListener {
                showDeleteConfirmDialog()
            }
        }
    }

    private fun showDiscardChangeDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle(R.string.discard_dialog_title)
            .setMessage(R.string.discard_dialog_message)
            .setIcon(R.drawable.ic_warning)
            .setCancelable(false)
            .setPositiveButton(R.string.discard_change) { _, _ ->
                finish()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onBackPressed() {
        if (!mItemHasChanged)
            super.onBackPressed()
        else
            showDiscardChangeDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (!mItemHasChanged)
                    finish()
                else
                    showDiscardChangeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(
            ItemEntry.COLUMN_ITEM_NAME,
            ItemEntry.COLUMN_ITEM_IMAGE,
            ItemEntry.COLUMN_SHELF_CODE,
            ItemEntry.COLUMN_ITEM_STATUS,
            ItemEntry.COLUMN_ITEM_CURRENT_QUANTITY,
            ItemEntry.COLUMN_ITEM_MAX_CAPACITY,
            ItemEntry.COLUMN_ITEM_PRICE
        )
        return CursorLoader(
            this,
            mCurrentUri!!,
            projection,
            null,
            null,
            null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (data?.moveToFirst() == true) {

            with(data) {
                mEditName.setText(getStringOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_NAME)))
                mEditImageUrl.setText(getStringOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE)))
                mEditShelfCode.setText(getStringOrNull(getColumnIndex(ItemEntry.COLUMN_SHELF_CODE)))
                mEditCurrent.setText(getIntOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_CURRENT_QUANTITY)).toString())
                mEditMax.setText(getIntOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_MAX_CAPACITY)).toString())
                mEditPrice.setText(getDoubleOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE)).toString())

                val status = getIntOrNull(getColumnIndex(ItemEntry.COLUMN_ITEM_STATUS))!!
                when (status) {
                    ItemEntry.STATUS_NORMAL -> mSpinner.setSelection(ItemEntry.STATUS_NORMAL)
                    ItemEntry.STATUS_OUT_OF_STOCK -> mSpinner.setSelection(ItemEntry.STATUS_OUT_OF_STOCK)
                    ItemEntry.STATUS_ABOUT_TO_RUN_OUT -> mSpinner.setSelection(ItemEntry.STATUS_ABOUT_TO_RUN_OUT)
                    ItemEntry.STATUS_ABOUT_TO_EXPIRE -> mSpinner.setSelection(ItemEntry.STATUS_ABOUT_TO_EXPIRE)
                    ItemEntry.STATUS_EXPIRED -> mSpinner.setSelection(ItemEntry.STATUS_EXPIRED)
                    ItemEntry.STATUS_FILLED -> mSpinner.setSelection(ItemEntry.STATUS_FILLED)
                }

                Log.d(
                    LOG_TAG,
                    "\nName: ${mEditName.text}\nImageUrl: ${mEditImageUrl.text}\nShelfCode: ${mEditShelfCode.text}\nCurrent Quantity: ${mEditCurrent.text}\nMax Quantity: ${mEditMax.text}\nPrice: ${mEditPrice.text}\nStatus: ${
                        mSpinner.getItemAtPosition(mStatus)
                    }"
                )
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mEditName.text?.clear()
        mEditImageUrl.text?.clear()
        mEditShelfCode.text?.clear()
        mEditCurrent.text?.clear()
        mEditMax.text?.clear()
        mEditPrice.text?.clear()
        mSpinner.setSelection(ItemEntry.STATUS_NORMAL)
    }
}