package AEV3;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.internal.Base64;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;


public class utiles {
	public static void main(String[] args) throws Exception  {
		MongoCollection<Document> coleccion=null;
	
		String ip="haitian:haitian@3.226.136.98";
		int port=27017;
		MongoClientURI uri= new MongoClientURI("mongodb://"+ip+":"+port);
		MongoClient mongoClient =new MongoClient(uri);
		MongoDatabase database = mongoClient.getDatabase("CNI");
		coleccion = database.getCollection("Criminales");
		if(coleccion!=null) {
			System.out.println("Conectado con exito");
		}else {
			System.out.println("Error al conectar");
		}
		String nombre="BBB";
		String alia="BB";
		String nacional="Cuba";
		int any=1000;
		
		String url=buscarImagen2();
		String ima=codificar(url);
		Document doc = new Document();
		
		doc.append("alias", alia);
		doc.append("fechaNacimiento", any);
		doc.append("nombreCompleto", nombre);
		doc.append("nacionalidad", nacional);
		doc.append("imagen", ima);
		coleccion.insertOne(doc);
	}
	
	public static String buscarImagen2() throws Exception {
		 JFileChooser selector=new JFileChooser();
		    FileNameExtensionFilter filtroImagen=new FileNameExtensionFilter("JPG, PNG & GIF","jpg","png","gif");
		    selector.setFileFilter(filtroImagen);
		    int r=selector.showOpenDialog(null);
		    if(r==JFileChooser.APPROVE_OPTION){
		     File f=selector.getSelectedFile();
		      System.out.println(f.getPath());
		   }
		    codificar(selector.getSelectedFile().getPath());
		    return selector.getSelectedFile().getPath();
	}

	/**
	 * Codifica una Image a un array de bytes per a despres codificarla a Base64
	 * @param url Direcció de la image
	 * @throws Exception
	 */
	public static String codificar(String url) throws Exception {
		byte[] buffer = loadImage64(url);
		String encodedString = Base64.encode(buffer);
		return encodedString;
	}
	
	
	/**
	 * Carrega una image i retorna el seu valor en un array de bytes
	 * @param url Direcció de la image
	 * @return Image guardada en un array de bytes
	 * @throws Exception
	 */
	public static byte[] loadImage64(String url)throws Exception{
	    File file= new File(url.toString());
	    if(file.exists()){
	        int lenght = (int)file.length();
	        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
	        byte[] bytes = new byte[lenght];
	        reader.read(bytes, 0, lenght);
	        reader.close();
	        return bytes;
	    }else{
	    	System.out.println("Recurso no encontrado");
	        return null;
	    }
	}
}
