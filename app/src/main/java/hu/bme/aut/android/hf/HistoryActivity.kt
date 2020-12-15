package hu.bme.aut.android.hf

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import hu.bme.aut.android.hf.data.MovieData
import hu.bme.aut.android.hf.data.MovieDetailsDatabase
import hu.bme.aut.android.hf.recyclerViewThings.HistoryResultAdapter
import kotlinx.android.synthetic.main.activity_history.*
import kotlin.concurrent.thread

class HistoryActivity : AppCompatActivity(), HistoryResultAdapter.HistoryResultClickListener {

    private lateinit var database: MovieDetailsDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        database = Room.databaseBuilder(applicationContext, MovieDetailsDatabase::class.java, "movies").build()
        initRecyclerView()

        backToMainButton.setOnClickListener {
            finish()
        }
        thread {
            loadAllData()
        }

    }

    private fun loadAllData() {
        val allSearches = database.movieDetailsDao().getAll()
        this.runOnUiThread {
            for (element in allSearches) {
                this.adapter.addItem(element)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        database.close()
    }

    override fun onItemChanged(item: MovieData) {
    }

    override fun onTap(item: MovieData) {
            Log.d("Movie", "new data: $item")
            val intent = Intent(this, DetailsView::class.java)
            val bundle = Bundle()
            bundle.putSerializable("details", item)
            intent.putExtras(bundle)
            startActivity(intent)
    }

    private fun initRecyclerView() {
        recyclerView = historyTableView
        adapter = HistoryResultAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
