package com.example.dicerollerproject.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dicerollerproject.R
import com.example.dicerollerproject.data.LocalStore
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.dataconnect.generated.DicerollerConnector
import com.google.firebase.dataconnect.generated.execute
import com.google.firebase.dataconnect.generated.instance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.HashMap

class LoginFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null
    private var emailField: EditText? = null
    private var passwordField: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()

        // Setup AppCheck
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        // Initialize UI
        emailField = view.findViewById(R.id.emailField)
        passwordField = view.findViewById(R.id.passwordField)

        view.findViewById<Button>(R.id.loginButton).setOnClickListener { loginUser() }
        view.findViewById<Button>(R.id.signupButton).setOnClickListener { registerUser() }

        // Help Overlay
        val helpOverlay = view.findViewById<View>(R.id.helpOverlay)
        view.findViewById<View>(R.id.btnHelp).setOnClickListener { helpOverlay.visibility = View.VISIBLE }
        view.findViewById<View>(R.id.btnCloseHelp).setOnClickListener { helpOverlay.visibility = View.GONE }

        // Check if already logged in
        if (mAuth?.currentUser != null) {
            proceedToApp()
        }
    }

    /**
     * Firebase Authentication Integration - Registration
     */
    private fun loginUser() {
        val email = emailField?.text.toString().trim()
        val password = passwordField?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) return

        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                syncUserProfile(mAuth?.currentUser)
                Log.d("LoginFragment", "Login Successful")
                proceedToApp()
            } else {
                Log.d("LoginFragment", "Login Failed: ${task.exception?.message}")
                Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    /**
     * Firebase Authentication Integration - Registration
     */
    private fun registerUser() {
        Toast.makeText(context, "Button pressed", Toast.LENGTH_SHORT).show()
        val email = emailField?.text.toString().trim()
        val password = passwordField?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) return

        mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                syncUserProfile(mAuth?.currentUser)
                proceedToApp()
            } else {
                Log.d("LoginFragment", "Registration Failed: ${task.exception?.message}")
            }
        }
    }

    /**
     * Firebase Data Connect Integration
     * Upserts the user into the PostgreSQL 'User' table
     */
    private fun syncUserProfile(user: FirebaseUser?) {
//        user?.let { firebaseUser ->
//            // Sync to Data Connect (Postgres)
//            val connector = DicerollerConnector.instance
//
//            viewLifecycleOwner.lifecycleScope.launch {
//                try {
//                    connector.upsertUser.execute(
//                        displayName = firebaseUser.displayName ?: "New User"
//                    ) {
//                        this.email = firebaseUser.email
//                        this.photoUrl = firebaseUser.photoUrl?.toString()
//                    }
//                    Log.d("LoginFragment", "Data Connect User Synced")
//                } catch (e: Exception) {
//                    Log.e("LoginFragment", "Data Connect Sync Failed: ${e.message}")
//                }
//            }
//        }
    }

     fun proceedToApp() {
         if (!findNavController().popBackStack()) {
             findNavController().navigate(R.id.rollFragment)
         }
    }
}