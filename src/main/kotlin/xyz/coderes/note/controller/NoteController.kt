package xyz.coderes.note.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
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
        @field:NotBlank(message = "Title can't be empty")
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
        @Valid @RequestBody body: NoteRequest
    ): NoteResponse {
        val ownerId = getRegisteredOwnerId()
        val note = noteRepository.save(
            Note(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                content = body.content,
                color = body.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)
            )
        )

        return note.toResponse()
    }


    @GetMapping
    fun getByOwnerId(
    ): List<NoteResponse> {
        val ownerId = getRegisteredOwnerId()
        return noteRepository.findAllByOwnerId(ObjectId(ownerId)).map {
            it.toResponse()
        }
    }

    private fun getRegisteredOwnerId(): String = SecurityContextHolder.getContext().authentication.principal as String

    @DeleteMapping(path = ["/{id}"])
    fun delete(@PathVariable id: String) {
        val item =
            noteRepository.findById(ObjectId(id))
                .orElseThrow { throw IllegalArgumentException("Invalid Note Id:$id") }

        if (item.ownerId.toHexString() != getRegisteredOwnerId()) {
            noteRepository.deleteById(ObjectId(id))
        }
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

