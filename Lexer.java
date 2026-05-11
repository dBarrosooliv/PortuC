/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.Compilador;

import com.mycompany.lex.Autômatos.AFDtexto;
import com.mycompany.lex.Autômatos.AFD;
import com.mycompany.lex.Autômatos.AFDid;
import com.mycompany.lex.Autômatos.AFDnumero;
import com.mycompany.lex.Autômatos.AFDoperadores;
import com.mycompany.lex.Autômatos.AFDsinais;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author unifdoliveira
 */
public class Lexer {
    
    private List<Token> tokens;
    private List<AFD> automatos;
    private CharacterIterator code;

    public Lexer(String code) {
        tokens = new ArrayList<>();
        automatos = new ArrayList<>();
        this.code = new StringCharacterIterator(code);
        automatos.add(new AFDoperadores());
        automatos.add(new AFDnumero());
        automatos.add(new AFDtexto());
        automatos.add(new AFDsinais());
        automatos.add(new AFDid());
        
    }

    /**Avança enquanto houver espaço em branco ou quebra de linha. */
    public void skipWhiteSpace() {
        char c = code.current();
        while (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
            c = code.next();
        }
    }
 
    /** Retorna a lista completa de tokens até EOF. */
    public List<Token> getTokens() {
        Token t;
        do {
            skipWhiteSpace();
            t = searchNextToken();
            if (t == null) {
                error();
                break;
            }
            tokens.add(t);
        } while (!t.tipo.equals("EOF"));
        return tokens;
    }
 
    // Tenta cada autômato em ordem; retorna o primeiro token reconhecido
    private Token searchNextToken() {
        if (code.current() == CharacterIterator.DONE) {
            return new Token("EOF", "$");
        }
 
        int pos = code.getIndex();
        for (AFD afd : automatos) {
            Token t = afd.evaluate(code);
            if (t != null) return t;
            code.setIndex(pos);
        }
 
        error();
        return null; 
    }
 
    private void error() {
        int pos  = code.getIndex();
        char bad = code.current();
        throw new RuntimeException(
            "Erro léxico na posição " + pos + ": caractere inesperado '" + bad + "'");
    }
}
