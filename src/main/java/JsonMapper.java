import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class JsonMapper {

    private static int max_nesting_depth;

    private static Map<Type, Callable<?>> base_exporters_table;
    private static Map<Type, Callable<?>> custom_exporters_table;

    private static Map<Type, Map<Type, Callable<?>>> base_importers_table;
    private static Map<Type,
            Map<Type, Callable<?>>> custom_importers_table;

    private static Map<Type, ArrayMetadata> array_metadata;
    private static Object array_metadata_lock = new Object ();

    private static Map<Type,
            Map<Type, Method>> conv_ops;
    private static Object conv_ops_lock = new Object ();

    private static Map<Type, ObjectMetadata> object_metadata;
    private static Object object_metadata_lock = new Object ();

    private static Map<Type,
            List<PropertyMetadata>> type_properties;
    private static Object type_properties_lock = new Object ();

    private static JsonWriter      static_writer;
    private static Object static_writer_lock = new Object ();
}
