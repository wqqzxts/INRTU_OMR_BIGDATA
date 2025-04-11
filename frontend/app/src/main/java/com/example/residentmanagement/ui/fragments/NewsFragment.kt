package com.example.residentmanagement.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper

import com.example.residentmanagement.R
import com.example.residentmanagement.ui.adapters.AdapterPublications
import com.example.residentmanagement.data.model.Publication
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.ui.util.OnFragmentChangedListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var publicationsAdapter: AdapterPublications
    private lateinit var publicationsList: MutableList<Publication>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.news_recycler_view)
        publicationsList = mutableListOf()

        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        publicationsAdapter = AdapterPublications(publicationsList)
        recyclerView.adapter = publicationsAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val publication = publicationsList[position]

                when (direction) {
                    ItemTouchHelper.LEFT -> deletePublication(publication.id)
                    ItemTouchHelper.RIGHT -> editPublication(publication.id)
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)

        loadPublications()
        // mockPublications()
    }

    override fun onResume() {
        super.onResume()
        (activity as? OnFragmentChangedListener)?.onFragmentChanged(this)
    }

    private fun loadPublications() {
        val apiService = RetrofitClient.getApiService()

        apiService.getPublications().enqueue(object : Callback<List<Publication>> {
            override fun onResponse(call: Call<List<Publication>>, response: Response<List<Publication>>) {
                if (response.code() == 200) {
                    response.body()?.let { publications ->
                        publicationsList.clear()
                        publicationsList.addAll(publications)
                        publicationsAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<List<Publication>>, t: Throwable) {
                Log.e("Загрузка публикаций провалилась", "Ошибка сети: ${t.message}")
            }
        })
    }

    private fun deletePublication(publicationId: Int) {
        RetrofitClient.getApiService().deleteSpecificPublication(publicationId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 204) {
                    val index = publicationsList.indexOfFirst { it.id == publicationId }
                    publicationsList.removeAt(index)
                    publicationsAdapter.notifyItemRemoved(index)
                }
                if (response.code() == 403) {
                    Toast.makeText(requireContext(), "У вас нет прав", Toast.LENGTH_SHORT).show()
                    loadPublications()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DELETE publication news", "Error: ${t.message}")
            }

        })
    }

    private fun editPublication(publicationId: Int) {
        val editFragment = NewsPublicationEditFragment().apply {
            arguments = Bundle().apply {
                putInt("PUBLICATION_ID", publicationId)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.home_container, editFragment)
            .addToBackStack("edit_publications")
            .commit()
    }

//    private fun mockPublications() {
//        publicationsList.clear()
//
//        val mockData = listOf(
//            Publication(
//                title = "First Publication",
//                content = "This is the content of the first publication.",
//                date_published = "2025-04-08",
//                user = User("John", "Doe", "john@example.com")
//            ),
//            Publication(
//                title = "Second Publication",
//                content = "This is the content of the second publication.",
//                date_published = "2025-04-07",
//                user = User("Jane", "Smith", "jane@example.com")
//            ),
//            Publication(
//                title = "Third Publication",
//                content = "This is the content of the third publication.",
//                date_published = "2025-04-06",
//                user = User("Alice", "Johnson", "alice@example.com")
//            )
//        )
//
//        publicationsList.addAll(mockData)
//
//        publicationsAdapter.notifyDataSetChanged()
//    }
}