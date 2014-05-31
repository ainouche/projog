package org.projog.build;

import static org.projog.build.BuildUtilsConstants.SCRIPTS_OUTPUT_DIR;
import static org.projog.build.BuildUtilsConstants.TEXT_FILE_EXTENSION;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

import org.projog.core.Calculatable;
import org.projog.core.PredicateFactory;

/**
 * Produces {@code .txt} and {@code .pl} files for every subclass of {@code PredicateFactory}.
 * <p>
 * The contents of the files are extracted from the comments in the {@code .java} file of the {@code PredicateFactory}.
 * The {@code .txt} file contains the contents of the javadoc comment of the class. The {@code .pl} file contains the
 * prolog syntax contained in the "{@code SYSTEM TEST}" comment at the top of the class.
 */
public class SysTestGenerator {
   private static final String SOURCE_INPUT_DIR = "src/core/";
   private static final File COMMANDS_OUTPUT_DIR = new File(SCRIPTS_OUTPUT_DIR, "commands");

   private static void findAllPredicates(File dir) {
      File[] directoryContents = dir.listFiles();
      for (File file : directoryContents) {
         if (file.isDirectory()) {
            findAllPredicates(file);
         } else if (isJavaSourceFileOfDocumentedClass(file)) {
            // produce the script file
            produceScriptFileFromJavaFile(file);
         }
      }
   }

   private static boolean isJavaSourceFileOfDocumentedClass(File file) {
      if (!isJavaSource(file)) {
         return false;
      }
      String className = getClassName(file);
      try {
         Class<?> c = Class.forName(className);
         if (isDocumentable(c)) {
            c.newInstance();
            System.out.println("SysTest: Created: " + className);
            return true;
         }
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
         System.out.println("Exception checking: " + className + " " + e);
      }
      return false;
   }

   private static boolean isDocumentable(Class<?> c) {
      return isConcrete(c) && isPublic(c) && (isPredicateFactory(c) || isCalculatable(c));
   }

   private static boolean isConcrete(Class<?> c) {
      return !Modifier.isAbstract(c.getModifiers());
   }

   private static boolean isPublic(Class<?> c) {
      return Modifier.isPublic(c.getModifiers());
   }

   private static boolean isPredicateFactory(Class<?> c) {
      return PredicateFactory.class.isAssignableFrom(c);
   }

   private static boolean isCalculatable(Class<?> c) {
      return Calculatable.class.isAssignableFrom(c);
   }

   private static boolean isJavaSource(File f) {
      return f.getName().endsWith(".java");
   }

   private static String getClassName(File javaFile) {
      String filePath = javaFile.getPath();
      String filePathMinusExtension = removeFileExtension(filePath);
      String filePathMinusSourceDirectoryAndFileExtension = filePath.substring(SOURCE_INPUT_DIR.length(), filePathMinusExtension.length());
      return filePathMinusSourceDirectoryAndFileExtension.replace(File.separatorChar, '.');
   }

   private static void produceScriptFileFromJavaFile(File javaFile) {
      COMMANDS_OUTPUT_DIR.mkdirs();
      try (FileReader fr = new FileReader(javaFile); BufferedReader br = new BufferedReader(fr)) {
         boolean sysTestRead = false;
         boolean javadocRead = false;
         String line;
         while ((!sysTestRead || !javadocRead) && (line = br.readLine()) != null) {
            line = line.trim();
            if ("/* TEST".equals(line)) {
               sysTestRead = true;
               writeScriptFile(javaFile, br);
            } else if (sysTestRead && !javadocRead && "/**".equals(line)) {
               javadocRead = true;
               writeTextFile(javaFile, br);
            }
         }
         if (!sysTestRead) {
            throw new Exception("No system tests read for: " + javaFile);
         }
         if (!javadocRead) {
            throw new Exception("No javadoc read for: " + javaFile);
         }
      } catch (Exception e) {
         throw new RuntimeException("cannot generate script from " + javaFile + " due to " + e, e);
      }
   }

   private static void writeScriptFile(File javaFile, BufferedReader br) {
      String scriptName = replaceFileExtension(javaFile.getName(), ".pl");
      File scriptFile = new File(COMMANDS_OUTPUT_DIR, scriptName);

      try (FileWriter fw = new FileWriter(scriptFile); BufferedWriter bw = new BufferedWriter(fw)) {
         String line;
         while (!"*/".equals(line = br.readLine().trim())) {
            bw.write(line);
            bw.newLine();
         }
      } catch (IOException e) {
         throw new RuntimeException("Could not produce: " + scriptFile + " due to: " + e, e);
      }
   }

   /**
    * Writes comments contained in Javadoc for class to a text file.
    * <p>
    * Comments can then be reused to construct user manual documentation.
    */
   private static void writeTextFile(File javaFile, BufferedReader br) {
      String textFileName = replaceFileExtension(javaFile.getName(), TEXT_FILE_EXTENSION);
      File textFile = new File(COMMANDS_OUTPUT_DIR, textFileName);

      try (FileWriter fw = new FileWriter(textFile); BufferedWriter bw = new BufferedWriter(fw)) {
         String line;
         while (!"*/".equals(line = br.readLine().trim())) {
            line = line.trim();
            if (line.startsWith("*")) {
               line = line.substring(1).trim();
            }
            // ignore ant @see annotations present in input Javadoc 
            if (!line.startsWith("@see")) {
               bw.write(line);
               bw.newLine();
            }
         }
      } catch (IOException e) {
         throw new RuntimeException("Could not produce: " + textFile + " due to: " + e, e);
      }
   }

   private static String replaceFileExtension(String fileName, String newExtension) {
      return removeFileExtension(fileName) + newExtension;
   }

   private static String removeFileExtension(String fileName) {
      int extensionPos = fileName.lastIndexOf('.');
      if (extensionPos == -1) {
         return fileName;
      } else {
         return fileName.substring(0, extensionPos);
      }
   }

   public static final void main(String[] args) {
      findAllPredicates(new File(SOURCE_INPUT_DIR));
   }
}