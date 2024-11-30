package com.example.fifthmobilelab.repository

import android.util.Log
import com.example.fifthmobilelab.dao.CharacterDao
import com.example.fifthmobilelab.entity.CharacterEntity

class CharacterRepository(private val characterDao: CharacterDao) {

    val charactersFlow: kotlinx.coroutines.flow.Flow<List<CharacterEntity>> = characterDao.getAllCharactersFlow()

    suspend fun insertCharacters(characters: List<CharacterEntity>) {
        characterDao.insertCharacters(characters)
    }

    suspend fun getAllCharacters(): List<CharacterEntity> {
        return characterDao.getAllCharacters()
    }

    suspend fun clearCharacters() {
        characterDao.clearCharacters()
    }
}
