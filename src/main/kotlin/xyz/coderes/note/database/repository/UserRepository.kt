package xyz.coderes.note.database.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import xyz.coderes.note.database.model.User

interface UserRepository: MongoRepository<User,ObjectId> {
    fun findByEmail(email: String): User?
}