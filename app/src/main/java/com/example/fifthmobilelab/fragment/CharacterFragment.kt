import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fifthmobilelab.databinding.FragmentCharacterListBinding
import io.ktor.client.HttpClient
import kotlinx.coroutines.*

class CharacterFragment : Fragment() {

    private var _binding: FragmentCharacterListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CharacterAdapter
    private val repository = CharacterRepository(HttpClient())

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

        // Затем начинаем загрузку данных
        fetchCharacters()
    }

    private fun fetchCharacters() {
        CoroutineScope(Dispatchers.IO).launch {
            val characters = repository.getCharacters(page = 5)
            withContext(Dispatchers.Main) {
                adapter = CharacterAdapter(characters) // Обновляем адаптер новыми данными
                binding.recyclerView.adapter = adapter // Устанавливаем адаптер с данными
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
