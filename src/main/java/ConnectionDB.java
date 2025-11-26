import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {

    //Detalles para la conexión a la base de datos.
    private static final String URL = "jdbc:postgresql://localhost:5432/hr_database";
    private static final String User = "admin";
    private static final String Password = "admin123";


    private static Connection database;

    public ConnectionDB() throws  SQLException{
        database = DriverManager.getConnection(URL, User, Password);
    }
    //TODO Insert, Delete, Update, Select


    public static void main(String[] args) throws SQLException {
        ConnectionDB conexion = new ConnectionDB();

    }
}




