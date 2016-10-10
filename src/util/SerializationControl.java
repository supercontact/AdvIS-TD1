package util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

// This static class provides methods to easily save/load serialized objects to/from the file system.
public class SerializationControl {

	public static Object load(File file) {
		if (!file.exists() || file.isDirectory()) return null;
		try (
				InputStream input = new FileInputStream(file);
				InputStream buffer = new BufferedInputStream(input);
				ObjectInput objInput = new ObjectInputStream (buffer);
				){
			return objInput.readObject();
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean save(Serializable obj, File file) {
		File directory = file.getParentFile();
		if (directory != null && !directory.isDirectory()) {
			if (!directory.mkdirs()) {
				System.out.println("Cannot create directory: " + directory.getPath());
				return false;
			}
		}
		
		try (
				OutputStream output = new FileOutputStream(file);
				OutputStream buffer = new BufferedOutputStream(output);
				ObjectOutput objOutput = new ObjectOutputStream(buffer);
				){
			objOutput.writeObject(obj);
			return true;
		} catch (IOException e){
			e.printStackTrace();
			return false;
		}
	}

}
