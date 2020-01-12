package com.lemonlab.all_in_one.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Environment
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.lemonlab.all_in_one.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


fun View.checkUser() {
    // tells user to login to continue.
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null)
        this.findNavController().navigate(R.id.mustLoginFragment)
}

fun View.navigateToAndClear(destinationId: Int, newdes: Int) {
    val navOptions = NavOptions.Builder().setPopUpTo(destinationId, true).build()
    this.findNavController().navigate(
        newdes,
        null, navOptions
    )
}

fun Context.showMessage(m: String) {
    Toast.makeText(this, m, Toast.LENGTH_LONG).show()
}

fun makeTheUserOnline() {
    val uid = FirebaseAuth.getInstance().uid
    // check if there user logged in
    if (!uid.isNullOrEmpty()) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document("$uid")
            .update("online", true)

        // store the data in the real time database
        FirebaseDatabase.getInstance().getReference("status/$uid").setValue("online")

        // Adding on disconnect hook
        FirebaseDatabase.getInstance().getReference("/status/$uid")
            .onDisconnect()     // Set up the disconnect hook
            .setValue("offline")

    }
}


// Highlights text background.
fun Context.highlightText(text: String): SpannableString {
    val str = SpannableString(text)
    str.setSpan(
        BackgroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimaryDark)),
        0,
        text.length,
        0
    )
    return str
}


fun Context.highlightTextWithColor(color: Int, text: String): SpannableString {
    val str = SpannableString(text)
    str.setSpan(
        BackgroundColorSpan(ContextCompat.getColor(this, color)),
        0,
        text.length,
        0
    )
    return str
}


fun Activity.createImageFile(): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir /* directory */
    )
}


fun Activity.setFragmentTitle(text: String) {
    (this as AppCompatActivity).supportActionBar!!.title = text
}

fun View.getBitmapFromView(): Bitmap {
    // Define a bitmap with the same size as the view
    val returnedBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    // Bind a canvas to it
    val canvas = Canvas(returnedBitmap)
    // Get the view's background
    val bgDrawable = this.background
    if (bgDrawable != null)
    // has background drawable, then draw it on the canvas
        bgDrawable.draw(canvas)
    else
    // does not have background drawable, then draw white background on the canvas
        canvas.drawColor(Color.WHITE)
    // draw the view on the canvas
    this.draw(canvas)
    //return the bitmap
    return returnedBitmap
}


fun View.recreateFragment(fragmentID: Int) {
    findNavController().navigate(
        fragmentID,
        null,
        NavOptions.Builder()
            .setPopUpTo(fragmentID, true)
            .build()
    )
}

fun Context.showYesNoDialog(
    functionToPerform: () -> Unit,
    functionIfCancel: () -> Unit,
    dialogTitle: String,
    dialogMessage: String
) {

    val dialogBuilder = AlertDialog.Builder(this).create()
    val dialogView = with(LayoutInflater.from(this)) {
        inflate(
            R.layout.yes_no_dialog,
            null
        )
    }

    dialogView.findViewById<AppCompatTextView>(R.id.dialogTitle).text = dialogTitle
    dialogView.findViewById<AppCompatTextView>(R.id.dialogMessageText).text = dialogMessage

    dialogView.findViewById<AppCompatButton>(R.id.dialogCancelButton).setOnClickListener {
        functionIfCancel()
        dialogBuilder.dismiss()
    }

    dialogView.findViewById<AppCompatButton>(R.id.dialogConfirmButton).setOnClickListener {
        functionToPerform()
        dialogBuilder.dismiss()
    }

    with(dialogBuilder) {
        setView(dialogView)
        show()
    }

}


fun String.removeWhitespace(): String {
    var isFirstSpace = false
    var result = ""
    for (char in this) {
        if (char != ' ' && char != '\n') {
            isFirstSpace = true
            result += char
        } else if (isFirstSpace) {
            result += " "
            isFirstSpace = false
        }
    }
    return result
}

fun Activity.hideKeypad(view: View) {
    val inputMethod =
        this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethod.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getDateAsString(date: Date) =
    DateFormat.format("yyyy-MM-dd hh:mm", date)!!