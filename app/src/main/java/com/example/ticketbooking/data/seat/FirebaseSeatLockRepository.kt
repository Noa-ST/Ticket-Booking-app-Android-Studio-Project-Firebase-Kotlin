package com.example.ticketbooking.data.seat

import com.example.ticketbooking.model.SeatLock
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.MutableData

class FirebaseSeatLockRepository(private val database: FirebaseDatabase) : SeatLockRepository {
    override fun lockSeats(showtimeId: String, seatIndices: List<Int>, holdMinutes: Int): SeatLock {
        val token = java.util.UUID.randomUUID().toString()
        val expiresAt = System.currentTimeMillis() + holdMinutes * 60_000L
        val lock = SeatLock(token, showtimeId, seatIndices, expiresAt)
        // Ghi lock theo showtime và token
        database.getReference("SeatLocks").child(showtimeId).child(token).setValue(lock)
        // Mapping token -> showtimeId để việc unlock dễ dàng
        database.getReference("SeatLockTokens").child(token).setValue(showtimeId)
        return lock
    }

    override fun unlock(token: String) {
        val tokensRef = database.getReference("SeatLockTokens").child(token)
        tokensRef.get().addOnSuccessListener { snapshot ->
            val showtimeId = snapshot.getValue(String::class.java)
            if (showtimeId != null) {
                // Lấy danh sách ghế từ lock
                database.getReference("SeatLocks").child(showtimeId).child(token).get()
                    .addOnSuccessListener { lockSnap ->
                        val lock = lockSnap.getValue(SeatLock::class.java)
                        val seatsRef = database.getReference("ShowtimeSeats").child(showtimeId)
                        seatsRef.runTransaction(object : Transaction.Handler {
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                val map = (currentData.value as? Map<String, Any?>)?.toMutableMap() ?: mutableMapOf()
                                lock?.seatIndices?.forEach { index ->
                                    val key = index.toString()
                                    val value = map[key]
                                    if (value == token) {
                                        map.remove(key)
                                    }
                                }
                                currentData.value = map
                                return Transaction.success(currentData)
                            }

                            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                                // Xoá lock và token mapping sau khi giải phóng
                                database.getReference("SeatLocks").child(showtimeId).child(token).removeValue()
                                tokensRef.removeValue()
                            }
                        })
                    }
            } else {
                tokensRef.removeValue()
            }
        }
    }

    override fun lockSeatsTransactional(
        showtimeId: String,
        seatIndices: List<Int>,
        holdMinutes: Int,
        onResult: (Boolean, SeatLock?) -> Unit
    ) {
        val token = java.util.UUID.randomUUID().toString()
        val expiresAt = System.currentTimeMillis() + holdMinutes * 60_000L
        val lock = SeatLock(token, showtimeId, seatIndices, expiresAt)
    
        // Dọn các lock đã hết hạn trước khi giữ ghế để tránh conflict giả
        cleanupExpiredLocks(showtimeId) {
            val seatsRef = database.getReference("ShowtimeSeats").child(showtimeId)
            seatsRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val map = (currentData.value as? Map<String, Any?>)?.toMutableMap() ?: mutableMapOf()
                    // Conflict nếu seat đã có giá trị (token khác hoặc SOLD)
                    for (index in seatIndices) {
                        val key = index.toString()
                        val value = map[key]
                        if (value != null) {
                            return Transaction.abort()
                        }
                    }
                    // Đặt token cho các ghế
                    for (index in seatIndices) {
                        map[index.toString()] = token
                    }
                    currentData.value = map
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (committed) {
                        // Ghi lock và token mapping
                        database.getReference("SeatLocks").child(showtimeId).child(token).setValue(lock)
                        database.getReference("SeatLockTokens").child(token).setValue(showtimeId)
                        onResult(true, lock)
                    } else {
                        // Fallback: nếu dữ liệu hiện tại đã có đúng token của mình (hiếm), coi như thành công
                        val map = currentData?.value as? Map<*, *>
                        val ours = map != null && seatIndices.all { idx ->
                            (map[idx.toString()] as? String) == token
                        }
                        if (ours) {
                            database.getReference("SeatLocks").child(showtimeId).child(token).setValue(lock)
                            database.getReference("SeatLockTokens").child(token).setValue(showtimeId)
                            onResult(true, lock)
                        } else {
                            onResult(false, null)
                        }
                    }
                }
            })
        }
    }

    override fun confirmPurchase(token: String, onResult: (Boolean) -> Unit) {
        val tokensRef = database.getReference("SeatLockTokens").child(token)
        tokensRef.get().addOnSuccessListener { snapshot ->
            val showtimeId = snapshot.getValue(String::class.java) ?: return@addOnSuccessListener onResult(false)
            database.getReference("SeatLocks").child(showtimeId).child(token).get()
                .addOnSuccessListener { lockSnap ->
                    val lock = lockSnap.getValue(SeatLock::class.java)
                    val seatsRef = database.getReference("ShowtimeSeats").child(showtimeId)
                    seatsRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val map = (currentData.value as? Map<String, Any?>)?.toMutableMap() ?: mutableMapOf()
                            lock?.seatIndices?.forEach { index ->
                                val key = index.toString()
                                val value = map[key]
                                // Chỉ chuyển sang SOLD nếu đúng token
                                if (value == token) {
                                    map[key] = "SOLD"
                                } else {
                                    // Conflict (ghế đã bị người khác giữ/bán)
                                    return Transaction.abort()
                                }
                            }
                            currentData.value = map
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                            if (committed) {
                                // Xoá lock và token mapping
                                database.getReference("SeatLocks").child(showtimeId).child(token).removeValue()
                                tokensRef.removeValue()
                                onResult(true)
                            } else {
                                onResult(false)
                            }
                        }
                    })
                }
        }
    }

    // Dọn các SeatLock đã hết hạn và tháo token khỏi ShowtimeSeats.
    // Đồng thời loại bỏ các token mồ côi (không có bản ghi lock hoặc token mapping) để tránh conflict giả.
    private fun cleanupExpiredLocks(showtimeId: String, onComplete: () -> Unit) {
        val now = System.currentTimeMillis()
        val locksRef = database.getReference("SeatLocks").child(showtimeId)
        val seatsRef = database.getReference("ShowtimeSeats").child(showtimeId)

        // Đọc tất cả lock hiện hữu
        locksRef.get().addOnSuccessListener { snapshot ->
            val expired = mutableListOf<SeatLock>()
            snapshot.children.forEach { child ->
                val lock = child.getValue(SeatLock::class.java)
                if (lock != null && lock.expiresAt < now) {
                    expired.add(lock)
                }
            }
            // Đọc trạng thái ghế để tìm token mồ côi
            seatsRef.get().addOnSuccessListener { seatSnap ->
                val seatMap = seatSnap.value as? Map<String, Any?> ?: emptyMap()
                val orphanTokens = mutableSetOf<String>()
                seatMap.forEach { (_, v) ->
                    val token = v as? String
                    if (token != null && token != "SOLD") {
                        // Nếu không có lock tương ứng hoặc lock đã hết hạn, coi là mồ côi
                        val lockNode = snapshot.child(token)
                        val locked = lockNode.getValue(SeatLock::class.java)
                        if (locked == null || locked.expiresAt < now) {
                            orphanTokens.add(token)
                        }
                    }
                }

                if (expired.isEmpty() && orphanTokens.isEmpty()) {
                    onComplete()
                    return@addOnSuccessListener
                }

                seatsRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val map = (currentData.value as? Map<String, Any?>)?.toMutableMap() ?: mutableMapOf()
                        expired.forEach { lock ->
                            lock.seatIndices.forEach { idx ->
                                val key = idx.toString()
                                val value = map[key]
                                if (value == lock.token) {
                                    map.remove(key)
                                }
                            }
                        }
                        // Loại bỏ tất cả token mồ côi
                        if (orphanTokens.isNotEmpty()) {
                            val keysToRemove = mutableListOf<String>()
                            map.forEach { (k, v) ->
                                val token = v as? String
                                if (token != null && orphanTokens.contains(token)) {
                                    keysToRemove.add(k)
                                }
                            }
                            keysToRemove.forEach { k -> map.remove(k) }
                        }
                        currentData.value = map
                        return Transaction.success(currentData)
                    }
                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                        // Xóa lock và token mapping đã hết hạn
                        expired.forEach { lock ->
                            locksRef.child(lock.token).removeValue()
                            database.getReference("SeatLockTokens").child(lock.token).removeValue()
                        }
                        // Xóa token mapping mồ côi
                        orphanTokens.forEach { token ->
                            database.getReference("SeatLockTokens").child(token).removeValue()
                            locksRef.child(token).removeValue()
                        }
                        onComplete()
                    }
                })
            }.addOnFailureListener {
                // Nếu không đọc được ghế, vẫn tiếp tục dựa trên expired
                if (expired.isEmpty()) {
                    onComplete()
                } else {
                    seatsRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val map = (currentData.value as? Map<String, Any?>)?.toMutableMap() ?: mutableMapOf()
                            expired.forEach { lock ->
                                lock.seatIndices.forEach { idx ->
                                    val key = idx.toString()
                                    val value = map[key]
                                    if (value == lock.token) {
                                        map.remove(key)
                                    }
                                }
                            }
                            currentData.value = map
                            return Transaction.success(currentData)
                        }
                        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                            expired.forEach { lock ->
                                locksRef.child(lock.token).removeValue()
                                database.getReference("SeatLockTokens").child(lock.token).removeValue()
                            }
                            onComplete()
                        }
                    })
                }
            }
        }.addOnFailureListener {
            // Nếu không đọc được thì vẫn tiếp tục để tránh chặn luồng người dùng
            onComplete()
        }
    }
}