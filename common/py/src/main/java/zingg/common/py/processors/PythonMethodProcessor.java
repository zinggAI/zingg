package zingg.common.py.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import java.util.Set;

import javax.lang.model.element.*;
import zingg.common.py.annotations.*;

@SupportedAnnotationTypes("zingg.common.py.annotations.PythonMethod")
public class PythonMethodProcessor extends AbstractProcessor {

    private Map<String, List<String>> classMethodsMap;
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        ProcessorContext processorContext = ProcessorContext.getInstance();
        classMethodsMap = processorContext.getClassMethodsMap();

        for (Element element : roundEnv.getElementsAnnotatedWith(PythonMethod.class)) {
            
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement methodElement = (ExecutableElement) element;
                String className = methodElement.getEnclosingElement().getSimpleName().toString();
                
                if (classMethodsMap.containsKey(className)) {
                    List<String> methodNames = classMethodsMap.get(className);

                    if (methodNames.contains(methodElement.getSimpleName().toString())) {
                        try (FileWriter fileWriter = new FileWriter("python/zingg" + File.separator + className + "Generated.py", true)) {

                            String javadoc = processingEnv.getElementUtils().getDocComment(methodElement);
                            if (javadoc != null) {
                                fileWriter.write("    '''\n");
                                fileWriter.write(javadoc.trim());
                                fileWriter.write("\n    '''\n");
                            }

                            fileWriter.write("    def " + methodElement.getSimpleName() + "(self" + generateMethodSignature(methodElement) + "):\n");
                            generateMethodReturn(methodElement, fileWriter);
                            generateFieldAssignment(methodElement, fileWriter);
                            fileWriter.write("\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
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

    private void generateMethodReturn(ExecutableElement methodElement, FileWriter fileWriter) throws IOException {
        TypeMirror returnType = methodElement.getReturnType();
        if (returnType.getKind() == TypeKind.VOID) {
            return;
        } else {
            String returnTypeString = resolveType(returnType);
            String methodName = methodElement.getSimpleName().toString();
            String className = methodElement.getEnclosingElement().getSimpleName().toString();
            fileWriter.write("        return self." + className.toLowerCase() + "." + methodName + "()\n");
        }
    }

    private String resolveType(TypeMirror typeMirror) {
        return typeMirror.toString();
    }

    private void generateFieldAssignment(ExecutableElement methodElement, FileWriter fileWriter) throws IOException {
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
