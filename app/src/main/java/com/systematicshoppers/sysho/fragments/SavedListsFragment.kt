package com.systematicshoppers.sysho.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.systematicshoppers.sysho.R

import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.systematicshoppers.sysho.adapters.ListItemAdapter
import com.systematicshoppers.sysho.database.FirebaseUtils
import com.systematicshoppers.sysho.database.ListItem
import com.systematicshoppers.sysho.database.UserList

private const val TAG = "SavedListsFragment"

class SavedListsFragment : Fragment() {

    private var savedLists = mutableListOf<UserList>()
    private lateinit var recyclerView: RecyclerView
    val adapter = ListItemAdapter(savedLists) { position ->
        deleteList(position)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_lists, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<RecyclerView>(R.id.saved_lists_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadUserLists { userLists ->
            savedLists.clear()
            savedLists.addAll(userLists)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadUserLists(callback: (List<UserList>) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val database = FirebaseFirestore.getInstance()
            val userListsRef = database.collection("Users").document(userId).collection("lists")

            userListsRef.get().addOnSuccessListener { querySnapshot ->
                val userLists = querySnapshot.documents.mapNotNull { documentSnapshot ->
                    val userList = documentSnapshot.toObject(UserList::class.java)
                    userList?.id = documentSnapshot.id
                    userList
                }
                callback(userLists)
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load lists: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun deleteList(position: Int) {
        val userList = savedLists[position]
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val database = FirebaseFirestore.getInstance()
            val userListsRef = database.collection("Users").document(userId).collection("lists")

            userListsRef.document(userList.id).delete().addOnSuccessListener {
                savedLists.removeAt(position)
                recyclerView.adapter?.notifyDataSetChanged()
                Toast.makeText(requireContext(), "List deleted successfully", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete list: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
