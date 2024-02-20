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

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        System.out.println("ProcessingEnv " + processingEnv);
        super.init(processingEnv);

        // Clear the output directory on initialization
        String outputDirectory = "python/zinggGenerated";
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            for (File file : dir.listFiles()) {
                file.delete();
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

        // System.out.println("Called for " + classElement);
        PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
        String packageName = packageElement.getQualifiedName().toString();
        PythonClass pythonClassAnnotation = classElement.getAnnotation(PythonClass.class);

        String outputDirectory = determineOutputDirectory(packageName);
        String moduleName = pythonClassAnnotation.module();
        String outputFile = outputDirectory + File.separator + moduleName + ".py";
      
        try (FileWriter fileWriter = new FileWriter(outputFile, true)) {
            generateImportsAndDeclarations(classElement, fileWriter);

            fileWriter.write("class " + classElement.getSimpleName() + ":\n");

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

    private String determineOutputDirectory(String packageName) {
        if (packageName.contains("enterprise") && packageName.contains("common")) {
            return "common/python";
        } else if (packageName.contains("enterprise") && packageName.contains("snowflake")) {
            return "snowflake/python";
        } else if (packageName.contains("enterprise") && packageName.contains("spark")) {
            return "spark/python";
        } else {
            return "python/zinggGenerated";
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
