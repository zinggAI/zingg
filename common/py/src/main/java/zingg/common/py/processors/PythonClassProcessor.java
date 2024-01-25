package zingg.common.py.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.*;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;

import zingg.common.py.annotations.*;

@SupportedAnnotationTypes("zingg.common.py.annotations.PythonClass")
public class PythonClassProcessor extends AbstractProcessor {

    private boolean importsAndDeclarationsGenerated = false;
    private Map<String, List<String>> classMethodsMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // Imports and global declarations
        if (!importsAndDeclarationsGenerated) {
            generateImportsAndDeclarations();
            importsAndDeclarationsGenerated = true;
        }

        
        // process Services annotation
        for (Element element : roundEnv.getElementsAnnotatedWith(PythonClass.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) element;
                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
                List<String> methodNames = new ArrayList<>();

                System.out.println("class " + element.getSimpleName() + ":");

                // __init__ method
                System.out.println("    def __init__(self" +
                        generateConstructorParameters(classElement) + "):");
                if (element.getSimpleName().contentEquals("Pipe")) {
                    generateClassInitializationCode(classElement, element);
                }
                for (VariableElement field : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
                    if (!field.getSimpleName().contentEquals("serialVersionUID")) {
                        generateFieldInitializationCode(field, element);
                    }
                }
                for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                    if (methodElement.getAnnotation(PythonMethod.class) != null) {
                        methodNames.add(methodElement.getSimpleName().toString());
                    }
                }
                classMethodsMap.put(element.getSimpleName().toString(), methodNames);
            }
            System.out.println();   
            // rest of generated class contents
        }
        ProcessorContext processorContext = ProcessorContext.getInstance();
        processorContext.getClassMethodsMap().putAll(classMethodsMap);

        return false;
    }

    Map<String, List<String>> getClassMethodsMap() {
        return classMethodsMap;
    }

    private void generateImportsAndDeclarations() {
        System.out.println("import logging");
        System.out.println("from zingg.client import *");
        System.out.println("LOG = logging.getLogger(\"zingg.pipes\")");
        System.out.println();
        System.out.println("JPipe = getJVM().zingg.spark.client.pipe.SparkPipe");
        System.out.println("FilePipe = getJVM().zingg.common.client.pipe.FilePipe");
        System.out.println("JStructType = getJVM().org.apache.spark.sql.types.StructType");
        System.out.println();
    }

    private void generateClassInitializationCode(TypeElement classElement, Element element) {
        System.out.println("        self." + element.getSimpleName().toString().toLowerCase() + " = getJVM().zingg.spark.client.pipe.SparkPipe()");
    }

    private void generateFieldInitializationCode(VariableElement field, Element element) {
        String fieldName = field.getSimpleName().toString();
        String fieldAssignment = "self." + element.getSimpleName().toString().toLowerCase() + "." + fieldName + " = " + fieldName;
    
        if (!fieldName.startsWith("FORMAT_")) {
            System.out.println("        " + fieldAssignment);
        }
    }

    private String generateConstructorParameters(TypeElement classElement) {
        StringBuilder parameters = new StringBuilder();
        List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());

        fields = fields.stream()
                .filter(field -> !field.getSimpleName().contentEquals("serialVersionUID"))
                .filter(this::isFieldForConstructor)
                .collect(Collectors.toList());

        for (VariableElement field : fields) {
            parameters.append(", ");
            parameters.append(field.getSimpleName());
        }
        return parameters.toString();
    }

    private boolean isFieldForConstructor(VariableElement field) {
        String fieldName = field.getSimpleName().toString();
    
        return !fieldName.equals(fieldName.toUpperCase())
                && !field.getModifiers().contains(Modifier.STATIC)
                && !fieldName.startsWith("FORMAT_");
    }
    
}
