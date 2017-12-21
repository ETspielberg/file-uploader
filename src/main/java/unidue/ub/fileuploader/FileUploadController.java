package unidue.ub.fileuploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    @CrossOrigin(origins = { "http://localhost:4200"})
    public ResponseEntity<?> listUploadedFiles(Model model, @RequestParam("module") String module) throws IOException {
        storageService.setModule(module);
        return ResponseEntity.ok(storageService.loadAll().map(
                path -> new FileWithLink(path.getFileName().toString(),MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString(),module).build().toString() ))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    @CrossOrigin(origins = { "http://localhost:4200"})
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, @RequestParam("module") String module) {
        storageService.setModule(module);
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    @CrossOrigin(origins = { "http://localhost:4200"})
    public String handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("module") String module,
                                   RedirectAttributes redirectAttributes) {
        storageService.setModule(module);
        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
