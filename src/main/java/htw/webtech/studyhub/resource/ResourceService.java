package htw.webtech.studyhub.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    @Autowired
    ResourceRepository repo;

    public Resource save(Resource resource) {
        return repo.save(resource);
    }

    public Resource get(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException());
    }
}
