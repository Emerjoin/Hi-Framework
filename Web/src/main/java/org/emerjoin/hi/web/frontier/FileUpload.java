package org.emerjoin.hi.web.frontier;

import javax.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
public class FileUpload {

    private Part part;

    public FileUpload(Part part){

        this.part = part;

    }

    public File saveToFolder(String path) throws IOException {
        String filePath = path+"/"+getUploadFileName();
        return saveAs(filePath);
    }

    public File saveAs(File file) throws IOException{

        if(file.exists())
            throw new IllegalArgumentException("File "+file.getAbsolutePath()+" already exists");

        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        InputStream partInput = getPart().getInputStream();
        byte[] buffer = new byte[2048];

        while (partInput.available() > 0) {
            if (partInput.available() < buffer.length)
                buffer = new byte[partInput.available()];
            int totalRead = partInput.read(buffer);
            fileOutputStream.write(buffer, 0, totalRead);
        }

        return file;

    }

    public File saveAs(String path) throws IOException{

        return saveAs(new File(path));

    }

    public String getUploadFileName(){

        return part.getSubmittedFileName();

    }

    public Part getPart(){

        return part;

    }

    public InputStream getInputStream() throws IOException{

        return part.getInputStream();

    }


}
