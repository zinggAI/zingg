package zingg.common.py.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private Map<String, List<String>> classMethodsMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        
        // process Services annotation
        for (Element element : roundEnv.getElementsAnnotatedWith(PythonClass.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) element;
                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
                String packageName = packageElement.getQualifiedName().toString();
                List<String> methodNames = new ArrayList<>();

                String outputDirectory = determineOutputDirectory(packageName);
                
                try (FileWriter fileWriter = new FileWriter(outputDirectory + File.separator + element.getSimpleName() + "Generated.py")) {
                    generateImportsAndDeclarations(element, fileWriter);

                    fileWriter.write("class " + element.getSimpleName() + ":\n");

                    // __init__ method
                    fileWriter.write("    def __init__(self" + generateConstructorParameters(classElement, element) + "):\n");
                    generateClassInitializationCode(classElement, element, fileWriter);

                    for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                        if (methodElement.getAnnotation(PythonMethod.class) != null) {
                            methodNames.add(methodElement.getSimpleName().toString());
                        }
                    }
                    classMethodsMap.put(element.getSimpleName().toString(), methodNames);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ProcessorContext processorContext = ProcessorContext.getInstance();
        processorContext.getClassMethodsMap().putAll(classMethodsMap);

        return false;
    }

    Map<String, List<String>> getClassMethodsMap() {
        return classMethodsMap;
    }

    private String determineOutputDirectory(String packageName) {
        if (packageName.contains("enterprise") && packageName.contains("common")) {
            return "common/python";
        } else if (packageName.contains("enterprise") && packageName.contains("snowflake")) {
            return "snowflake/python";
        } else if (packageName.contains("enterprise") && packageName.contains("spark")) {
            return "spark/python";
        } else {
            return "python/zingg";
        }
    }    

    private void generateImportsAndDeclarations(Element element, FileWriter fileWriter) throws IOException {
        fileWriter.write("from zingg.otherThanGenerated import *\n");
        if (element.getSimpleName().contentEquals("Pipe")) {
            fileWriter.write("import logging\n");
            fileWriter.write("LOG = logging.getLogger(\"zingg.pipes\")\n");
            fileWriter.write("\n");
            fileWriter.write("JPipe = getJVM().zingg.spark.client.pipe.SparkPipe\n");
            fileWriter.write("FilePipe = getJVM().zingg.common.client.pipe.FilePipe\n");
            fileWriter.write("JStructType = getJVM().org.apache.spark.sql.types.StructType\n");
            fileWriter.write("\n");
        }
    }

    private void generateClassInitializationCode(TypeElement classElement, Element element, FileWriter fileWriter) throws IOException {
        if (element.getSimpleName().contentEquals("Pipe")) {
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + " = getJVM().zingg.spark.client.pipe.SparkPipe()\n");
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + ".setName(name)\n");
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + ".setFormat(format)\n");
        }
        else if (element.getSimpleName().contentEquals("EPipe")) {
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + " = getJVM().zingg.spark.client.pipe.SparkPipe()\n");
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + ".setPassthroughExpr(passthroughExpr)\n");
        }
        else if (element.getSimpleName().contentEquals("Arguments")) {
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + " = getJVM().zingg.common.client.Arguments()\n");
        }
        else if (element.getSimpleName().contentEquals("EArguments")) {
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + " = getJVM().zingg.common.client.Arguments()\n");
        }
        else if (element.getSimpleName().contentEquals("FieldDefinition")) {
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + " = getJVM().zingg.common.client.FieldDefinition()\n");
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + ".setFieldName(name)\n");
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + ".setDataType(self.stringify(dataType))\n");
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + ".setMatchType(matchType)\n");
            fileWriter.write("        self." + element.getSimpleName().toString().toLowerCase() + ".setFields(name)\n");
            fileWriter.write("\n");
            fileWriter.write("    def getFieldDefinition(self):\n");
            fileWriter.write("        return self.fielddefinition\n");
        }
        fileWriter.write("\n");
    }

    // private void generateFieldInitializationCode(VariableElement field, Element element) {
    //     String fieldName = field.getSimpleName().toString();
    //     String fieldAssignment = "self." + element.getSimpleName().toString().toLowerCase() + "." + fieldName + " = " + fieldName;
    
    //     if (!fieldName.startsWith("FORMAT_")) {
    //         System.out.println("        " + fieldAssignment);
    //     }
    // }

    private String generateConstructorParameters(TypeElement classElement, Element element) {

        StringBuilder parameters = new StringBuilder();

        if (element.getSimpleName().contentEquals("Arguments")) {
            // For the "Arguments" class, no constructor parameters are needed
            return "";
        }
        else if (element.getSimpleName().contentEquals("Pipe")) {
            parameters.append(", name, format");
        } 
        else if (element.getSimpleName().contentEquals("FieldDefinition")) {
            parameters.append(", name, dataType, *matchType");
        } 
        else {
            List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());
    
            fields = fields.stream()
                    .filter(field -> !field.getSimpleName().contentEquals("serialVersionUID"))
                    .filter(this::isFieldForConstructor)
                    .collect(Collectors.toList());
    
            for (VariableElement field : fields) {
                parameters.append(", ");
                parameters.append(field.getSimpleName());
            }
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
