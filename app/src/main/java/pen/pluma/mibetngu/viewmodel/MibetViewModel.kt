package pen.pluma.mibetngu.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MibetViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val data = MutableLiveData<List<DocumentSnapshot>>()

    fun getData(): LiveData<List<DocumentSnapshot>> {
        db.collection("05May").document("22MIBETNGU")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(ContentValues.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    data.value = listOf(snapshot)
                } else {
                    Log.d(ContentValues.TAG, "No data found")
                }
            }
        return data
    }
}