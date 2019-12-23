package com.kylecorry.survival_aid.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kylecorry.survival_aid.R
import com.kylecorry.survival_aid.navigator.gps.UnitSystem
import org.w3c.dom.Text
import java.util.*
import kotlin.math.roundToInt

class WeatherFragment : Fragment(), Observer {


    private val unitSystem = UnitSystem.IMPERIAL
    private lateinit var moonPhase: MoonPhase
    private lateinit var thermometer: Thermometer
    private lateinit var hygrometer: Hygrometer

    private lateinit var moonTxt: TextView
    private lateinit var tempTxt: TextView
    private lateinit var humidityTxt: TextView
    private lateinit var feelsLikeTxt: TextView
    private lateinit var dewpointTxt: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_weather, container, false)

        moonPhase = MoonPhase()
        thermometer = Thermometer(context!!)
        hygrometer = Hygrometer(context!!)

        moonTxt = view.findViewById(R.id.moonphase)
        tempTxt = view.findViewById(R.id.temperature)
        humidityTxt = view.findViewById(R.id.humidity)
        feelsLikeTxt = view.findViewById(R.id.feelslike)
        dewpointTxt = view.findViewById(R.id.dewpoint)

        return view
    }

    override fun onResume() {
        super.onResume()
        thermometer.addObserver(this)
        hygrometer.addObserver(this)

        thermometer.start()
        hygrometer.start()

        updateMoonPhase()
        updateTemperature()
        updateHumidity()
    }

    override fun onPause() {
        super.onPause()
        thermometer.stop()
        hygrometer.stop()

        thermometer.deleteObserver(this)
        hygrometer.deleteObserver(this)
    }

    override fun update(o: Observable?, arg: Any?) {
        if (o == thermometer) updateTemperature()
        if (o == hygrometer) updateHumidity()
    }

    private fun updateHumidity(){
        val humidity = hygrometer.humidity.roundToInt()

        humidityTxt.text = "$humidity% Humidity"
        updateTempHumidityCombos()
    }

    private fun updateTemperature(){
        val temp = if (unitSystem == UnitSystem.IMPERIAL) WeatherUtils.celsiusToFahrenheit(thermometer.temperature) else thermometer.temperature
        val symbol = if (unitSystem == UnitSystem.IMPERIAL) "F" else "C"
        tempTxt.text = "${temp.roundToInt()} °$symbol"
        updateTempHumidityCombos()
    }


    private fun updateTempHumidityCombos(){
        val temp = thermometer.temperature
        val humidity = hygrometer.humidity

        val heatIndex = convertTemp(WeatherUtils.getHeatIndex(temp, humidity)).roundToInt()
        val comfortLevel = WeatherUtils.getHumidityComfortLevel(temp, humidity)
        val dewPoint = convertTemp(WeatherUtils.getDewPoint(temp, humidity)).roundToInt()
        val heatAlert = WeatherUtils.getHeatAlert(temp, humidity)

        val symbol = if (unitSystem == UnitSystem.IMPERIAL) "F" else "C"
        var feelsLikeString = "Feels like $heatIndex °$symbol"

        if (heatAlert != WeatherUtils.HeatAlert.NORMAL){
            feelsLikeString += " - ${heatAlert.readableName}"
        }

        feelsLikeTxt.text = feelsLikeString

        dewpointTxt.text = "Dew point: $dewPoint °$symbol"
    }


    private fun convertTemp(temp: Float): Float {
        if (unitSystem == UnitSystem.IMPERIAL){
            return WeatherUtils.celsiusToFahrenheit(temp)
        }

        return temp
    }


    private fun updateMoonPhase(){
        val phase = moonPhase.getPhase()
        val stringPhase = when {
            phase == MoonPhase.Phase.WANING_CRESCENT -> "Waning Crescent"
            phase == MoonPhase.Phase.WAXING_CRESCENT -> "Waxing Crescent"
            phase == MoonPhase.Phase.WANING_GIBBOUS -> "Waning Gibbous"
            phase == MoonPhase.Phase.WAXING_GIBBOUS -> "Waxing Gibbous"
            phase == MoonPhase.Phase.FIRST_QUARTER -> "First Quarter"
            phase == MoonPhase.Phase.LAST_QUARTER -> "Last Quarter"
            phase == MoonPhase.Phase.FULL -> "Full"
            else -> "New"
        }

        moonTxt.text = "Moon: $stringPhase"
    }
}