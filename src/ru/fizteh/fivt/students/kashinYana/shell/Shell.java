package ru.fizteh.fivt.students.kashinYana.shell;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;

import java.io.*;

/**
 * User: Yana Kashinskaya
 * Group: 195
 */

public class Shell {

    static File path = new File(".");

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            packageRegime(args);
        } else {
            interactiveRegime();
        }
    }

    static void packageRegime(String[] args) throws Exception {
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            input.append(args[i] + " ");
        }
        String commands = input.toString();
        String[] command = commands.split("[\\s]*[;][\\s]*");
        for (int i = 0; i < command.length; i++) {
            try {
                recognizeCommand(command[i]);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }

    static void interactiveRegime() throws Exception {
        while (true) {
            System.out.print("$ ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String command = reader.readLine();
            try {
                recognizeCommand(command);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    static void recognizeCommand(String command) throws Exception {
        String[] parseCommand = command.split("\\s+");
        String nameCommand = parseCommand[0];
        if (nameCommand.equals("exit")) {
            System.exit(0);
        }
        if (nameCommand.equals("cd")) {
            if (parseCommand.length != 2) {
                throw new Exception("strange numbers of argv");
            }
            File newPath = getFile(parseCommand[1]);
            if (newPath.isDirectory()) {
                path = newPath;
            } else {
                throw new Exception("cd: \'" + parseCommand[1] + "\': No such file or directory");
            }
        } else if (nameCommand.equals("pwd")) {
            if (parseCommand.length != 1) {
                throw new Exception("strange numbers of argv");
            }
            System.out.println(path.getCanonicalPath());
        } else if (nameCommand.equals("dir")) {
            if (parseCommand.length != 1) {
                throw new Exception("strange numbers of argv");
            }
            String[] list;
            list = path.list();
            for (int i = 0; i < list.length; i++) {
                System.out.println(list[i]);
            }
        } else if (nameCommand.equals("mkdir")) {
            if (parseCommand.length != 2) {
                throw new Exception("strange numbers of argv");
            }
            File newPath = getFile(parseCommand[1]);
            if (!newPath.mkdir()) {
                throw new Exception("mkdir: cannot create directory \'" + parseCommand[1] +
                        "\': No such file or directory");
            }
        } else if (nameCommand.equals("rm")) {
            if (parseCommand.length != 2) {
                throw new Exception("strange numbers of argv");
            }
            File newPath = getFile(parseCommand[1]);
            if (newPath.exists()) {
                try {
                    delete(newPath);
                } catch (Exception e) {
                    throw new Exception("rm:" + e.getMessage());
                }
            } else {
                throw new Exception("rm: cannot remove \'" + parseCommand[1] + "\': No such file or directory");
            }
        } else if (nameCommand.equals("mv")) {
            if (parseCommand.length != 3) {
                throw new Exception("strange numbers of argv");
            }
            File firstPath = getFile(parseCommand[1]);
            File secondPath = getFile(parseCommand[2]);
            if (!firstPath.exists()) {
                throw new Exception("mv: cannot stat \'" + parseCommand[1] + "\': No such file or directory");
            }
            try {
                if (firstPath.isFile()) {
                    fileCopy(firstPath, secondPath);
                } else {
                    dirCopy(firstPath, secondPath);
                }
                delete(firstPath);
            } catch (Exception e) {
                throw new Exception("mv: " + e.getMessage());
            }
        } else if (nameCommand.equals("cp")) {
            if (parseCommand.length != 3) {
                throw new Exception("strange numbers of argv");
            }
            File firstPath = getFile(parseCommand[1]);
            File secondPath = getFile(parseCommand[2]);
            if (!firstPath.exists()) {
                throw new Exception("cp: cannot stat \'" + parseCommand[1] + "\': No such file or directory");
            }
            try {
                if (firstPath.isFile()) {
                    fileCopy(firstPath, secondPath);
                } else {
                    dirCopy(firstPath, secondPath);
                }
            } catch (Exception e) {
                throw new Exception("cp:" + e.getMessage());
            }
        } else {
            throw new Exception("command-not-found");
        }
    }

    static File getFile(String file) {
        String newNamePath;
        if (file.charAt(0) == '/') {
            newNamePath = file;
        } else {
            newNamePath = path.getAbsolutePath() + "/" + file;
        }
        File newPath = new File(newNamePath);
        return newPath;
    }

    static void fileCopy(File firstPath, File secondPath) throws Exception {
        FileInputStream in =
                new FileInputStream(firstPath);
        FileOutputStream out =
                new FileOutputStream(secondPath);
        int nLength;
        byte[] buf = new byte[8000];
        while (true) {
            nLength = in.read(buf);
            if (nLength < 0)
                break;
            out.write(buf, 0, nLength);
        }
        in.close();
        out.close();
    }

    static void dirCopy(File firstPath, File secondPath) throws Exception {
        if (!secondPath.mkdir()) {
            throw new Exception("cannot create directory \'" + secondPath +
                    "\': No such file or directory");
        }
        String[] children = firstPath.list();
        for (String s : children) {
            if (getFile(firstPath.getAbsolutePath() + "/" + s).isFile()) {
                fileCopy(getFile(firstPath.getAbsoluteFile() + "/" + s),
                        getFile(secondPath.getAbsoluteFile() + "/" + s));
            } else {
                dirCopy(getFile(firstPath.getAbsoluteFile() + "/" + s),
                        getFile(secondPath.getAbsoluteFile() + "/" + s));
            }
        }
    }

    static void delete(File newFile) throws Exception {
        if (newFile.isFile()) {
            if (!newFile.delete()) {
                throw new Exception("cannot remove file or directory");
            }
        } else {
            String[] children = newFile.list();
            for (String s : children) {
                delete(getFile(newFile.getAbsolutePath() + "/" + s));
            }
            if (!newFile.delete()) {
                throw new Exception("cannot remove file or directory");
            }
        }
    }

}

