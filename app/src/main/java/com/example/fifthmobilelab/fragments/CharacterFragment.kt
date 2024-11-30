package com.example.fifthmobilelab.fragments

import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fifthmobilelab.AppDatabase
import com.example.fifthmobilelab.entity.CharacterEntity
import com.example.fifthmobilelab.repository.CharacterRepository
import com.example.mobilelab.CharacterAdapter
import com.example.mobilelab.R
import com.example.mobilelab.databinding.FragmentCharacterListBinding
import com.example.mobilelab.model.Character
import com.example.mobilelab.repository.CharacterApi
import io.ktor.client.HttpClient
import kotlinx.coroutines.*

class CharacterFragment : Fragment() {

    private var _binding: FragmentCharacterListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CharacterAdapter
    private val api = CharacterApi(HttpClient())
    private lateinit var repository: CharacterRepository
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация базы данных и репозитория
        val database = AppDatabase.getInstance(requireContext()) // Получаем экземпляр базы данных
        val characterDao = database.characterDao() // Получаем DAO
        repository = CharacterRepository(characterDao) // Создаём репозиторий
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCharacterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация layoutManager и пустого адаптера для RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CharacterAdapter(emptyList()) // Пустой список для инициализации
        binding.recyclerView.adapter = adapter


        viewModelScope.launch {
            repository.charactersFlow.collect { characters ->
                val mappedCharacters = characters.map {
                    com.example.mobilelab.model.Character(
                        name = it.name,
                        culture = it.culture,
                        born = it.born,
                        titles = it.titles,
                        aliases = it.aliases,
                        playedBy = it.playedBy
                    )
                }
                adapter.updateData(mappedCharacters)
            }
        }

        // Переход в настройки при нажатии на кнопку
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_charactersFragment_to_settingsFragment)
        }

        binding.btnRefresh.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                repository.clearCharacters() // Очистить базу
                val page = binding.etPageNumber.text.toString().toIntOrNull()
                if (page != null && page > 0)
                {
                    fetchCharactersFromApi(page)
                }
                else {
                    fetchCharactersFromApi(page = 10)
                }
            }
        }

        binding.btnLoadPage.setOnClickListener {
            val page = binding.etPageNumber.text.toString().toIntOrNull()
            if (page != null && page > 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    repository.clearCharacters() // Очистить базу
                    fetchCharactersFromApi(page) // Загружаем новую страницу
                }
            } else {
                Toast.makeText(requireContext(), "Введите корректный номер страницы", Toast.LENGTH_SHORT).show()
            }
        }

        // Затем начинаем загрузку данных
//        fetchCharacters(10)
    }


//    private fun fetchCharacters() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val characters = api.getCharacters(page = 5)
//
//                try {
//                    val characterEntities = characters.map { character ->
//                        CharacterEntity(
//                            name = character.name,
//                            culture = character.culture,
//                            born = character.born,
//                            titles = character.titles,
//                            aliases = character.aliases,
//                            playedBy = character.playedBy
//                        )
//                    }
//                    repository.insertCharacters(characterEntities) // Сохраняем в локальную базу
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//                withContext(Dispatchers.Main) {
//                    adapter = CharacterAdapter(characters) // Обновляем адаптер новыми данными
//                    binding.recyclerView.adapter = adapter // Устанавливаем адаптер с данными
//
//                    saveCharactersToFile(characters, "5.txt")
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                withContext(Dispatchers.Main) {
//                    if (isAdded) { // Проверяем, что фрагмент всё ещё активен
//                        Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }

    private fun fetchCharacters(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val charactersFromDb = repository.getAllCharacters()
            if (charactersFromDb.isNotEmpty()) {
                // Преобразуем CharacterEntity в Character
                val characters = charactersFromDb.map { entity ->
                    com.example.mobilelab.model.Character(
                        name = entity.name,
                        culture = entity.culture,
                        born = entity.born,
                        titles = entity.titles,
                        aliases = entity.aliases,
                        playedBy = entity.playedBy
                    )
                }
                withContext(Dispatchers.Main) {
                    adapter.updateData(characters) // Обновляем адаптер преобразованными данными
                }
            } else {
                fetchCharactersFromApi(page) // Запрос к API, если база пуста
            }
        }
    }

    private fun fetchCharactersFromApi(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val characters = api.getCharacters(page)
                val characterEntities = characters.map { character ->
                    CharacterEntity(
                        name = character.name,
                        culture = character.culture,
                        born = character.born,
                        titles = character.titles,
                        aliases = character.aliases,
                        playedBy = character.playedBy
                    )
                }
                repository.insertCharacters(characterEntities) // Сохраняем данные в базу

                withContext(Dispatchers.Main) {
                    adapter.updateData(characters) // Обновляем адаптер после API-запроса
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Ошибка API: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveCharactersToFile(characters: List<Character>, fileName: String) {
        try {
            val contentResolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // Сохраняем в /Downloads
            }

            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(characters.joinToString("\n") { it.toString() }.toByteArray())
                    Toast.makeText(context, "Файл сохранён в /Downloads", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(context, "Ошибка открытия потока для записи", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(context, "Ошибка сохранения файла", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
