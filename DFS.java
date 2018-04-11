import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.*;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
// import a json package


/* JSON Format

{
    "metadata" :
    {
        file :
        {
            name  : "File1"
            numberOfPages : "3"
            pageSize : "1024"
            size : "2291"
            page :
            {
                number : "1"
                guid   : "22412"
                size   : "1024"
            }
            page :
            {
                number : "2"
                guid   : "46312"
                size   : "1024"
            }
            page :
            {
                number : "3"
                guid   : "93719"
                size   : "243"
            }
        }
    }
}
 
 
 */


public class DFS {
    int port;
    Chord  chord;
    
    private long md5(String objectName) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(objectName.getBytes());
            BigInteger bigInt = new BigInteger(1,m.digest());
            return Math.abs(bigInt.longValue());

        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public DFS(int port) throws Exception {
        
        this.port = port;
        long guid = md5("" + port);
        chord = new Chord(port, guid);
        Files.createDirectories(Paths.get(guid+"/repository"));
        
        File file = new File(guid+"/repository/"+md5("Metadata"));
        
        if(!file.exists()){
            PrintWriter pr = new PrintWriter(file);
            pr.print("{\"metadata\":[]}");
            pr.close();
            file.createNewFile();
        }
    }
    
    public void join(String Ip, int port) throws Exception {
        chord.joinRing(Ip, port);
        chord.Print();
    }
    
    public JsonReader readMetaData() throws Exception {
        //Gson jsonParser = null;
        long guid = md5("Metadata");
        ChordMessageInterface peer = chord.locateSuccessor(guid);
        InputStream metadataraw = peer.get(guid);
        // jsonParser = Json.createParser(metadataraw);
        JsonReader reader = new JsonReader(new InputStreamReader(metadataraw, "UTF-8"));
        return reader;
    }
    
    public void writeMetaData(InputStream stream) throws Exception {
        //JsonParser jsonParser _ null;
        long guid = md5("Metadata");
        ChordMessageInterface peer = chord.locateSuccessor(guid);
        peer.put(guid, stream);
    }
   
    public void mv(String oldName, String newName) throws Exception {
        JsonParser jp = new JsonParser();
        JsonReader jr = readMetaData();
        JsonObject metaData = (JsonObject)jp.parse(jr);
        JsonArray ja = metaData.getAsJsonArray("metadata");

        for(int i = 0; i < ja.size(); i++){
            JsonObject jo = ja.get(i).getAsJsonObject();
            String name = jo.get("name").getAsString();
            if (name.equals(oldName)) {
                jo.addProperty("name", newName);
                JsonArray pageArray = jo.get("page").getAsJsonArray();
                
                for (int j=0;j<pageArray.size();j++) {
                    JsonObject page = pageArray.get(j).getAsJsonObject();
                    long guid = md5(newName+(j+1));
                    
                    page.addProperty("guid",guid);
 
                    byte[] content = read(oldName,j+1);
                    ChordMessageInterface peer = chord.locateSuccessor(guid);
                    InputStream is = new FileStream(content);
                    peer.put(guid, is);                
                }
            }
        }
        String s = metaData.toString();
        InputStream input = new FileStream(s.getBytes());
        writeMetaData(input);
    }
    
    public String ls() throws Exception {
        System.out.println("======= list =======");
        String listOfFiles = "";
     
        JsonReader jr = readMetaData();
        
        jr.beginObject();
        jr.skipValue();
        jr.beginArray();
        while (jr.hasNext()) {
            jr.beginObject();
            while (jr.hasNext()) {
                String name = jr.nextName();
                if (name.equals("name")) {
                    listOfFiles += jr.nextString()+"\n";
                } else {
                    jr.skipValue();
                }
            }
            jr.endObject();
        }
        jr.endArray();
        jr.endObject();
        return listOfFiles;
    }

    public void touch(String fileName) throws Exception {

        Metadata meta = new Metadata(fileName, 0, new JsonArray());
        Gson gson = new GsonBuilder().create();

        String json = gson.toJson(meta);

        JsonParser jp = new JsonParser();
        JsonReader jr = readMetaData();
        JsonObject metaData = (JsonObject)jp.parse(jr);
        JsonArray ja = metaData.getAsJsonArray("metadata");

        JsonObject fileObj = new JsonParser().parse(json).getAsJsonObject();
        ja.add(fileObj);

        String s = metaData.toString();
        InputStream input = new FileStream(s.getBytes());
        writeMetaData(input);

    }

    public void delete(String fileName) throws Exception {

        //create JsonArray
        JsonParser jp = new JsonParser();
        JsonObject metaData = (JsonObject)jp.parse(readMetaData());
        JsonArray ja = metaData.getAsJsonArray("metadata");

        //index to remove
        int del = -1;

        for(int i = 0; i < ja.size(); i++){
            //get JObj at index i
            JsonObject jo = ja.get(i).getAsJsonObject();

            //get name of file - e.g. value of key "name" of this object
            String name = jo.get("name").getAsString();
            
            if(name.equals(fileName)){

                //get pages as JsonArray
                JsonArray pages = jo.get("pages").getAsJsonArray();

                //delete each page
                for(int j = 0; j < pages.size(); j++){
                    JsonObject page = pages.get(j).getAsJsonObject();
                    long guid = md5("Metadata");
                    long pGuid = page.get("guid").getAsLong();
                    
                    ChordMessageInterface peer = chord.locateSuccessor(guid);
                    peer.delete(pGuid);
                }
                del = i;
            }
        }

        if (del > -1) {
            ja.remove(del);
            String s = metaData.toString();
            InputStream input = new FileStream(s.getBytes());
            writeMetaData(input);
        }
    }
    
    public byte[] read(String fileName, int pageNumber) throws Exception {
        // TODO: read pageNumber from fileName
        
        JsonParser jp = new JsonParser();
        JsonReader jr = readMetaData();
        JsonObject metaData = (JsonObject)jp.parse(jr);
        JsonArray ja = metaData.getAsJsonArray("metadata");
        
        byte[] result = null;
        
        for(int i = 0; i < ja.size(); i++){
            JsonObject jo = ja.get(i).getAsJsonObject();
            String name = jo.get("name").getAsString();
            if (name.equals(fileName)) {
                JsonArray pageArray = jo.get("page").getAsJsonArray();
                int index = 0;
                if(pageNumber != -1)
                    index = pageNumber-1;
                else
                    index = pageArray.size()-1;
                
                JsonObject page = pageArray.get(index).getAsJsonObject();
                int size = page.get("size").getAsInt();
                long pageGuid = page.get("guid").getAsLong();

                ChordMessageInterface peer = chord.locateSuccessor(pageGuid);
                InputStream is = peer.get(pageGuid);
                result = new byte[size];

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int r = is.read(result, 0, result.length);
                buffer.write(result, 0, r);
                buffer.flush();
                is.close();
            }
        }
        return result;
    }
    
    
    public byte[] tail(String fileName) throws Exception
    {
        return read(fileName, -1);
    }
    public byte[] head(String fileName) throws Exception
    {
        return read(fileName, 1);
    }
    public void append(String filename, byte[] data) throws Exception
    {
        // TODO: append data to fileName. If it is needed, add a new page.
        // Let guid be the last page in Metadata.filename
        //ChordMessageInterface peer = chord.locateSuccessor(guid);
        //peer.put(guid, data);
        // Write Metadata
        JsonParser jp = new JsonParser();
        JsonReader jr = readMetaData();
        JsonObject metaData = (JsonObject)jp.parse(jr);
        JsonArray ja = metaData.getAsJsonArray("metadata");
        
        for(int i = 0; i < ja.size(); i++){
            JsonObject jo = ja.get(i).getAsJsonObject();
            String name = jo.get("name").getAsString();
            int numberOfPages = jo.get("numberOfPages").getAsInt();
            
            if (name.equals(filename)) {
                int pageSize = jo.get("pageSize").getAsInt();
                JsonArray pageArray = jo.get("page").getAsJsonArray();
                int offset = 0;
                int pgNum = 1;
                
                if(numberOfPages > 0){
                    int lastPageIndex = pageArray.size()-1;
                    JsonObject pagejo = pageArray.get(lastPageIndex).getAsJsonObject();
                    int size = pagejo.get("size").getAsInt();
                    long guid = pagejo.get("guid").getAsLong();
                    numberOfPages = pageArray.size();
                    
                    if(size < pageSize) {
                        if(pageSize-size < data.length) {
                            offset = pageSize-size;
                        } 
                        else{
                            offset = data.length;
                        }
                        byte[] subdata = Arrays.copyOfRange(data,0,offset);
                        // read what's already in there
                        byte[] existingData = read(filename, lastPageIndex+1);
                        // combine arrays
                        byte[] combinedArray = new byte[subdata.length+existingData.length];
                        for(int j = 0; j < existingData.length; j++){
                            combinedArray[j] = existingData[j];
                        }
                        for(int j = 0; j<subdata.length; j++){
                            combinedArray[j+existingData.length] = subdata[j];
                        }

                        ChordMessageInterface peer = chord.locateSuccessor(guid);
                        InputStream is = new FileStream(combinedArray);
                        peer.put(guid, is);

                        pagejo.addProperty("size",combinedArray.length);
                        numberOfPages += 1;
                    }
                }
                byte[] restOfData = Arrays.copyOfRange(data, offset, data.length);
                for(int j = offset; j < restOfData.length; j++){
                    if (j % pageSize == 0){
                        JsonObject page = new JsonObject();
                        long newGuid = md5(name + numberOfPages);
                        page.addProperty("number", numberOfPages);
                        page.addProperty("guid", newGuid);
                        
                        int beginIndex = j;
                        int endIndex = j+pageSize;
                        
                        if(endIndex > restOfData.length){
                            endIndex = restOfData.length;
                        }
                        byte[] subdata = Arrays.copyOfRange(restOfData, beginIndex, endIndex);
                        page.addProperty("size",subdata.length);

                        ChordMessageInterface peer = chord.locateSuccessor(newGuid);
                        
                        InputStream is = new FileStream(subdata);
                        peer.put(newGuid, is);
                        pageArray.add(page);
                        numberOfPages += 1;
                    }
                }
                jo.addProperty("numberOfPages",numberOfPages-1);
            }
        }
    }
}
