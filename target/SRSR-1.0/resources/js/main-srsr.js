/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */


/* global PrimeFaces */

// resources/js/mais.js

var formAlterado = false;

// essa função vai marcar quando qualquer campo do formulário for alterado
window.addEventListener("input", function () {
    formAlterado = true;
});

// pergunta antes de sair/atualizar a página - é a página de confirmação do próprio navegador, achei mais rápido para implementar
window.addEventListener("beforeunload", function (e) {
    if (formAlterado) {
        e.preventDefault();
        e.returnValue = ''; // mostra mensagem padrão do navegador
    }
});

// função para resetar (USAR NO BOTÃO DE SALVAR, COM A PROPRIEDADE ONCLICK="resetAlterador()" VAI RESETAR O ESTADO DA VARIAVÉL)
// ESSA FUNÇÃO SERVE PARA NÃO APARECER A MENSAGEM DE CONFIRMAÇÃO PARA TROCAR DE TELA COM MODIFICAÇÕES EM ANDAMENTO
function resetAlterado() {
    formAlterado = false;
}

