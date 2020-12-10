package hu.bme.aut.android.hf.data

import android.graphics.Movie
import androidx.room.*

@Dao
interface MovieDetailsDao {

    @Query("SELECT * FROM movieData")
    fun getAll(): List<MovieData>

    @Insert
    fun insert(movieData: MovieData): Long

    @Update
    fun update(movieData: MovieData)

    @Delete
    fun deleteItem(movieData: MovieData)

}