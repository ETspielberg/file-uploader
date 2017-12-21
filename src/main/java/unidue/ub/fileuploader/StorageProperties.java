package unidue.ub.fileuploader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

    @Value("${ub.statistics.data.dir}")
    private String datadir;

    private String location = "";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = datadir + location;
    }
}
