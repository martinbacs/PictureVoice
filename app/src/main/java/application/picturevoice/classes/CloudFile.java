package application.picturevoice.classes;

public class CloudFile {

    private String fileName;
    private String fileSize;
    private String fileId;

    public CloudFile() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public CloudFile(String fileName, String fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
