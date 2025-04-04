package xyz.coderes.note.controller

import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.*
import xyz.coderes.note.database.model.Note
import xyz.coderes.note.database.repository.NoteRepository
import java.time.Instant


@RestController
@RequestMapping("/notes")
class NoteController(
    private val noteRepository: NoteRepository
) {
    data class NoteRequest(
        val id: String?,
        val title: String,
        val content: String,
        val color: Long,
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant,
        val ownerId: String
    )

    @PostMapping
    fun save(
        @RequestBody body: NoteRequest
    ): NoteResponse {
        val note = noteRepository.save(
            Note(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                content = body.content,
                color = body.color,
                createdAt = Instant.now(),
                ownerId = ObjectId()
            )
        )

        return note.toResponse()
    }

    @GetMapping
    fun getAll(): List<NoteResponse> {
        return noteRepository.findAll().map {
            it.toResponse()
        }
    }

    @GetMapping("/owner/{ownerId}")
    fun getByOwnerId(
        @RequestParam(required = true) ownerId: String
    ): List<NoteResponse> {
        return noteRepository.findAllByOwnerId(ObjectId(ownerId)).map {
            it.toResponse()
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun delete(@PathVariable id: String) {
        noteRepository.deleteById(ObjectId(id))
    }

    private fun Note.toResponse() = NoteResponse(
        id = id.toHexString(),
        title = title,
        content = content,
        color = color,
        createdAt = createdAt,
        ownerId = ownerId.toHexString()
    )
}

