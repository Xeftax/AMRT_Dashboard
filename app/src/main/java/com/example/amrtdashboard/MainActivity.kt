package com.example.amrtdashboard


import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    lateinit var accelerometer: Sensor

    var vitesseLineaire: Float = 0f
    var vitesse: FloatArray = floatArrayOf(0f, 0f, 0f)
    var acceleration: FloatArray = floatArrayOf(0f, 0f, 0f)
    var initAcceleration: FloatArray = floatArrayOf(0f, 0f, 0f)
    var time: Long = System.currentTimeMillis()

    lateinit var speedDisplay: TextView
    lateinit var powerDisplay: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_main)

        val mainLayout: ConstraintLayout = findViewById(R.id.main_layout)

        val speedTest: SeekBar = findViewById(R.id.speed_test)
        val batteryTest: SeekBar = findViewById(R.id.battery_test)
        val powerTest: SeekBar = findViewById(R.id.power_test)
        speedDisplay = findViewById(R.id.speed_val)
        val batteryValue: TextView = findViewById(R.id.battery_val)
        val batteryDisplay: ProgressBar = findViewById(R.id.battery_progress)
        batteryDisplay.progressDrawable = resources.getDrawable(R.drawable.battery_progressbar)
        powerDisplay = findViewById(R.id.power_progress)
        val batteryTemp: ImageView = findViewById(R.id.battery_temp)
        val motorTemp: ImageView = findViewById(R.id.motor_temp)

        val paramLayout: ConstraintLayout = findViewById(R.id.param_layout)
        val btnParamLayout: ImageView = findViewById(R.id.show_param_layout)
        val btnConfAcc: Button = findViewById(R.id.btn_conf_acc)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        btnParamLayout.setOnClickListener {
            val constraint = ConstraintSet()
            constraint.clone(mainLayout)
            if (btnParamLayout.rotation == 90f){
                constraint.clear(paramLayout.id, ConstraintSet.START)
                constraint.connect(
                    paramLayout.id,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END
                )
                TransitionManager.beginDelayedTransition(mainLayout)
                constraint.applyTo(mainLayout)
                btnParamLayout.rotation = 270f
            } else {
                constraint.clear(paramLayout.id, ConstraintSet.END)
                constraint.connect(
                    paramLayout.id,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END
                )
                TransitionManager.beginDelayedTransition(mainLayout)
                constraint.applyTo(mainLayout)
                btnParamLayout.rotation = 90f
            }
        }

        btnConfAcc.setOnClickListener {
            initAcceleration = floatArrayOf(acceleration[0], acceleration[1],acceleration[2])
            vitesse = floatArrayOf(0f, 0f, 0f)
            vitesseLineaire = 0f
        }

        var (i, j) = Pair(0, 0)
        batteryTemp.setColorFilter(Color.parseColor("#9916DC16"))
        motorTemp.setColorFilter(Color.parseColor("#9916DC16"))

        batteryTemp.setOnClickListener {
            if (i == 0) batteryTemp.setColorFilter(Color.parseColor("#99FFA100"))
            if (i == 1) batteryTemp.setColorFilter(Color.parseColor("#F81010"))
            if (i == 2) {
                batteryTemp.setColorFilter(Color.parseColor("#9916DC16"))
                i=-1
            }
            i++
        }

        motorTemp.setOnClickListener {
            if (j == 0) motorTemp.setColorFilter(Color.parseColor("#99FFA100"))
            if (j == 1) motorTemp.setColorFilter(Color.parseColor("#F81010"))
            if (j == 2) {
                motorTemp.setColorFilter(Color.parseColor("#9916DC16"))
                j=-1
            }
            j++
        }


        /*speedTest.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speedDisplay.text = (progress * 1.5).toInt().toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("", "start tracking")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("", "stop tracking")
            }
        })*/

        batteryTest.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val batteryDrawable = batteryDisplay.progressDrawable as LayerDrawable
                val progressDrawable = batteryDrawable.getDrawable(1) as ClipDrawable
                if (progress <= 20) {
                    progressDrawable.setColorFilter(
                        Color.parseColor("#F81010"),
                        PorterDuff.Mode.SRC_IN
                    )
                } else if (progress <= 50) {
                    progressDrawable.setColorFilter(
                        Color.parseColor("#99FFA100"),
                        PorterDuff.Mode.SRC_IN
                    )
                } else {
                    progressDrawable.setColorFilter(
                        Color.parseColor("#9916DC16"),
                        PorterDuff.Mode.SRC_IN
                    )
                }
                batteryDisplay.progressDrawable = batteryDrawable
                batteryDisplay.progress = progress
                batteryValue.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("", "start tracking")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("", "stop tracking")
            }
        })

        powerTest.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                powerDisplay.progress = (progress * 0.65).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("", "start tracking")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("", "stop tracking")
            }
        })
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val dt:Float = (event!!.timestamp - time).toFloat()/1000000000

        acceleration[0] = event.values[0] - initAcceleration[0]
        acceleration[1] = event.values[1] - initAcceleration[1]
        acceleration[2] = event.values[2] - initAcceleration[2]

        vitesse[0] = vitesse[0] + acceleration[0]*dt
        vitesse[1] = vitesse[1] + acceleration[1]*dt
        vitesse[2] = vitesse[2] + acceleration[2]*dt

        vitesseLineaire = sqrt(vitesse[0].pow(2) + vitesse[1].pow(2) + vitesse[2].pow(2))

        //Log.d("acc",acceleration[0].toString())
        //Log.d("acc",initAcceleration[0].toString())
        Log.d("vit",vitesse.toString())
        //Log.d("t",t.toString())

        speedDisplay.text = (vitesseLineaire/3.6).roundToInt().toString()
        powerDisplay.progress = sqrt(acceleration[0].pow(2) + acceleration[1].pow(2) + acceleration[2].pow(2)).toInt()


        time = event.timestamp
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("accur", "blabla")
    }


}