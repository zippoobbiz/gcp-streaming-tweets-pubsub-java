import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
	public static int port = 9000;
	public static void main(String[] args) {
//		// start http server
		
		

		if(System.getenv("CREDENTIALS") != null) {
			writeUsingFiles(System.getenv("CREDENTIALS"));
			SimpleHttpServer httpServer = new SimpleHttpServer();
			httpServer.Start(port);
		} else {
			System.out.println("credential not found.");
		}
		// start https server
		// SimpleHttpsServer httpsServer = new SimpleHttpsServer();
		// httpsServer.Start(port);
		
//		System.out.println(System.getProperty("user.dir"));
//		System.out.println(Main.class.getClassLoader().getResource("").getPath());
		
	}

	private static void writeUsingFiles(String data) {
        try {
            Files.write(Paths.get("/tmp/credentials.json"), data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
