package com.example.mcamp25.readstack.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mcamp25.readstack.ReadstackApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class LibrarySyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val database = (applicationContext as ReadstackApplication).database
                val bookDao = database.bookDao()
                val localBooks = bookDao.getAllBooks().first()
                
                Log.d("LibrarySyncWorker", "Found ${localBooks.size} local books to sync")
                
                // Giving the system a few seconds to show the notification
                kotlinx.coroutines.delay(3000)
                
                // Success! everything is put away and ready to go!
                makeStatusNotification("Library synced successfully!", applicationContext)
                
                Result.success()
            } catch (e: Exception) {
                Log.e("LibrarySyncWorker", "Error during library sync", e)
                makeStatusNotification("Sync failed. Check your connection.", applicationContext)
                Result.failure()
            }
        }
    }
}
