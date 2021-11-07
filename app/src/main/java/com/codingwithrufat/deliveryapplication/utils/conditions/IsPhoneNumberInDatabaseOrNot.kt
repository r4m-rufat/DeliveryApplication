package com.codingwithrufat.deliveryapplication.utils.conditions

import com.google.firebase.firestore.FirebaseFirestore

fun isPhoneNumberInDatabaseOrNot(
    db: FirebaseFirestore,
    phoneNumber: String,
    type: String,
    isInDB: () -> Unit,
    isNotInDB: () -> Unit
) {
    db.collection(type)
        .whereEqualTo("phone_number", phoneNumber)
        .get()
        .addOnSuccessListener {
            isInDB()
        }.addOnFailureListener {
            isNotInDB()
        }
}