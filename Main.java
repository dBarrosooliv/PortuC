/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.Compilador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author unifdoliveira
 */
public class Main {
 
    public static void main(String[] args) {
        
        String caminho = "src/main/java/com/mycompany/Compilador/Arquivos/programa.txt";
        String codigo;
        
        try {
            codigo = new String(Files.readAllBytes(Paths.get(caminho)));
        } catch (IOException e) {
            System.err.println("Verificar arquivo: " + caminho + ": " + e.getMessage());
            System.exit(1);
            return;
        }
 
        System.out.println("\n::::: Analise Lexica :::::\n");
        
        List<Token> tokens = null;
        
        // ::: Léxico
        try {
            Lexer lexer = new Lexer(codigo);
            tokens = lexer.getTokens();
 
            for (Token t : tokens) {
                System.out.println(t);
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        // ::: Sintático
        System.out.println("\n:: Analise Sintatica :::::>\n");
        try {
            Parser parser = new Parser(new java.util.ArrayList<>(tokens));
            boolean resultado = parser.programa();
 
            if (resultado) {
 
                System.out.println("\n::::: Arvore Sintatica Abstrata (AST) :::::\n");
                parser.getAST().printTree();
 
                // ::: Tradução
                String saida = parser.getSaida();
                String arquivoSaida = caminho.replaceAll("\\.txt$", "") + "_traduzido.cpp";
                Files.write(Paths.get(arquivoSaida), saida.getBytes());
                System.out.println("Traducao salva em: " + arquivoSaida);
 
                System.out.println("\n::::: Traducao C++ :::::\n");
                System.out.println(saida);
            } else {
                System.out.println("Programa contém erros sintáticos. Tradução não gerada.");
            }
        } catch (RuntimeException e) {
            System.err.println("!!Erro: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("!Erro ao salvar arquivo: " + e.getMessage());
            System.exit(1);
        }
        
    }
}
