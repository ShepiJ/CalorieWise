package com.example.caloriewise

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var morningEditText: EditText
    private lateinit var midDayEditText: EditText
    private lateinit var nightEditText: EditText
    private lateinit var totalCaloriesTextView: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_color)

        morningEditText = findViewById(R.id.morning)
        midDayEditText = findViewById(R.id.mid_day)
        nightEditText = findViewById(R.id.night)
        totalCaloriesTextView = findViewById(R.id.total_calories)


        sharedPreferences = getSharedPreferences("CalorieData", Context.MODE_PRIVATE)


        loadCalorieData()


        setupTextChangeListeners()


        updateTotalCalories()

        val historyButton: Button = findViewById(R.id.previous_month_button)
        historyButton.setOnClickListener {
            showHistoryPopup()
        }

        val tipsButton: Button = findViewById(R.id.Tips)
        tipsButton.setOnClickListener {
            showTipsPopup()
        }
    }

    private fun setupTextChangeListeners() {

        morningEditText.addTextChangedListener(textWatcher)
        midDayEditText.addTextChangedListener(textWatcher)
        nightEditText.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateTotalCalories()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun updateTotalCalories() {
        val morningCalories = morningEditText.text.toString().toIntOrNull() ?: 0
        val midDayCalories = midDayEditText.text.toString().toIntOrNull() ?: 0
        val nightCalories = nightEditText.text.toString().toIntOrNull() ?: 0

        val totalCalories = morningCalories + midDayCalories + nightCalories

        totalCaloriesTextView.text = totalCalories.toString()


        when {
            totalCalories < 1500 -> {
                totalCaloriesTextView.setTextColor(ContextCompat.getColor(this, R.color.cyan_blue))
            }

            totalCalories >= 1500 && totalCalories < 2300 -> {
                totalCaloriesTextView.setTextColor(ContextCompat.getColor(this, R.color.yellow))
            }

            else -> {
                totalCaloriesTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }


        saveCalorieData(morningCalories, midDayCalories, nightCalories)
    }

    private fun saveCalorieData(morning: Int, midDay: Int, night: Int) {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val editor = sharedPreferences.edit()
        editor.putInt(currentDate + "_morning", morning)
        editor.putInt(currentDate + "_midDay", midDay)
        editor.putInt(currentDate + "_night", night)
        editor.apply()
    }

    private fun loadCalorieData() {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val morningCalories = sharedPreferences.getInt(currentDate + "_morning", 0)
        val midDayCalories = sharedPreferences.getInt(currentDate + "_midDay", 0)
        val nightCalories = sharedPreferences.getInt(currentDate + "_night", 0)

        morningEditText.setText(if (morningCalories != 0) morningCalories.toString() else "")
        midDayEditText.setText(if (midDayCalories != 0) midDayCalories.toString() else "")
        nightEditText.setText(if (nightCalories != 0) nightCalories.toString() else "")
    }

    private fun showHistoryPopup() {
        val dialogView = layoutInflater.inflate(R.layout.history_popup, null)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("")

        val historyTextView: TextView = dialogView.findViewById(R.id.history_text)
        val closeButton: Button = dialogView.findViewById(R.id.close_button)

        val calendar = Calendar.getInstance()
        val newHistory = StringBuilder()

        for (day in 1 until calendar.get(Calendar.DAY_OF_MONTH)) {
            calendar.set(Calendar.DAY_OF_MONTH, day)

            val historyDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            val morningCalories = sharedPreferences.getInt(historyDate + "_morning", 0)
            val midDayCalories = sharedPreferences.getInt(historyDate + "_midDay", 0)
            val nightCalories = sharedPreferences.getInt(historyDate + "_night", 0)

            if (morningCalories != 0 || midDayCalories != 0 || nightCalories != 0) {
                val totalCalories = morningCalories + midDayCalories + nightCalories
                val color = getTextColorForCalories(totalCalories)
                newHistory.append("$historyDate - ").append(color).append("<br>")
            }
        }

        historyTextView.text = Html.fromHtml(newHistory.toString(), Html.FROM_HTML_MODE_LEGACY)

        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.show()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        flushCurrentData()
    }



    private fun getTextColorForCalories(calories: Int): String {
        return when {
            calories < 1500 -> "<font color='#00FFFF'>$calories</font>"
            calories >= 1500 && calories < 2300 -> "<font color='#FFFF00'>$calories</font>"
            else -> "<font color='#FF0000'>$calories</font>"
        }
    }

    private fun flushCurrentData() {

        val lastSavedDate = sharedPreferences.getString("lastSavedDate", "")


        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())


        if (lastSavedDate != currentDate) {

            val morningCalories = sharedPreferences.getInt(currentDate + "_morning", 0)
            val midDayCalories = sharedPreferences.getInt(currentDate + "_midDay", 0)
            val nightCalories = sharedPreferences.getInt(currentDate + "_night", 0)

            val totalCalories = morningCalories + midDayCalories + nightCalories
            val historyData = sharedPreferences.getString("historyData", "") ?: ""

            val newHistory = StringBuilder(historyData)
            if (totalCalories != 0) {
                newHistory.append("$currentDate - $totalCalories\n")
            }

            val editor = sharedPreferences.edit()
            editor.putString("historyData", newHistory.toString())

            editor.remove(currentDate + "_morning")
            editor.remove(currentDate + "_midDay")
            editor.remove(currentDate + "_night")

            editor.putString("lastSavedDate", currentDate)
            editor.apply()
        }
    }

    private fun showTipsPopup() {
        val dialogView = layoutInflater.inflate(R.layout.tips_popup, null)

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("")

        val closeButton: Button = dialogView.findViewById(R.id.close_button)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.show()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }

}
