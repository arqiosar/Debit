package fr.mastergime.arqioui.debit.services
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.mastergime.arqioui.debit.MainActivity
import fr.mastergime.arqioui.debit.R
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.DecimalFormat


class DebitService : Service() {
    var speed: Double = 0.0
    private val fileName = "values.txt"
    private val channelId = "Notification from Debit APP"
    var lowestNumber = Double.MAX_VALUE

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val thread = Thread {
            try {
                Log.d("info", "service running")
                calculateSpeed("https://picsum.photos/200/300")
                saveValueFile(speed.toString(), fileName)

                val test = lowestNumber
                readValueFile(fileName)
                if(test != lowestNumber){
                    createNotification(lowestNumber)
                    Log.d("info","Lowest number is $lowestNumber")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    private fun calculateSpeed(url: String) {
        val startTime: Long = System.currentTimeMillis()
        Log.d("info", "StartTime: $startTime")

        val stream = ByteArrayOutputStream()
        mLoad(url)?.compress(Bitmap.CompressFormat.JPEG, 99, stream)

        val imageInByte = stream.toByteArray()
        val lengthBmp = imageInByte.size.toLong()

        var endTime: Long = System.currentTimeMillis()
        Log.d("info", "EndTime: $endTime")
        Log.d("info", lengthBmp.toString() + "")

        val dataSize = (lengthBmp.toInt() / 1024).toLong()
        val takenTime: Long = endTime - startTime
        val s = takenTime.toDouble() / 1000
        speed = dataSize / s
        Log.d("info", "speed: " + "" + DecimalFormat("##.##").format(speed) + "kb/second")

    }

    private fun mLoad(string: String): Bitmap? {
        val url: URL = stringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun stringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun saveValueFile(value: String, fileName: String) {
        try {
            val fos: FileOutputStream = openFileOutput(fileName, Context.MODE_APPEND)
            val writer = PrintWriter(OutputStreamWriter(fos))
            writer.println(value)
            writer.close()
            Log.d("sara", "value saved to file")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readValueFile(fileName: String) {

        try {
            val fis = openFileInput(fileName)
            val br = BufferedReader(InputStreamReader(fis))
            var line: String
            var number: Double

            while (br.readLine().also { line = it } != null) {
                try {
                    number = line.toDouble()
                    val test = lowestNumber

                    if(number < lowestNumber){
                        lowestNumber = number
                        //return lowestNumber
                    } else lowestNumber

                } catch (ex: NumberFormatException) {

                }

            }

            br.close()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        //return null
    }

    private fun createNotification(value: Double) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(
                    channelId,
                    "Internet Speed Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("My notification")
            .setContentText("$value")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(1234, builder.build())
        }
    }
}