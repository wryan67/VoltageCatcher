package org.wryan67.vc.common.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public abstract class JsonObject {
    @JsonIgnore
    ObjectMapper objectMapper=new ObjectMapper();

    @JsonIgnore
    String rootName=this.getClass().getSimpleName();

    @Override
    public String toString() {
        return JsonConverter.toJson(this,rootName,objectMapper,false);
    }

    public String toJson(boolean shrink) {
        return JsonConverter.toJson(this,null,objectMapper,shrink);
    }

    public <Type> Type fromJson(String jsonString) throws IOException {
        return objectMapper.readerForUpdating(this).readValue(jsonString);
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

}
