package com.sagar.aspiretestapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sagar.aspiretestapp.other.toast
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private final val REQUEST_ID_MULTIPLE_PERMISSIONS = 23

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkAndRequestPermissions()) {
            toast("We have permission")
            val list = getSMS()
            Log.d("Count", "Message Count: ${list.size}")
        } else {
            toast("We dont have Permission to read messages")
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val sms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        if (sms != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }

    private fun getSMS(): List<String> {
        val sms: MutableList<String> = ArrayList()
        val uriSmsUri: Uri = Uri.parse("content://sms/inbox")

        val date = Date(System.currentTimeMillis() - 93L * 24 * 3600 * 1000).time
        val cursor =
            contentResolver.query(uriSmsUri, null, "date" + ">?", arrayOf("" + date), "date DESC")

        while (cursor != null && cursor.moveToNext()) {
            val address: String = cursor.getString(cursor.getColumnIndex("address"))
            val body: String = cursor.getString(cursor.getColumnIndexOrThrow("body"))
            val formattedDate =
                millisToDate(cursor.getString(cursor.getColumnIndexOrThrow("date")).toLong())
            sms.add("Number: $address .Message: $body Date $formattedDate")
        }

//        val cur: Cursor? = contentResolver.query(uriSmsUri, null, null, null, null)
//        while (cur != null && cur.moveToNext()) {
//            val address: String = cur.getString(cur.getColumnIndex("address"))
//            val body: String = cur.getString(cur.getColumnIndexOrThrow("body"))
//            val formattedDate = millisToDate(cur.getString(cur.getColumnIndexOrThrow("date")).toLong())
//            sms.add("Number: $address .Message: $body")
//        }
//        cur?.close()
        cursor?.close()
        return sms
    }

    private fun millisToDate(currentTime: Long): String {
        val finalDate: String
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val date: Date = calendar.time
        finalDate = date.toString()
        return finalDate
    }
}
