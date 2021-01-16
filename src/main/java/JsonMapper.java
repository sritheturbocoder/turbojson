import exception.ArgumentNullException;
import exception.JsonException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class JsonMapper {

    private static int max_nesting_depth;

    private static Map<Type, BiFunction<Object, JsonWriter, Integer>> base_exporters_table;
    private static Map<Type, BiFunction<Object, JsonWriter, Integer>> custom_exporters_table;

    private static Map<Type, Map<Type, BiFunction<Object, JsonWriter, Integer>>> base_importers_table;
    private static Map<Type,
            Map<Type, BiFunction<Object, JsonWriter, Integer>>> custom_importers_table;

    private static Map<Type, ArrayMetadata> array_metadata;
    private static Object array_metadata_lock = new Object ();

    private static HashMap<Type, HashMap<Type, Method>> conv_ops;
    private static Object conv_ops_lock = new Object ();

    private static Map<Type, ObjectMetadata> object_metadata;
    private static Object object_metadata_lock = new Object ();

    private static Map<Type,
            List<PropertyMetadata>> type_properties;
    private static Object type_properties_lock = new Object ();

    private static JsonWriter      static_writer;
    private static Object static_writer_lock = new Object ();

    JsonMapper () throws ArgumentNullException {
        max_nesting_depth = 100;

        array_metadata = new HashMap<>();
        conv_ops = new HashMap<>();
        object_metadata = new HashMap<>();
        type_properties = new HashMap<>();

        static_writer = new JsonWriter ();

        base_exporters_table   = new HashMap<>();
        custom_exporters_table = new HashMap<>();

        base_importers_table = new HashMap<>();
        custom_importers_table = new HashMap<>();

        registerBaseExporters ();
        registerBaseImporters ();
    }

    private void registerBaseExporters() {
        base_exporters_table.put(byte.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((int) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(char.class, (Object obj, JsonWriter writer) -> {
            try {
                writer.Write((char) obj);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });

        base_exporters_table.put(LocalDateTime.class, (Object obj, JsonWriter writer) -> {
            try {
                LocalDateTime dateTime = (LocalDateTime) obj;
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                String dateTimeString = dateFormat.format(dateTime);
                writer.Write(dateTimeString);
                return Constants.SUCCESS;
            } catch (JsonException e) {
                e.printStackTrace();
                return Constants.FAILURE;
            }
        });



    }

    private void registerBaseImporters() {

    }
}