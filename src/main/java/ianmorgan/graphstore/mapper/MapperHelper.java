package ianmorgan.graphstore.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Makes it easier to copy data between an input map and an output map,
 * with common conversion prebuilt.
 */
public class MapperHelper {

    private Map<String,Object> input;
    private Map<String,Object> output;

    /**
     * Build a helper, giving both the input and output maps
     * @param input
     * @param output
     */
    public MapperHelper(Map<String,Object> input, Map<String,Object> output){
        this.input = input;
        this.output = output;
    }

    /**
     * Build a helper for a input map. An empty output map is created automatically
     *
     * @param input
     */
    public MapperHelper(Map<String,Object> input){
        this(input,new HashMap<>());
    }

    public void copyIfExists(String key){
        copyIfExists(key,key);
    }

    public void copyIfExists(String inputKey, String outputKey){
        Object in = input.get(inputKey);
        if (in != null) {
            output.put(outputKey,in);
        }
    }

    /**
     * Get the final output map
     * @return
     */
    public Map<String, Object> output() {
        return output;
    }
}
