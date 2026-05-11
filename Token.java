/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.Compilador;

/**
 *
 * @author unifdoliveira
 */

public class Token {
    String tipo;
    String lexema;

    public Token(String tipo, String lexema) {
    this.lexema = lexema;
    this.tipo = tipo;
    }

    @Override
    public String toString() {
    return "<" + tipo + ", " + lexema + ">";
    }
    
}
