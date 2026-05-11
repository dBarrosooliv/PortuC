/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.Compilador;

import com.mycompany.Compilador.Node;

/**
 *
 * @author Daniel Barroso
 */
public class Tree {
 
    Node raiz;
 
    public Tree(Node raiz) {
        this.raiz = raiz;
    }
 
    public void printTree() {
        printTree(raiz, "", true);
    }
 
    private void printTree(Node node, String prefixo, boolean ehUltimo) {
        if (node == null) return;
 
        System.out.println(prefixo + (ehUltimo ? "'-- " : "+-- ") + node.valor);
 
        String novoPrefixo = prefixo + (ehUltimo ? "    " : "|   ");
        for (int i = 0; i < node.filhos.size(); i++) {
            boolean ultimo = (i == node.filhos.size() - 1);
            printTree(node.filhos.get(i), novoPrefixo, ultimo);
        }
    }
}