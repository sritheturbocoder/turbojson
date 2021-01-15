import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

class PropertyMetadata
{
    public Field[] info;
    public Method[] methods;
    public boolean       isField;
    public java.lang.reflect.Type type;
}

class ArrayMetadata
{
    private Type element_type;
    private boolean is_array;
    private boolean is_list;


    public Type getElement_type() {
        return element_type;
    }

    public void setElement_type(Type element_type) {
        this.element_type = element_type;
    }

    public boolean isIs_array() {
        return is_array;
    }

    public void setIs_array(boolean is_array) {
        this.is_array = is_array;
    }

    public boolean isIs_list() {
        return is_list;
    }

    public void setIs_list(boolean is_list) {
        this.is_list = is_list;
    }
}

class ObjectMetadata
{
    private Type element_type;
    private boolean is_dictionary;
    private Map<String, PropertyMetadata> properties;


    public Type getElement_type() {
        return element_type;
    }

    public void setElement_type(Type element_type) {
        this.element_type = element_type;
    }

    public boolean isIs_dictionary() {
        return is_dictionary;
    }

    public void setIs_dictionary(boolean is_dictionary) {
        this.is_dictionary = is_dictionary;
    }

    public Map<String, PropertyMetadata> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, PropertyMetadata> properties) {
        this.properties = properties;
    }
}


