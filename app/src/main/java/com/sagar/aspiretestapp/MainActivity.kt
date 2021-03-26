package com.sagar.aspiretestapp

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sagar.aspiretestapp.databinding.ActivityMainBinding
import com.sagar.aspiretestapp.other.Constants.LAST_DAYS_TO_FETCH_MESSAGE
import com.sagar.aspiretestapp.other.Constants.RECENT_TIME
import com.sagar.aspiretestapp.other.Constants.SETTINGS
import com.sagar.aspiretestapp.other.toast
import com.sagar.aspiretestapp.response.Message
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val messageRequest = 23
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)

        binding.tvText.movementMethod = ScrollingMovementMethod()

        if (checkAndRequestPermissions()) {
            readMessages()
            toast("We have permission")
        } else {
            toast("We dont have Permission to read messages")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == messageRequest && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readMessages()
            } else {
                toast("Need Read Permission to work!")
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val sms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        if (sms != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS),
                messageRequest
            )
            return false
        }
        return true
    }

    private fun displayData(list: List<Message>) {
        var result = ""
        result += "${list.size} \n"

        for (value in list) {
            result += "$value \n"
        }

        binding.tvText.text = result
    }

    private fun readMessages() {
        val messageList: MutableList<Message> = ArrayList()
        val uriSmsUri: Uri = Uri.parse("content://sms/inbox")
        var recentDay = sharedPref.getLong(RECENT_TIME, -1L)
        if (recentDay < 0) {
            recentDay =
                Date(System.currentTimeMillis() - LAST_DAYS_TO_FETCH_MESSAGE * 24 * 3600 * 1000).time
        }

        val cursor =
            contentResolver.query(
                uriSmsUri,
                null,
                "date" + ">?",
                arrayOf("" + recentDay),
                "date DESC"
            )

        while (cursor != null && cursor.moveToNext()) {
            val address: String = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS))
            val body: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
            val formattedDate =
                cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)).toLong()
            messageList.add(Message(address, body, formattedDate))
        }
        cursor?.close()

        if (messageList.isNotEmpty()) {
            updateRecentMessage(messageList[0].date)
        }

        displayData(messageList)
    }

    private fun updateRecentMessage(time: Long) {
        editor = sharedPref.edit()
        editor.putLong(RECENT_TIME, time)
        editor.apply()
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
