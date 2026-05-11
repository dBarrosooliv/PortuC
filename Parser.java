/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.Compilador;

import java.util.List;

/**
 *
 * @author Daniel Barroso
 */
public class Parser {
 
    private List<Token> tokens;
    private Token token;
 
    // Tradução
    private StringBuilder saida;
    private int indentacao;
 
    // AST
    private Tree ast;
 
    public Parser(List<Token> tokens) {
        this.tokens     = tokens;
        this.saida      = new StringBuilder();
        this.indentacao = 0;
        this.token      = getNextToken();
    }

    public Token getNextToken() {
        if (tokens.size() > 0)
            return tokens.remove(0);
        return null;
    }
 
    //Consome token, retorna lexema
    private String matchT(String tipo) {
        if (token.tipo.equals(tipo)) {
            String lexema = token.lexema;
            token = getNextToken();
            return lexema;
        }
        erro("matchT esperava [" + tipo + "] mas encontrou [" + token.tipo + "]");
        return "";
    }
 
    //Consome token, adiciona filho ao nó pai e retorna lexema
    private String matchT(String tipo, Node pai) {
        String lexema = matchT(tipo);
        pai.addFilho(new Node(lexema));
        return lexema;
    }
 
    // Consome token e emite tradução
    private void matchL(String tipo, String traducao) {
        if (token.tipo.equals(tipo)) {
            token = getNextToken();
            traduz(traducao);
            return;
        }
        erro("matchL esperava [" + tipo + "] mas encontrou [" + token.tipo + "]");
    }
 
    // Consome token, emite tradução e adiciona filho ao nó pai
    private void matchL(String tipo, String traducao, Node pai) {
        if (token.tipo.equals(tipo)) {
            String lexema = token.lexema;
            token = getNextToken();
            traduz(traducao);
            pai.addFilho(new Node(lexema));
            return;
        }
        erro("matchL esperava [" + tipo + "] mas encontrou [" + token.tipo + "]");
    }
 
    private void traduz(String str) {
        saida.append(str);
    }
 
    public String getSaida() {
        return saida.toString();
    }
 
    public Tree getAST() {
        return ast;
    }
 
    private void erro(String regra) {
        System.out.println("Erro sintatico na regra: " + regra);
        System.out.println("Token inválido: <" + token.tipo + ", " + token.lexema + ">");
        System.out.println("---------------------");
    }
 
    // =========================================================================
    // Programa → INICIO bloco FIMPROG
    // =========================================================================
    public boolean programa() {
        if (token.tipo.equals("INICIO")) {
            Node nodeProg = new Node("programa");
            ast = new Tree(nodeProg);
 
            matchL("INICIO", "#include <iostream>\n#include <string>\nusing namespace std;\n\nint main() {\n", nodeProg);
            indentacao++;
            if (bloco(nodeProg)) {
                indentacao--;
                matchL("FIMPROG", "    return 0;\n}\n", nodeProg);
                return true;
            }
        }
        erro("programa");
        return false;
    }
 
    // =========================================================================
    // Bloco → instrução bloco | instrução
    // =========================================================================
    public boolean bloco(Node pai) {
        if (instrucao(pai)) {
            bloco(pai);
            return true;
        }
        return false;
    }
 
    // =========================================================================
    // Instrução → Declaração | Atribuição | Leitura | Escrita | Se | Enquanto | Para
    // =========================================================================
    public boolean instrucao(Node pai) {
        if (declaracao(pai))  return true;
        if (atribuicao(pai))  return true;
        if (leitura(pai))     return true;
        if (escrita(pai))     return true;
        if (se(pai))          return true;
        if (enquanto(pai))    return true;
        if (para(pai))        return true;
        return false;
    }
 
    // =========================================================================
    // Declaração → Tipo ID PONTO_V
    // =========================================================================
    public boolean declaracao(Node pai) {
        if (token.tipo.equals("INTEIRO") || token.tipo.equals("DECIMAL") || token.tipo.equals("TEXTO")) {
            Node nodeDecl = new Node("declaracao");
            pai.addFilho(nodeDecl);
 
            String tipoCpp = tipo(nodeDecl);
            traduz("    ".repeat(indentacao) + tipoCpp + " ");
            String nome = matchT("ID", nodeDecl);
            traduz(nome);
            matchL("PONTO_V", ";\n", nodeDecl);
            return true;
        }
        return false;
    }
 
    private String tipo(Node pai) {
        String t = token.tipo;
        String lexema = token.lexema;
        token = getNextToken();
        pai.addFilho(new Node(lexema));
        switch (t) {
            case "INTEIRO":  return "int";
            case "DECIMAL":  return "double";
            case "TEXTO":    return "string";
            default:         return t;
        }
    }
 
    // =========================================================================
    // Atribuição → ID ATRB expressão PONTO_V
    // =========================================================================
    public boolean atribuicao(Node pai) {
        if (token.tipo.equals("ID")) {
            Node nodeAtrb = new Node("atribuicao");
 
            String nome = matchT("ID");
            if (token.tipo.equals("ATRB")) {
                pai.addFilho(nodeAtrb);
                nodeAtrb.addFilho(new Node(nome));
                traduz("    ".repeat(indentacao) + nome + " ");
                matchL("ATRB", "= ", nodeAtrb);
                expressao(nodeAtrb);
                matchL("PONTO_V", ";\n", nodeAtrb);
                return true;
            }
            erro("atribuicao");
            return false;
        }
        return false;
    }
 
    // =========================================================================
    // Leitura → LEIA ABRE_PAR ID FECHA_PAR PONTO_V
    // =========================================================================
    public boolean leitura(Node pai) {
        if (token.tipo.equals("LEIA")) {
            Node nodeLeia = new Node("leitura");
            pai.addFilho(nodeLeia);
 
            matchL("LEIA", "", nodeLeia);
            matchL("ABRE_PAR", "", nodeLeia);
            traduz("    ".repeat(indentacao) + "cin >> ");
            String nome = matchT("ID", nodeLeia);
            traduz(nome);
            matchL("FECHA_PAR", "", nodeLeia);
            matchL("PONTO_V", ";\n", nodeLeia);
            return true;
        }
        return false;
    }
 
    // =========================================================================
    // Escrita → IMPRIMA ABRE_PAR conteúdo FECHA_PAR PONTO_V
    // =========================================================================
    public boolean escrita(Node pai) {
        if (token.tipo.equals("IMPRIMA")) {
            Node nodeEscrita = new Node("escrita");
            pai.addFilho(nodeEscrita);
 
            matchL("IMPRIMA", "", nodeEscrita);
            matchL("ABRE_PAR", "", nodeEscrita);
            traduz("    ".repeat(indentacao) + "cout << ");
            conteudo(nodeEscrita);
            traduz(" << endl");
            matchL("FECHA_PAR", "", nodeEscrita);
            matchL("PONTO_V", ";\n", nodeEscrita);
            return true;
        }
        return false;
    }
 
    // =========================================================================
    // Conteúdo → TEXTO_REAL | ID
    // =========================================================================
    public boolean conteudo(Node pai) {
        if (token.tipo.equals("TEXTO_REAL")) {
            traduz(matchT("TEXTO_REAL", pai));
            return true;
        }
        if (token.tipo.equals("ID")) {
            traduz(matchT("ID", pai));
            return true;
        }
        erro("conteudo");
        return false;
    }
 
    // =========================================================================
    // Se → SE ABRE_PAR exprRel FECHA_PAR ABRE_CHAVE bloco FECHA_CHAVE casoSenao
    // =========================================================================
    public boolean se(Node pai) {
        if (token.tipo.equals("SE")) {
            Node nodeSe = new Node("se");
            pai.addFilho(nodeSe);
 
            matchL("SE", "", nodeSe);
            traduz("    ".repeat(indentacao) + "if (");
            matchL("ABRE_PAR", "", nodeSe);
            exprRel(nodeSe);
            matchL("FECHA_PAR", "", nodeSe);
            traduz(") {\n");
            matchL("ABRE_CHAVE", "", nodeSe);
            indentacao++;
            Node nodeBloco = new Node("bloco-se");
            nodeSe.addFilho(nodeBloco);
            bloco(nodeBloco);
            indentacao--;
            matchL("FECHA_CHAVE", "", nodeSe);
            traduz("    ".repeat(indentacao) + "}");
            casoSenao(nodeSe);
            traduz("\n");
            return true;
        }
        return false;
    }
 
    // =========================================================================
    // CasoSenao → SENAO ABRE_CHAVE bloco FECHA_CHAVE | ε
    // =========================================================================
    public boolean casoSenao(Node pai) {
        if (token.tipo.equals("SENAO")) {
            Node nodeSenao = new Node("senao");
            pai.addFilho(nodeSenao);
 
            matchL("SENAO", "", nodeSenao);
            traduz(" else {\n");
            matchL("ABRE_CHAVE", "", nodeSenao);
            indentacao++;
            Node nodeBloco = new Node("bloco-senao");
            nodeSenao.addFilho(nodeBloco);
            bloco(nodeBloco);
            indentacao--;
            matchL("FECHA_CHAVE", "", nodeSenao);
            traduz("    ".repeat(indentacao) + "}");
            return true;
        }
        return true; // ε
    }
 
    // =========================================================================
    // Enquanto → ENQUANTO ABRE_PAR exprRel FECHA_PAR FACA ABRE_CHAVE bloco FECHA_CHAVE
    // =========================================================================
    public boolean enquanto(Node pai) {
        if (token.tipo.equals("ENQUANTO")) {
            Node nodeEnq = new Node("enquanto");
            pai.addFilho(nodeEnq);
 
            matchL("ENQUANTO", "", nodeEnq);
            traduz("    ".repeat(indentacao) + "while (");
            matchL("ABRE_PAR", "", nodeEnq);
            exprRel(nodeEnq);
            matchL("FECHA_PAR", "", nodeEnq);
            matchL("FACA", "", nodeEnq);
            traduz(") {\n");
            matchL("ABRE_CHAVE", "", nodeEnq);
            indentacao++;
            Node nodeBloco = new Node("bloco-enquanto");
            nodeEnq.addFilho(nodeBloco);
            bloco(nodeBloco);
            indentacao--;
            matchL("FECHA_CHAVE", "", nodeEnq);
            traduz("    ".repeat(indentacao) + "}\n");
            return true;
        }
        return false;
    }
 
    // =========================================================================
    // Para → PARA ABRE_PAR atribuição exprRel PONTO_V atribuiçãoPara FECHA_PAR ABRE_CHAVE bloco FECHA_CHAVE
    // =========================================================================
    public boolean para(Node pai) {
        if (token.tipo.equals("PARA")) {
            Node nodePara = new Node("para");
            pai.addFilho(nodePara);
 
            matchL("PARA", "", nodePara);
            traduz("    ".repeat(indentacao) + "for (");
            matchL("ABRE_PAR", "", nodePara);
            atribuicaoForInit(nodePara);
            traduz("; ");
            exprRel(nodePara);
            traduz("; ");
            matchL("PONTO_V", "", nodePara);
            atribuicaoPara(nodePara);
            traduz(") {\n");
            matchL("FECHA_PAR", "", nodePara);
            matchL("ABRE_CHAVE", "", nodePara);
            indentacao++;
            Node nodeBloco = new Node("bloco-para");
            nodePara.addFilho(nodeBloco);
            bloco(nodeBloco);
            indentacao--;
            matchL("FECHA_CHAVE", "", nodePara);
            traduz("    ".repeat(indentacao) + "}\n");
            return true;
        }
        return false;
    }
 
    private boolean atribuicaoForInit(Node pai) {
        if (token.tipo.equals("ID")) {
            Node nodeAtrb = new Node("atribuicao-init");
            pai.addFilho(nodeAtrb);
            String nome = matchT("ID", nodeAtrb);
            traduz(nome + " = ");
            matchL("ATRB", "", nodeAtrb);
            expressao(nodeAtrb);
            matchL("PONTO_V", "", nodeAtrb);
            return true;
        }
        erro("atribuicaoForInit");
        return false;
    }
 
    public boolean atribuicaoPara(Node pai) {
        if (token.tipo.equals("ID")) {
            Node nodeAtrb = new Node("atribuicao-passo");
            pai.addFilho(nodeAtrb);
            String nome = matchT("ID", nodeAtrb);
            traduz(nome + " = ");
            matchL("ATRB", "", nodeAtrb);
            expressao(nodeAtrb);
            return true;
        }
        erro("atribuicaoPara");
        return false;
    }
 
    // =========================================================================
    // ExprRel → expressão operadorRelacional expressão
    // =========================================================================
    public boolean exprRel(Node pai) {
        Node nodeRel = new Node("exprRel");
        pai.addFilho(nodeRel);
        if (expressao(nodeRel)) {
            if (operadorRelacional(nodeRel)) {
                if (expressao(nodeRel)) {
                    return true;
                }
            }
            erro("exprRel");
        }
        return false;
    }
 
    public boolean operadorRelacional(Node pai) {
        switch (token.tipo) {
            case "MAIOR":       traduz(" > ");  pai.addFilho(new Node(matchT("MAIOR")));        return true;
            case "MENOR":       traduz(" < ");  pai.addFilho(new Node(matchT("MENOR")));        return true;
            case "MAIOR_IGUAL": traduz(" >= "); pai.addFilho(new Node(matchT("MAIOR_IGUAL"))); return true;
            case "MENOR_IGUAL": traduz(" <= "); pai.addFilho(new Node(matchT("MENOR_IGUAL"))); return true;
            case "IGUAL":       traduz(" == "); pai.addFilho(new Node(matchT("IGUAL")));        return true;
            case "DIFERENTE":   traduz(" != "); pai.addFilho(new Node(matchT("DIFERENTE")));   return true;
            default:            return false;
        }
    }
 
    // =========================================================================
    // Expressão → termo expressãoR
    // =========================================================================
    public boolean expressao(Node pai) {
        Node nodeExpr = new Node("expressao");
        pai.addFilho(nodeExpr);
        if (termo(nodeExpr)) {
            expressaoR(nodeExpr);
            return true;
        }
        // Se não reconheceu nada, remove o nó vazio
        pai.filhos.remove(nodeExpr);
        return false;
    }
 
    public boolean expressaoR(Node pai) {
        if (token.tipo.equals("SOMA")) {
            matchL("SOMA", " + ", pai);
            termo(pai);
            expressaoR(pai);
            return true;
        }
        if (token.tipo.equals("SUBT")) {
            matchL("SUBT", " - ", pai);
            termo(pai);
            expressaoR(pai);
            return true;
        }
        return true; // ε
    }
 
    public boolean termo(Node pai) {
        Node nodeTermo = new Node("termo");
        pai.addFilho(nodeTermo);
        if (valor(nodeTermo)) {
            termoR(nodeTermo);
            return true;
        }
        pai.filhos.remove(nodeTermo);
        return false;
    }
 
    public boolean termoR(Node pai) {
        if (token.tipo.equals("MULT")) {
            matchL("MULT", " * ", pai);
            valor(pai);
            termoR(pai);
            return true;
        }
        if (token.tipo.equals("DIVS")) {
            matchL("DIVS", " / ", pai);
            valor(pai);
            termoR(pai);
            return true;
        }
        return true; // ε
    }
 
    // =========================================================================
    // Valor → NUM_INTEIRO | NUM_DECIMAL | ID | ABRE_PAR expressão FECHA_PAR 
    // =========================================================================
    public boolean valor(Node pai) {
        if (token.tipo.equals("NUM_INTEIRO") || token.tipo.equals("NUM_DECIMAL") || token.tipo.equals("TEXTO_REAL")) {
            String v = matchT(token.tipo);
            pai.addFilho(new Node(v));
            traduz(v);
            return true;
        }
        if (token.tipo.equals("ID")) {
            String v = matchT("ID");
            pai.addFilho(new Node(v));
            traduz(v);
            return true;
        }
        if (token.tipo.equals("ABRE_PAR")) {
            matchL("ABRE_PAR", "(", pai);
            expressao(pai);
            matchL("FECHA_PAR", ")", pai);
            return true;
        }
        return false;
    }
}
 