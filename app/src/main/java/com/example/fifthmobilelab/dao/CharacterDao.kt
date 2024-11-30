package com.example.fifthmobilelab.dao

import androidx.room.*
import com.example.fifthmobilelab.entity.CharacterEntity

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters")
    fun getAllCharactersFlow(): kotlinx.coroutines.flow.Flow<List<CharacterEntity>> // Возвращаем Flow

    @Query("SELECT * FROM characters")
    fun getAllCharacters(): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCharacters(characters: List<CharacterEntity>): List<Long>

    @Update
    fun updateCharacters(characters: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    fun clearCharacters()

}
