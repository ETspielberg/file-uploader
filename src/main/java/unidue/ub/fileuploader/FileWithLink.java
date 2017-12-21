package unidue.ub.fileuploader;

public class FileWithLink {

    private String filename;

    private String link;

    FileWithLink(String filename, String link) {
        this.filename = filename;
        this.link = link;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
