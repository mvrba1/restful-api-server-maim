package utb.fai.RESTAPIServer;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/users")
    public ResponseEntity<List<MyUser>> fetchAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/getUser")
    public ResponseEntity<MyUser> fetchById(@RequestParam("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/createUser")
    public ResponseEntity<MyUser> create(@RequestBody MyUser user) {

        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        user.setId(null); // always new
        if (!user.isUserDataValid()) {
            return ResponseEntity.badRequest().build();
        }

        MyUser saved = repo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/editUser")
    public ResponseEntity<MyUser> update(
            @RequestParam(value = "id", required = false) Long id,
            @RequestBody(required = false) MyUser changes) {

        if (changes == null) {
            return ResponseEntity.badRequest().build();
        }

        Long targetId = (id != null) ? id : changes.getId();
        if (targetId == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<MyUser> existingOpt = repo.findById(targetId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        MyUser existing = existingOpt.get();
        applyChanges(existing, changes);

        if (!existing.isUserDataValid()) {
            return ResponseEntity.badRequest().build();
        }

        MyUser saved = repo.save(existing);
        return ResponseEntity.ok(saved);
    }

    private void applyChanges(MyUser original, MyUser modified) {
        original.setName(modified.getName());
        original.setEmail(modified.getEmail());
        original.setPhoneNumber(modified.getPhoneNumber());
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<String> delete(@RequestParam("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().body("Invalid id");
        }

        if (!repo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }

        try {
            repo.deleteById(id);
            return ResponseEntity.ok("Deleted id=" + id);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Delete failed");
        }
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<String> deleteAll() {
        try {
            repo.deleteAll();
            return ResponseEntity.ok("Deleted all");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Delete all failed");
        }
    }
}
