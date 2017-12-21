package unidue.ub.fileuploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    @Value("${ub.statistics.data.dir}")
    private String datadir;

    private Path rootLocation;

    private String module = "";

    public void setModule(String module) {
        this.module = module;
    }
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void store(MultipartFile file) {
        rootLocation = Paths.get(datadir + "/" + module);
        if (!rootLocation.toFile().exists())
            rootLocation.toFile().mkdirs();
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        log.info("storing " + filename + " to " + rootLocation.toString());
        try {
            if (file.isEmpty()) {
                log.info("empty file");
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                log.info("cannot Store file at given directory");
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            log.info("saved file");
        } catch (IOException e) {
            log.info("failed to store file");
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        rootLocation = Paths.get(datadir + "/" + module);
        if (!rootLocation.toFile().exists())
            rootLocation.toFile().mkdirs();
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        rootLocation = Paths.get(datadir + "/" + module);
        if (!rootLocation.toFile().exists())
            rootLocation.toFile().mkdirs();
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        rootLocation = Paths.get(datadir + "/" + module);
        if (!rootLocation.toFile().exists())
            rootLocation.toFile().mkdirs();
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            rootLocation = Paths.get(datadir + "/" + module);
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
