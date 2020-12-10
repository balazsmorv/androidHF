package hu.bme.aut.android.hf.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MovieData::class], version = 1)
@TypeConverters(RatingList::class)
abstract class MovieDetailsDatabase: RoomDatabase() {
    abstract fun movieDetailsDao() : MovieDetailsDao
}