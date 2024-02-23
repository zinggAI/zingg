package zingg.common.py.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.annotation.processing.*;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;

import zingg.common.py.annotations.*;

@SupportedAnnotationTypes("zingg.common.py.annotations.PythonClass")
public class PythonClassProcessor extends AbstractProcessor {

    private Set<TypeElement> processedElements = new HashSet<>();
    private Set<String> folders = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        System.out.println("ProcessingEnv " + processingEnv);
        super.init(processingEnv);

        // Clear the output directory on initialization
        folders.add("python/zinggGenerated");
        folders.add("common/python");
        folders.add("snowflake/python");
        folders.add("spark/python");

        for (String folder : folders) {
            File directory = new File(folder);
            if (directory.exists()) {
                for (File file : directory.listFiles()) {
                    file.delete();
                    System.out.println(file + "deeellleeeeteeed");
                    System.out.println(file + "geeneerateedddd");
                }
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // Process each PythonClass annotated element
        for (Element element : roundEnv.getElementsAnnotatedWith(PythonClass.class)) {
            if (element.getKind() == ElementKind.CLASS && !processedElements.contains(element)) {
                processClass((TypeElement) element, roundEnv);
            }
        }
        return false;
    }


    private void processClass(TypeElement classElement, RoundEnvironment roundEnv) {

        // Mark the class as processed
        processedElements.add(classElement);

        PythonClass pythonClassAnnotation = classElement.getAnnotation(PythonClass.class);

        String outputDirectory = pythonClassAnnotation.outputDirectory();
        String moduleName = pythonClassAnnotation.module();
        String outputFile = outputDirectory + File.separator + moduleName + ".py";
        String parentClassName = pythonClassAnnotation.parent();
      
        try (FileWriter fileWriter = new FileWriter(outputFile, true)) {
            generateImportsAndDeclarations(classElement, fileWriter);
            
            if (!parentClassName.isEmpty()) {
                fileWriter.write("class " + classElement.getSimpleName() + "(" + parentClassName + "):\n");
            } else {
                fileWriter.write("class " + classElement.getSimpleName() + ":\n");
            }
            // System.out.println(classElement.getSimpleName() + "ccccccccccccccccccccccccc");

            // __init__ method
            fileWriter.write("    def __init__(self" + generateConstructorParameters(classElement, classElement) + "):\n");
            generateClassInitializationCode(classElement, classElement, fileWriter);

            for (ExecutableElement methodElement : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                if (methodElement.getAnnotation(PythonMethod.class) != null) {
                    String javadoc = processingEnv.getElementUtils().getDocComment(methodElement);
                    if (javadoc != null) {
                        fileWriter.write("    '''\n");
                        fileWriter.write(javadoc.trim());
                        fileWriter.write("\n    '''\n");
                    }

                    fileWriter.write("    def " + methodElement.getSimpleName() + "(self" + PythonMethodProcessor.generateMethodSignature(methodElement) + "):\n");
                    PythonMethodProcessor.generateMethodReturn(methodElement, fileWriter);
                    PythonMethodProcessor.generateFieldAssignment(methodElement, fileWriter);
                    fileWriter.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

        String javadoc = processingEnv.getElementUtils().getDocComment(element);
        if (javadoc != null) {
            fileWriter.write("'''\n");
            fileWriter.write(javadoc.trim());
            fileWriter.write("\n'''\n");
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
