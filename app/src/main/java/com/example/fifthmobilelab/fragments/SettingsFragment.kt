package com.example.fifthmobilelab.fragments

import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.fifthmobilelab.AppPreferences
import com.example.mobilelab.FontSizeApplier
import com.example.mobilelab.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            loadSettings()
        }

        // Theme Selection
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioLight.id -> {
                    saveThemePreference(false)
                    applyTheme(false)
                }
                binding.radioDark.id -> {
                    saveThemePreference(true)
                    applyTheme(true)
                }
            }
        }

        // Notifications
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                AppPreferences.setNotifications(requireContext(), isChecked)
            }
        }

        // Font Size
        binding.fontSizeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedFontSize = when (checkedId) {
                binding.radioSmall.id -> "Small"
                binding.radioMedium.id -> "Medium"
                binding.radioLarge.id -> "Large"
                else -> "Medium"
            }
            lifecycleScope.launch {
                AppPreferences.setFontSize(requireContext(), selectedFontSize)
                (activity as? FontSizeApplier)?.applyFontSize(selectedFontSize)
            }
        }

        // Language
        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = parent?.getItemAtPosition(position).toString()
                lifecycleScope.launch {
                    AppPreferences.setLanguage(requireContext(), selectedLanguage)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // File Management
        checkFileStatus()

        binding.btnDeleteFile.setOnClickListener {
            backupFileBeforeDelete("5.txt", "backup_5.txt")
            deleteFileFromDownloads("5.txt")
            checkFileStatus()
        }

        binding.btnRestoreFile.setOnClickListener {
            restoreBackup("backup_5.txt", "5.txt")
            checkFileStatus()
        }
    }

    private suspend fun loadSettings() {
        try {
            // Theme
            val isDarkMode = AppPreferences.isDarkMode(requireContext()).first()
            applyTheme(isDarkMode)
            binding.themeRadioGroup.check(if (isDarkMode) binding.radioDark.id else binding.radioLight.id)

            // Notifications
            binding.notificationsSwitch.isChecked =
                AppPreferences.areNotificationsEnabled(requireContext()).first()

            // Font Size
            val fontSize = AppPreferences.getFontSize(requireContext()).first()
            binding.fontSizeRadioGroup.check(
                when (fontSize) {
                    "Small" -> binding.radioSmall.id
                    "Medium" -> binding.radioMedium.id
                    "Large" -> binding.radioLarge.id
                    else -> binding.radioMedium.id
                }
            )

            // Language
            val languages = listOf("English", "Spanish", "French", "German")
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                languages
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.languageSpinner.adapter = adapter
            val language = AppPreferences.getLanguage(requireContext()).first()
            binding.languageSpinner.setSelection(languages.indexOf(language))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyTheme(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        lifecycleScope.launch {
            AppPreferences.setDarkMode(requireContext(), isDarkMode)
        }
    }

    private fun checkFileStatus() {
        val isFileExists = isFileInDownloads("5.txt")
        binding.fileStatus.text = if (isFileExists) "Статус файла: Найден" else "Статус файла: Не найден"
        binding.btnDeleteFile.visibility = if (isFileExists) View.VISIBLE else View.GONE

        val backupFile = File(requireContext().filesDir, "backup_5.txt")
        binding.btnRestoreFile.visibility = if (backupFile.exists()) View.VISIBLE else View.GONE
    }

    private fun isFileInDownloads(fileName: String): Boolean {
        val contentResolver = requireContext().contentResolver
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null).use { cursor ->
            return cursor != null && cursor.moveToFirst()
        }
    }

    private fun deleteFileFromDownloads(fileName: String) {
        val contentResolver = requireContext().contentResolver
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        val deletedRows = contentResolver.delete(MediaStore.Downloads.EXTERNAL_CONTENT_URI, selection, selectionArgs)
        if (deletedRows > 0) {
            Toast.makeText(context, "Файл удалён", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun backupFileBeforeDelete(fileName: String, backupFileName: String) {
        val contentResolver = requireContext().contentResolver
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        val cursor = contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID),
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build()

                val backupFile = File(requireContext().filesDir, backupFileName)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    backupFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    Toast.makeText(context, "Резервная копия создана", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Файл не найден для создания резервной копии", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun restoreBackup(backupFileName: String, originalFileName: String) {
        val backupFile = File(requireContext().filesDir, backupFileName)

        if (backupFile.exists()) {
            val content = backupFile.readText()
            saveFileToDownloads(content, originalFileName)
            if (backupFile.delete()) {
                Toast.makeText(context, "Файл восстановлен и резервная копия удалена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Файл восстановлен, но резервная копия не удалена", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Резервная копия отсутствует", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFileToDownloads(content: String, fileName: String) {
        val contentResolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
