package zingg.common.py.processors;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.annotation.processing.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import java.util.Set;

import javax.lang.model.element.*;

@SupportedAnnotationTypes("zingg.common.py.annotations.PythonMethod")
public class PythonMethodProcessor extends AbstractProcessor {
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    public static String generateMethodSignature(ExecutableElement methodElement) {
        StringBuilder signature = new StringBuilder();
        signature.append(generateMethodParameters(methodElement));
        return signature.toString();
    }

    public static String generateMethodParameters(ExecutableElement methodElement) {
        StringBuilder parameters = new StringBuilder();
        for (VariableElement parameter : methodElement.getParameters()) {
            parameters.append(", ");
            parameters.append(parameter.getSimpleName());
        }
        return parameters.toString();
    }

    public static void generateMethodReturn(ExecutableElement methodElement, FileWriter fileWriter) throws IOException {
        TypeMirror returnType = methodElement.getReturnType();
        if (returnType.getKind() == TypeKind.VOID) {
            return;
        } else {
            String methodName = methodElement.getSimpleName().toString();
            String className = methodElement.getEnclosingElement().getSimpleName().toString();
            fileWriter.write("        return self." + className.toLowerCase() + "." + methodName + "()\n");
        }
    }

    public static void generateFieldAssignment(ExecutableElement methodElement, FileWriter fileWriter) throws IOException {
        List<? extends VariableElement> parameters = methodElement.getParameters();
        
        if (!parameters.isEmpty()) {
            String methodName = methodElement.getSimpleName().toString();
            String className = methodElement.getEnclosingElement().getSimpleName().toString();

            StringBuilder parameterList = new StringBuilder();
            for (VariableElement parameter : parameters) {
                if (parameterList.length() > 0) {
                    parameterList.append(", ");
                }
                parameterList.append(parameter.getSimpleName());
            }
            fileWriter.write("        self." + className.toLowerCase() + "." + methodName + "(" + parameterList + ")\n");
        }
    }
    
}
