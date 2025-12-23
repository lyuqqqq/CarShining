package net.uniquecomputer.houseguru

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.ArrayAdapter

class CarManagementActivity : AppCompatActivity() {

    private lateinit var editPlate: TextInputEditText
    private lateinit var dropdownCarType: AutoCompleteTextView
    private lateinit var dropdownCarColor: AutoCompleteTextView
    private lateinit var buttonSaveCar: MaterialButton

    private lateinit var dbHelper: AppDatabaseHelper
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_management)

        // ---------- Top App Bar ----------
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val title = SpannableString("Car Management").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
        }
        supportActionBar?.title = title

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        dbHelper = AppDatabaseHelper(this)

        val sessionPrefs = getSharedPreferences("user_session", MODE_PRIVATE)
        currentUserId = sessionPrefs.getInt("current_user_id", -1)

        editPlate = findViewById(R.id.editPlate)
        dropdownCarType = findViewById(R.id.dropdownCarType)
        dropdownCarColor = findViewById(R.id.dropdownCarColor)
        buttonSaveCar = findViewById(R.id.buttonSaveCar)

        val carTypes = listOf("Sedan", "SUV", "7-seater or more")
        val carColors = listOf("Black", "White", "Grey", "Silver", "Brown", "Red", "Blue", "Yellow")

        dropdownCarType.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, carTypes)
        )
        dropdownCarColor.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, carColors)
        )

        if (currentUserId != -1) {
            val carInfo = dbHelper.getCarInfoForUser(currentUserId)
            editPlate.setText(carInfo?.plate ?: "")
            dropdownCarType.setText(carInfo?.type ?: "", false)
            dropdownCarColor.setText(carInfo?.color ?: "", false)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        buttonSaveCar.setOnClickListener {
            val plate = editPlate.text?.toString()?.trim() ?: ""
            val type = dropdownCarType.text.toString().trim()
            val color = dropdownCarColor.text.toString().trim()

            when {
                plate.isEmpty() -> {
                    editPlate.error = "Plate number is required"
                    editPlate.requestFocus()
                }
                type.isEmpty() -> {
                    Toast.makeText(this, "Please select car type", Toast.LENGTH_SHORT).show()
                    dropdownCarType.requestFocus()
                }
                color.isEmpty() -> {
                    Toast.makeText(this, "Please select car color", Toast.LENGTH_SHORT).show()
                    dropdownCarColor.requestFocus()
                }
                currentUserId == -1 -> {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    dbHelper.updateCarInfoForUser(
                        userId = currentUserId,
                        plate = plate,
                        type = type,
                        color = color
                    )

                    Toast.makeText(this, "Car information saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
