package xyz.coderes.note.database.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import xyz.coderes.note.database.model.Note

interface NoteRepository: MongoRepository<Note,ObjectId > {
    fun findAllByOwnerId(ownerId: ObjectId): List<Note>

}