package br.com.srsr.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConnectionFactory {

    static public Connection getConnection() throws ClassNotFoundException {
        try {
            Class.forName("org.postgresql.Driver");// sem essa linha aqui, da erro de driver na conexão com o manegedBean
            return DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5433/srsr", "postgres", "EPS_postgres");
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionFactory.class.getName()).log(Level.SEVERE, null, ex);//faz com que o erro apareça no log
            throw new RuntimeException("erro a abrir conexão com ConnectionFactory", ex);
        }
        //a diferença entre exception e...
        //que runtime exception, não é verificada, só informa caso dê erro
    }
}

