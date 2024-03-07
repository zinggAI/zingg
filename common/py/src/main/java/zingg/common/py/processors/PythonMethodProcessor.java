package zingg.common.py.processors;

import java.util.List;
import javax.annotation.processing.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;

import zingg.common.py.annotations.*;

@SupportedAnnotationTypes("zingg.common.py.annotations.PythonMethod")
public class PythonMethodProcessor extends AbstractProcessor {

    private boolean importsAndDeclarationsGenerated = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        // process Services annotation
        for (Element element : roundEnv.getElementsAnnotatedWith(PythonMethod.class)) {
            
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement methodElement = (ExecutableElement) element;
                System.out.println("    def " + methodElement.getSimpleName() + "(self" +
                        generateMethodSignature(methodElement) + "):\n        " + generateMethodReturn(methodElement));
                generateFieldAssignment(methodElement);
            }
            System.out.println();
                
                // rest of generated class contents
        }
        return false;
    }

    private String generateMethodSignature(ExecutableElement methodElement) {
        StringBuilder signature = new StringBuilder();
        signature.append(generateMethodParameters(methodElement));
        return signature.toString();
    }

    private String generateMethodParameters(ExecutableElement methodElement) {
        StringBuilder parameters = new StringBuilder();
        for (VariableElement parameter : methodElement.getParameters()) {
            parameters.append(", ");
            parameters.append(parameter.getSimpleName());
        }
        return parameters.toString();
    }

    private String generateMethodReturn(ExecutableElement methodElement) {
        TypeMirror returnType = methodElement.getReturnType();
        if (returnType.getKind() == TypeKind.VOID) {
            return "";
        } else {
            String returnTypeString = resolveType(returnType);
            String variableName = methodElement.getSimpleName().toString();
            return "return " + variableName;
        }
    }

    private String resolveType(TypeMirror typeMirror) {
        return typeMirror.toString();
    }

    private void generateFieldAssignment(ExecutableElement methodElement) {
    List<? extends VariableElement> parameters = methodElement.getParameters();
    if (!parameters.isEmpty()) {
        VariableElement parameter = parameters.get(0);
        String variableName = parameter.getSimpleName().toString();
        System.out.println("        self." + variableName + " = " + variableName);
    }
}
    
}
