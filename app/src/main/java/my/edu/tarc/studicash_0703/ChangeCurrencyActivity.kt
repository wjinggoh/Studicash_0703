package my.edu.tarc.studicash_0703

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import my.edu.tarc.studicash_0703.databinding.ActivityChangeCurrencyBinding

class ChangeCurrencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeCurrencyBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currencies = mapOf(
        "Afghanistan" to "AFN",
        "Albania" to "ALL",
        "Algeria" to "DZD",
        "Andorra" to "EUR",
        "Angola" to "AOA",
        "Antigua and Barbuda" to "XCD",
        "Argentina" to "ARS",
        "Armenia" to "AMD",
        "Australia" to "AUD",
        "Austria" to "EUR",
        "Azerbaijan" to "AZN",
        "Bahamas" to "BSD",
        "Bahrain" to "BHD",
        "Bangladesh" to "BDT",
        "Barbados" to "BBD",
        "Belarus" to "BYN",
        "Belgium" to "EUR",
        "Belize" to "BZD",
        "Benin" to "XOF",
        "Bhutan" to "INR",
        "Bolivia" to "BOB",
        "Bosnia and Herzegovina" to "BAM",
        "Botswana" to "BWP",
        "Brazil" to "BRL",
        "Brunei" to "BND",
        "Bulgaria" to "BGN",
        "Burkina Faso" to "XOF",
        "Burundi" to "BIF",
        "Cabo Verde" to "CVE",
        "Cambodia" to "KHR",
        "Cameroon" to "XAF",
        "Canada" to "CAD",
        "Central African Republic" to "XAF",
        "Chad" to "XAF",
        "Chile" to "CLP",
        "China" to "CNY",
        "Colombia" to "COP",
        "Comoros" to "KMF",
        "Congo (Congo-Brazzaville)" to "XAF",
        "Costa Rica" to "CRC",
        "Croatia" to "HRK",
        "Cuba" to "CUP",
        "Cyprus" to "EUR",
        "Czechia" to "CZK",
        "Denmark" to "DKK",
        "Djibouti" to "DJF",
        "Dominica" to "XCD",
        "Dominican Republic" to "DOP",
        "Ecuador" to "USD",
        "Egypt" to "EGP",
        "El Salvador" to "USD",
        "Equatorial Guinea" to "XAF",
        "Eritrea" to "ERN",
        "Estonia" to "EUR",
        "Eswatini" to "SZL",
        "Ethiopia" to "ETB",
        "Fiji" to "FJD",
        "Finland" to "EUR",
        "France" to "EUR",
        "Gabon" to "XAF",
        "Gambia" to "GMD",
        "Georgia" to "GEL",
        "Germany" to "EUR",
        "Ghana" to "GHS",
        "Greece" to "EUR",
        "Grenada" to "XCD",
        "Guatemala" to "GTQ",
        "Guinea" to "GNF",
        "Guinea-Bissau" to "XOF",
        "Guyana" to "GYD",
        "Haiti" to "HTG",
        "Honduras" to "HNL",
        "Hungary" to "HUF",
        "Iceland" to "ISK",
        "India" to "INR",
        "Indonesia" to "IDR",
        "Iran" to "IRR",
        "Iraq" to "IQD",
        "Ireland" to "EUR",
        "Israel" to "ILS",
        "Italy" to "EUR",
        "Jamaica" to "JMD",
        "Japan" to "JPY",
        "Jordan" to "JOD",
        "Kazakhstan" to "KZT",
        "Kenya" to "KES",
        "Kiribati" to "AUD",
        "Kuwait" to "KWD",
        "Kyrgyzstan" to "KGS",
        "Laos" to "LAK",
        "Latvia" to "EUR",
        "Lebanon" to "LBP",
        "Lesotho" to "LSL",
        "Liberia" to "LRD",
        "Libya" to "LYD",
        "Liechtenstein" to "CHF",
        "Lithuania" to "EUR",
        "Luxembourg" to "EUR",
        "Madagascar" to "MGA",
        "Malawi" to "MWK",
        "Malaysia" to "MYR",
        "Maldives" to "MVR",
        "Mali" to "XOF",
        "Malta" to "EUR",
        "Marshall Islands" to "USD",
        "Mauritania" to "MRU",
        "Mauritius" to "MUR",
        "Mexico" to "MXN",
        "Micronesia" to "USD",
        "Moldova" to "MDL",
        "Monaco" to "EUR",
        "Mongolia" to "MNT",
        "Montenegro" to "EUR",
        "Morocco" to "MAD",
        "Mozambique" to "MZN",
        "Myanmar (Burma)" to "MMK",
        "Namibia" to "NAD",
        "Nauru" to "AUD",
        "Nepal" to "NPR",
        "Netherlands" to "EUR",
        "New Zealand" to "NZD",
        "Nicaragua" to "NIO",
        "Niger" to "XOF",
        "Nigeria" to "NGN",
        "North Korea" to "KPW",
        "North Macedonia" to "MKD",
        "Norway" to "NOK",
        "Oman" to "OMR",
        "Pakistan" to "PKR",
        "Palau" to "USD",
        "Palestine" to "ILS",
        "Panama" to "PAB",
        "Papua New Guinea" to "PGK",
        "Paraguay" to "PYG",
        "Peru" to "PEN",
        "Philippines" to "PHP",
        "Poland" to "PLN",
        "Portugal" to "EUR",
        "Qatar" to "QAR",
        "Romania" to "RON",
        "Russia" to "RUB",
        "Rwanda" to "RWF",
        "Saint Kitts and Nevis" to "XCD",
        "Saint Lucia" to "XCD",
        "Saint Vincent and the Grenadines" to "XCD",
        "Samoa" to "WST",
        "San Marino" to "EUR",
        "Sao Tome and Principe" to "STN",
        "Saudi Arabia" to "SAR",
        "Senegal" to "XOF",
        "Serbia" to "RSD",
        "Seychelles" to "SCR",
        "Sierra Leone" to "SLL",
        "Singapore" to "SGD",
        "Sint Maarten" to "ANG",
        "Slovakia" to "EUR",
        "Slovenia" to "EUR",
        "Solomon Islands" to "SBD",
        "Somalia" to "SOS",
        "South Africa" to "ZAR",
        "South Sudan" to "SSP",
        "Spain" to "EUR",
        "Sri Lanka" to "LKR",
        "Sudan" to "SDG",
        "Suriname" to "SRD",
        "Sweden" to "SEK",
        "Switzerland" to "CHF",
        "Syria" to "SYP",
        "Taiwan" to "TWD",
        "Tajikistan" to "TJS",
        "Tanzania" to "TZS",
        "Thailand" to "THB",
        "Timor-Leste" to "USD",
        "Togo" to "XOF",
        "Tonga" to "TOP",
        "Trinidad and Tobago" to "TTD",
        "Tunisia" to "TND",
        "Turkey" to "TRY",
        "Turkmenistan" to "TMT",
        "Tuvalu" to "AUD",
        "Uganda" to "UGX",
        "Ukraine" to "UAH",
        "United Arab Emirates" to "AED",
        "United Kingdom" to "GBP",
        "United States" to "USD",
        "Uruguay" to "UYU",
        "Uzbekistan" to "UZS",
        "Vanuatu" to "VUV",
        "Vatican City" to "EUR",
        "Venezuela" to "VES",
        "Vietnam" to "VND",
        "Yemen" to "YER",
        "Zambia" to "ZMW",
        "Zimbabwe" to "ZWL"
    )

    private val currencySymbols = mapOf(
        "AFN" to "؋",
        "ALL" to "L",
        "DZD" to "د.ج",
        "EUR" to "€",
        "AOA" to "Kz",
        "XCD" to "$",
        "ARS" to "$",
        "AMD" to "֏",
        "AUD" to "$",
        "AZN" to "₼",
        "BSD" to "$",
        "BHD" to "ب.د",
        "BDT" to "৳",
        "BBD" to "$",
        "BYN" to "Br",
        "BZD" to "$",
        "XOF" to "Fr",
        "INR" to "₹",
        "BOB" to "$/b",
        "BAM" to "KM",
        "BWP" to "P",
        "BRL" to "R$",
        "BND" to "$",
        "BGN" to "лв",
        "BIF" to "FBu",
        "CVE" to "$",
        "KHR" to "៛",
        "XAF" to "Fr",
        "CAD" to "$",
        "CLP" to "$",
        "CNY" to "¥",
        "COP" to "$",
        "KMF" to "CF",
        "CRC" to "₡",
        "HRK" to "kn",
        "CUP" to "$",
        "CZK" to "Kč",
        "DKK" to "kr",
        "DJF" to "Fdj",
        "DOP" to "$",
        "USD" to "$",
        "EGP" to "ج.م",
        "ERN" to "Nfk",
        "ETB" to "Br",
        "FJD" to "$",
        "GMD" to "D",
        "GEL" to "ლ",
        "GHS" to "₵",
        "GTQ" to "Q",
        "GNF" to "FG",
        "GYD" to "$",
        "HTG" to "G",
        "HNL" to "L",
        "HUF" to "Ft",
        "ISK" to "kr",
        "IDR" to "Rp",
        "IRR" to "﷼",
        "IQD" to "ع.د",
        "ILS" to "₪",
        "JMD" to "$",
        "JPY" to "¥",
        "JOD" to "د.ا",
        "KZT" to "₸",
        "KES" to "KSh",
        "KWD" to "د.ك",
        "KGS" to "с",
        "LAK" to "₭",
        "LKR" to "₨",
        "LSL" to "L",
        "LRD" to "$",
        "LYD" to "د.ل",
        "CHF" to "CHF",
        "MGA" to "Ar",
        "MWK" to "MK",
        "MYR" to "RM",
        "MVR" to "Rf",
        "MRU" to "UM",
        "MUR" to "₨",
        "MXN" to "$",
        "MDL" to "lei",
        "MNT" to "₮",
        "MAD" to "د.م.",
        "MZN" to "MT",
        "MMK" to "K",
        "NAD" to "$",
        "NPR" to "Rs",
        "NZD" to "$",
        "NIO" to "C$",
        "NGN" to "₦",
        "KPW" to "₩",
        "MKD" to "ден",
        "NOK" to "kr",
        "OMR" to "ر.ع.",
        "PKR" to "₨",
        "PAB" to "B/.",
        "PGK" to "K",
        "PYG" to "₲",
        "PEN" to "S/",
        "PHP" to "₱",
        "PLN" to "zł",
        "QAR" to "ر.ق",
        "RON" to "lei",
        "RUB" to "₽",
        "RWF" to "FRw",
        "WST" to "T",
        "STN" to "Db",
        "SAR" to "ر.س",
        "SCR" to "₨",
        "SLL" to "Le",
        "SGD" to "$",
        "ANG" to "ƒ",
        "SBD" to "$",
        "SOS" to "S",
        "ZAR" to "R",
        "SSP" to "£",
        "LKR" to "₨",
        "SDG" to "ج.س",
        "SRD" to "$",
        "SEK" to "kr",
        "SYP" to "ل.س",
        "TWD" to "NT$",
        "TJS" to "ЅМ",
        "TZS" to "TSh",
        "THB" to "฿",
        "TOP" to "$",
        "TTD" to "$",
        "TND" to "د.ت",
        "TRY" to "₺",
        "TMT" to "m",
        "UGX" to "USh",
        "UAH" to "₴",
        "AED" to "د.إ",
        "GBP" to "£",
        "UYU" to "$",
        "UZS" to "сўм",
        "VUV" to "Vt",
        "VES" to "Bs.S.",
        "VND" to "₫",
        "YER" to "﷼",
        "ZMW" to "ZK",
        "ZWL" to "$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeCurrencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Populate spinner with country names
        val countryNames = currencies.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter

        // Set up spinner selection listener
        binding.spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedCountry = parent?.getItemAtPosition(position) as String
                val currencyCode = currencies[selectedCountry] ?: "USD"
                updateCurrency(currencyCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when no item is selected, if necessary
            }
        }

        binding.saveCurrencyBtn.setOnClickListener {
            val selectedCountry = binding.spinnerCurrency.selectedItem as String
            val currencyCode = currencies[selectedCountry] ?: "USD"
            updateCurrency(currencyCode)
        }
    }

    private fun updateCurrency(currencyCode: String) {
        // Retrieve the current user ID dynamically
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("User").document(userId)
                .update("currency", currencyCode)
                .addOnSuccessListener {
                    // Handle success
                    updateCurrencySymbol(currencyCode)
                    Toast.makeText(this, "Currency updated successfully.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Handle failure
                    showError("Failed to update currency. Please try again.")
                }
        } else {
            showError("User not authenticated. Please log in again.")
        }
    }

    private fun updateCurrencySymbol(currencyCode: String) {
        val currencySymbol = currencySymbols[currencyCode] ?: "$"
        // Store the currency symbol in SharedPreferences or another persistent storage
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("currency_symbol", currencySymbol)
            apply()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
