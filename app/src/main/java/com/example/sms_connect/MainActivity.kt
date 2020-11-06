package com.example.sms_connect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private val TAG = "MyFirebaseToken"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Sin permiso par leer telefono")
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.SEND_SMS),
                    1)
            }else{
                val telephonyManager: TelephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                val subscriberId = telephonyManager.subscriberId
                registerPhone(subscriberId, token);
            }

            // Log and toast
            // val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, "holasd")
            Log.d(TAG, token)
        })
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
