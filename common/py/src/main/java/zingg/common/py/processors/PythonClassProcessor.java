package zingg.common.py.processors;

import java.util.List;
import javax.annotation.processing.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;

import zingg.common.py.annotations.*;

@SupportedAnnotationTypes("zingg.common.py.annotations.PythonClass")
public class PythonClassProcessor extends AbstractProcessor {

    private boolean importsAndDeclarationsGenerated = false;

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
                PackageElement packageElement =
                    (PackageElement) classElement.getEnclosingElement();
                System.out.println("class " + element.getSimpleName() + ":");

                // __init__ method
                System.out.println("    def __init__(self" +
                        generateConstructorParameters(classElement) + "):");
                if (element.getSimpleName().contentEquals("pipe")) {
                    generateClassInitializationCode(classElement);
                }
                for (VariableElement field : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
                    if (!field.getSimpleName().contentEquals("serialVersionUID")) {
                        generateFieldInitializationCode(field);
                    }
                }
            }
            System.out.println();   
                // rest of generated class contents
        }

        return false;

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

    private void generateClassInitializationCode(TypeElement classElement) {
        System.out.println("        self.pipe = getJVM().zingg.spark.client.pipe.SparkPipe()");
    }

    // private void generateFieldInitializationCode(VariableElement field, ExecutableElement methodElement, TypeElement classElement) {
    private void generateFieldInitializationCode(VariableElement field) {
        System.out.println("        self.pipe." + field.getSimpleName() + " = " + field.getSimpleName());
        // String fieldName = field.getSimpleName().toString();
        // String methodName = methodElement.getSimpleName().toString();
        // System.out.println("        self." + fieldName + " = " + "getJVM()." +
        //         classElement.getQualifiedName().toString() + "." + methodName + "(" + fieldName + ")");
    }

    private String generateConstructorParameters(TypeElement classElement) {
        StringBuilder parameters = new StringBuilder();
        for (VariableElement field : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            if (!field.getSimpleName().contentEquals("serialVersionUID")) {
                parameters.append(", ");
                parameters.append(field.getSimpleName());
            }
        }
        return parameters.toString();
    }
    
}
