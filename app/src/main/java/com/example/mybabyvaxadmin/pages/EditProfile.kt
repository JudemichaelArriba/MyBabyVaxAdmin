package com.example.mybabyvaxadmin.pages

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mybabyvaxadmin.R
import com.example.mybabyvaxadmin.components.DialogHelper
import com.example.mybabyvaxadmin.databinding.ActivityEditProfileBinding
import com.example.mybabyvaxadmin.models.Users
import com.example.iptfinal.interfaces.InterfaceClass
import com.example.iptfinal.services.DatabaseService
import com.example.iptfinal.services.SessionManager
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.hbb20.CountryCodePicker

class EditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val dbService = DatabaseService()
    private var selectedImageUri: Uri? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var user: Users

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data
                Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.default_profile)
                    .into(binding.profileImage)
                checkForChanges()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBarsInsets.bottom, imeInsets.bottom)
            view.setPadding(0, systemBarsInsets.top, 0, bottomPadding)
            insets
        }

        sessionManager = SessionManager(this)
        user = sessionManager.getUser()

        setupCountryCodePicker()
        loadUserProfile()
        setupTextWatchers()
        setupButtons()
        loadPuroks()

        setEditButtonEnabled(false)
    }

    private fun setupCountryCodePicker() {
        val ccp: CountryCodePicker = binding.countryCodePicker
        ccp.setCountryForNameCode("PH")
        ccp.setAutoDetectedCountry(true)
        ccp.registerCarrierNumberEditText(binding.mobileTv)

        binding.mobileTv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var input = s.toString()
                if (input.startsWith("0")) {
                    input = input.drop(1)
                    binding.mobileTv.setText(input)
                    binding.mobileTv.setSelection(input.length)
                }
                if (input.length > 12) {
                    input = input.substring(0, 12)
                    binding.mobileTv.setText(input)
                    binding.mobileTv.setSelection(12)
                }
                checkForChanges()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (user.profilePic.isNotEmpty()) {
                        if (user.profilePic.startsWith("/9j") || user.profilePic.contains("base64")) {
                            val imageBytes = Base64.decode(user.profilePic, Base64.DEFAULT)
                            val bitmap =
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            withContext(Dispatchers.Main) {
                                binding.profileImage.setImageBitmap(bitmap)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Glide.with(this@EditProfile)
                                    .load(user.profilePic)
                                    .placeholder(R.drawable.default_profile)
                                    .into(binding.profileImage)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.profileImage.setImageResource(R.drawable.default_profile)
                        }
                    }
                }

                binding.firstnameTv.setText(user.firstname)
                binding.lastnameTv.setText(user.lastname)
                binding.emailTv.setText(user.email)
                binding.addressTv.setText(if (user.address.isNullOrEmpty()) "" else user.address)

                val ccp: CountryCodePicker = binding.countryCodePicker
                if (!user.mobileNum.isNullOrEmpty() && user.mobileNum.startsWith("+")) {
                    try {
                        ccp.setFullNumber(user.mobileNum.replace("+", ""))
                        val number = user.mobileNum.takeLast(10)
                        binding.mobileTv.setText(number)
                    } catch (e: Exception) {
                        binding.mobileTv.setText(user.mobileNum)
                    }
                } else {
                    binding.mobileTv.setText(if (user.mobileNum == "null") "" else user.mobileNum)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@EditProfile, "Failed to load profile", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkForChanges()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.firstnameTv.addTextChangedListener(watcher)
        binding.lastnameTv.addTextChangedListener(watcher)
        binding.emailTv.addTextChangedListener(watcher)
        binding.addressTv.addTextChangedListener(watcher)
    }

    private fun checkForChanges() {
        val firstnameChanged = binding.firstnameTv.text.toString().trim() != user.firstname
        val lastnameChanged = binding.lastnameTv.text.toString().trim() != user.lastname
        val emailChanged = binding.emailTv.text.toString().trim() != user.email
        val addressChanged = binding.addressTv.text.toString().trim() != (user.address ?: "")
        val mobileChanged = binding.mobileTv.text.toString().trim() != user.mobileNum?.takeLast(10)
        val imageChanged = selectedImageUri != null

        val hasChanges =
            firstnameChanged || lastnameChanged || emailChanged || addressChanged || mobileChanged || imageChanged
        setEditButtonEnabled(hasChanges)
    }

    private fun setupButtons() {
        binding.uploadBtn.setOnClickListener { openGallery() }
        binding.backButton.setOnClickListener { finish() }
        binding.editBtn.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
        val ccp: CountryCodePicker = binding.countryCodePicker
        val fullNumber = "+" + ccp.fullNumber.trim()

        val updatedUser = user.copy(
            firstname = binding.firstnameTv.text.toString().trim(),
            lastname = binding.lastnameTv.text.toString().trim(),
            email = binding.emailTv.text.toString().trim(),
            address = binding.addressTv.text.toString().trim(),
            mobileNum = fullNumber
        )

        if (updatedUser == user && selectedImageUri == null) {
            setEditButtonEnabled(false)
            DialogHelper.showWarning(
                this,
                "No Changes Detected",
                "You haven’t modified any profile information."
            )
            return
        }

        DialogHelper.showWarning(
            this,
            "Update Profile",
            "Are you sure you want to save these changes?",
            onConfirm = {
                binding.loadingOverlay.visibility = android.view.View.VISIBLE
                lifecycleScope.launch {
                    val finalUser = if (selectedImageUri != null) {
                        val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val baos = java.io.ByteArrayOutputStream()
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos)
                        val base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                        updatedUser.copy(profilePic = base64Image)
                    } else updatedUser

                    dbService.updateUser(
                        finalUser.uid,
                        finalUser,
                        object : InterfaceClass.StatusCallback {
                            override fun onSuccess(message: String) {
                                sessionManager.saveUser(finalUser)
                                binding.loadingOverlay.visibility = android.view.View.GONE
                                DialogHelper.showSuccess(this@EditProfile, "Success", message) {
                                    finish()
                                }
                            }

                            override fun onError(message: String) {
                                binding.loadingOverlay.visibility = android.view.View.GONE
                                DialogHelper.showError(this@EditProfile, "Error", message)
                            }
                        })
                }
            })
    }

    private fun loadPuroks() {
        dbService.fetchPuroks(object : InterfaceClass.PurokCallback {
            override fun onPuroksLoaded(puroks: List<String>) {
                val adapter =
                    ArrayAdapter(this@EditProfile, android.R.layout.simple_spinner_item, puroks)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.addressTv.setAdapter(adapter)
            }

            override fun onError(message: String) {
                Toast.makeText(
                    this@EditProfile,
                    "Failed to load puroks: $message",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setEditButtonEnabled(enabled: Boolean) {
        binding.editBtn.isEnabled = enabled
        binding.editBtn.alpha = if (enabled) 1f else 0.5f
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
}
