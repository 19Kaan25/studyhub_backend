package htw.webtech.studyhub.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class ResourceController {

    @Autowired
    ResourceService service;

    @PostMapping("/resources")
    public Resource createResource(@RequestBody Resource resource) {
        return service.save(resource);
    }

    @GetMapping("/resources/{id}")
    public Resource getResource(@PathVariable String id) {
        Long resourceId = Long.parseLong(id);
        return service.get(resourceId);
    }
}
