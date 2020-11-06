package com.example.sms_connect

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseToken"
    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "Android4Dev"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, token)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Sin permiso par leer telefono")
        }else{
            val telephonyManager: TelephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            val subscriberId = telephonyManager.subscriberId
            registerPhone(subscriberId, token);
        }
    }

    private fun registerPhone(subscriberId: String, notificationToken: String?){
        val url = "https://sms-fastsuov2.herokuapp.com/registerphone"
        Log.d(TAG, subscriberId)
        Log.d(TAG, url)
        val data = hashMapOf<String, Any?>()
        data["phoneId"] = subscriberId
        data["notificationToken"] = notificationToken

        val jsonObjectRequest = object: StringRequest(
            Request.Method.POST, url,
            { response ->
                Log.d(TAG, response.toString())
                Toast.makeText(applicationContext, response.toString(), Toast.LENGTH_SHORT).show()
            },
            { error ->
                Log.d(TAG, error.toString())
                Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
            }
        ){
            override fun getBodyContentType(): String {
                return "application/json"
            }
            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val params2 = HashMap<String, String>()
                params2.put("Login","your credentials" )
                params2.put("Password", "your credentials")
                return JSONObject(data).toString().toByteArray()
            }
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjoiYWRtaW4iLCJpYXQiOjE2MDI1NTIyMTh9.knnsVp9PDr5_dP_CgzNEDE9aVHYCb1pGnwyJd5WI0D0"
                return headers
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(jsonObjectRequest);
    }

    fun sendMessage(number: String?, message: String? ) {
        if(!number.isNullOrEmpty()){
            val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(number, null, message, null, null)
            } else {
                Log.i(TAG, "Error sin acceso a sms")
            }
        }else{
            Log.i(TAG, "Sin numero de telefono")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage?.let { message ->
            Log.i(TAG, message.getData().get("message"))
            sendMessage(message.data["number"], message.data["smsBody"])
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Setting up Notification channels for android O and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setupNotificationChannels()
            }
            val notificationId = Random().nextInt(60000)

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)  //a resource for your custom small icon
                .setContentTitle(message.data["title"]) //the "title" value you sent in your notification
                .setContentText(message.data["message"]) //ditto
                .setAutoCancel(true)  //dismisses the notification on click
                .setSound(defaultSoundUri)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build())

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels() {
        val adminChannelName = getString(R.string.notifications_admin_channel_name)
        val adminChannelDescription = getString(R.string.notifications_admin_channel_description)

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager.createNotificationChannel(adminChannel)
    }
}