package zingg.common.py.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Target({ElementType.TYPE})
public @interface PythonClass {
    String module();
}