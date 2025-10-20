# 💻 SRSR – Sistema de Registro de Serviços Realizados

Sistema Web desenvolvido em Jakarta Faces 4 com PrimeFaces e PostgreSQL, destinado ao registro, consulta e gerenciamento de serviços realizados para clientes.

🧩 Tecnologias Utilizadas
- Java WEB com Maven
- Jakarta Faces 4 (com CDI)
- PrimeFaces 13
- PostgreSQL 17
- Apache Tomcat 10.1
- NetBeans 27
- Arquitetura MVC (Model–DAO–Bean–View)

🗂️ Estrutura
- Entidades: `Cliente`, `Servico`
- Padrão de Camadas: MVC com DAO Pattern
- Interface: XHTML + PrimeFaces + CSS + JS
- Namespace: `jakarta.*` (Jakarta EE 10)

🧠 Funcionalidades
- Cadastro, edição e exclusão de clientes e serviços
- Consultas filtradas por cliente e período
- Mensagens Growl/Dialog (PrimeFaces + Ajax)
- Validação de CPF/CNPJ e campos obrigatórios
- Interface responsiva

🗃️ Banco de Dados (PostgreSQL)
Duas tabelas: `cliente` e `servico`, com relacionamento 1:N (`cliente.id_cliente` → `servico.id_cliente`).

👨‍💻 Desenvolvedor
Danilo da Silva Pereira – RGM 45698937  
Projeto Integrador II – Sistemas de Informação  
Universidade Cruzeiro do Sul – 2025
