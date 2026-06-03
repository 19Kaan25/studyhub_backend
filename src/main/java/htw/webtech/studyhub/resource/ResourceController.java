package htw.webtech.studyhub.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceController {

    @Autowired
    ResourceService service;

    @PostMapping("/resources")
    public Resource createResource(@RequestBody Resource resource) {
        return service.save(resource);
    }

    public Resource getResource(@PathVariable String id) {
        Long resourceId = Long.parseLong(id);
        return service.get(resourceId);
    }
}
