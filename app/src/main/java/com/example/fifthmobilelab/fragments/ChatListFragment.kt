package com.example.mobilelab.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobilelab.Chat
import com.example.mobilelab.ChatAdapter
import com.example.mobilelab.databinding.ChatListFragmentBinding

class ChatListFragment : Fragment() {

    private var _binding: ChatListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChatListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val login = arguments?.getString("LOGIN")
        binding.loginTextView.text = login

        // Список фиктивных данных
        val chatList = listOf(
            Chat("Андрей", "12:30 PM", "Привет, как дела?"),
            Chat("Марина", "12:32 PM", "Занята на работе."),
            Chat("Сергей", "12:45 PM", "Поговорим позже."),
            Chat("Ольга", "1:00 PM", "Привет, давно не виделись!"),
            Chat("Виктор", "1:15 PM", "Когда встречаемся?")
        )

        // Установите макет-менеджер
        binding.chatList.layoutManager = LinearLayoutManager(context)

        // Подключите адаптер
        binding.chatList.adapter = ChatAdapter(chatList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}