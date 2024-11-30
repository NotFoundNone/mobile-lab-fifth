package com.example.mobilelab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilelab.databinding.ItemChatBinding

class ChatAdapter(private val chatList: List<Chat>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.binding.chatName.text = chat.name
        holder.binding.chatTime.text = chat.time
        holder.binding.chatMessage.text = chat.message
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}
