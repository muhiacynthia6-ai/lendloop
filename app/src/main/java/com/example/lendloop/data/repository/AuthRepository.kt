package com.example.lendloop.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class AuthUser(
    val id: String,
    val name: String,
    val email: String
)

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun register(name: String, email: String, password: String): Result<AuthUser> {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            result.user?.updateProfile(profileUpdate)?.await()

            Result.success(
                AuthUser(
                    id    = result.user?.uid         ?: "",
                    name  = result.user?.displayName ?: name,
                    email = result.user?.email       ?: email
                )
            )
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password is too weak"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("An account with this email already exists"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Invalid email address"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registration failed"))
        }
    }

    suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            Result.success(
                AuthUser(
                    id    = result.user?.uid         ?: "",
                    name  = result.user?.displayName ?: "",
                    email = result.user?.email       ?: email
                )
            )
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Incorrect email or password"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Login failed"))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("No account found with this email"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to send reset email"))
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): AuthUser? {
        val user = firebaseAuth.currentUser ?: return null
        return AuthUser(
            id    = user.uid,
            name  = user.displayName ?: "",
            email = user.email       ?: ""
        )
    }
}