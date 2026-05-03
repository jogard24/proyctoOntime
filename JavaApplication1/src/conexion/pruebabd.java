/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package conexion;

import conexion.conexionBD;

/**
 *
 * @author g_o17
 */
public class pruebabd {

    public static void main(String[] args) {
        conexionBD Online = new conexionBD();
        
        Online.getConnection(); //Realizamos una prueva del funcionamiendo de la conexion con la base de datos 
        
    }
    
}
