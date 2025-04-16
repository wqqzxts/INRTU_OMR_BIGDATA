package com.example.residentmanagement.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.example.residentmanagement.R
import com.example.residentmanagement.data.network.RetrofitClient
import com.example.residentmanagement.data.util.AuthManager
import com.example.residentmanagement.ui.activities.MainActivity


class ProfileFragment : Fragment() {
    private lateinit var menuButton: ImageButton
    private lateinit var logoutButton: Button
    private lateinit var firstName: TextView
    private lateinit var lastName: TextView
    private lateinit var gender: TextView
    private lateinit var apartments: TextView
    private lateinit var apartmentsTitle: TextView
    private lateinit var email: TextView
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authManager = AuthManager(requireContext())

        val isStaff = authManager.isStaff

        menuButton = view.findViewById(R.id.profile_menu_button)
        logoutButton = view.findViewById(R.id.button_logout)
        firstName = view.findViewById(R.id.profile_first_name)
        lastName = view.findViewById(R.id.profile_last_name)
        gender = view.findViewById(R.id.profile_gender)
        apartments = view.findViewById(R.id.profile_apartments)
        apartmentsTitle = view.findViewById(R.id.profile_apartments_title)
        apartments.visibility = if (!isStaff) View.VISIBLE else View.GONE
        apartmentsTitle.visibility = if (!isStaff) View.VISIBLE else View.GONE
        email = view.findViewById(R.id.profile_email)

        menuButton.setOnClickListener { v ->
            showPopupMenu(v)
        }

        loadProfileInfo()

        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun showPopupMenu(v: View) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(R.menu.popup_menu_profile, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.profile_menu_edit -> {
                    val createFragment = ProfileEditFragment()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.home_container, createFragment)
                        .addToBackStack("profile_fragment")
                        .commit()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun loadProfileInfo() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().getProfileInfo()

                if (response.code() == 200) {
                    val user = response.body()
                    if (user != null) {
                        firstName.text = user.firstName
                        lastName.text = user.lastName
                        if (user.gender == "Male") {
                            gender.text = "Мужской"
                        } else {
                            gender.text = "Женский"
                        }
                        apartments.text = user.apartments.toString()
                        email.text = user.email
                    } else {
                        Log.e("ProfileFragment GET profile info", "Empty body in response")
                    }
                    if (response.code() == 401) {
                        loadProfileInfo()
                    }
                    if (response.code() == 403) {
                        Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment GET profile info", "Error: ${e.message}")
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApiService().logoutUser()

                if (response.code() == 204) {
                    authManager.clearTokens()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()

                    Toast.makeText(requireContext(), "Выход произведен успешно", Toast.LENGTH_SHORT).show()
                }
                if (response.code() == 401) {
                    logout()
                }
                if (response.code() == 403) {
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите снова", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment POST logout", "Error: ${e.message}")
            }
        }
    }
}