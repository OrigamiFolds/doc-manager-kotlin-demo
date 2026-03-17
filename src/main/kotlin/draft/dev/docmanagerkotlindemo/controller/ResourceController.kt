package draft.dev.docmanagerkotlindemo.controller


import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ResourceController(

) {
    @GetMapping("/fetchDocuments")
    @PreAuthorize("hasRole('admin') and hasAuthority('documents:read:all')")
    fun fetchDocumentsEndpoint(): String {
        return "Here are your documents"
    }
}