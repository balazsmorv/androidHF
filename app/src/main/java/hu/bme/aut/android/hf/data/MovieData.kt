package hu.bme.aut.android.hf.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import hu.bme.aut.android.hf.dto.MovieSearchDTO

@Entity(tableName = "movieData")
data class MovieData(val title: String,
                     val year: String?,
                     val rated: String?,
                     val released: Int?,
                     val runtime: String?,
                     val genre: String?,
                     val director: String?,
                     val writer: String?,
                     val actors: String?,
                     val plot: String?,
                     val language: String?,
                     val country: String?,
                     val awards: String?,
                     val poster: String?,
                     val ratings: RatingList,
                     val metascore: String?,
                     val imdbRating: String?,
                     val imdbVotes: Int?,
                     @PrimaryKey(autoGenerate = false) val imdbID: String,
                     val type: String?) {




}

class RatingList(var ratings: List<Ratings>) {

    companion object {
        @JvmStatic
        @TypeConverter
        fun toString(ratingList: RatingList) : String {
            var str: String = ""
            for(r in ratingList.ratings) {
                str += "${r.source},${r.score}|"
            }
            return str
        }

        @JvmStatic
        @TypeConverter
        fun toRatingList(string: String) : RatingList {
            var list = mutableListOf<Ratings>()
            val ratingStrings = string.split("|").toTypedArray()
            for (rs in ratingStrings) {
                val rating = rs.split(",").toTypedArray()
                if (rating.size == 2) {
                    list.add(Ratings(rating[0], rating[1]))
                }
            }
            return RatingList(list)
        }

    }

}

class Ratings(val source: String, val score: String) {
}

class RatingsConverter {
    fun toRatingList(from: MovieSearchDTO) : RatingList {
        if (from.Ratings == null ) {
            return RatingList(emptyList())
        } else {
            return RatingList(from.Ratings!!.map {
                Ratings(it.Source, it.Value)
            })
        }
    }
}