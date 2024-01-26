package zingg.common.py.processors;

import java.util.List;
import java.util.Map;

import javax.annotation.processing.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import java.util.Set;
// import java.util.logging.Logger;

import javax.lang.model.element.*;
import zingg.common.py.annotations.*;

@SupportedAnnotationTypes("zingg.common.py.annotations.PythonMethod")
public class PythonMethodProcessor extends AbstractProcessor {

    private Map<String, List<String>> classMethodsMap;
    // private static final Logger LOG = Logger.getLogger(PythonMethodProcessor.class.getName());
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        ProcessorContext processorContext = ProcessorContext.getInstance();
        classMethodsMap = processorContext.getClassMethodsMap();
        // LOG.info("Processing PythonMethod annotations...");

        // process Services annotation
        for (Element element : roundEnv.getElementsAnnotatedWith(PythonMethod.class)) {
            
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement methodElement = (ExecutableElement) element;
                String className = methodElement.getEnclosingElement().getSimpleName().toString();
                
                if (classMethodsMap.containsKey(className)) {
                    List<String> methodNames = classMethodsMap.get(className);

                    if (methodNames.contains(methodElement.getSimpleName().toString())) {
                        // LOG.info("Generating Python method for: " + methodElement.getSimpleName());
                        System.out.println("    def " + methodElement.getSimpleName() + "(self" +
                                generateMethodSignature(methodElement) + "):");
                        generateMethodReturn(methodElement);
                        generateFieldAssignment(methodElement);
                    }
                }
            }
            System.out.println();  
        }
        // LOG.info("Processing complete.");
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

    private void generateMethodReturn(ExecutableElement methodElement) {
        TypeMirror returnType = methodElement.getReturnType();
        if (returnType.getKind() == TypeKind.VOID) {
            return;
        } else {
            String returnTypeString = resolveType(returnType);
            String methodName = methodElement.getSimpleName().toString();
            String className = methodElement.getEnclosingElement().getSimpleName().toString();
            System.out.println("        return self." + className.toLowerCase() + "." + methodName + "()");
        }
    }

    private String resolveType(TypeMirror typeMirror) {
        return typeMirror.toString();
    }

    private void generateFieldAssignment(ExecutableElement methodElement) {
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
            System.out.println("        self." + className.toLowerCase() + "." + methodName + "(" + parameterList + ")");
        }
    }
    
}
